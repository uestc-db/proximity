package TrajPrefixSpan;

import grid.Configuration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;



import loadData.MapLoc2Grid;
import loadData.SQLiteDriver;
import TrajPrefixSpan.FPGrowthDT.FPTree;
import TrajPrefixSpan.PrefixSpanDT.AlgoPrefixSpan;
import TrajPrefixSpan.PrefixSpanDT.AlgoPrefixSpanFP;
import TrajPrefixSpan.PrefixSpanDT.Itemset;
import TrajPrefixSpan.PrefixSpanDT.Sequence;
import TrajPrefixSpan.PrefixSpanDT.SequenceDatabase;


public class TrajPrefixSpanDT {
	private static HashMap<String,Integer> IdMap;
	private static int MOid;
	

	/**
	 * tuple in the cache structure
	 * @author workshop
	 *
	 */
	private class MOTuple{
		int timestamp;
		double lat;
		double lng;
		
		public MOTuple(double inLat,double inLng,int inTimestamp){
			lat=inLat;
			lng=inLng;
			timestamp=inTimestamp;
		}
		
		public void setNewTuple(double inLat,double inLng,int inTimestamp){
			lat=inLat;
			lng=inLng;
			timestamp=inTimestamp;
		}
		
		public int getTimestamp(){return timestamp;}
		public double getLat(){return lat;}
		public double getLng() {return lng;}
	}
	
	public void update(int inTraId,double inLat,double inLng,int inTimestamp,HashMap<Integer,MOTuple> inMoc,HashMap<Integer,Sequence> outSeqMap){
		
		
		MOTuple moTuple=inMoc.get(inTraId);
		
		if(moTuple==null){//if this is the first update of this moving object, store it in cache
			MOTuple tuple=new MOTuple(inLat,inLng,inTimestamp);
			inMoc.put(inTraId,tuple);
		} else {
			int ti=inTimestamp-moTuple.getTimestamp();
			if(ti<PatternConfiguration.T_sample) return;//ignore this update
			else{
				double lat2=0,lng2=0,lat1=0,lng1=0;
				int t2=0,t1=0;
				
				lat1=moTuple.getLat();
				lng1=moTuple.getLng();
				t1=moTuple.getTimestamp();
				
				double voc_lat=(inLat-lat1)/ti;
				double voc_lng=(inLng-lng1)/ti;
				
				//outlier, ignore this update
				if(Math.abs(voc_lat)<Configuration.ExtremLowVelocityLat&&Math.abs(voc_lng)<Configuration.ExtremLowVelocityLat){
					return;
				}
				int ts=PatternConfiguration.T_sample;//count the time
				
				while(ts<=ti){

					
					 lat2=voc_lat*PatternConfiguration.T_sample+lat1;
					 lng2=voc_lng*PatternConfiguration.T_sample+lng1;
					 t2=PatternConfiguration.T_sample+t1;

					 int keyXY=getKeyXY(lat2,lng2);
					 
					 Sequence moSeq=outSeqMap.get(inTraId);
						if(null==moSeq){
							moSeq=new Sequence(inTraId);
							outSeqMap.put(inTraId, moSeq);
						}
						Itemset itemset = new Itemset();
						itemset.addItem(keyXY);
						moSeq.addItemset(itemset);
										
					 lat1=lat2;
					 lng1=lng2;
					 t1=t2;
					 ts+=PatternConfiguration.T_sample;
					
				}
				moTuple.setNewTuple(lat2,lng2, t2);//update the cache 
			}
		}
		
	}
	
	
	private int getWxgVideoSeconds(int t){
		
		return (t+12)/24;
	}
	
