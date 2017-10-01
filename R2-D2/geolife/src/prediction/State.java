package prediction;

import grid.Configuration;
import grid.RoICell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import File.writefile;

import loadData.MapLoc2Grid;

public class State {

	int id;//each micro state has one id
	
	public int n;//the number of points, (LC.size())
	//public double minBound;//the distance to farthest point
	public HashSet<RoICell> LC;//record all the cells( or points)
	//public ArrayList<Integer> LT;//record all the trajectory id//change it into a hashmap
	public HashMap<Integer,RoICell> LT;//traId and its corresponding cells
	
	public double SumDensity;//the sum of density of all cells
	public double LSX;//x-the sum the position of all cells
	public double LSY;//y-..
	public double SSX;//x-the square sum of the position of all cells
	public double SSY;//y-..
	
	
	public State(int inId){
	
		id=inId;
		
		LC=new HashSet<RoICell>();
		//LT=new ArrayList<Integer>();
		LT= new HashMap<Integer,RoICell>();
		
		n=0;		
		SumDensity=0;
		LSX=0;
		LSY=0;
		SSX=0;
		SSY=0;
	}
	
	public ArrayList<Integer> getLTArray(){

		Set<Integer> LTKeys=LT.keySet();
		
		ArrayList<Integer> res=new ArrayList<Integer>(LTKeys);
		return res;
	}
	
	public State(int inId,State ms1,State ms2){
		id=inId;
		
		LC=new HashSet<RoICell>();
		//LT=new ArrayList<Integer>();
		LT= new HashMap<Integer,RoICell>();
		
		LC.addAll(ms1.LC);
		LC.addAll(ms2.LC);
		//LT.addAll(ms1.LT);
		//LT.addAll(ms2.LT);
		mergeLT(LT,ms1.LT);
		mergeLT(LT,ms2.LT);
		
		n=ms1.n+ms2.n;
		
		//sum of position
		LSX=ms1.LSX+ms2.LSX;
		LSY=ms1.LSY+ms2.LSY;
		
		//sum of square position
		SSX=ms1.SSX+ms2.SSX;
		SSY=ms1.SSY+ms2.SSY;
		
		//sum of density
		SumDensity=ms1.SumDensity+ms2.SumDensity;
	}
	
	public State(int inId, State ms1){
		id=inId;
		
		LC=new HashSet<RoICell>();
		LT=new HashMap<Integer,RoICell>();
		LC.addAll(ms1.LC);
		//LT.addAll(ms1.LT);
		mergeLT(LT,ms1.LT);
		
		n=ms1.n;
		
		//sum of position
		LSX=ms1.LSX;
		LSY=ms1.LSY;
		
		//sum of square position
		SSX=ms1.SSX;
		SSY=ms1.SSY;
		
		//sum of density
		SumDensity=ms1.SumDensity;
	}
	
	/**
	 * get the current center of 
	 * @return
	 */
	public double[] getCenter(){
		double[] c=new double[2];
		if(!Configuration.doSelfCorrection){
		c[0]=LSX/n;
		c[1]=LSY/n;
		return c;
		}else{
			return  getCenterWithLifetime();
		
		}
		
	}
	
	public double getStateTraLifetime(){
		if(!Configuration.doSelfCorrection){
			return LT.size();
		}
		else{
			double ltSum=0;
			Set<Entry<Integer,RoICell>> LTSet=LT.entrySet();
			Iterator<Entry<Integer,RoICell>> LTSetItr=LTSet.iterator();
			while(LTSetItr.hasNext()){
				Entry<Integer,RoICell> LTSetItem=LTSetItr.next();//one traId with cell
				//Integer lt=null;
				Integer lt=Configuration.lifetimeMap.get(LTSetItem.getKey());//get the lifetime
				Configuration.fullcount++;//for debug
				if(null==lt){//if empty, just 1
					ltSum+=1;
					Configuration.lossCount++;//for debug
				} else{
					
					double ltWeight=Math.pow(Configuration.doSelfParameter, lt-1);//Exponentially increase
					//double ltWeight=lt;
					ltSum+=ltWeight;
				}
			}
			return ltSum;
		}
	}
	
	/**
	 * get center with lifetime
	 * @return
	 */
	public double[] getCenterWithLifetime(){
		
			double[] c=new double[2];
			double LSXlt=0;
			double LSYlt=0;
			double ltSum=0;
			
			Set<Entry<Integer,RoICell>> LTSet=LT.entrySet();
			Iterator<Entry<Integer,RoICell>> LTSetItr=LTSet.iterator();
			while(LTSetItr.hasNext()){
				Entry<Integer,RoICell> LTSetItem=LTSetItr.next();//one traId with cell
				//Integer lt=null;
				Integer lt=Configuration.lifetimeMap.get(LTSetItem.getKey());//get the lifetime
				Configuration.fullcount++;//for debug
				if(null==lt){//if empty, just 1
					LSXlt+=LTSetItem.getValue().roiX;
					LSYlt+=LTSetItem.getValue().roiY;
					ltSum+=1;
					Configuration.lossCount++;//for debug
				} else{
					
					double ltWeight=Math.pow(Configuration.doSelfParameter, lt-1);//Exponentially increase
					//double ltWeight=lt;
					LSXlt+=LTSetItem.getValue().roiX*ltWeight;
					LSYlt+=LTSetItem.getValue().roiY*ltWeight;
					ltSum+=ltWeight;
				}
			}
			c[0]=LSXlt/ltSum;
			c[1]=LSYlt/ltSum;
			
			

			
			c[0]=LSXlt/ltSum;
			c[1]=LSYlt/ltSum;
			return c;
	
		
		
	}
	
