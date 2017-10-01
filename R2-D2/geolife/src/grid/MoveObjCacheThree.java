package grid;

import java.util.*;

import stp.predictor.Point;

import loadData.MapLoc2Grid;
public class MoveObjCacheThree {
	
	HashMap<Integer,MOTuple> moc=null;
	
	HashMap<Integer,ArrayList<RoICell>> sampleGridHashList=null;
	HashMap<Integer,ArrayList<Point>> sampleLocHashList=null;
	int sampleLen=-1;

	Grid grid=null;
	
	/**
	 * T_u is the update frequency. By default, it can be one.
	 * @param inTu
	 */
	public MoveObjCacheThree(Grid inGrid){
		moc=new HashMap<Integer,MOTuple>();

		grid=inGrid;
		

	
	}
	
	public void set3Sample(int usernum,int sl){//为连续Id的样本申请空间
		sampleLen=sl;
		sampleGridHashList=new HashMap<Integer,ArrayList<RoICell>>();
		sampleLocHashList=new HashMap<Integer,ArrayList<Point>>();
		for(int i=0;i<usernum;i++){//如果不是连续的id，这里要小心了
			ArrayList<RoICell> item=new ArrayList<RoICell>();
			sampleGridHashList.put(i, item);
			
			ArrayList<Point> locItem=new ArrayList<Point>();
			sampleLocHashList.put(i, locItem);
		}
		
		
	}
	
	public void setSample_no(int [] sa,int sl){//为不连续的id样本申请空间
		sampleLen=sl;
		sampleGridHashList=new HashMap<Integer,ArrayList<RoICell>>();
		sampleLocHashList=new HashMap<Integer,ArrayList<Point>>();
		for(int i:sa){
			ArrayList<RoICell> item=new ArrayList<RoICell>();
			sampleGridHashList.put(i, item);
			//System.out.println("in :"+i);
			ArrayList<Point> locItem=new ArrayList<Point>();
			sampleLocHashList.put(i, locItem);
		}
		
		
	}
	
	

	
	

	
	public void insertSampleGrid(java.awt.Point p1,int traId){
		if(null==sampleGridHashList) return;
	
		ArrayList<RoICell> sitem=sampleGridHashList.get(traId);
	
		if(null!=sitem){
			//System.out.println("插入点的目标Id:"+traId);
			if(sitem.size()<this.sampleLen){
			sitem.add(new RoICell(p1.x,p1.y));
			//System.out.println("   插入点："+p1.x+","+p1.y);
			
			}
		}
	}
	
	public void insertSampleLoc(Point loc1, int traId){
		if(null==sampleLocHashList) return;
		ArrayList<Point> locItem=sampleLocHashList.get(traId);
		if(null!=locItem){
			if(locItem.size()<this.sampleLen){
				locItem.add(new Point(loc1.x,loc1.y));
			}
		}
	}
	
	
	
	public void update(int inTraId,double inLat,double inLng,int inTimestamp){
		MOTuple moTuple=moc.get(inTraId);
		
		//System.out.println("update  id,lat,lng,time:"+inTraId+"  ,"+inLat+"  ,"+inLng+"  ,"+inTimestamp);
		
		if(moTuple==null){//if this is the first update of this moving object, store it in cache
			MOTuple tuple=new MOTuple(inLat,inLng,inTimestamp);
			moc.put(inTraId,tuple);
			java.awt.Point pp=MapLoc2Grid.transferGrid(inLat, inLng);
			insertSampleGrid( pp, inTraId);
			
			Point loc1=new Point(inLat,inLng);
			insertSampleLoc(loc1,inTraId);
		} else {
			
			    double lat1=moTuple.getLat();
			    double lng1=moTuple.getLng();
			    double lat2=inLat,lng2=inLng;
			    int t1=moTuple.getTimestamp(),t2=inTimestamp;
			    java.awt.Point p2=MapLoc2Grid.transferGrid(lat2,lng2);
				java.awt.Point p1=MapLoc2Grid.transferGrid(lat1, lng1);
				
			
				
				grid.updateLineTra(p1.x,p1.y,t1, p2.x, p2.y, t2, inTraId);//update the line to grid
				
				//System.out.println("id  time  转换前："+inTraId+":"+inTimestamp+";"+lat1+","+lng1+"   转换后："+p1.x+","+p1.y);
				insertSampleGrid( p2, inTraId);//已经验证没有问题，可以插入
				
				Point loc1=new Point(inLat,inLng);
				insertSampleLoc(loc1,inTraId);
				moTuple.setNewTuple(lat2,lng2,t2);//update the cache 
			
		}
		
	}
	public HashMap<Integer,ArrayList<RoICell>> getGridSampleHashList(){
		return this.sampleGridHashList;
	}
	
	public HashMap<Integer,ArrayList<Point>> getLocSampleHashList(){
		return this.sampleLocHashList;
	}
	
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
}