	private int getWxgVideoTraTime(int second){
		int second_24=second*24;
		return second_24;
	}
	public SequenceDatabase wxgVideoTraLoad2SequenceDatabase(String db, String table, int startTime,
			int endTime) {
		IdMap=new HashMap<String,Integer>();
		MOid=0;
		
	//	setMovingObjParameter(Configuration.TaxiLatMin,Configuration.TaxiLngMin,double inStep0)
				
		SequenceDatabase resSD = new SequenceDatabase(); 
		
		SQLiteDriver.openDB(db);
		
		SQLiteDriver.loadWxgVideoTraDB(table, getWxgVideoTraTime(startTime), getWxgVideoTraTime(endTime));
		
	
		 HashMap<Integer,Sequence> seqMap=new  HashMap<Integer,Sequence> () ;//stor all the sequences
		 HashMap<Integer,MOTuple> moc=new HashMap<Integer,MOTuple>();
		 try {
			while (SQLiteDriver.rs.next()) {
				WxgVideoMovingObject mo = this.parseWxgVideoMovingObject(SQLiteDriver.rs);
			
				update(mo.id,mo.lat,mo.lng,mo.timeStamp,moc,seqMap);
				
				//Sequence moSeq=seqMap.get(mo.id);
				//if(null==moSeq){
				//	moSeq=new Sequence(mo.id);
				//	seqMap.put(mo.id, moSeq);
				//}
			//	Itemset itemset = new Itemset();
			//	itemset.addItem(mo.keyXY);
			//	moSeq.addItemset(itemset);
				
	    		//moc.update(mo.id, mo.lat, mo.lng, mo.timeStamp);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Set<Entry<Integer,Sequence> > seqSet=seqMap.entrySet();
		Iterator<Entry<Integer,Sequence>> seqItr=seqSet.iterator();
		int seqFragId=0;
		while(seqItr.hasNext()){
			Entry<Integer,Sequence> entrySeq=seqItr.next();
			
			Sequence traSeq=entrySeq.getValue();
			if(PatternConfiguration.SequenceLen==-1){
				seqFragId++;
				resSD.addSequence(traSeq);
			} else{
			Sequence traItemSeqFrag=new Sequence(seqFragId++);
			
			for(int i=0;i<traSeq.size();i++){
				if(traItemSeqFrag.size()<PatternConfiguration.SequenceLen){
					traItemSeqFrag.addItemset(traSeq.get(i));
				} else{
					resSD.addSequence(traItemSeqFrag);
					traItemSeqFrag=new Sequence(seqFragId++);
					traItemSeqFrag.addItemset(traSeq.get(i));
				}
			}
			if(traItemSeqFrag.size()>0&&traItemSeqFrag.size()<PatternConfiguration.SequenceLen){
				resSD.addSequence(traItemSeqFrag);
			}
			}
			
			if(seqFragId>PatternConfiguration.TrajNum){
				break;
			}
			
		}
		IdMap=null;
		return resSD; 
	}
	
	
	public SequenceDatabase TraTaxiLoad2SequenceDatabase(String db, String table, String startTime,
			String endTime,String status) {
		IdMap=new HashMap<String,Integer>();
		MOid=0;
		
	//	setMovingObjParameter(Configuration.TaxiLatMin,Configuration.TaxiLngMin,double inStep0)
				
		SequenceDatabase resSD = new SequenceDatabase(); 
		
		SQLiteDriver.openDB(db);
		if(status.equals("")||status.length()<=1){
		SQLiteDriver.loadTaxiDB(table, startTime, endTime);
		}else{
			SQLiteDriver.loadTaxiDB(table, startTime, endTime,status);
		}
	
		 HashMap<Integer,Sequence> seqMap=new  HashMap<Integer,Sequence> () ;//stor all the sequences
		 HashMap<Integer,MOTuple> moc=new HashMap<Integer,MOTuple>();
		 try {
			while (SQLiteDriver.rs.next()) {
				MovingObject mo = this.ParseMovingObject(SQLiteDriver.rs);
			
				update(mo.id,mo.lat,mo.lng,mo.timeStamp,moc,seqMap);
				
				//Sequence moSeq=seqMap.get(mo.id);
				//if(null==moSeq){
				//	moSeq=new Sequence(mo.id);
				//	seqMap.put(mo.id, moSeq);
				//}
			//	Itemset itemset = new Itemset();
			//	itemset.addItem(mo.keyXY);
			//	moSeq.addItemset(itemset);
				
	    		//moc.update(mo.id, mo.lat, mo.lng, mo.timeStamp);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Set<Entry<Integer,Sequence> > seqSet=seqMap.entrySet();
		Iterator<Entry<Integer,Sequence>> seqItr=seqSet.iterator();
		int seqFragId=0;
		while(seqItr.hasNext()){
			Entry<Integer,Sequence> entrySeq=seqItr.next();
			
			Sequence taxiSeq=entrySeq.getValue();
			if(PatternConfiguration.SequenceLen==-1){
				seqFragId++;
				resSD.addSequence(taxiSeq);
			} else{
			Sequence taxiSeqFrag=new Sequence(seqFragId++);
			
			for(int i=0;i<taxiSeq.size();i++){
				if(taxiSeqFrag.size()<PatternConfiguration.SequenceLen){
					taxiSeqFrag.addItemset(taxiSeq.get(i));
				} else{
					resSD.addSequence(taxiSeqFrag);
					taxiSeqFrag=new Sequence(seqFragId++);
					taxiSeqFrag.addItemset(taxiSeq.get(i));
				}
			}
			if(taxiSeqFrag.size()>0&&taxiSeqFrag.size()<PatternConfiguration.SequenceLen){
				resSD.addSequence(taxiSeqFrag);
			}
			}
			
			if(seqFragId>PatternConfiguration.TrajNum){
				break;
			}
			
		}
		IdMap=null;
		return resSD; 
	}
	
	 /**
	  * parsing moving object from string
	  * @param res
	  * @return
	  */
	 private MovingObject ParseMovingObject(ResultSet sqlRs){
		 MovingObject mo=new MovingObject();
		 try{
		 mo.timeStamp=SQLiteDriver.getSeconds(sqlRs.getString("time"));
		 mo.id=getMOid(sqlRs.getString("id"));
		 //!!!!lat and lng is reversed
		 mo.lng=sqlRs.getDouble("lat");
		 mo.lat=sqlRs.getDouble("lng");
		 mo.v=sqlRs.getDouble("v");	
		 
		 mo.tranferToGrid();
		 
		 }catch(Exception e){
			 e.printStackTrace();
		 }
		 return mo;
	 }
	 
	 	private static double lat0;
		private static double lng0;
		private static double step0;
		
		
		public void CalibrationParameter(double minLat, double minLng,
				double maxLat, double maxLng,int gridDivided) {

			lat0 = minLat;
			lng0 = minLng;

			double xScale = maxLat - minLat;
			double yScale = maxLng - minLng;

			double maxScale = (xScale > yScale) ? xScale : yScale;
			int divided = gridDivided;
			step0 = maxScale / divided;
		}
		
	 //parsing moving objects
		public void setMovingObjParameter(double inLat0,double inLng0,double inStep0){
			lat0=inLat0;
			lng0=inLng0;
			step0=inStep0;
		}
		

		
		
		/**
		 * get the id of moving object
		 * @param idStr
		 * @return
		 */
		private static int getMOid(String idStr){
			Integer id=IdMap.get(idStr);
			if(null==id){
				Integer newId=MOid;
				IdMap.put(idStr, newId);
				MOid++;
			return newId;
			} else{
				return id;
			}
			
		}
		
		private int getKeyXY(double inLat,double inLng){
			double offx = inLat - lat0;
			double offy = inLng - lng0;
			int gridX = (int) ( offx / (step0));
			int gridY = (int) ( offy / (step0));
			
			int tempX=gridX<<16;
			int tempY=gridY<<16;//clear high
			int keyXY=tempX+(tempY>>16);
			
			return keyXY;
		}
		
		private class WxgVideoMovingObject {
			int id = -1;
			int sequence = -1;
			//int classId = -1;
			int timeStamp = -1;
			double lat = -1;
			double lng = -1;

			int gridX = -1;
			int gridY = -1;
		}
		
		 /**
		  * parsing moving object from string
		  * @param res
		  * @return
		  */
		 private WxgVideoMovingObject parseWxgVideoMovingObject(ResultSet sqlRs){
			 WxgVideoMovingObject mo=new WxgVideoMovingObject();
			 try{
			 mo.timeStamp=getWxgVideoSeconds(sqlRs.getInt("t"));
			 mo.id=sqlRs.getInt("id");
			 //!!!!lat and lng is reversed
			 mo.lng=sqlRs.getInt("x");
			 mo.lat=sqlRs.getInt("y");
			 mo.sequence=sqlRs.getInt("seq");
			 }catch(Exception e){
				 e.printStackTrace();
			 }
			 return mo;
		 }
		
		
		private class MovingObject {
			
			
			int id = -1;
		
			//int sequence = -1;
			//int classId = -1;
			int timeStamp = -1;
			double lat = -1;
			double lng = -1;
			double v = -1;

			int gridX = -1;
			int gridY = -1;
			int keyXY=-1;

			public void tranferToGrid() {
				double offx = lat - lat0;
				double offy = lng - lng0;
				gridX = (int) ( offx / (step0));
				gridY = (int) ( offy / (step0));
				
				int tempX=gridX<<16;
				int tempY=gridY<<16;//clear high
				keyXY=tempX+(tempY>>16);
	
		}
		}
		
		private  FPTree wxgVideoTraPrefixSpanFPtree(int startTime,int endTime,int support){
			// Load a sequence database
			CalibrationParameter(PatternConfiguration.LatMin,PatternConfiguration.LngMin,
					 PatternConfiguration.LatMax, PatternConfiguration.LngMax,PatternConfiguration.GridDivision);
			SequenceDatabase sequenceDatabase = wxgVideoTraLoad2SequenceDatabase(PatternConfiguration.TrajDB, PatternConfiguration.TrajTable, startTime, endTime) ; 
		    System.out.println("finish loading...");
		    
		    AlgoPrefixSpanFP algo = new AlgoPrefixSpanFP();
		    
		    FPTree FPPattern=algo.runAlgorithm(support, sequenceDatabase);    
		    algo.printStatistics(sequenceDatabase.size());
		    
		    return FPPattern;
		}
		
		
		private  FPTree traPrefixSpanFPtreeTaxi(String startTime,String endTime,int support){
			// Load a sequence database
			CalibrationParameter(PatternConfiguration.LatMin,PatternConfiguration.LngMin,
					 PatternConfiguration.LatMax, PatternConfiguration.LngMax,PatternConfiguration.GridDivision);
			SequenceDatabase sequenceDatabase = TraTaxiLoad2SequenceDatabase(PatternConfiguration.TrajDB, PatternConfiguration.TrajTable, startTime, endTime,"") ; 
		    System.out.println("finish loading...");
		    
		    AlgoPrefixSpanFP algo = new AlgoPrefixSpanFP();
		    
		    FPTree FPPattern=algo.runAlgorithm(support, sequenceDatabase);    
		    algo.printStatistics(sequenceDatabase.size());
		    
		    return FPPattern;
		}
		

		public void wxgVideoTrajPrefixSpanFPtreeTaxiSupportDivision(int startTime,int endTime,int support,int division){
			
			PatternConfiguration.GridDivision=division;
			FPTree FPPattern=wxgVideoTraPrefixSpanFPtree(startTime,endTime,support);
			 String fileInfo="";
			 try{
			 fileInfo="FPTree/TSV/"+PatternConfiguration.TrajTable+"_s-"+startTime+"_e-"+endTime+"_sup-"+support+"_div-"+division+"_seqLen-"+PatternConfiguration.SequenceLen+".fptree";
			 FileOutputStream fio=new FileOutputStream(fileInfo);
			 BufferedOutputStream buffer=new BufferedOutputStream(fio); 
			 ObjectOutputStream obj=new ObjectOutputStream(buffer);
			 try{
			 obj.writeObject(FPPattern);
			 }finally{
				 obj.close();
				 System.out.println("Output to file:"+fileInfo);
		      }
			
			 }catch(Exception e){
				 e.printStackTrace();
			 }
			 
		}
		

		
		public void taxiTrajPrefixSpanFPtreeTaxiSupportDivision(String startTime,String endTime,int support,int division){
			
			PatternConfiguration.GridDivision=division;
			FPTree FPPattern=traPrefixSpanFPtreeTaxi(startTime,endTime,support);
			 String fileInfo="";
			 try{
				 String ss=startTime;
				 ss=ss.replace(':', '-');
				 String es=endTime;
				 es=es.replace(':', '-');
				 
			 fileInfo="FPTree/"+PatternConfiguration.TrajTable+"_"+ss+"_"+es+"_sup-"+support+"_div-"+division+"_seqLen-"+PatternConfiguration.SequenceLen+".fptree";
			 FileOutputStream fio=new FileOutputStream(fileInfo);
			 BufferedOutputStream buffer=new BufferedOutputStream(fio); 
			 ObjectOutputStream obj=new ObjectOutputStream(buffer);
			 try{
			 obj.writeObject(FPPattern);
			 }finally{
				 obj.close();
				 System.out.println("Output to file:"+fileInfo);
		      }
			
			 }catch(Exception e){
				 e.printStackTrace();
			 }
			 
		}
		

		
		public void trajPrefixSpanFPtreeTaxiSupport(String startTime,String endTime,int support){
			
		
			
		     FPTree FPPattern=traPrefixSpanFPtreeTaxi(startTime,endTime,support);
		   	 String fileInfo="";
			 try{
				 String ss=startTime;
				 ss=ss.replace(':', '-');
				 String es=endTime;
				 es=es.replace(':', '-');
				 
		
			 FileOutputStream fio=new FileOutputStream(PatternConfiguration.TrajTable+"_"+ss+"_"+es+"_"+support+".fptree");
			 BufferedOutputStream buffer=new BufferedOutputStream(fio); 
			 ObjectOutputStream obj=new ObjectOutputStream(buffer);
			 try{
			 obj.writeObject(FPPattern);
			 }finally{
				 obj.close();
				
				 System.out.println("Output to file:"+PatternConfiguration.TrajTable+"_"+ss+"_"+es+"_"+support+".fptree");
		      }
			
			 }catch(Exception e){
				 e.printStackTrace();
			 }
			 
		}
		
		

		public static void testPredictionLogical(){    
			
			TrajPrefixSpanDT tpst=new TrajPrefixSpanDT();
			
			
			// Load a sequence database
			tpst.CalibrationParameter(Configuration.TaxiLatMin,Configuration.TaxiLngMin,
					 Configuration.TaxiLatMax, Configuration.TaxiLngMax,256);
			SequenceDatabase sequenceDatabase = tpst.TraTaxiLoad2SequenceDatabase("data/taxi/taxi.db", "PrefixSpanTest", "00:00:00", "15:00:00","") ; 
		    System.out.println("finish loading...");
			
			
			
			//	sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
			// print the database to console
			//sequenceDatabase.print();
			
			// Create an instance of the algorithm 
		 
		     AlgoPrefixSpanFP algo = new AlgoPrefixSpanFP();
		    //AlgoPrefixSpan algo = new AlgoPrefixSpan();
			
			  // System.out.println("Start mining by PrefixSpan...");
			// execute the algorithm with minsup = 50 %
			   FPTree FPPattern=algo.runAlgorithm(sequenceDatabase, 0.5);    
			//  algo.runAlgorithm(sequenceDatabase, 0.5);    
			   
			   PatternPredictor pp=new PatternPredictor();
			   pp.setPatternFP(FPPattern);
			   
			   ArrayList<Integer> tail=new ArrayList<Integer>();
			   Integer i1=(4<<16)+5;//(1,5)
			   Integer i2=(5<<16)+5;//(2,5)
			   Integer i3=(3<<16)+5;//(3,5)
			   
			   tail.add(i1);
			   tail.add(i2);
			  // tail.add(i3);
			   
			   ArrayList<Integer>ppRes=pp.PrefixSpanWholePredictorLogic(tail, 0.1);
			  
			   System.out.println("Prediction result");
			   for(int i=0;i<ppRes.size();i++){
				   int ppResItem=ppRes.get(i);
				   System.out.println("time:"+i+" (x,y):"+" ("+(ppResItem>>16)+","+((ppResItem<<16)>>16)+")");
			   }
			   
			   
			//  System.out.println("finish PrefixSpan...");
			algo.printStatistics(sequenceDatabase.size());
		
			
		}
		
		
		public static void testWriteFPTree(){

			
			TrajPrefixSpanDT tpst=new TrajPrefixSpanDT();
			
			
			// Load a sequence database
			tpst.CalibrationParameter(Configuration.TaxiLatMin,Configuration.TaxiLngMin,
					 Configuration.TaxiLatMax, Configuration.TaxiLngMax,256);
			SequenceDatabase sequenceDatabase = tpst.TraTaxiLoad2SequenceDatabase("data/taxi/taxi.db", "taxi", "15:00:00", "15:30:00","") ; 
		    System.out.println("finish loading...");
			
			
			
			//	sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
			// print the database to console
			//sequenceDatabase.print();
			
			// Create an instance of the algorithm 
		 
		    // AlgoPrefixSpanFP algo = new AlgoPrefixSpanFP();
		   AlgoPrefixSpan algo = new AlgoPrefixSpan();
			
			  // System.out.println("Start mining by PrefixSpan...");
			// execute the algorithm with minsup = 50 %
			 //  FPTree FPPattern=algo.runAlgorithm(sequenceDatabase, 0.015);    
			  algo.runAlgorithm(sequenceDatabase, 0.01);    
			   algo.printStatistics(sequenceDatabase.size());
			   
			   
//			 try{
//			 FileOutputStream fio=new FileOutputStream("taxi_15_16.fptree");
//			 BufferedOutputStream buffer=new BufferedOutputStream(fio); 
//			 ObjectOutputStream obj=new ObjectOutputStream(buffer);
//			 try{
//			 obj.writeObject(FPPattern);
//			 }finally{
//				 obj.close();
//		      }
//			
//			 }catch(Exception e){
//				 e.printStackTrace();
//			 }
			 
			 
			 
//			 try{
//			      //use buffering
//			      InputStream file = new FileInputStream( "taxi_15_16.fptree" );
//			      InputStream buffer = new BufferedInputStream( file );
//			      ObjectInput input = new ObjectInputStream ( buffer );
//			      try{
//			        FPTree fpRead=(FPTree) input.readObject();
//			        fpRead.mapItemNodes.get(5);
//			      }
//			      finally{
//			        input.close();
//			      }
//			    }
//			    catch(ClassNotFoundException ex){
//			     ex.printStackTrace();
//			    }
//			    catch(IOException ex){
//			     ex.printStackTrace();
//			    }
		}
		
		
		public static void testFPTreeMining(){


			
			TrajPrefixSpanDT tpst=new TrajPrefixSpanDT();
			
			
			// Load a sequence database
			tpst.CalibrationParameter(Configuration.TaxiLatMin,Configuration.TaxiLngMin,
					 Configuration.TaxiLatMax, Configuration.TaxiLngMax,256);
			SequenceDatabase sequenceDatabase = tpst.TraTaxiLoad2SequenceDatabase("data/taxi/taxi.db", "PrefixSpanTest", "00:00:00", "01:00:00","") ; 
		    System.out.println("finish loading...");
			
			
			
			//	sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
			// print the database to console
			//sequenceDatabase.print();
			
			// Create an instance of the algorithm 
		 
		     AlgoPrefixSpanFP algo = new AlgoPrefixSpanFP();
		    //AlgoPrefixSpan algo = new AlgoPrefixSpan();
			
			  // System.out.println("Start mining by PrefixSpan...");
			// execute the algorithm with minsup = 50 %
			   FPTree FPPattern=algo.runAlgorithm(sequenceDatabase, 0.05);    
			//  algo.runAlgorithm(sequenceDatabase, 0.5);    
			   
			   algo.printStatistics(sequenceDatabase.size());
			   
			 try{
			 FileOutputStream fio=new FileOutputStream("PrefixSpanTest.fptree");
			 BufferedOutputStream buffer=new BufferedOutputStream(fio); 
			 ObjectOutputStream obj=new ObjectOutputStream(buffer);
			 try{
			 obj.writeObject(FPPattern);
			 }finally{
				 obj.close();
		      }
			
			 }catch(Exception e){
				 e.printStackTrace();
			 }
			 
			 
			 
			 try{
			      //use buffering
			      InputStream file = new FileInputStream( "test.fptree" );
			      InputStream buffer = new BufferedInputStream( file );
			      ObjectInput input = new ObjectInputStream ( buffer );
			      try{
			        FPTree fpRead=(FPTree) input.readObject();
			        fpRead.mapItemNodes.get(5);
			      }
			      finally{
			        input.close();
			      }
			    }
			    catch(ClassNotFoundException ex){
			     ex.printStackTrace();
			    }
			    catch(IOException ex){
			     ex.printStackTrace();
			    }
		
		}
		
		public static void testFPtreeSupport(String [] arg){
			if(arg.length<3){
				System.out.println("input parameter: startTime endTime Support");
				System.out.println("example: 14:00:00 15:00:00 0.02");
				return;
			}
			
			PatternConfiguration.GridDivision=512;
			//PatternConfiguration.TaxiGridDivision=256;
			PatternConfiguration.TrajDB="data/taxi/taxi.db";
			PatternConfiguration.TrajTable="mtaxi";
			//PatternConfiguration.TaxiTrajTable="PrefixSpanTest";
			
			PatternConfiguration.LatMin=Configuration.TaxiLatMin;
			PatternConfiguration.LngMin=Configuration.TaxiLngMin;
			PatternConfiguration.LatMax= Configuration.TaxiLatMax;
			PatternConfiguration.LngMax=Configuration.TaxiLngMax;
			
			PatternConfiguration.SequenceLen=-1;
			PatternConfiguration.T_sample=Configuration.Taxi_T_Sample;
			
			String startTime, endTime;
			int support;
			
			startTime=arg[0];
			endTime=arg[1];
			support=Integer.parseInt(arg[2]);//.parseInteger(arg[2]);
			
			TrajPrefixSpanDT traPrefixSpanDT=new TrajPrefixSpanDT();
			traPrefixSpanDT.trajPrefixSpanFPtreeTaxiSupport( startTime, endTime, support);
		}
		
		
		public static void testFPtreeSupportDivision(String [] arg){
			if(arg.length<4){
				System.out.println("input parameter: startTime endTime Support");
				System.out.println("example: 14:00:00 15:00:00 20 512");
				return;
			}
			
		//	PatternConfiguration.TaxiGridDivision=512;
			//PatternConfiguration.TaxiGridDivision=256;
			PatternConfiguration.TrajDB="data/taxi/taxi.db";
			PatternConfiguration.TrajTable="mtaxi";
			//PatternConfiguration.TaxiTrajTable="PrefixSpanTest";
			
			PatternConfiguration.LatMin=Configuration.TaxiLatMin;
			PatternConfiguration.LngMin=Configuration.TaxiLngMin;
			PatternConfiguration.LatMax= Configuration.TaxiLatMax;
			PatternConfiguration.LngMax=Configuration.TaxiLngMax;
			
			PatternConfiguration.SequenceLen=-1;
			PatternConfiguration.T_sample=Configuration.Taxi_T_Sample;
			
			String startTime, endTime;
			int support;
			int division;
			
			startTime=arg[0];
			endTime=arg[1];
			support=Integer.parseInt(arg[2]);//.parseInteger(arg[2]);
			division=Integer.parseInt(arg[3]);
			
			TrajPrefixSpanDT traPrefixSpanDT=new TrajPrefixSpanDT();
			traPrefixSpanDT.taxiTrajPrefixSpanFPtreeTaxiSupportDivision( startTime, endTime, support,division);
		}
		
		public static void testXiaogangVideoTrajPrefixSpanFPtreeSupport(String[] arg){
			if(arg.length<5){
				System.out.println("input parameter: table startTime endTime Support division");
				System.out.println("example: TSV480 100 500 20 64 -1");
				return;
			}
			
//			PatternConfiguration.TaxiGridDivision=512;
			//PatternConfiguration.TaxiGridDivision=256;
			PatternConfiguration.TrajDB="data/TrainStationVideo/tsv.db";
			PatternConfiguration.TrajTable=arg[0];
			//PatternConfiguration.TaxiTrajTable="PrefixSpanTest";
			
			PatternConfiguration.LatMin=Configuration.TSVLatMin;
			PatternConfiguration.LngMin=Configuration.TSVLngMin;
			PatternConfiguration.LatMax= Configuration.TSVLatMax;
			PatternConfiguration.LngMax=Configuration.TSVLngMax;
			
			PatternConfiguration.SequenceLen=Integer.parseInt(arg[5]);
			PatternConfiguration.T_sample=Configuration.TSV_T_Sample;
			PatternConfiguration.ExtremLowVelocityLat=Configuration.ExtremLowVelocityLat;
			PatternConfiguration.ExtremLowVelocityLng=Configuration.ExtremLowVelocityLng;
			
			
			int startTime, endTime;
			int support;
			int division;
			
			startTime=Integer.parseInt(arg[1]);
			endTime=Integer.parseInt(arg[2]);
			support=Integer.parseInt(arg[3]);//.parseInteger(arg[2]);
			division=Integer.parseInt(arg[4]);
			
			TrajPrefixSpanDT traPrefixSpanDT=new TrajPrefixSpanDT();
			traPrefixSpanDT.wxgVideoTrajPrefixSpanFPtreeTaxiSupportDivision( startTime, endTime, support,division);
		}
		
		
		public static void testTaxiFPtreeTableSupportDivisionSeqlen(String [] arg){
			if(arg.length<5){
				System.out.println("input parameter: table startTime endTime Support division");
				System.out.println("example: mtaxi 14:00:00 15:00:00 20 512 -1");
				return;
			}
			
		//	PatternConfiguration.TaxiGridDivision=512;
			//PatternConfiguration.TaxiGridDivision=256;
			PatternConfiguration.TrajDB="data/taxi/taxi.db";
			PatternConfiguration.TrajTable=arg[0];
			//PatternConfiguration.TaxiTrajTable="PrefixSpanTest";
			
			PatternConfiguration.LatMin=Configuration.TaxiLatMin;
			PatternConfiguration.LngMin=Configuration.TaxiLngMin;
			PatternConfiguration.LatMax= Configuration.TaxiLatMax;
			PatternConfiguration.LngMax=Configuration.TaxiLngMax;
			
			PatternConfiguration.SequenceLen=Integer.parseInt(arg[5]);
			PatternConfiguration.T_sample=Configuration.Taxi_T_Sample;
			
			String startTime, endTime;
			int support;
			int division;
			
			startTime=arg[1];
			endTime=arg[2];
			support=Integer.parseInt(arg[3]);//.parseInteger(arg[2]);
			division=Integer.parseInt(arg[4]);
			
			TrajPrefixSpanDT traPrefixSpanDT=new TrajPrefixSpanDT();
			traPrefixSpanDT.taxiTrajPrefixSpanFPtreeTaxiSupportDivision( startTime, endTime, support,division);
		}
		
		


		
		public static void main(String [] arg) throws IOException{
			
			//testPredictionLogical();
			//testWriteFPTree();
			//testFPtreeSupport(arg);
			//testFPTreeMining();
			
			//this is for Taxi trajectories
			//testFPtreeSupportDivision( arg);
			//testTaxiFPtreeTableSupportDivisionSeqlen(arg);
			
			//for station trajectories
			testXiaogangVideoTrajPrefixSpanFPtreeSupport(arg);
			
			//for BBFOld trajectories i.e. brinkhoffOldburg trajectories
			
			
		}
		
}
