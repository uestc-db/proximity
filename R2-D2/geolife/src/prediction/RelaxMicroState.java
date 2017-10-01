package prediction;

import grid.Configuration;
import grid.Grid;
import grid.GridLeafTraHashItem;

import java.util.ArrayList;
import java.util.Map.Entry;

public class RelaxMicroState {
	private static int RELAX_MICROSTATE_ID=-1;
	private ArrayList<Entry<Long,GridLeafTraHashItem>> Ltratime;
	
	private double LSX;//x-the sum the position of all cells
	private double LSY;//y-..
	private int n;
	
	public RelaxMicroState(){
		
		Ltratime=new ArrayList<Entry<Long,GridLeafTraHashItem>>();
		
		LSX=0;
		LSY=0;
		n=0;
	}
	

	public void addLtratime(ArrayList<Entry<Long,GridLeafTraHashItem>> in){
		Ltratime.addAll(in);
		
		for(Entry<Long,GridLeafTraHashItem> ein:in){
			LSX+=ein.getValue().getCellX();
			LSY+=ein.getValue().getCellY();
		}
		n+=in.size();
		
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	private void minusLS(double x,double y){
		LSX-=x;
		LSY-=y;
		n--;
	}
	
	/**
	 * get the current center of 
	 * @return
	 */
	public double[] getCenter(){
		double[] c=new double[2];
		
		c[0]=LSX/n;
		c[1]=LSY/n;
		
		return c;
	}
	
	/**
	 * the distance from center to a point
	 * @param txi
	 * @param tyi
	 * @return
	 */
	public double getDisCenter(double txi, double tyi) {
		// TODO Auto-generated method stub
		double c[]=getCenter();
		
		double p[]={txi,tyi};
		
		double d=Math.sqrt((c[0]-p[0])*(c[0]-p[0])+(c[1]-p[1])*(c[1]-p[1]));
		
		return d;
	}
	
	/**
	 * generate a set of micro state from relax micro state
	 * @param r
	 * @param inG
	 * @return
	 */
	public ArrayList<MicroState> generateMics(double r,Grid inG){
		if(null==this.Ltratime||0==n){
			return null;
		}
		
		ArrayList<MicroState> res=new ArrayList<MicroState>();
		
		MicroState micDomain=new MicroState(Configuration.getStateId());//store the master state
		
		//visit all points
	    for(Entry<Long,GridLeafTraHashItem> en:Ltratime){
	    	int enx=en.getValue().getCellX();
	    	int eny=en.getValue().getCellY();
	    	//System.out.println("generatemic");
	    	double den=inG.getGridCell(enx, eny).density;//get density
	    	
	    	if(this.getDisCenter(enx, eny)>r){//split this point as a new state
	    		MicroState enMic=new MicroState(Configuration.getStateId());
	    		enMic.addPoint(enx, eny, den, en);
	    	
	    		minusLS(enx,eny);	
	    		
	    		res.add(enMic);
	    	}else{
	    		micDomain.addPoint(enx, eny, den, en,false);//keep them as a new state
	    	}
	    }
	    micDomain.setMinBound();
	    res.add(micDomain);
		
		return res;
	}
}
