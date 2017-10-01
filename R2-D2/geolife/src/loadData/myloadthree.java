package loadData;

import grid.Configuration;
import grid.Grid;
import grid.MoveObjCacheBBFOld;
import grid.MoveObjCacheThree;
import grid.MoveObjCacheWxgVideoTra;
import grid.RoICell;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

//import loadData.myfourload.MovingObject;

import TrajPrefixSpan.PatternConfiguration;

import stp.predictor.Point;

public class myloadthree {
	double lat0 = 0;// the origin point of whole coordinate latitude
	double lng0 = 0;// the origin point of whole coordinate longitude
	double step = 0;// 4X4, four level of grid, approximate one meter width for
	
	// record the sample trajectory part to do experiment
	private HashMap<Integer, ArrayList<RoICell>> sampleGridHash;//in grid coordination
	private HashMap<Integer, ArrayList<Point>> sampleLocHash;//in location coordination
	
	
	private int sampleLen;
	
	public myloadthree() {
		//MOSet = new HashMap<Integer, MovingObject>();
		
		//IdMap=new HashMap<String,Integer>();

		// record the sample, for experiment
		sampleGridHash = null;
		sampleLocHash=null;
		
		sampleLen = -1;
		
	}
	
	
	
	
	public HashMap<Integer, ArrayList<RoICell>> getGridSampleList() {
		return this.sampleGridHash;
	}
 
	public HashMap<Integer,ArrayList<Point>> getLocSampleList(){
		return this.sampleLocHash;
	}
	
	
	/**
	 * 
	 * @param inSampleNum
	 * @param inSampleLen
	 */
	public void setSample(int inSampleLen) {

		sampleGridHash = new HashMap<Integer, ArrayList<RoICell>>();
		sampleLocHash =new HashMap<Integer,ArrayList<Point>>();

		
		sampleLen = inSampleLen;

		// for(int i=0;i<sampleNum;i++){
		// sampleList.add(new ArrayList<RoICell>());
		// }
	}
	
	
	
