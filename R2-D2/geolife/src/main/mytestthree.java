package main;

import grid.Configuration;
import grid.Grid;
import grid.GridLeafTraHashItem;
import grid.RoICell;

//import java.lang.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import File.writefile;
import TrajPrefixSpan.PatternConfiguration;
import TrajPrefixSpan.PatternPredictor;
import TrajPrefixSpan.TrajPrefixSpanDTBBFOld;

import loadData.BBFOldLoad;
import loadData.MapLoc2Grid;
import loadData.myloadthree;
import prediction.MacroState;
import prediction.Predictor;
import prediction.StateGridFilter;
import stp.predictor.MotionPredictor;
import stp.predictor.Point;
import visulalization.VisGrid;

public class mytestthree {


	public static void main(String args[]){
		
			int t_period = Integer.parseInt(args[0]);
			int sampleLen = Integer.parseInt(args[1]);
			int stepstart = Integer.parseInt(args[2]);
			int stepend = Integer.parseInt(args[3]);
			int timeStart = Integer.parseInt(args[4]);
			int timeEnd = Integer.parseInt(args[5]);
			int prd_num = Integer.parseInt(args[6]);
			String input_file = args[7];
			String table_name = args[8];
			String out_folder = args[9];

			System.out.println("In main: t_period   = " + t_period);
			System.out.println("In main: sampleLen  = " + sampleLen);
			System.out.println("In main: stepstart  = " + stepstart);
			System.out.println("In main: stepend    = " + stepend);
			System.out.println("In main: timeStart  = " + timeStart);
			System.out.println("In main: timeEnd    = " + timeEnd);
			System.out.println("In main: prd_num    = " + prd_num);
			System.out.println("In main: input_file = " + input_file);
			System.out.println("In main: table_name = " + table_name);
			System.out.println("In main: out_folder = " + out_folder);

		
			expPredictionErr_R2D2(t_period, sampleLen, stepstart, stepend, 
		timeStart, timeEnd, prd_num, input_file, table_name, out_folder);
			//expPredictionErr_RMF();
			//expPredictionErr_TraPattern();
	}
		
	
//=================================method for R2-D2===================================================
	public static void expPredictionErr_R2D2(int t_period, int sampleLen, int stepstart, int stepend, 
		int timeStart, int timeEnd, int prd_num, String input_file, String table_name, String out_folder){		
		
		Configuration.BITS_PER_GRID=4;
		Configuration.MAX_LEVEL=3;
		Configuration.GridDivided=2048;
		
		Configuration.T_period=t_period;
		
		Configuration.BrinkConstraintRoI=16; 
		Configuration.TraSupport=1;
		
		//define parameter related with MacroState and MicroState 
		Configuration.MaxRadius=50000; //maximum size of state, not import parameters. It is defined on real distance
		Configuration.MaxStateDis=5000;
		Configuration.AlphaRadius=1.5;//should be larger than 1
		Configuration.AlphaScore=1/16.0;
		Configuration.ProDown=0.00000001;
		Configuration.MAPPro=0;
		
		/*Configuration.FourLatMin=931700000.0;
		Configuration.FourLatMax=932500000.0;
		Configuration.FourLngMin=664400000.0;
		Configuration.FourLngMax=665200000.0;
		*/
		
		Configuration.MicroStateRadius=Configuration.cellRadius*2;
				
		int refTime=10;
		
		int DBBackStep=3;
		int predcount=0;
		// int timeStart;
		// int timeEnd;
		// int  stepstart;
		// int stepend;		
		// int sampleLen=210;//time
		
		// timeStart=0;
		// timeEnd=210;
		// stepstart=0;
		// stepend=200;
			

			long startTime = System.nanoTime();
			long whole_time = 0;
			int pred_num = 0;		

			int stid=0,edid=0;
			//we load the data from sqlite database into our Trajectory Grid structure
			System.out.println("#start loading data and sample queries");
			
			for( int iter=0;iter<2;iter++){
				   
				Predictor pdr=new Predictor();

				myloadthree tl=new myloadthree();
				tl.setSample(sampleLen);
				stid=iter*5000;
				edid=stid+5000;
				System.out.println("stid:"+stid);
				Grid g=tl.Load3Grid(input_file,table_name,timeStart ,timeEnd,stid,edid);
				System.out.println("#finished loading data");
				
				//System.out.println("#show part of the map");
//				VisGrid vg=VisGrid.visGridPart(g, 0, 0,Configuration.GridDivided,Configuration.GridDivided);//show a map
				
				HashMap<Integer, ArrayList<RoICell>> gridRes=tl.getGridSampleList();
				
				Iterator<Entry<Integer,ArrayList<RoICell>>> lItr=gridRes.entrySet().iterator();
				//int[] avgCount=new int[sampleLen+refTime];
				//double[] avgDT=new double[sampleLen+refTime]

				/*int stpCount=0;
				for(int j=0;j<sampleLen+refTime;j++){
					avgDT[j]=0;
					avgCount[j]=0;
				}
				*/
				double countSum=gridRes.size();
				//int vlCount=0;
				
				System.out.println("#start prediction and statistic the prediction rate and prediction error countsum:"+countSum);
				int realSum=0;
				while(lItr.hasNext()){
					realSum++;//
//					if(realSum > 10) // xy for test
//						break;	// xy for test				

					//long dtStart=0,dtEnd=0;
					Entry<Integer,ArrayList<RoICell>> gridItem=lItr.next();
					System.out.println("Item: id=" + gridItem.getKey());
//					if(iter == 0 && gridItem.getKey() < 2185)
//						continue;

					//dtStart=System.currentTimeMillis();//
					//int number=0;//
					
					for(int time=stepstart;time<stepend;time++){//					
//					for(int time=stepstart;time<20;time++){//					

						ArrayList<RoICell> rcGridList=gridItem.getValue();
						//if(rcGridList.size())
						ArrayList<RoICell> ref=new ArrayList<RoICell>(rcGridList.subList(time+refTime-DBBackStep+1-stepstart, time+refTime+1-stepstart));
//						System.out.println("\tIts backward RoICells=" + ref);
				       
						//System.out.println("time:"+time);
						
						 ArrayList<Entry<Long, GridLeafTraHashItem>>  testBres=g.queryRangeTimeSeqCells(ref);
						 
//						System.out.print("\tLook at its ref objects:");
//						for(int i=0; i<testBres.size(); i++){
//							int timeyep = Configuration.getTime(testBres.get(i).getKey());
//							Long keyyep = testBres.get(i).getKey();
//							keyyep>>=32;
//							System.out.print("" + keyyep + ", ");
//						}
//						System.out.print("\n");

						 if(null==testBres||testBres.size()<Configuration.TraSupport){ //
							 //System.out.println("null id,time:"+gridItem.getKey()+","+(time+refTime));
							 RoICell tItem=rcGridList.get(time+refTime-stepstart);
							 //System.out.println("null time:"+time);
//							 for(int len=1;len<=20;len++){
//								 double s[]=MapLoc2Grid.transfersource(tItem.roiX,tItem.roiY);
//									 String sss=((gridItem.getKey()+stid)+" ")+(len+" ")+(s[0]+" ")+(s[1]+" "); 
//									 try {
//										writefile.writeStrToFile("data/R2D2_newsig_predfile_"+((time+refTime)+"")+".txt",sss);
//									} catch (IOException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//									 
//							 }
							 continue;
					     }
						
						 StateGridFilter sgf=pdr.PathPrediction(testBres,g,Configuration.ProDown,Configuration.MAPPro,Configuration.MicroStateRadius, prd_num);
						
						 ArrayList<MacroState> mp=sgf.gfStates.getMacroStatePath();
						  

							 int len=mp.size()-1;
							//  number+=mp.size();
							 predcount+=len+1;

							 pred_num += predcount;

							 for(int i=0;i<=len;i++){//2016.7.8  
								 
								 MacroState pItem=mp.get(i);
								 /*if(i+refTime>rcGridList.size()-1){
									 continue;
								 }*/
								 RoICell tItem=rcGridList.get(i+refTime);
								
								pItem.writeFile((gridItem.getKey()+stid),time+refTime,i,len,tItem.roiX, tItem.roiY, out_folder);
								 
								 
						     }
					}
				}
				
				
//				System.out.println("realSum:"+realSum);	
				
			}
//		System.out.println("Now, the row number"+predcount);
		long estimatedTime = System.nanoTime() - startTime;
                whole_time += estimatedTime;
                double avg_time = (double)whole_time / pred_num;
                System.out.println("### the whole time: " + whole_time + " the pred num: " + pred_num + " the avg time: " + avg_time + " ###");
	
		String ss = (t_period+" iters")+(", the whole time: "+whole_time)+(", the pred num: "+pred_num)+(", the avg time: "+avg_time);

		try{
			writefile.writeStrToFile("cpu-time.txt", ss);
		} catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }

		System.exit(0);	
	}
	
	
	
}