	//Add Hashmap B into A
	private void mergeLT(HashMap<Integer,RoICell> LTA,HashMap<Integer,RoICell> LTB){
		if(null==LTB){
			return;
		}
		Set<Entry<Integer,RoICell>> LTBSet=LTB.entrySet();
		Iterator<Entry<Integer,RoICell>> LTBSetItr=LTBSet.iterator();
		
		while(LTBSetItr.hasNext()){
			Entry<Integer,RoICell> LTBItem=LTBSetItr.next();
			LTA.put(LTBItem.getKey(), LTBItem.getValue());
		}
	}
	
	/**
	 * the distance from center to a point
	 * @param txi
	 * @param tyi
	 * @return
	 */
	public double getDisCenter(int id,int time,int t,int len,double txi, double tyi) {
		// TODO Auto-generated method stub
		double c[]=getCenter();
		
		double p[]={txi,tyi};
		
		double d=Math.sqrt((c[0]-p[0])*(c[0]-p[0])+(c[1]-p[1])*(c[1]-p[1]));
		
		//System.out.println("棰勬祴鐐逛负锛�("+p[0]+","+p[1]+"),瀹為檯鐐逛负锛�"+c[0]+","+c[1]+"),棰勬祴璇樊涓猴細"+d);
		double s[]=MapLoc2Grid.transfersource(c[0],c[1]);
		double pp[]=MapLoc2Grid.transfersource(p[0],p[1]);
		double dd=Math.sqrt((s[0]-pp[0])*(s[0]-pp[0])+(s[1]-pp[1])*(s[1]-pp[1]));
		//System.out.println("杞崲鍚庨娴嬬偣涓猴細("+pp[0]+","+pp[1]+"),瀹為檯鐐逛负锛�"+s[0]+","+s[1]+"),棰勬祴璇樊涓猴細"+dd);
		String ss=(id+" ")+(t+" ")+(pp[0]+" ")+(pp[1]+" "); 
		 try {
			writefile.writeStrToFile("data/result/R2D2_geo1_predfile_"+(time+"")+".txt",ss);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 if (len<20&&t==len)
		 {
			 //璇存槑棰勬祴闀垮害涓嶅埌20锛岃ˉ鍏呭畬鏁�
			 
			 while(len<=20){
				 
				 String sss=(id+" ")+(len+" ")+(pp[0]+" ")+(pp[1]+" "); 
				 try {
					writefile.writeStrToFile("data/result/R2D2_geo1_predfile_"+(time+"")+".txt",sss);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 len++;
				 
			 }
			 
		 }
		 
		 
		 
		return d;
	}
	
	
	
	
	public void writeFile(int id,int time,int t,int len,double txi, double tyi, String out_folder) {
		
		
		
//		double pp[]=MapLoc2Grid.transfersource(txi,tyi);
		double cc[]=getCenter();
		double pp[]=MapLoc2Grid.transfersource(cc[0],cc[1]);
	//	pp[0] = pp[0]/10000 + 103;
	//	pp[1] = pp[1]/10000 + 1;
		
		//System.out.println("杞崲鍚庨娴嬬偣涓猴細("+pp[0]+","+pp[1]+"),瀹為檯鐐逛负锛�"+s[0]+","+s[1]+"),棰勬祴璇樊涓猴細"+dd);
		String ss=(id+" ")+((t+1)+" ")+(pp[0]+" ")+(pp[1]+" "); 
		 try {
			writefile.writeStrToFile(out_folder +(time+"")+".txt",ss);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		 if (len<20&&t==len)
//		 {
//			 //璇存槑棰勬祴闀垮害涓嶅埌20锛岃ˉ鍏呭畬鏁�
//			 len++;//鍥犱负浠�1寮�濮嬬紪鍙凤紝鍓嶉潰宸茬粡鍙栬繃浜唋en
//			 while(len<20){
//				 len++;
//				 String sss=(id+" ")+(len+" ")+(pp[0]+" ")+(pp[1]+" "); 
//				 try {
//					writefile.writeStrToFile("data/resulttest/R2D2_newsig_predfile_"+(time+"")+".txt",sss);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				 
//				 
//			 }
//			 
//		 }
		 
		 
		 
		
	}
	
	
	
	
	/**
	 * the distance from center to a center of the other micro state
	 * @param ms
	 * @return
	 */
	public double getDisCenter(State ms){
		double d;
	
		double c[]=getCenter();
		double p[]=ms.getCenter();
		d=Math.sqrt((c[0]-p[0])*(c[0]-p[0])+(c[1]-p[1])*(c[1]-p[1]));
		
		return d;
	}

	/**
	 * merge two state
	 * @param ms
	 */
	public void addState(State ms){
		//sum of position
		LSX+=ms.LSX;
		LSY+=ms.LSY;
		
		//sum of square position
		SSX+=ms.SSX;
		SSY+=ms.SSY;
		
		//sum of density
		SumDensity+=ms.SumDensity;
		
		LC.addAll(ms.LC);
		//LT.addAll(ms.LT);
		mergeLT(LT,ms.LT);
		
		n+=ms.n;
	}

	public int getSize(){
		return n;
	}
	
	
}