	/**
	 * 
	 * @param minLat
	 * @param minLng
	 * @param maxLat
	 * @param maxLng
	 */
	private void CalibrationParameter(double minLat, double minLng,
			double maxLat, double maxLng) {

		lat0 = minLat;
		lng0 = minLng;

		double xScale = maxLat - minLat;
		double yScale = maxLng - minLng;

		double maxScale = (xScale > yScale) ? xScale : yScale;
		int divided = Configuration.GridDivided;
		
		assert maxScale>Configuration.GridDivided:"maxScale>Configuration.GridDivided is false";
		step = maxScale / divided;
		
		System.out.println("maxscale   divide  step0:"+maxScale+";"+divided+";"+step);
	}
	
	
	
	
	/**
	  * 
	  * @param db
	  * @param table
	  * @param startTime
	  * @param endTime
	  * @return
	  */
	private Grid threeGrid(String db, String table, int startTime,
			int endTime,int stid,int edid) {

		Grid g = new Grid();

		MoveObjCacheThree moc = new MoveObjCacheThree(g);
	
			
			double res[]= SQLiteDriver.getWholeNumSub(db,table, startTime, endTime,stid,edid);//res閲岄潰瀛樹簡鏈�ぇ鏈�皬鐨剎y鍧愭爣锛屽拰鎬荤殑鐢ㄦ埛鏁扮洰
			int userNum=(int)res[4];
			System.out.println("JingweeidufaneieL:"+res[0]+";"+res[1]+";"+res[2]+";"+res[3]+";"+res[4]+";"+res[5]+";"+res[6]);
			Configuration.FourLatMin=res[0];
			Configuration.FourLngMin=res[1];
			Configuration.FourLatMax=res[2];
			Configuration.FourLngMax=res[3];
			CalibrationParameter(Configuration.FourLatMin,Configuration.FourLngMin,Configuration.FourLatMax,Configuration.FourLngMax);
			MapLoc2Grid.setParameter(this.lat0, this.lng0, this.step);
			int idarray[]=SQLiteDriver.getAllId(db,table, startTime, endTime,stid,edid,userNum);
			//moc.set3Sample(userNum, sampleLen);//瀵逛簬杩炵画id
			//System.out.println("in number:"+idarray.length);
			moc.setSample_no(idarray, sampleLen);//瀵逛簬闈炶繛缁璉d,宸查獙璇佽繖閲屾病鏈夐敊璇�
			
		

		SQLiteDriver.openDB(db);
	
		SQLiteDriver.load3(table, startTime, endTime,stid,edid);
		

		try {
			while (SQLiteDriver.rs.next()) {
				MovingObject mo = this.ParseMovingObject(SQLiteDriver.rs);
				//mo.tranferToGrid();
				//if(isOutlier(mo.lat,mo.lng)) continue;
				//MOSet.put(mo.id,mo);
	      		//g.updatePoint(mo.gridX,mo.gridY, mo.timeStamp,mo.id,mo.sequence);
				//System.out.println("update涔嬪墠锛�+mo.id+","+mo.lat+","+ mo.lng+","+ mo.timeStamp);
				moc.update(mo.id-stid, mo.lat, mo.lng, mo.timeStamp);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

  	if(null!=this.sampleGridHash){
  		//System.out.println("鎵ц鎵ц");
  		this.prosSampleResList(moc.getGridSampleHashList(),moc.getLocSampleHashList());
  	}
		SQLiteDriver.closeDB();
        //System.out.println("this:"+this.sampleGridHash.size());
		return g;
	}
	

	private boolean isOutlier(double lat,double lng){
		 if(lat>Configuration.FourLatMax||lat<Configuration.FourLatMin) {
			 //System.out.println("lat out:"+lat+">"+Configuration.TaxiLatMax);
			 return true;
		}
		 if(lng>Configuration.FourLngMax||lng<Configuration.TaxiLngMin) {
			// System.out.println("lng out:"+lng+"<"+Configuration.TaxiLngMin);
			 return true;
		}
		 return false;
	 }
	
	
	
	private void prosSampleResList(HashMap<Integer, ArrayList<RoICell>> sgh,HashMap<Integer, ArrayList<Point>> loc) {
		Iterator<Entry<Integer, ArrayList<RoICell>>> shItr = sgh.entrySet()
				.iterator();
		while (shItr.hasNext()) {
			Entry<Integer, ArrayList<RoICell>> item = shItr.next();
			//System.out.println("time闀垮害锛�+item.getValue().size() );
			if (item.getValue().size() >= this.sampleLen) {
				this.sampleGridHash.put(item.getKey(),item.getValue());
			}
		}
	
		Iterator<Entry<Integer, ArrayList<Point>>> locItr = loc.entrySet()
		.iterator();
		while(locItr.hasNext()){
			Entry<Integer, ArrayList<Point>> item = locItr.next();
			if(item.getValue().size()>=this.sampleLen){
				this.sampleLocHash.put(item.getKey(), item.getValue());
			}
		}
	}
	
	
	 /**
	  * 
	  * @param db
	  * @param table
	  * @param timeStart
	  * @param timeEnd
	  * @return
	  */
	 public Grid Load3Grid(String db,String table,int timeStart,int timeEnd,int stid,int edid){
		 
		 
		 
		 //setGridSampleIds( db, table, timeStart, timeEnd);
		 
		// MOSet.clear();
		// CalibrationParameter(Configuration.FourLatMin,Configuration.FourLngMin,Configuration.FourLatMax,Configuration.FourLngMax);
		// MapLoc2Grid.setParameter(this.lat0, this.lng0, this.step);
		 
		 Grid g=threeGrid( db, table, timeStart, timeEnd,stid,edid);
		 
		 return g; 
	 }
	 
	 
	 private class MovingObject {
		 int id = -1;
			//int sequence = -1;
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
	 private MovingObject ParseMovingObject(ResultSet sqlRs){
		 MovingObject mo=new MovingObject();
		 try{
		 mo.timeStamp=sqlRs.getInt("time");
		 mo.id=sqlRs.getInt("id");
		 //!!!!lat and lng is reversed
		 mo.lat=sqlRs.getDouble("lat");
		 mo.lng=sqlRs.getDouble("lng");		 	
		// System.out.println("mo information:"+mo.timeStamp+";"+mo.id+";"+mo.lat+";"+mo.lng+";"+mo.v);
		 }catch(Exception e){
			 e.printStackTrace();
		 }
		 return mo;}
	 
	 
	 
	 
		



	 
	 
	 
}



