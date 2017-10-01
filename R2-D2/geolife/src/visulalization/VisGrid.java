package visulalization;

import grid.Grid;
import grid.GridCell;
import grid.GridLeafTraHashItem;
import grid.RoICell;
import grid.RoIState;
import grid.TraListItem;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JFrame;

import prediction.TimeTraState;

import traStore.TraStoreListItem;

@SuppressWarnings("serial")
public class VisGrid extends Canvas{
	public static double densityUnit=0.1;
	private int width=800;
	private int height=800;
	private Grid visGid=null;
	private int gridX0;
	private int gridX1;
	private int gridY0;
	private int gridY1;
	
	private double lat0;
	private double lng0;
	
	private double step;
	
	private boolean drawMap=true;
	//private int mouseX;
	//private int mouseY;
	
	//Hashtable<Integer,TraListItem> interRes=null; 
	Hashtable<Integer,ArrayList<TraStoreListItem>> traSet=null;
	TimeTraState timeTraState=null;
	int[] MAPPath=null;
	double moveObjLat=-1;
	double moveObjLng=-1;
	
	ArrayList<Entry<Long,GridLeafTraHashItem>> traGrid=null;
	
	private  int      lastMouseX;
	private  int      lastMouseY;
	
	private  Graphics2D context; 
	Color timeColorSet[];
	int timeColorNum;
	public VisGrid(Grid g,int inGridX0,int inGridY0,int inGridX1,int inGridY1){
		visGid=g;
		
		gridX0=inGridX0;
		gridY0=inGridY0;
		gridX1=inGridX1;
		gridY1=inGridY1;
		
		width=gridX1-gridX0;
		height=gridY1-gridY0;
		
		lastMouseX=-1;
		lastMouseY=-1;
		
		 this.enableEvents( AWTEvent.MOUSE_EVENT_MASK| AWTEvent.MOUSE_MOTION_EVENT_MASK );   
		 
		 timeColorNum=10;
		 timeColorSet=new Color[timeColorNum];
		 timeColorSet[0]=Color.RED;
		 timeColorSet[1]=Color.BLUE;
		 timeColorSet[2]=Color.YELLOW;
		 timeColorSet[3]=Color.GREEN;
		 timeColorSet[4]=Color.PINK;
		 timeColorSet[5]=Color.CYAN;
		 timeColorSet[6]=Color.ORANGE;
		 timeColorSet[7]=Color.MAGENTA;
		 timeColorSet[8]=Color.WHITE;
		 timeColorSet[9]=Color.DARK_GRAY;
	}
	
	 public void addNotify() { 
		 super.addNotify();   
		 context = (Graphics2D)this.getGraphics().create();
     } // End addNotify.
	 
