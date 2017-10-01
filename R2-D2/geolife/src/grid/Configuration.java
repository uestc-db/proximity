package grid;

import java.util.HashMap;

public class Configuration {
	public static int BITS_PER_GRID=4;
	public static int MAX_LEVEL=3;
	public static int GridDivided=2048;//maximum number of grids at each axes,鍘熸潵鏄�2048
	
	public static int T_Sample=1;//the sample time interval for each update
	public static int T_period=1000;//the period of trajectories which we think they are not old
	public static double ExtremLowVelocityLat=0;
	public static double ExtremLowVelocityLng=0;
	
	public static int BrinkConstraintRoI=3; 
	public static double BrinkThreshold=0.1;
	//the support of reference trajectories. How many minimum trajectories are required 
	public static int TraSupport=5;
	
	public static double cellRadius=Math.sqrt(2)/2;//use to compute the micro state, each cell has a radius sqrt(2)/2
	public static double MicroStateRadius=4*cellRadius;
	
	public static int minNumPerMic=1;//the minimum number of tra id at each micro state, 
	
	

	
	//define parameter related with MacroState and MicroState 
	public static double ProDown=0.9;//stop to go down to next level of the macro states
	public static double MAPPro=0.0001;//terminate the MAP process鍘熸潵鏄�0.0001
	public static int MaxRadius=40;
	public static double MaxStateDis=40;
	public static double AlphaRadius=2;//should be larger than 1
	
	public static double AlphaScore=0.5;
	
	//for continuous prediction
	public static boolean doSelfCorrection=false;
	public static HashMap<Integer,Integer> lifetimeMap=new HashMap<Integer,Integer>();//entry <traId,lifetime>
	public static int lossCount=0;//for debug
	public static int fullcount=0;//for debug
	public static double doSelfParameter=3;
	
	
	//for construct the Grid Map, for Taxi data
	//taxi south point: 1.239487, 103.832755
	//taxi north point:1.470224,103.812517
	//taxi east point: 1.372931,104.007788
	//taxi west point: 1.274843,103.617828
	public  static double TaxiLatMin=1.239487;
	public static double TaxiLatMax=1.470224;
	public static double TaxiLngMin=103.617828;
	public static double TaxiLngMax=104.007788;
	public static int TaxiTraNum=14523;
	
	public static int Taxi_T_Sample=30;
	
	
	
	//闄堝績鍦� 2016.5.14   涓変釜鏁版嵁闆嗗弬鏁拌缃�(涔嬪墠浠ヤ负鏄洓涓紝鎵�浠ュ啓鎴恌our浜�)锛屽彲浠ユ牴鎹笁涓暟鎹泦鐨勬暟鍊艰寖鍥磋繘琛岃皟鏁�,鎵╁ぇ浜�1000000浣垮叾涓烘暣鏁�
	public  static double FourLatMin=93100000.0;
	public static double FourLatMax=93300000.0;
	public static double FourLngMin=66400000.0;
	public static double FourLngMax=66600000.0;
	
	
	//the maximum possible velocity of taxi, it is used to remove outlier
	//v=180*1000/3600,  180km/h
	//need to transfer to lat-lng system
	//鏂板姞鍧″矝锛屾柊鍔犲潯鍏卞拰鍥界殑鏈�澶у矝灞裤�備綅浜庨┈鏉ュ崐宀涘崡绔紝涓滆タ鏈�闀�41.84鍏噷锛屽崡鍖楁渶瀹�22.53鍏噷
	public static double TaxiLatUnit=(TaxiLatMax-TaxiLatMin)/22530;
	public static double TaxiLngUnit=(TaxiLngMax-TaxiLatMin)/41840;
	public static double TaxiExtremVelocity=200;
	public static double TaxiExtremVelocityLat=TaxiExtremVelocity*TaxiLatUnit;//m/s
	public static double TaxiExtremVelocityLng=TaxiExtremVelocity*TaxiLngUnit;//m/s
	public static double TaxiExtremLowVelocity=2;
	//public static double TaxiExtremLowVelocityLat=TaxiExtremLowVelocity*TaxiLatUnit;
	//public static double TaxiExtremLowVelocityLng=TaxiExtremLowVelocity*TaxiLngUnit;
	public static double TaxiUnitCell=18;
	/**
	 * 
	 * @author workshop
	 * this class is the leaf,which store the trajectories information of the grid.
	 * note that:
	 *
	 * 
	 * 1. For 4k page, the size of 
	 * type				size	byte
	 * LinkedHashMap    135   4002
	 * TreeMap 			140	  4063
	 * 
	 * Noted that, the key is traId<<32+time, i.e. the traId and offset is
	 * the key of HashMap, while, the value of HashMap is the location in the grid of traId. The corresponding next time can be
	 * calculated by currrent timestamp + T_sample, where T_sample the fixed sample time interval 
	 */
	public static int CapacityPerPage=135;
	public static int PageSize=4096;
	public static String GridFile="GridDiskBuffer";
	
	
	public static int getTraId(Long key){
		int traid=(int)(key>>32);
		return traid;
	}
	
