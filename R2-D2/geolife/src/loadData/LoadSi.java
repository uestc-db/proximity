package loadData;

import grid.Configuration;
import grid.Grid;
import grid.MoveObjCache;
import grid.MoveObjCacheBBFOld;
import grid.RoICell;

import java.awt.Point;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/*陈心圆 2016.3.29
 * 新加坡数据集log20080602的读取
 * 
 * */
public class LoadSi {
	double lat0,lng0,step0;
	private static HashMap<String,Integer> IdMap;	

	// record the sample trajectory part to do experiment
	private HashMap<Integer, ArrayList<RoICell>> sampleHash;
	private int sampleNum;
	private int sampleLen;
	private int[] sampleIdx;
	private static int MOid;
	public LoadSi(){
		lat0=0;
		lng0=0;
		step0=0;
		
		IdMap=new HashMap<String,Integer>();

		// record the sample, for experiment
		sampleHash = null;
		sampleNum = -1;
		sampleLen = -1;
		sampleIdx = null;
		
		MOid=0;
	}
	
	public LoadSi(double inLat0,double inLng0,double inStep0){
		lat0=inLat0;
		lng0=inLng0;
		step0=inStep0;
		
		IdMap=new HashMap<String,Integer>();

		// record the sample, for experiment
		sampleHash = null;
		sampleNum = -1;
		sampleLen = -1;
		sampleIdx = null;
		
		MOid=0;
	}
	
	/**
	 * 
	 * @param inSampleNum
	 * @param inSampleLen
	 */
	public void setSample(int inSampleNum, int inSampleLen) {

		sampleHash = new HashMap<Integer, ArrayList<RoICell>>();

		sampleNum = inSampleNum;
		sampleLen = inSampleLen;

		// for(int i=0;i<sampleNum;i++){
		// sampleList.add(new ArrayList<RoICell>());
		// }
		
		
	}
	
	/**
	 * 
	 * @param maxNumTras
	 */
	private void setSampleIds(int maxNumTras) {//这里求出了这些样本的id号都是哪些，存储在了sam
		if (sampleLen != -1) {
			//Iterator<Entry<Integer, MovingObject>> moItr = MOSet.entrySet()
			//		.iterator();
			int resIdx = 0;
			sampleIdx = new int[sampleNum];
			int count = 0;
			int interval = maxNumTras/ sampleNum;
			
			while(count<maxNumTras){
				if (count % interval == 1 && resIdx < sampleIdx.length) {
					//System.out.println("count  interval  resIdx:"+count+","+interval+","+resIdx+","+sampleNum);
					sampleIdx[resIdx] = count;
					 //ArrayList<RoICell> item=new  ArrayList<RoICell>();
					 //sampleHash.put(resIdx, item);
					resIdx++;
				}
				count++;
			}
		}
		//System.out.println("sampleHashsize:"+sampleHash.size());这里跟sampleNum相等啊，为什么主函数里面的countNum不相等
	}
	
	private void prosSampleResList(HashMap<Integer, ArrayList<RoICell>> sh) {
		Iterator<Entry<Integer, ArrayList<RoICell>>> shItr = sh.entrySet()
				.iterator();
		//System.out.println("里面的采样结果："+sh.size());
		while (shItr.hasNext()) {
			Entry<Integer, ArrayList<RoICell>> item = shItr.next();
			if (item.getValue().size() >= this.sampleLen) {//大于samplelen的样本我们才用，座椅最后的样本数是780而不是1000
				this.sampleHash.put(item.getKey(),item.getValue());
			}
		}
	}
	