	 public void paint(Graphics g) {
	      		 
		 g.setColor(Color.white);
		 g.fillRect(0, 0, width,height);
		 System.out.println("the window width" + width + "the window height" + height);
	    if(drawMap){
		 for(int x=gridX0;x<gridX1;x++){
			 for(int y=gridY0;y<gridY1;y++){
				// System.out.println("VisGrid:gridx0,gridy0,gridx1,gridy1,x,y:"+gridX0+","+gridY0+","+gridX1+","+gridY1+x+","+y);
				 GridCell gc=visGid.getGridCell(x, y);
				 if(gc!=null){
					 double d=gc.density;
					 //System.out.println("density("+x+","+y+"):"+d);
					 int l=255-(int)(d/densityUnit);
					//System.out.println("density("+x+","+y+"):"+d+",l:"+l+";gridx0:gridy0:"+gridX0+";"+gridY0);
					 if(l<0) l=0;
					 //g.setColor(new Color(l,l,255));//鍥犱负瀵嗗害澶皬锛岀敾鎴愪簡鐧借壊濡傛灉鐢╨锛屾墍浠ヨ繖閲屽尯鍒嗕簡涓�涓�
					g.setColor(Color.BLACK);
					// g.setColor(Color.BLUE);
					 g.drawLine(x-gridX0, y-gridY0, x-gridX0, y-gridY0);
				 }
				 //else 
					// System.out.println("gc("+x+","+y+") is null"); 
				 
			 }
		 }
	    }
		 if(traSet!=null){
			
			// g.setColor(new Color(255,0,0));
			// g.drawLine(0, 0, 50,50);
			  context.setColor(new Color(255,0,0));//red
			  Enumeration<ArrayList<TraStoreListItem> > traElements=traSet.elements();
			  Enumeration<Integer> traKeys=traSet.keys();
			 // context.drawLine((int)(0), (int)(0),
				//	  (int)(200),(int)(200)); 
			  int colorSwitch=0;
			  while(traElements.hasMoreElements()&&traKeys.hasMoreElements()){
				  ArrayList<TraStoreListItem> itemRes=traElements.nextElement();
				  int itemTraId=traKeys.nextElement();
				 // System.out.println("trajectory id is:"+itemTraId);
			
				  Color colorItem=timeColorSet[colorSwitch%timeColorNum];
				  context.setColor(colorItem);
				  for(int i=0;i<itemRes.size();i++){
					  TraStoreListItem offItem=itemRes.get(i);
					  if (i==0){
						  context.setColor(Color.BLACK);
						  context.drawString("traid:"+itemTraId+" off_set:"+offItem.off,
								  5+(int)((offItem.lat-lat0)/step), 5+(int)((offItem.lng-lng0)/step));
						  context.setColor(colorItem);
					  }
					  
				//	  System.out.print("<lat:"+offItem.lat+" lng:"+offItem.lng+" time:"+offItem.timestamp+"> ");
					  context.drawLine(2+(int)((offItem.lat-lat0)/step), 2+(int)((offItem.lng-lng0)/step),
							  -2+(int)((offItem.lat-lat0)/step),-2+(int)((offItem.lng-lng0)/step)); 
					  context.drawLine(2+(int)((offItem.lat-lat0)/step), -2+(int)((offItem.lng-lng0)/step),
							  -2+(int)((offItem.lat-lat0)/step),2+(int)((offItem.lng-lng0)/step)); 
					
				  }
				  colorSwitch++;
				//  System.out.println();
			  }
		 } else if(null!=timeTraState&&null==MAPPath){
			 context.setColor(new Color(255,0,0));
			 
			 for(int k=1;k<timeTraState.getTimeLength()+1;k++){
				 for(int i=0;i<timeTraState.getStateNum(k);i++){
					 HashSet<RoICell> roiSet=timeTraState.getState(k, i).roiSet;
					 Iterator<RoICell> itrCells=roiSet.iterator();
					 while(itrCells.hasNext()){
						 RoICell rc=itrCells.next();
						 Color colorItem=timeColorSet[(k-1)%timeColorNum];
						 context.setColor(colorItem);
						 context.drawLine(rc.roiX, rc.roiY, rc.roiX, rc.roiY);
					 }
				 }
			 }
		 } else if(null!=timeTraState&&null!=MAPPath){
			 int oldx=0,oldy=0;
			 for(int i=1;i<MAPPath.length;i++){
				 Color colorItem=timeColorSet[(i-1)%timeColorNum];
				 context.setColor(colorItem);
				 int x=(int)(timeTraState.getState(i, MAPPath[i]).getCenterX()+0.5);
				 int y=(int)(timeTraState.getState(i, MAPPath[i]).getCenterY()+0.5);
				 drawCircle(context,x,y);
				 if(1==i) {
					 oldx=x;
					 oldy=y;
				 }
				//	System.out.println("time:"+i+" pos x:"+timeTraState.getState(i,MAPPath[i]).getCenterX()+" y:"+timeTraState.getState(i, MAPPath[i]).getCenterY());
				}
		 }else if(null!=traGrid){
			 context.setColor(new Color(255,0,0));
			 for(int i=0;i<traGrid.size();i++){
				 Entry<Long,GridLeafTraHashItem> item=traGrid.get(i);
				 drawX(context,item.getValue().getCellX(),item.getValue().getCellY(),0);
			 }
		 }
		 
		 if(-1!=moveObjLat&&-1!=moveObjLng){
			 paintMoveObj(context,moveObjLat,moveObjLng);
		 }
	 }
	 
	 private void drawX(Graphics2D inContex,int x, int y,int r){
		 inContex.drawLine(r+x, r+y,
				  -r+x,-r+y); 
		 inContex.drawLine(r+x, -r+y,
				  -r+x,r+y); 
		
	 }
	 
	 private void drawX(Graphics2D inContex,int x, int y){
		 drawX(inContex,x,y,2);
	 }
	 
