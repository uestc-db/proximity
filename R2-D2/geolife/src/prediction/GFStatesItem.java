package prediction;

import grid.Configuration;

import java.util.ArrayList;

/**
 * the possible states for grid filter at a time stamp
 * @author workshop
 *in order to get a suitable states, there are three steps, they are:
 *1. addMacStates();
 *2. addGFSat();
 *3. AddGFW();
 */
/*
 * cxy.2016.3.16
 * 对应论文section3.2中的statespace
 */
public class GFStatesItem {
	ArrayList<MacroState> macs;
	
	
	double[] w;//weight of each states
	double[][] P_x_k_x_k_1;//probablity from x_k-1^i to x_k^j，着两个参数是section6.1中计算权重用到的p(si k|sj k−1)和p(ROk|si k)
	double [] P_z_k_x_k;//under th state x_k, the probablity of z_k( observation)
	
	int traSumNum;//the total number of trajectories, maintane it!!
	double[] beta;//compute by density
	
	int addCount;
	public GFStatesItem(){
		
		addCount=0;
	}
	
	/**
	 * add the macro state into the Item. However, other useful information still is empty
	 * 1).compute the total number of trajectories for prediction
	 * 2).compute beta
	 * @param inMacs
	 */
	public void addMacStates(ArrayList<MacroState> inMacs){
		macs=inMacs;
		traSumNum=compTraSumNum();
		beta=compBeta();
		addCount++;
	}
	
	/**
	 * 
	 * @param in_p_x_k_x_k_1: //probablity from x_k-1^i to x_k^j
	 * @param in_p_z_k_x_k: //under th state x_k, the probablity of z_k( observation)
	 */
	public void addGFSat(double[][] in_p_x_k_x_k_1,double[] in_p_z_k_x_k){
		P_x_k_x_k_1=in_p_x_k_x_k_1;
		P_z_k_x_k=in_p_z_k_x_k;
		
		addCount++;
	}
	
	/**
	 * weight of each states
	 * @param inW
	 */
	public void addGFW(double [] inW){
		w=inW;
		
		addCount++;
	}
	
	/**
	 * test wether this item is good enough for our prediction. Currently, the 
	 * condition is at least one state has weight whichi is larger than p.
	 * @param p
	 * @return
	 */
	public boolean statCondition(double p){
		assert(addCount==3);
		
		for(int i=0;i<w.length;i++){
			if(w[i]>=p) return true;
		}
		
		return false;
	}
	
	/**
	 * all the number of trajectories for prediction 
	 * @return
	 */
	private int compTraSumNum(){
		int sum=0;
		for(int i=0;i<macs.size();i++){
			sum+=macs.get(i).LT.size();
		}
		return sum;
	}

	/**
	 * compute beta，在公式(14)里面，计算先验概率的，rou是平均的density
	 * @return
	 */
	private double[] compBeta(){
		double[] beta_i=new double[macs.size()];
		
		double C=0;
		for(int i=0;i<macs.size();i++){
			double sumDensity=macs.get(i).SumDensity;
			double n=macs.get(i).n;
			beta_i[i]=sumDensity/n;
			C+=beta_i[i];
		}
		
		for(int i=0;i<macs.size();i++){
			beta_i[i]/=C;
		}
		
		return beta_i;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getStateNum(){
		return macs.size();
	}
	
	/**
	 * 
	 * @param i
	 * @return
	 */
	public MacroState getState(int i){
		return macs.get(i);
	}
	
	/**
	 * 
	 * @return
	 */
	public int getTraSumNum(){
		return traSumNum;
	}
	
	public double getTraSumLifetime(){
		double lifetimeSum=0;
		for(int i=0;i<macs.size();i++){
			lifetimeSum+=macs.get(i).getStateTraLifetime();
		}
		return lifetimeSum;
	}
	
	public double getTraLifetimePerState(int i){
		return macs.get(i).getStateTraLifetime();
	}
	
	/**
	 * 
	 * @param i
	 * @return
	 */
	public int getTraNumPerState(int i){
		return macs.get(i).LT.size();
	}
	
	public double getScore(){//section 6.2.2 score函数,在概率和半径之间的一个权衡
		double p=-1;
		int index=-1;
		for(int i=0;i<w.length;i++){
			if(w[i]>p){
				p=w[i];
				index=i;
			}
		}
		
		double R=macs.get(index).getRadius();
		return p/Math.pow(R, Configuration.AlphaScore);
		
		
	}
	
	/*public double JUNKgetScore(){
				
		double entropy=0;
		
		for(int i=0;i<w.length;i++){
			entropy+=-w[i]*Math.log(w[i]);
		}
		
		double error=0;
		double sumPR=0;
		for(int i=0;i<this.macs.size();i++){
			double r=macs.get(i).getRadius();
			sumPR=w[i]*r;
		}
		
		error=sumPR/Configuration.MicroStateRadius;
		
		
		return Configuration.AlphaScore*entropy+(1-Configuration.AlphaScore)*error;
	}
	
	//for debug
	
	public double JUNKgetScoreTrue(){
		
		double entropy=0;
		
		for(int i=0;i<w.length;i++){
			entropy+=-w[i]*Math.log(w[i]);
		}
		
		double error=0;
		double sumPR=0;
		for(int i=0;i<this.macs.size();i++){
			double r=macs.get(i).getRadius();
			sumPR=w[i]*r;
		}
		
		error=Math.log(sumPR)/Math.log(Configuration.MicroStateRadius);
		
		
		return Configuration.AlphaScore*entropy+(1-Configuration.AlphaScore)*error;
	}*/
	//end for debug

	public String toSring(){
		String str="";
		//System.out.println("State Num:"+macs.size());
		for(int i=0;i<macs.size();i++){
			str+=" cells number:"+macs.get(i).getSize();
			str+=", State center: x:"+macs.get(i).getCenter()[0]+", y: "+macs.get(i).getCenter()[1]+", R: "+macs.get(i).getRadius();
			str+=", W(p): "+w[i]+" "+",\n";
		}
		return str;

	}
	
}
