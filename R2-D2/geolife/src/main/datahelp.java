package main;

import java.sql.SQLException;


import loadData.SQLiteDriver;

//辅助文档
public class datahelp {
	
	
	public static void main(String args[]){
		
		setid("data/mygeo.db", "easygeo");
		//expPredictionErr_RMF();
		//expPredictionErr_TraPattern();
}
	
	
	
	
	
	public  static  void  setid(String db,String table)
	{   
		int tempid=0,tempnewid=0;
		int flag=0;
		SQLiteDriver.openDB(db);
		SQLiteDriver.loadId(table);
		try {
			while (SQLiteDriver.rs.next()) {
				int id=SQLiteDriver.rs.getInt("id");
				int newid=SQLiteDriver.rs.getInt("newid");
				if(flag==0)
				{
					tempid=id;
					flag=1;
				}
				if(id==tempid)
				{
					SQLiteDriver.setnewId(db,id,tempnewid);
				}
				else{//
					tempid=id;
					tempnewid++;
					SQLiteDriver.setnewId(db,id,tempnewid);
				}
				

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SQLiteDriver.closeDB();
		
		System.out.println("It's OK");
		
	}

}