	 private void drawCircle(Graphics inContext,int x,int y){
		 inContext.fillOval(x, y, 7, 7);
	 }
	 
	 private void paintMoveObj(Graphics2D inContext,double lat,double lng){
		 int x=(int)((lat-lat0)/step);
		 int y=(int)((lng-lng0)/step);
		 drawXCircle(inContext,new Point(x,y));
	 }
	 private void drawXCircle(Graphics2D inContex,Point obj){
		 drawXCircle(inContex,obj,5);
	 }
	 private void drawXCircle(Graphics2D inContex,Point obj,int r){
		 Stroke defaultStroke=inContex.getStroke();
		 Color defaultColor=inContex.getColor();
		 inContex.setColor(Color.BLACK);
		 inContex.setStroke(new BasicStroke(2));
		 inContex.drawOval(obj.x-r, obj.y-r, 2*r, 2*r);
		 drawX(inContex,obj.x,obj.y,5);
		 inContex.setStroke(defaultStroke);
		 inContex.setColor(defaultColor);
	 }
		 
	 public static VisGrid visGridPart(Grid grid,int x0,int y0, int x1,int y1){
		 VisGrid canvas=new VisGrid(grid,x0,y0,x1,y1);
		 JFrame frame=new JFrame();
		 
		 frame.setSize(x1-x0,y1-y0);
		  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  frame.getContentPane().add(canvas);
		  frame.setVisible(true);
		  return canvas;
	 }
	 
	 protected void processMouseEvent(MouseEvent e){  
	        if(e.getID()==MouseEvent.MOUSE_PRESSED){  
	        	lastMouseX=e.getX();
	        	lastMouseY=e.getY();
	           // System.out.println("x:"+e.getX()+" y:"+e.getY());
	          //repaint();
	        	context.setColor(new Color(255,0,0));
	        	context.drawString("x:"+lastMouseX+" y:"+lastMouseY, lastMouseX, lastMouseY);
	            }  
	        else if(e.getID()==MouseEvent.MOUSE_RELEASED){  
	        	
	            } 
	      
	        }  
	 protected void processMouseMotionEvent( MouseEvent event) { 
		     
		      if ( event.getID() == MouseEvent.MOUSE_DRAGGED) { 
		         int currentX = event.getX();
		         int currentY = event.getY();
		         
		            context.drawLine( lastMouseX, lastMouseY, currentX, currentY); 
		            lastMouseX = currentX;
		            lastMouseY = currentY;  
		            repaint();
		        } // End if.  
		      else if(event.getID()==MouseEvent.MOUSE_WHEEL){
		    	  repaint();
		      }
		     
		      } // End processMouseMotionEvent.

	 /**
	  * 
	  * @param interRes
	  * @param traSet
	  */
     public void visQueryResult( 
				Hashtable<Integer,ArrayList<TraStoreListItem>> inputTraSet, double inLat0,double inLng0,double inStep){

	          //interRes=inputInterRes;
	          traSet=inputTraSet;
	          step=inStep;
	          lat0=inLat0;
	          lng0=inLng0;
	          
	          timeTraState=null;
	          
	          repaint();
		
    }
     
   public void visQueryResultByGrid( ArrayList<Entry<Long,GridLeafTraHashItem>> res){
	   this.traGrid=res;
	   
	   timeTraState=null;
	   traSet=null;
	   MAPPath=null;
	   repaint();
   }
	 
     public void setDrawMap(boolean d){
    	 drawMap=d;
     }
     
     
     
     public void visTimeTraState(TimeTraState inTimeTraState){
    	 this.timeTraState=inTimeTraState;

    //	 interRes=null;
    	 this.traSet=null;
    	 
    	 repaint();
     }
	 
	 public void visMAPPath(TimeTraState inTimeTraState, int[] inMAPpath){
		 timeTraState=inTimeTraState;
		 MAPPath=inMAPpath;
		 repaint();
	 }
     
	 public void visMovingObject(double lat,double lng,double inLat0,double inLng0,double inStep){
		 step=inStep;
         lat0=inLat0;
         lng0=inLng0;
         
		moveObjLat=lat;
		moveObjLng=lng;
		
		 repaint();
	 }
	 public static void main(String[] args)
	    {//(Grid grid,int x0,int y0, int x1,int y1){
		 Grid g=new Grid();
		 visGridPart(g,0,0,4000,4000);
	    }
}