	public static int getTime(Long key){
		long id2=key>>32;
		id2<<=32;	
		long timeLong=key-id2;
		int time=(int)(timeLong);
		return time;
	}
	
	public static Long getKey(int traId,int time){
		Long key=new Long(traId);
		key<<=32;
		key+=time;
//		System.out.println("int2long: id=" + traId + ", time=" + time + ", final key=" + key);
		Long newkey = key - time;
		newkey>>=32;
//		System.out.println("return~~~: new id=" + newkey);
		return key;
	}
	
	public static int hitCount=0;
	
	
	private static int stateId=0;
	
	public static int getStateId(){
		return stateId++;
	}
	
	
	//configuration of MIT Trajectory dataset
	
	public static double XiaogangVideoTraLatMin=0;
	public static double XiaogangVideoTraLngMIN=0;
	public static double XiaogangVideoTraLatMax=357;
	public static double XiaogangVideoTraLngMax=477;
	public static int XiaogangVideoTraTraNum=40753;
	
	//the public parameter need to consider
	//public static double MITTraLatMin=0;
	//public static double MITTraLngMin=0;
	//public static double MITTraLatMax=357;
	//public static double MITTraLngMax=477;
	//public static int MITTraTraNum=40753;
	
	//public static int MITTra_BITS_PER_GRID=4;
	//public static int MITTra_MAX_LEVEL=3;
	//public static int MITTra_GridDivided=256;//maximum number of grids at each axes
	//public static int T_Sample=1;//the sample time interval for each update
	//public static int T_period=1000;//the period of trajectories which we think they are not old

	
	
	//TSV configuration, this parameters need to be considered
	public static int TSV_T_Sample=1;//the sample time interval for each update
	public static int TSV_T_period=600;//the period of trajectories which we think they are not old
	
	public static double TSVLatMin=24;
	public static double TSVLngMin=24;
	public static double TSVLatMax=695;
	public static double TSVLngMax=455;
	public static int TSVTraNum=9800;
	
	public static double WxgVideoTraExtremLowVelocity=2;
	public static double WxgVideoTraExtremLowVelocityLat=WxgVideoTraExtremLowVelocity;
	public static double WxgVideoTraExtremLowVelocityLng=WxgVideoTraExtremLowVelocity;
	
	
	// BigBrinkhoffOldenburgTest BBFOld
	//public static int BBFOld_T_Sample=1;
	public static double BBFOldXMin=292.0;
	public static double BBFOldYMin=3935.0;
	public static double BBFOldXMax=23056.0;
	public static double BBFOldYMax=30851.0;
	public static double BBFOldUnitCell=18;
	public static int BBFOldTraNum=1005;
	
}
