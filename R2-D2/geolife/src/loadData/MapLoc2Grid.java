package loadData;

import java.awt.Point;

public  class MapLoc2Grid {
	private static double lat0;
	private static double lng0;
	private static double step0;
	
	public static Point transferGrid(double lat,double lng){
		double offx=lat-lat0;
		double offy=lng-lng0;
	
		//System.out.println("后的点    lat0,"+lat0+"   lng0,"+lng0);
		
		int gridX=(int)(offx/(step0));
		int gridY=(int)(offy/(step0));
		//System.out.println("srep0:"+step0);
		//System.out.println("插入转化后的点    lat,"+lat+"   lng,"+lng+"   step0,"+step0+"   offx,"+offx+"   offy,"+offy+"   gridx,"+gridX+"   gridy,"+gridY);
		return new Point(gridX,gridY);
	}
	
	public static double[] transfersource(double gridx,double gridy){
		double offx=gridx*step0+lat0;
		double offy=gridy*step0+lng0;
	    
		double[] result=new double[2];
		
		double a=offx;
		double b=offy;
		java.text.DecimalFormat df = new java.text.DecimalFormat("########.000000");
		result[0]=(double) (Math.round(a*1000000)/1000000.0); 
		result[1]=(double) (Math.round(b*1000000)/1000000.0);
		return result;
		//System.out.println("insert    lat,"+lat+"   lng,"+lng+"   step0,"+step0+"   offx,"+offx+"   offy,"+offy+"   gridx,"+gridX+"   gridy,"+gridY);
		//return new Point(gridX,gridY);
	}
	
	public static Point mytransferGrid(double lat,double lng){
		double offx=lat-lat0;
		double offy=lng-lng0;
	
		
		
		int gridX=(int)offx;
		int gridY=(int)offy;
		//System.out.println("srep0:"+step0);
		//System.out.println("insert    lat,"+lat+"   lng,"+lng+"   step0,"+step0+"   offx,"+offx+"   offy,"+offy+"   gridx,"+gridX+"   gridy,"+gridY);
		return new Point(gridX,gridY);
	}
	
	
	public static void setParameter(double inLat0,double inLng0,double inStep0){
		lat0=inLat0;
		lng0=inLng0;
		step0=inStep0;
	}
}
