package TrajPrefixSpan;

import grid.Configuration;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import stp.predictor.Point;

import TrajPrefixSpan.FPGrowthDT.FPNode;
import TrajPrefixSpan.FPGrowthDT.FPTree;
import TrajPrefixSpan.PrefixSpanDT.AlgoPrefixSpanFP;
import TrajPrefixSpan.PrefixSpanDT.SequenceDatabase;

public class PatternPredictor {
	public FPTree patternFP;
	public PatternPredictor(){
		CalibrationParameter(PatternConfiguration.LatMin, PatternConfiguration.LngMin,
				PatternConfiguration.LatMax, PatternConfiguration.LngMax,PatternConfiguration.GridDivision);
	}
	
	public void readPatternFP(String fptreeFile){
		
		FPTree fpRead=null;

		try{
		      //use buffering
		      InputStream file = new FileInputStream( fptreeFile );
		      InputStream buffer = new BufferedInputStream( file );
		      ObjectInput input = new ObjectInputStream ( buffer );
		      try{
		        fpRead=(FPTree) input.readObject();
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
		    
		    setPatternFP(fpRead);
		
	}
	
	public void setPatternFP(FPTree inPatterns){
		patternFP=inPatterns;
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
	
	public int tranferRealToLogic(double lat, double lng ) {
		double offx = lat - lat0;
		double offy = lng - lng0;
		int gridX = (int) ( offx / (step0));
		int gridY = (int) ( offy / (step0));
		
		int tempX=gridX<<16;
		int tempY=gridY<<16;//clear high
		int keyXY=tempX+(tempY>>16);

		return keyXY;
}
	
	public Point tranferLogicalToGrid(int keyXY){
		Point res=new Point();
		
		res.x=keyXY>>16;
		res.y=(keyXY<<16)>>16;
		
		return res;
	}
	
	public Point tranferGridToReal(Point grid){
		Point res=new Point();
		
		res.x=grid.x*step0+lat0;
		res.y=grid.y*step0+lng0;
		
		return res;
	}
	
	public Point transferLogicalToReal(int keyXY){
		Point g=tranferLogicalToGrid(keyXY);
		Point r=tranferGridToReal(g);
		return r;
	}
	
	public ArrayList<Point> PrefixSpanWholePredictorReal(ArrayList<Point> tail,double usrConfidence){
		ArrayList<Integer> logicTail=new ArrayList<Integer>();
		
		for(Point tp:tail){
			int tpLogical=tranferRealToLogic(tp.x,tp.y);
			logicTail.add(tpLogical);
		}
		
		ArrayList<Integer> preResLogical=PrefixSpanWholePredictorLogic(logicTail,usrConfidence);
		
		ArrayList<Point> preResReal=new ArrayList<Point>();
		
		for(Integer keyItem:preResLogical){
			Point pitem=transferLogicalToReal(keyItem);
			preResReal.add(pitem);
		}
		
		return preResReal;
		
	}
	
	public ArrayList<Integer> PrefixSpanWholePredictorLogic(ArrayList<Integer> tail,double usrConfidence){
		ArrayList<Integer> res=new ArrayList<Integer>();
		
		ArrayList<FPNode> cadidateNodeList=new ArrayList<FPNode>();//candidate nodes
		ArrayList<FPNode> matchNodeList=new ArrayList<FPNode>();
		ArrayList<ArrayList<Integer>> matchPath=new ArrayList<ArrayList<Integer>>();
		
		assert tail.size()>0:"input trajectories error! tail cannot be 0";
		
		Set<Entry<Integer, FPNode>> mapValue=patternFP.mapItemNodes.entrySet();
		
		Iterator<Entry<Integer,FPNode>> mapValueItr=mapValue.iterator();
		
		FPNode start;
		
		while(mapValueItr.hasNext()){
			Entry<Integer,FPNode> fpEntry=mapValueItr.next();
			FPNode fpn=fpEntry.getValue();
			
			Integer key=tail.get(tail.size()-1);
			int x=(key>>16);
			int y=((key<<16)>>16);
			
			int mapkey=fpEntry.getKey();
			int mapx=(mapkey>>16);
			int mapy=((mapkey>>16)<<16);
			
			if(mapx==x&&mapy==y){
				start=fpn;
			}
		}
		
		start=patternFP.mapItemNodes.get(tail.get(tail.size()-1));
		
		//add all the node into nodeList
		while(null!=start){
			cadidateNodeList.add(start);
			start=start.nodeLink;
		}
		
		for(FPNode cc:cadidateNodeList){
			boolean cIsOK=true;
			FPNode visitorC=cc;
		for(int i=tail.size()-1;i>=0;i--){
			if(tail.get(i)==visitorC.itemID){
				visitorC=visitorC.parent;
			}else{
				cIsOK=false;
				break;
			}
		}
		if(cIsOK){
			matchNodeList.add(cc);
			ArrayList<Integer> ccPath=new ArrayList<Integer>();
			matchPath.add(ccPath);
		}
		}
		
		if(matchNodeList.size()==0) return res;
		
	
		//traverse to find the longest path
		for(int i=0;i<matchNodeList.size();i++){
			FPNode mc=matchNodeList.get(i);
			ArrayList<Integer> mcPath=matchPath.get(i);
			
			while(null!=mc.childs){
				
				 ArrayList<FPNode> mcChilds=new ArrayList<FPNode>(mc.childs);
				 double supportMc=mc.counter;
				 double maxChild=-1;
				 FPNode bestChild=null;
				 
				 //find the best child for path
				 for(FPNode mcChild:mcChilds){
					 double supportChild=mcChild.counter;
					 double childCon=supportChild/supportMc;
					 if(childCon>maxChild){
						 maxChild=childCon;
						 bestChild=mcChild;
					 }
				 }
				 
				 if(maxChild>=usrConfidence){
					 mc=bestChild;
					 mcPath.add(mc.itemID);
				 } else{
					 break;
				 }
			}
		}
		
		int maxLen=0;
		for(int i=0;i<matchPath.size();i++){
			int itemLen=matchPath.get(i).size();
			if(itemLen>maxLen){
				maxLen=itemLen;
				res=matchPath.get(i);
			}
		}
		
		return res;
		
	}
	
	
	public static void testPredictorTaxi(){
		
		  PatternPredictor pp=new PatternPredictor();
	
		// Load a sequence database
		  pp.CalibrationParameter(Configuration.TaxiLatMin,Configuration.TaxiLngMin,
				 Configuration.TaxiLatMax, Configuration.TaxiLngMax,256);
	      pp.readPatternFP("PrefixSpanTest.fptree");
	      
	    int h=  pp.patternFP.getHight();
	    System.out.println("FP hights:"+h);
	    System.out.println("finish loading FPtree...");
				   
		   ArrayList<Point> tail=new ArrayList<Point>();
		   Point p1=new Point();//(4<<16)+5;//(1,5)
		   Point p2=new Point();//(5<<16)+5;//(2,5)
		   Point p3=new Point();//(3<<16)+5;//(3,5)
		   
		   //03/06/2008,00:00:10,	C1,	103.626206,	1.240248641,	153,	POB		
		   //03/06/2008,00:00:10,	C1,	103.626206,	1.241771922,	153,	POB		
		  // 03/06/2008,00:00:10,	C1,	103.626206,	1.243295203,	153,	POB
		   
		   //in the database, lat and lng is reversed
		   p1.y=103.626206;
		   p1.x=1.240248641;
		   p2.y=103.626206;
		   p2.x=1.241771922;
		   p3.y=103.626206;
		   p3.x=1.243295203;
		   
		   tail.add(p1);
		   tail.add(p2);
		  // tail.add(p3);
		   
		   ArrayList<Point>ppRes=pp.PrefixSpanWholePredictorReal(tail, 0.8);
		  
		   System.out.println("Prediction result");
		   for(int i=0;i<ppRes.size();i++){
			   Point ppResItem=ppRes.get(i);
			   System.out.println("time:"+i+" (x,y):"+" ("+(ppResItem.x)+","+(ppResItem.y)+")");
		   }
		   
	
	
		
	}
	
	public static void main(String [] arg) throws IOException{
		
		testPredictorTaxi();
	}

	
}