	public HashMap<Integer, ArrayList<RoICell>> getSampleList() {
		return this.sampleHash;
	}
	
	
	public void loadSampleReal(String db, String table, String startTime,
			String endTime, int inSampleNum, int inSampleLen) {

	}
	
	
	//替换Main中的Load2Grid
	public Grid LoadSampleGrid(String db, String table, String startTime,
			String endTime){
		Grid g = new Grid();

		MoveObjCache moc = new MoveObjCache(g);//经过此步骤后moc.grid=g
		CalibrationParameter(Configuration.TaxiLatMin,Configuration.TaxiLngMin,Configuration.TaxiLatMax,Configuration.TaxiLngMax);
		 MapLoc2Grid.setParameter(this.lat0, this.lng0, this.step0);
			
		
		 double res[]= SQLiteDriver.getMaxMinNum(db,table,startTime,endTime);
		 int maxNumTras=(int)res[4];
		 System.out.println("num of taxi:"+maxNumTras);
		 
		 setSampleIds(maxNumTras);//好像也只是求了样本空间容量
		 //这个语句应该是申请好了存储样本的空间
		 moc.setSample(this.sampleIdx, sampleLen,SQLiteDriver.getSeconds(startTime),SQLiteDriver.getSeconds(endTime));
		 
		 SQLiteDriver.openDB(db);
			
		 SQLiteDriver.loadTaxiDB(table, startTime, endTime);
			

			try {
				while (SQLiteDriver.rs.next()) {
					MovingObject mo = this.ParseMovingObject(SQLiteDriver.rs);
					//mo.tranferToGrid();
					if(isOutlier(mo.lat,mo.lng)) continue;//配置文件中定义了最大最小经纬度范围
					moc.update(mo.id, mo.lat, mo.lng, mo.timeStamp);
			}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(null!=this.sampleHash){
		  		this.prosSampleResList(moc.getGridSampleHashList());
		  	}
				SQLiteDriver.closeDB();

				return g;
	}
	
	
	
	
	
	
	
	/**
	 * get the id of moving object,将string的id对应到了integer
	 * @param idStr
	 * @return
	 */
	private static int getMOid(String idStr){
		Integer id=IdMap.get(idStr);
		if(null==id){
			Integer newId=MOid;
			IdMap.put(idStr, newId);
			MOid++;
		return newId;
		} else{
			return id;
		}
		
	}
	
	
	
	 /**
	  * parsing moving object from string
	  * @param res
	  * @return
	  */
	 private MovingObject ParseMovingObject(ResultSet sqlRs){
		 MovingObject mo=new MovingObject();
		 try{
		 mo.timeStamp=SQLiteDriver.getSeconds(sqlRs.getString("time"));
		 mo.id=getMOid(sqlRs.getString("id"));
		 //!!!!lat and lng is reversed
		 mo.lat=sqlRs.getDouble("lng");//因为数据库里面这两个搞反了
		 mo.lng=sqlRs.getDouble("lat");
		 mo.v=sqlRs.getDouble("v");	
		// System.out.println("mo information:"+mo.timeStamp+";"+mo.id+";"+mo.lat+";"+mo.lng+";"+mo.v);
		 }catch(Exception e){
			 e.printStackTrace();
		 }
		 return mo;
	 }
	 
	 private boolean isOutlier(double lat,double lng){
		 if(lat>Configuration.TaxiLatMax||lat<Configuration.TaxiLatMin) {
			 //System.out.println("lat out:"+lat+">"+Configuration.TaxiLatMax);
			 return true;
		}
		 if(lng>Configuration.TaxiLngMax||lng<Configuration.TaxiLngMin) {
			// System.out.println("lng out:"+lng+"<"+Configuration.TaxiLngMin);
			 return true;
		}
		 return false;
	 }
	 
	
	private class MovingObject {
		int id = -1;
		//int sequence = -1;
		//int classId = -1;
		int timeStamp = -1;
		double lat = -1;
		double lng = -1;
		double v = -1;

		int gridX = -1;
		int gridY = -1;

	}
	
	private void CalibrationParameter(double minLat, double minLng,
			double maxLat, double maxLng) {

		lat0 = minLat;
		lng0 = minLng;

		double xScale = maxLat - minLat;
		double yScale = maxLng - minLng;

		double maxScale = (xScale > yScale) ? xScale : yScale;
		int divided = Configuration.GridDivided;
		
		assert maxScale>Configuration.GridDivided:"maxScale>Configuration.GridDivided is false";
		step0 = maxScale / divided;
	}


}
