package TrajPrefixSpan;

import grid.Configuration;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
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



public class TrajPrefixSpanDTBBFOld {
	private static double lat0;
	private static double lng0;
	private static double step0;
	
	
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
	
	
	 
	 private class BBFOldMovingObject {
			String type="";
			//  nextX INTEGER, nextY INTEGER)

			int id = -1;
			int seq = -1;
			int objClass=-1;
			//int classId = -1;
			int t = -1;
			double x = -1;
			double y = -1;
			double speed=0;
			double nextX=-1;
			double nextY=-1;

			int gridX = -1;
			int gridY = -1;
		}
	 
	 /**
	  * parsing moving object from string
	  * @param res
	  * @return
	  */
	 private BBFOldMovingObject ParseBBFOldMovingObject(ResultSet sqlRs){
		 BBFOldMovingObject mo=new BBFOldMovingObject();
		 try{
		 mo.t=sqlRs.getInt("t");
		 mo.id=sqlRs.getInt("id");

		 mo.x=sqlRs.getInt("x");
		 mo.y=sqlRs.getInt("y");
		 mo.seq=sqlRs.getInt("seq");
		 }catch(Exception e){
			 e.printStackTrace();
		 }
		 return mo;
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
	
	public SequenceDatabase BBFOldTraLoad2SequenceDatabase(String db, String table, int startTime,
			int endTime) {
		IdMap=new HashMap<String,Integer>();
		MOid=0;
		
	//	setMovingObjParameter(Configuration.TaxiLatMin,Configuration.TaxiLngMin,double inStep0)
				
		SequenceDatabase resSD = new SequenceDatabase(); 
		
		SQLiteDriver.openDB(db);
		
		SQLiteDriver.loadBBFOld(table, startTime, endTime);
		
		
	
		 HashMap<Integer,Sequence> seqMap=new  HashMap<Integer,Sequence> () ;//stor all the sequences
		 HashMap<Integer,MOTuple> moc=new HashMap<Integer,MOTuple>();
		 try {
			while (SQLiteDriver.rs.next()) {
				BBFOldMovingObject mo = this.ParseBBFOldMovingObject(SQLiteDriver.rs);
			
				update(mo.id,mo.x,mo.y,mo.t,moc,seqMap);
				
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
	
	
	private  FPTree BBFOldTraPrefixSpanFPtree(int startTime,int endTime,int support){
		// Load a sequence database
		CalibrationParameter(PatternConfiguration.LatMin,PatternConfiguration.LngMin,
				 PatternConfiguration.LatMax, PatternConfiguration.LngMax,PatternConfiguration.GridDivision);
		SequenceDatabase sequenceDatabase = BBFOldTraLoad2SequenceDatabase(PatternConfiguration.TrajDB, PatternConfiguration.TrajTable, startTime, endTime) ; 
	    System.out.println("finish loading...");
	    
	    AlgoPrefixSpanFP algo = new AlgoPrefixSpanFP();
	    
	    FPTree FPPattern=algo.runAlgorithm(support, sequenceDatabase);    
	    algo.printStatistics(sequenceDatabase.size());
	    
	    return FPPattern;
	}
	
	
	public void BBFOldTrajPrefixSpanFPtreeTaxiSupportDivision(int startTime,int endTime,int support,int division){
		
		PatternConfiguration.GridDivision=division;
		FPTree FPPattern=BBFOldTraPrefixSpanFPtree(startTime,endTime,support);
		 String fileInfo="";
		 String folder="FPTree/BBFOld/";
		 try{
		
		File fld=new File(folder);
		if(!fld.exists()){
			fld.mkdirs();
		}
		 fileInfo=folder+PatternConfiguration.TrajTable+"_s-"+startTime+"_e-"+endTime+"_sup-"+support+"_div-"+division+"_seqLen-"+PatternConfiguration.SequenceLen+".fptree";
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
	
	
	
	public static void testBBFOldTrajPrefixSpanFPtreeSupport(String[] arg){
		if(arg.length<5){
			System.out.println("input parameter: table startTime endTime Support division sqlen");
			System.out.println("example: BBFOldTest18 1 100 20 1024 -1");
			return;
		}
		
//		PatternConfiguration.TaxiGridDivision=512;
		//PatternConfiguration.TaxiGridDivision=256;
		PatternConfiguration.TrajDB="data/BigBrinkhoff/bigBrinkhoff.db";
		PatternConfiguration.TrajTable=arg[0];
		//PatternConfiguration.TaxiTrajTable="PrefixSpanTest";
		
		PatternConfiguration.LatMin=Configuration.BBFOldXMin;
		PatternConfiguration.LngMin=Configuration.BBFOldYMin;
		PatternConfiguration.LatMax= Configuration.BBFOldXMax;
		PatternConfiguration.LngMax=Configuration.BBFOldYMax;
		
		PatternConfiguration.SequenceLen=Integer.parseInt(arg[5]);
		PatternConfiguration.T_sample=1;
		PatternConfiguration.ExtremLowVelocityLat=1;
		PatternConfiguration.ExtremLowVelocityLng=1;
		
		
		int startTime, endTime;
		int support;
		int division;
		
		startTime=Integer.parseInt(arg[1]);
		endTime=Integer.parseInt(arg[2]);
		support=Integer.parseInt(arg[3]);//.parseInteger(arg[2]);
		division=Integer.parseInt(arg[4]);
		
		TrajPrefixSpanDTBBFOld traPrefixSpanDT=new TrajPrefixSpanDTBBFOld();
		traPrefixSpanDT.BBFOldTrajPrefixSpanFPtreeTaxiSupportDivision( startTime, endTime, support,division);
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
		//testXiaogangVideoTrajPrefixSpanFPtreeSupport(arg);
		
		//for BBFOld trajectories i.e. brinkhoffOldburg trajectories
		
		testBBFOldTrajPrefixSpanFPtreeSupport(arg);
	}
		
}
