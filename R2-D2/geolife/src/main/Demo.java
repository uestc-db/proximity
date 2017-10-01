package main;

import grid.Configuration;
import grid.Grid;
import grid.GridLeafTraHashItem;
import grid.RoICell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import TrajPrefixSpan.PatternConfiguration;
import TrajPrefixSpan.PatternPredictor;
import TrajPrefixSpan.TrajPrefixSpanDTBBFOld;

import loadData.BBFOldLoad;
import loadData.MapLoc2Grid;
import prediction.MacroState;
import prediction.Predictor;
import prediction.StateGridFilter;
import stp.predictor.MotionPredictor;
import stp.predictor.Point;
import visulalization.VisGrid;
import File.writefile;;
public class Demo {


	public static void main(String args[]){
		
			expPredictionErr_R2D2();
			//expPredictionErr_RMF();
			//expPredictionErr_TraPattern();
	}
		
	
//=================================method for R2-D2===================================================
	public static void expPredictionErr_R2D2(){
		
		Predictor pdr=new Predictor();

		BBFOldLoad tl=new BBFOldLoad();
		
		Configuration.BITS_PER_GRID=5;
		Configuration.MAX_LEVEL=3;
		Configuration.GridDivided=2048;
		

		Configuration.BBFOldXMin=931700000;
		Configuration.BBFOldYMin=664400000;
		Configuration.BBFOldXMax=932500000;
		Configuration.BBFOldYMax=665200000;
	
		Configuration.T_period=200;
		
		Configuration.BrinkConstraintRoI=16; 
		Configuration.TraSupport=1;
		
		//define parameter related with MacroState and MicroState 
		Configuration.MaxRadius=5000; //maximum size of state, not import parameters. It is defined on real distance
		Configuration.MaxStateDis=500;
		Configuration.AlphaRadius=1.5;//should be larger than 1
		Configuration.AlphaScore=1/16.0;
		Configuration.ProDown=0.001;
		Configuration.MAPPro=0.001;
		
		Configuration.MicroStateRadius=Configuration.cellRadius*2;
				
		int refTime=20;
		
		int DBBackStep=3;//论文中说越小越好
		
		int sampleNum=200;//采样的用户数目
		int sampleLen=30;
		
		int timeStart=0;
		int timeEnd=500;
		
		int sampleStart=0;
		int sampleEnd=500;
		tl.setSample(sampleNum, sampleLen,sampleStart,sampleEnd);
		//we load the data from sqlite database into our Trajectory Grid structure
		System.out.println("#start loading data and sample queries from data/BigBrinkhoff/bigBrinkhoff.db tablename: littlegeo");
		Grid g=tl.Load2Grid("data/mytestfour.db", "littlegeo",timeStart ,timeEnd);
		System.out.println("#finished loading data");
		
		
		
		
		
		System.out.println("#show part of the map");
		VisGrid vg=VisGrid.visGridPart(g, 0, 0,Configuration.GridDivided,Configuration.GridDivided);//show a map
		
		HashMap<Integer, ArrayList<RoICell>> gridRes=tl.getGridSampleList();
		
		Iterator<Entry<Integer,ArrayList<RoICell>>> lItr=gridRes.entrySet().iterator();
		int[] avgCount=new int[sampleLen+refTime];
		double[] avgDT=new double[sampleLen+refTime];

		int stpCount=0;
		for(int j=0;j<sampleLen+refTime;j++){
			avgDT[j]=0;
			avgCount[j]=0;
		}
		
		double countSum=gridRes.size();
		int vlCount=0;
		System.out.println("gridRes.size():"+gridRes.size());
		System.out.println("#start prediction and statistic the prediction rate and prediction error");
		while(lItr.hasNext()){
			
			long dtStart=0,dtEnd=0;
			Entry<Integer,ArrayList<RoICell>> gridItem=lItr.next();
		
			ArrayList<RoICell> rcGridList=gridItem.getValue();
			//get the trajectory of predicted objec
			//System.out.println("rcGridList:"+ rcGridList.size());
			ArrayList<RoICell> ref=new ArrayList<RoICell>(rcGridList.subList(refTime-DBBackStep+1, refTime+1));
	
			dtStart=System.currentTimeMillis();
			//lookup, retrieve the similar trajectories，section3.1 and section 5
			 ArrayList<Entry<Long, GridLeafTraHashItem>>  testBres=g.queryRangeTimeSeqCells(ref);
			 if(null==testBres||testBres.size()<Configuration.TraSupport) {
				 
				 //continue;
				 //说明这个点不可预测，则所有的预测20个点都用这个点来代替,向文件中写入20个点
				 int id,t;
				 double p[];
				 id=gridItem.getKey();
				 p=MapLoc2Grid.transfersource(rcGridList.get(refTime).roiX,rcGridList.get(refTime).roiY);
				 System.out.println("输入文件");
				 for(t=0;t<20;t++)
				 {
					 String s=(id+"")+(t+"")+(p[0]+"")+(p[1]+""); 
					 try {
						writefile.writeStrToFile("data/test.txt",s);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 }
				 
			 }
			// make prediction ,section 6
			 System.out.println("id:   refsize"+gridItem.getKey()+"  "+testBres.size());
			 StateGridFilter sgf=pdr.PathPrediction(testBres,g,Configuration.ProDown,Configuration.MAPPro,Configuration.MicroStateRadius);
			//retrieve predicted path
			 ArrayList<MacroState> mp=sgf.gfStates.getMacroStatePath();
			  dtEnd=System.currentTimeMillis();
              //double  x[]={rcGridList.get(refTime).roiX,rcGridList.get(refTime).roiY};
				 int len=mp.size()-1;
				
				 for(int i=1;i<=len;i++){
					 
					 MacroState pItem=mp.get(i);
					 if(i+refTime>rcGridList.size()-1){
						 continue;
					 }
					 RoICell tItem=rcGridList.get(i+refTime);
					// System.out.println("id:  i:"+gridItem.getKey()+"   "+i);
					// double d_dt=pItem.getDisCenter(gridItem.getKey(),i,len,tItem.roiX, tItem.roiY);
					// x=pItem.getCenter();
					// avgDT[i]+=d_dt;
				 avgCount[i]++;	 
			     }
				 
				
		}
		
		
		//time step,预测次数，可预测的比例，距离误差（预测位置与实际位置的差值）
		System.out.println("#time	count	Rate	DT_error	");
		for(int i=1;i<avgDT.length;i++){
			System.out.println(i+"	"+avgCount[i]+"	"+avgCount[i]/(countSum-vlCount)+"	"+avgDT[i]/avgCount[i]*9+"	");//error is to multiple the cell width
		}
	
		System.out.print("#stp count:"+stpCount);
	}
	
	
	//=================================method for RMF===================================================
	public static void expPredictionErr_RMF(){
		
		BBFOldLoad tl=new BBFOldLoad();
		
		int refTime=4;

		int STPBackStep=3;
		
		int sampleNum=1000;
		int sampleLen=10;
		
		int timeStart=0;
		int timeEnd=15;
		
		int sampleStart=0;
		int sampleEnd=15;
		
		tl.setSample(sampleNum, sampleLen,sampleStart,sampleEnd);
		
		System.out.println("#start loading data and sample queries from data/BigBrinkhoff/bigBrinkhoff.db tablename: BBFOldTest");
		Grid g=tl.Load2Grid("data/BigBrinkhoff/bigBrinkhoff.db", "BBFOldTest",timeStart ,timeEnd);
		System.out.println("#finished loading data");
				
		HashMap<Integer,ArrayList<stp.predictor.Point>> locRes=tl.getLocSampleList();
		
		int[] avgCount=new int[sampleLen+refTime];
		double[] avgDT=new double[sampleLen+refTime];
		double[] avgSTP=new double[sampleLen+refTime];
		int stpCount=0;
		for(int j=0;j<sampleLen+refTime;j++){
			avgDT[j]=0;
			avgSTP[j]=0;
			avgCount[j]=0;
		}
		
		
		Iterator<Entry<Integer,ArrayList<stp.predictor.Point>>> rcLocItr=locRes.entrySet().iterator();
		stpCount=0;
		while(rcLocItr.hasNext()){
			long stpStart=0,stpEnd=0;
			 long stpTime=0;
			Entry<Integer,ArrayList<stp.predictor.Point>> locItem=rcLocItr.next();
			ArrayList<stp.predictor.Point> rcLocList=locItem.getValue();
			 
			 stpStart=System.currentTimeMillis();

			 List<stp.predictor.Point> stpRef= rcLocList.subList(refTime-STPBackStep+1, refTime+1);

			 stpCount++;
			 ArrayList<stp.predictor.Point> stpP=MotionPredictor.STPLocPredictor(stpRef, sampleLen-refTime);
 
			 stpEnd=System.currentTimeMillis();
			stpTime=stpEnd-stpStart;
		for(int j=1;j<sampleLen-refTime;j++){
			 stpStart=System.currentTimeMillis();
			 
			 stp.predictor.Point locItemTrue=rcLocList.get(j+refTime);
			 stp.predictor.Point stpItem=stpP.get(j-1);
			 stpEnd=System.currentTimeMillis();
			 stpTime+=stpEnd-stpStart;
			 double d_stp=stpItem.distance(locItemTrue.x, locItemTrue.y);//
			 avgSTP[j]+=d_stp;
		}
		}
		
		System.out.println("#time	RMF error");
		for(int i=1;i<avgDT.length;i++){
			System.out.println(i+"	"+avgSTP[i]/stpCount);
		}
	
		System.out.print("#stp count:"+stpCount);
	}
	
	
//=================================method for TraPattern===================================================
	public static void expPredictionErr_TraPattern(){
		
		String[] arg=new String[6];
		arg[0]="BBFOldTest";
		arg[1] = "0";
		arg[2] = "15";
		arg[3] = "5";//support
		arg[4] = "1024";//GridDivision
		arg[5]= "-1";
		System.out.println("input parameter: table startTime endTime Support division sqlen");
		
		expPredictionErr_TraPattern_mining(arg);
		expPredictionErr_TraPattern_prediction(arg);
	}
	
	
	public static void expPredictionErr_TraPattern_mining(String[] arg){
		
		
		System.out.println("start mining pattern from data"+arg[0]);
		TrajPrefixSpanDTBBFOld.testBBFOldTrajPrefixSpanFPtreeSupport(arg);
		System.out.println("finish mining pattern:)");
		 
		System.out.println("start making prediction based on pattern");		
	}
	
	public static void expPredictionErr_TraPattern_prediction(String[] arg){
			

		PatternConfiguration.TrajDB="data/BigBrinkhoff/bigBrinkhoff.db";
		PatternConfiguration.TrajTable=arg[0];
		
		PatternConfiguration.T_sample=1;

		
		PatternConfiguration.LatMin=Configuration.BBFOldXMin = 292.0;
		PatternConfiguration.LngMin=Configuration.BBFOldYMin=3935.0;
		PatternConfiguration.LatMax= Configuration.BBFOldXMax=23056.0;
		PatternConfiguration.LngMax=Configuration.BBFOldYMax=30851.0;
		PatternConfiguration.GridDivision=Integer.parseInt(arg[4]);
		PatternConfiguration.support=Integer.parseInt(arg[3]);
		
		PatternPredictor patternPredictor=new PatternPredictor();

		long startLoading = System.currentTimeMillis();
		String fptreeStr = "FPTree/BBFOld/BBFOldTest_s-"+arg[1]+"_e-"+arg[2]+"_sup-"+arg[3]+"_div-"+arg[4]+"_seqLen-"+arg[5]+".fptree";
		patternPredictor.readPatternFP(fptreeStr);
		long endLoading = System.currentTimeMillis();
		System.out.println("#High of FP tree:"
				+ patternPredictor.patternFP.getHight());
		System.out.println("#loading time of FPtree:"
				+ (endLoading - startLoading) + " ms");
		System.out.println("Using FPTree:" + fptreeStr);

		BBFOldLoad tl = new BBFOldLoad();

		int refTime = 4;

		int prefixSpanBack = 1;

		int sampleNum = 1000;
		int sampleLen = 10;


		int sampleStart = Integer.parseInt(arg[1]);
		int sampleEnd = Integer.parseInt(arg[2]);
		int sampleSkip = 1;

		double[] avgPrefixSpan = new double[2 * sampleLen + refTime];
		int[] prefixSpanCount = new int[2 * sampleLen + refTime];

		for (int j = 0; j < sampleLen + refTime; j++) {

			avgPrefixSpan[j] = 0;
			prefixSpanCount[j] = 0;
		}
		PatternConfiguration.TrajNum = 200000;
		ArrayList<ArrayList<Point>> sampleRes = tl.sampleFromDB(
				PatternConfiguration.TrajDB, PatternConfiguration.TrajTable,
				sampleStart, sampleEnd, sampleNum, sampleLen, sampleSkip);

		long startPrediction = System.currentTimeMillis();
		int prefixSpanSumCount = sampleRes.size();
		for (int i = 0; i < sampleRes.size(); i++) {
			ArrayList<Point> rcLocList = sampleRes.get(i);

			List<stp.predictor.Point> prefixSpanRef = rcLocList.subList(refTime
					- prefixSpanBack + 1, refTime + 1);
			ArrayList<stp.predictor.Point> prefixSpanRes = patternPredictor
					.PrefixSpanWholePredictorReal(
							new ArrayList<stp.predictor.Point>(prefixSpanRef),
							0);

			for (int j = 1; j < prefixSpanRes.size() + 1
					&& j + refTime < rcLocList.size(); j++) {

				stp.predictor.Point locItemTrue = rcLocList.get(j + refTime);
				stp.predictor.Point stpItem = prefixSpanRes.get(j - 1);

				double d_stp = stpItem.distance(locItemTrue.x, locItemTrue.y);//
				avgPrefixSpan[j] += d_stp;
				prefixSpanCount[j]++;
			}

		}
		long endPrediction = System.currentTimeMillis();
		System.out.println("#Pattern count:" + prefixSpanSumCount);
		System.out.println("#prediction time:"
				+ (endPrediction - startPrediction) + " ms");
		System.out.println("#timePrefixSpan(s) count Rate PrefixSpan error");
		for (int i = 1; i < avgPrefixSpan.length; i++) {
			System.out.println(i + " " + prefixSpanCount[i] + "	"
					+ ((double) prefixSpanCount[i]) / prefixSpanSumCount + " "
					+ avgPrefixSpan[i] / prefixSpanCount[i] );
		}

		System.out.println("total count:" + prefixSpanSumCount);

	}
	

		
}
