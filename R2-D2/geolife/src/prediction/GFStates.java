package prediction;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * collect all the GFStateItem at all predictable time stamp
 * @author workshop
 *
 */
/*
 *cxy.2016.3.16
 *将所有可能的statespace收集起来 。公式部分section6.1
 *Then, PF estimates a future path of Op in two steps. First, at each future time t = t0 + k, we generate the state space Sk of Op (see Section 6.2). For example, when k = 2 we have S2 =
{s0 2, s1 2}. A sequence of states is a possible path. Next, PF selects the longest path whose probability is larger than the probability
threshold θ. If there are multiple such paths, the one with the highest probability is selected (see Section 6.1).

To find the longest path whose probability is greater than the given threshold θ, we increase the length of the predicted path until its probability is smaller than θ. To realize this, we increase
the value of k, and for each k value we find a path SS1:k =<s1, ..., sk > whose value of p(SS1:k|RO1:k) is maximized and then check whether its probability is still greater than θ.
 */
public class GFStates {
	
	ArrayList<GFStatesItem> gfsList;
	
	ArrayList<Double[]> delta;//section 6.1中的函数ηk
	ArrayList<Integer[]> psi;//记录路径SS1:k
	double max_delta;
	
	public GFStates(){
		gfsList=new ArrayList<GFStatesItem>();
		gfsList.add(new GFStatesItem());
		
		delta=new ArrayList<Double[]>();
		Double[]delta_0=new Double[1];
		delta_0[0]=1.;
		delta.add(delta_0);
		
		psi=new ArrayList<Integer[]>();
		Integer[] psi_0=new Integer[1];
		psi_0[0]=0;
		psi.add(psi_0);
		
		max_delta=-1;
		
	}
	
	public double getMaxDelta(){
		return max_delta;
	}
	
	/**
	 * update the state by traIdCommSet
	 * @param traIdCommSet: a hashmap records the weights of different reference objects
	 */
	public void updateStatesContinuousWeights(HashMap<Integer,Integer> traIdCommSet){
		
	}
	
	/**
	 * 
	 * @param gfi
	 * @return: the current maximum value of MAP path
	 */
	public void addStatesItem(GFStatesItem gfi){
		gfsList.add(gfi);
		max_delta= MAPForward();
	}
	
	/**
	 * the recursion part of MAP
	 * @param k
	 * @return return the maximum probability of the path at current timestamp k
	 */
	private double MAPForward(){
		int k=gfsList.size()-1;
		int N_ks=getStateNum(k);//最远的那个statespace里面有多少个Microstate
		GFStatesItem gfski=getStatesItem(k);//获取最远的那个（刚发生的）statespace
		
		
		Double delta_ki[]=new Double[N_ks];
		Integer psi_ki[ ]=new Integer[N_ks];
		
		delta.add(delta_ki);
		psi.add(psi_ki);
		
		assert(delta.size()==k+1);
		assert(psi.size()==k+1);
			
		double[] p_zk_xk= gfski.P_z_k_x_k;  //the observation likelihood function,section6.3.2中的Likehood function
		double[][] p_xk_xk_1=gfski.P_x_k_x_k_1;//the translation function,section6.3.1中的transition function
		
		double maxPro=-1;
		for(int i=0;i<N_ks;i++){
			double pro=MAPMaxDeltaPsiUpdate(k,i,p_xk_xk_1[i],p_zk_xk[i]);//update delta_kj and psi_kj
			if(pro>maxPro) maxPro=pro;
		}
		return maxPro;
	}
	
	
	/**
	 * update delta_kj and psi_kj
	 * @param k=[1,...,T]
	 * @param j
	 * @param p_x_kj_x_k_1
	 * @param p_zk_xkj
	 */
	public double MAPMaxDeltaPsiUpdate(int k,int ki,double p_x_kj_x_k_1[], double p_zk_xkj){
		double max=-1;
		int max_index=-1;
		int N_k_1s=getStateNum(k-1);
		//double[] item=new double[N_k_1s];
		double item=0;
		for(int j=0;j<N_k_1s;j++){
			item=delta.get(k-1)[j]*p_x_kj_x_k_1[j];//section 6.1中的公式(4),结合下面的等式大白
			if(item>max){
				max=item;
				max_index=j;
			}
		}
		
		delta.get(k)[ki]=max*p_zk_xkj;//大白
		psi.get(k)[ki]=max_index;
		
		return max*p_zk_xkj;
	}
	
	
	
	/**
	 * 
	 * @param T
	 * @param path
	 * @param maxPro
	 */
	public int[] MAPTraceBack(){
		
		//int T=timeTraState.getTimeLength();
		int seedIdx[]=new int[1];
	
		if(gfsList.size()<1) return null; 
		int T=gfsList.size()-1;
		
		int[] path=new int [T+1];//the one more position is left for virtual point at time t=0
		
		path[T]=argmaxIndex(this.delta.get(T));//找到第T个delta[]中最大的delta,返回其索引
		
		for(int t=T-1;t>=0;t--){
			path[t]=psi.get(t+1)[path[t+1]];
		}
		return path;
	}
	
	/**
	 * 
	 * @return
	 */
	//path里面记录了最优路径上的各个sk对应的存储在mac里面的索引值，故此函数根据索引值一次取出对应的microstate组成最优路径
	public ArrayList<MacroState> getMacroStatePath(){
		int[] path=MAPTraceBack();
		if(null==path) return null;
		ArrayList<MacroState> res=new ArrayList<MacroState>();
		
		res.add(new MacroState(-1));
		
		for(int i=1;i<gfsList.size();i++){
			res.add(gfsList.get(i).macs.get(path[i]));
		}
		return res;
	}
	
	private int argmaxIndex(Double[] a){//返回a中最大元素的索引
		double max=-1;
		int argmax=-1;
		for(int i=0;i<a.length;i++){
			if(max<a[i]){
				max=a[i];
				argmax=i;
			}
		}
		return argmax;
	}
	
	/**
	 * 
	 * @param k time stamp k
	 * @return
	 */
	public int getStateNum(int k){
		if(0==k) return 1;
		return gfsList.get(k).macs.size();
	}
	
	/**
	 * i-th state at time k
	 * @param k
	 * @param ki: i-th state
	 * @return
	 */
	public MacroState  getState(int k,int ki){
		return gfsList.get(k).macs.get(ki);
	}
	
	/**
	 * get GFStateItem at time k
	 * @param k: time k
	 * @return
	 */
	public GFStatesItem getStatesItem(int k){
		return gfsList.get(k);
	}

}
