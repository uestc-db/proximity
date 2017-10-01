package TrajPrefixSpan;

import grid.Configuration;

public class PatternConfiguration {
	
	public static int GridDivision=256;
	public static String TrajDB="data/taxi/taxi.db";
	public static String TrajTable="taxi";
	
	public static int T_sample=30;
	public static double ExtremLowVelocityLat=0;
	public static double ExtremLowVelocityLng=0;
	
	
	public static double LatMin=Configuration.TaxiLatMin;
	public static double LngMin=Configuration.TaxiLngMin;
	public static double LatMax= Configuration.TaxiLatMax;
	public static double LngMax=Configuration.TaxiLngMax;
	public static int    support =5;
	
	public static int SequenceLen=40;//if equal -1, turnoff this function
	public static int TrajNum=9800;
	
	
	
	//for Taxi, need to set the following parameters (by default)
//	public static int TaxiGridDivision=256;
//	public static String TaxiTrajDB="data/taxi/taxi.db";
//	public static String TaxiTrajTable="taxi";
//	
//	public static int Taxi_T_sample=30;
//	
//	public static double TaxiLatMin=Configuration.TaxiLatMin;
//	public static double TaxiLngMin=Configuration.TaxiLngMin;
//	public static double TaxiLatMax= Configuration.TaxiLatMax;
//	public static double TaxiLngMax=Configuration.TaxiLngMax;
//	
//	public static int TaxiSequenceLen=40;//if equal -1, turnoff this function
//	public static int TaxiTrajNum=50000;
	
}
