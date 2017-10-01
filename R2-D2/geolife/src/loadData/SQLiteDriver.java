package loadData;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class SQLiteDriver {
	public static Connection conn;
	public static ResultSet rs;
	
	/**
	 * Open DB, togather with closeDB and exeSQL
	 * @param db
	 */
	public static void openDB(String db){
		try {
			Class.forName("org.sqlite.JDBC");
		
		String conStr="jdbc:sqlite:"+db;
		 conn = DriverManager.getConnection(conStr);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * close DB, with openDB and exeSQL
	 */
	public static void closeDB(){
		try {
		conn.close();
		rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * with opendb and closedb
	 * @param sql
	 */
	public static void exeSQL(String sql){
		try{
		 Statement stat = conn.createStatement(); 
		
		 rs = stat.executeQuery(sql);
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param db
	 * @param timeStart
	 * @param timeEnd
	 * @return there are five elements, and they are [minLat,minLng,maxLat,maxLng,num of taxi]
	 */
	public static double[] getMaxMinNum(String db,String table,String timeStart,String timeEnd){
		double[] res=new double[5];
		
		try {
			openDB( db);
			 /*String sql="select min(lat),min(lng),max(lat),max(lng),count(distinct  id) from "+table
			 + " where time>time(\""+timeStart+"\") and time<time(\""+timeEnd+"\")";*/
			 String sql="select min(lat),min(lng),max(lat),min(time),count(distinct  id) from "+table;
			 
			 exeSQL(sql);
			 
			 while(rs.next()){
				 res[0]=rs.getDouble(1);
				 res[1]=rs.getDouble(2);
				 res[2]=rs.getDouble(3);
				 res[3]=rs.getDouble(4);
				 res[4]=rs.getInt(5);
			 }
			 closeDB();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	
	
	//陈心圆  2016.5.14，获得总的用户数目和最大最小xy坐标
	public static double[] getWholeNum(String db,String table,int timeStart,int timeEnd){
		double[] res=new double[7];
		
		try {
			openDB( db);
			 
			 String sql="select min(lat),min(lng),max(lat),max(lng),count(distinct  id), min(time),max(time) from "+table+
					 " where time>="+timeStart+" and time<="+timeEnd+" order by time asc";
			 
			 exeSQL(sql);
			 
			 while(rs.next()){
				 res[0]=rs.getDouble(1);
				 res[1]=rs.getDouble(2);
				 res[2]=rs.getDouble(3);
				 res[3]=rs.getDouble(4);
				 res[4]=rs.getInt(5);
				 res[5]=rs.getInt(6);
				 res[6]=rs.getInt(7);
			 }
			 closeDB();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	//陈心圆  2016.5.14，获得总的用户数目和最大最小xy坐标
		public static double[] getWholeNumSub(String db,String table,int timeStart,int timeEnd,int stid,int edid){
			double[] res=new double[7];
			
			try {
				openDB( db);
				 
				 String sql="select min(lat),min(lng),max(lat),max(lng),count(distinct  id), min(time),max(time) from "+table+
						 " where time>="+timeStart+" and time<="+timeEnd+" and id>="+stid+" and id<"+edid+" order by time asc";
				 
				 exeSQL(sql);
				 
				 while(rs.next()){
					 res[0]=rs.getDouble(1);
					 res[1]=rs.getDouble(2);
					 res[2]=rs.getDouble(3);
					 res[3]=rs.getDouble(4);
					 res[4]=rs.getInt(5);
					 res[5]=rs.getInt(6);
					 res[6]=rs.getInt(7);
				 }
				 closeDB();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return res;
		}
	
	
	
	public static int[] getAllId(String db,String table,int timeStart,int timeEnd,int stid,int edid,int usernum){//获取数据库中所有的id号
		int[] res=new int[usernum];
		
		try {
			openDB( db);
			 
			 String sql="select distinct id from "+table+
					 " where time>"+timeStart+" and time<"+timeEnd+" and id>="+stid+" and id<"+edid+" order by time asc";
			 
			 exeSQL(sql);
			 int  index=0;
			 while(rs.next()){
				 res[index]=rs.getInt("id")-stid;//为了把它统一到1-999区间内，后面会再加上来
				 index++;
				 
			 }
			 closeDB();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	
	
	
	
	
	public static double[] getWholeNumFour(String db,String table){
		double[] res=new double[5];
		
		try {
			openDB( db);
			 
			 String sql="select min(lat),min(lng),max(lat),max(lng),count(distinct  id) from "+table;
			 
			 exeSQL(sql);
			 
			 while(rs.next()){
				 res[0]=rs.getDouble(1);
				 res[1]=rs.getDouble(2);
				 res[2]=rs.getDouble(3);
				 res[3]=rs.getDouble(4);
				 res[4]=rs.getInt(5);
			 }
			 closeDB();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
	
	
	
	
	//important, by time order
	public static void loadTaxiDB(String table,String timeStart,String timeEnd){
		//String sql= "select * from "+
		// table+ " where time>time(\""+timeStart+"\") and time<time(\""+timeEnd+"\") ";
		String sql= "select * from "+
				 table;
		exeSQL(sql);
		System.out.println("load success");
	}
	
	//陈心圆loadid5.17
	public static void loadId(String table){
		//String sql= "select * from "+
		// table+ " where time>time(\""+timeStart+"\") and time<time(\""+timeEnd+"\") ";
		String sql= "select * from "+
				 table+" order by id asc";
		exeSQL(sql);
		System.out.println("load success");
	}
	
	//陈心圆5.17赋予新的id
	public static void setnewId(String table,int id,int newid){
		String sql= "update "+
				 table+"set newid="+newid+" where id="+id;
		exeSQL(sql);
		//System.out.println("update success");
	}
	
	
	
	//陈心圆2016.5.14
	public static void loadTaxiDB(String table){
		
		String sql= "select * from "+
				 table;
		exeSQL(sql);
		System.out.println("load success");
	}
	
	
	//important, by time order
	public static void loadTaxiDB(String table,String timeStart,String timeEnd,String status){
		String sql= "select * from "+
		 table+ " where time>time(\""+timeStart+"\") and time<time(\""+timeEnd+"\")"+" and status='"+status+"' order by time asc";
		exeSQL(sql);
	}
	
	//important, by time order
	public static void loadWxgVideoTraDB(String table, int timeStart,int timeEnd){
		String sql= "select * from "+
		 table+ " where t>"+timeStart+" and t<"+timeEnd+" order by t asc";
		exeSQL(sql);
	}
	
	//by time order
	public static void loadBBFOld(String table, int timeStart, int timeEnd){
		String sql= "select id,time,lat,lng from "+
		 table+ " where time>"+timeStart+" and time<"+timeEnd+" order by time asc";
		exeSQL(sql);
	}
	
	//5.15陈心圆
	
	public static void load3(String table, int timeStart, int timeEnd,int stid,int edid){
		String sql= "select * from "+
		 table+ " where time>="+timeStart+" and time<="+timeEnd+" and id>="+stid+" and id<"+edid+" order by time asc";
		exeSQL(sql);
		System.out.println("load success"); 
	}
	
	public static int[] getMITTraStartEndId(String db,String table, int timeStart, int timeEnd){
		int[] res= new int[2];;
		try {
			openDB( db);
			 String sql="select min(id),max(id) from "+table
			 + " where time>"+timeStart+" and time<"+timeEnd+"";
			 
			 exeSQL(sql);
			
			 while(rs.next()){
				 res[0]=rs.getInt(1);
				 res[1]=rs.getInt(2);
			 }
			 closeDB();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			res=null;
			e.printStackTrace();
		}
		
		
		return res;
	}
	
	public static int getSeconds(String str){
		//原来代码如下：
		/*String[] resStr=str.split(":|");
		int h=Integer.parseInt(resStr[0]);
		int m=Integer.parseInt(resStr[1]);
		int s=Integer.parseInt(resStr[2]);*/
        
		String[] resStr=str.split(":|/| |,");
		//陈心圆 2016329
		//System.out.println("time:"+str+"\nlength:"+resStr.length+"\n 0:"+resStr[0]+"\n 1:"+resStr[1]+"\n 2:"+resStr[2]);
		int h=Integer.parseInt(resStr[3]);
		int m=Integer.parseInt(resStr[4]);
		int s=Integer.parseInt(resStr[5]);
		
		return h*3600+m*60+s;
	}
	
	
	public static void test()  {
		
        try {
			Class.forName("org.sqlite.JDBC");
		
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/taxi/test.db");
        Statement stat = conn.createStatement();
        stat.executeUpdate("drop table if exists people;");
        stat.executeUpdate("create table people (name, occupation);");
        PreparedStatement prep = conn.prepareStatement(
            "insert into people values (?, ?);");

        prep.setString(1, "Gandhi");
        prep.setString(2, "politics");
        prep.addBatch();
        prep.setString(1, "Turing");
        prep.setString(2, "computers");
        prep.addBatch();
        prep.setString(1, "Wittgenstein");
        prep.setString(2, "smartypants");
        prep.addBatch();

        conn.setAutoCommit(false);
        prep.executeBatch();
        conn.setAutoCommit(true);

        ResultSet rs = stat.executeQuery("select * from people;");
        while (rs.next()) {
            System.out.println("name = " + rs.getString("name"));
            System.out.println("job = " + rs.getString("occupation"));
        }
        rs.close();
        conn.close();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testTime() throws SQLException{

		
		//double[] x=getMaxMinNum("data/taxi/taxi.db","test","00:00:03","00:00:10");
		openDB("data/taxi/taxi.db");
		loadTaxiDB("taxi","08:00:00","08:00:04");
		while(rs.next()){
		Time ts=rs.getTime("time");
		String str=rs.getString("time");
		System.out.println(SQLiteDriver.getSeconds(str));
		//int l=getSeconds(str);
		//System.out.println(l);
		
		//double lat=rs.getDouble("lat");
		//System.out.println(lat);
		}
		closeDB();
	
	}
	
}
