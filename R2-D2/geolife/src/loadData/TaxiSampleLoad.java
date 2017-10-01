package loadData;

import grid.Configuration;
import grid.RoICell;

import java.awt.Point;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


public class TaxiSampleLoad {
	double lat0,lng0,step0;
	private static HashMap<String,Integer> IdMap;	

	// record the sample trajectory part to do experiment
	private HashMap<Integer, ArrayList<RoICell>> sampleHash;
	private int sampleNum;
	private int sampleLen;
	private int[] sampleIdx;
	private static int MOid;
	
	public TaxiSampleLoad(double inLat0,double inLng0,double inStep0){
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
	private void setSampleIds(int maxNumTras) {
		if (sampleLen != -1) {
			//Iterator<Entry<Integer, MovingObject>> moItr = MOSet.entrySet()
			//		.iterator();
			int resIdx = 0;
			sampleIdx = new int[sampleNum];
			int count = 0;
			int interval = maxNumTras/ sampleNum;
			
			while(count<maxNumTras){
				if (count % interval == 1 && resIdx < sampleIdx.length) {

					//sampleIdx[resIdx] = count;
					 ArrayList<RoICell> item=new  ArrayList<RoICell>();
					 sampleHash.put(resIdx, item);
					resIdx++;
				}
				count++;
			}
		}
	}
	
	private void prosSampleResList(HashMap<Integer, ArrayList<RoICell>> sh) {
		Iterator<Entry<Integer, ArrayList<RoICell>>> shItr = sh.entrySet()
				.iterator();
		while (shItr.hasNext()) {
			Entry<Integer, ArrayList<RoICell>> item = shItr.next();
			if (item.getValue().size() >= this.sampleLen) {
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
	
	public void LoadSampleGrid(String db, String table, String startTime,
			String endTime,String status){
		
		 MapLoc2Grid.setParameter(this.lat0, this.lng0, this.step0);
		 
		 double res[]= SQLiteDriver.getMaxMinNum(db,table,startTime,endTime);
		 int maxNumTras=(int)res[4];
		 
		 setSampleIds(maxNumTras);//出租车的数量
		 
		 SQLiteDriver.openDB(db);
			if(status.equals("")||status.length()<=1){
			SQLiteDriver.loadTaxiDB(table, startTime, endTime);
			}else{
				SQLiteDriver.loadTaxiDB(table, startTime, endTime,status);
			}

			try {
				while (SQLiteDriver.rs.next()) {
					MovingObject mo = this.ParseMovingObject(SQLiteDriver.rs);
					//mo.tranferToGrid();
					if(isOutlier(mo.lat,mo.lng)) continue;//配置文件中定义了最大最小经纬度范围
					
					Point gc=MapLoc2Grid.transferGrid(mo.lat,mo.lng);
					ArrayList<RoICell> gcList=sampleHash.get(mo.id);
					
					

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		 mo.lng=sqlRs.getDouble("lat");
		 mo.lat=sqlRs.getDouble("lng");
		 mo.v=sqlRs.getDouble("v");		
		 }catch(Exception e){
			 e.printStackTrace();
		 }
		 return mo;
	 }
	 
	 private boolean isOutlier(double lat,double lng){
		 if(lat>Configuration.TaxiLatMax||lat<Configuration.TaxiLatMin) return true;
		 if(lng>Configuration.TaxiLngMax||lng<Configuration.TaxiLngMin) return true;
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

}
