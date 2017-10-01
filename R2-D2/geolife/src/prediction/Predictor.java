package prediction;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;



import stp.predictor.MotionPredictor;
import utl.kdtree.KDTree;
import utl.kdtree.KeyDuplicateException;
import utl.kdtree.KeyMissingException;
import utl.kdtree.KeySizeException;
import visulalization.GridPainter;
import visulalization.VisGrid;

import grid.Configuration;
import grid.Grid;
import grid.GridCell;
import grid.GridLeafTraHashItem;
import grid.RoICell;

/**
 * the predictor class
 * @author workshop
 *
 */
public class Predictor {
	
	//Grid g;//grid map structure
	
	
	
	public Predictor(){
		
	//	g=inG;
	}
	
	/**
	 * @param startSet : first initialize query result from grid
	 * @param g   ： grid
	 * @param p   : maximum possible prediction path probability
	 * @param minDelta : the minimum delta for MAP, termination for MAP
	 * @param r   : the threshold form micro state
	 * @return
	 */
	public StateGridFilter PathPrediction(
			ArrayList<Entry<Long, GridLeafTraHashItem>> startSet, Grid g,
			double p, double minDelta, double r, int prd_num) {
		return PathPrediction(
				startSet,  g,
				 p,  minDelta,  r,null, prd_num);
	}
	
	/**
	 * 
	 * @param startSet
	 * @param g
	 * @param p
	 * @param minDelta
	 * @param r
	 * @param outSDList: if outSDList ==null, just ignore it, else, add each dendrogram into this list
	 * @return
	 */
	private  StateGridFilter PathPrediction(
			ArrayList<Entry<Long, GridLeafTraHashItem>> startSet, Grid g,
			double p, double minDelta, double r,ArrayList<StatesDendrogram> outSDList, int prd_num){

		int time_k = 0;// count the future time

		StateGridFilter sgf = new StateGridFilter();//
		AgglomerativeCluster ac = new AgglomerativeCluster(g);

		// first dendrogram got by query result
		StatesDendrogram sd = ac.getDendrogram(startSet, r);
		
		if(null!=outSDList){											 
		outSDList.add(sd);
		}
		
		time_k++;
		// prediction at time k==1
		GFStatesItem first = sgf.GenerateGFState(sd.macsTree, p, time_k);
		if(null==first)
			{
			   //System.out.println("only  first");
			   return sgf;
			}
		sgf.gfStates.addStatesItem(first);// add to states
		//GFStatesItem old=first;
		// while prediction probability is larger than p, continue to predict
		while (sgf.getMaxDelta() >= minDelta) {//实际条件语句：sgf.getMaxDelta() >= minDelta
			// get relax micro state from grid
			ArrayList<RelaxMicroState> rf = g.forwardQueryMics(sd.micsLevel);
			sd = ac.getDendrogramRelaxMics(rf, r);// get dendrogram
			if(null!=outSDList){
			outSDList.add(sd);
			} 
			time_k++;

			GFStatesItem sec = sgf.GenerateGFState(sd.macsTree, p, time_k);// Following prediction
			if(null==sec||time_k>prd_num){//事实证明，time_k就是用来控制预测步数的
				break;
				//sgf.gfStates.addStatesItem(old);continue;
			}
			sgf.gfStates.addStatesItem(sec);
			//old=sec;
			
		}

		return sgf;
	}
	
	/**
	 * 
	 * @param followSet
	 *            : first input query set
	 * @param g
	 *            : grid map
	 * @param p
	 *            :for the generation of macro state
	 * @param minDelta
	 *            : for stopping condition of MAP
	 * @param r
	 *            : maximum radius of micro state
	 * @param inReuseSDs
	 *            : previous query result
	 * @param outSDList
	 *            : store the StatesDendrogram
	 * @return
	 */
	private StateGridFilter PathPredictionReuse(
			ArrayList<Entry<Long, GridLeafTraHashItem>> followSet, Grid g,
			double p, double minDelta, double r,
			ArrayList<StatesDendrogram> inReuseSDs,
			ArrayList<StatesDendrogram> outSDList) {

		int time_k = 0;// count the future time

		StateGridFilter sgf = new StateGridFilter();//
		AgglomerativeCluster ac = new AgglomerativeCluster(g);

		// first dendrogram got by query result. this one use direct generatio of micro states
		StatesDendrogram sd = ac.getDendrogram(followSet, r);
		if (null != outSDList) {
			outSDList.add(sd);
		}

		// prediction at time k==1
		time_k++;
		GFStatesItem first = sgf.GenerateGFState(sd.macsTree, p, time_k);
		if(null==first){
			return sgf;
		}
		sgf.gfStates.addStatesItem(first);// add to states

		// while prediction probability is larger than p, continue to predict
		while (sgf.getMaxDelta() >= minDelta) {

			// get relax micro state from grid
			// ArrayList<RelaxMicroState> rf = g.forwardQueryMics(sd.micsLevel);
			time_k++;
			ArrayList<MicroState> reuseMics = null;
			if (null!=inReuseSDs&&time_k + 1 < inReuseSDs.size()) {
				
				// as reuse, should go ahead. In data generation, the time start from 1, where reuseSD start to 
				//store data from 0, therefore, time_k just go head
				reuseMics = inReuseSDs.get(time_k ).micsLevel;
			}

			ArrayList<MicroState> nextMics = forwardQeryMicsReuse(sd.micsLevel,
					reuseMics, g, ac, r);

			sd = ac.getDendrogramMics(nextMics, r);// get dendrogram
			if (null != outSDList) {
				outSDList.add(sd);
			}
			GFStatesItem sec = sgf.GenerateGFState(sd.macsTree, p, time_k);// follwing // prediction
			if(null==sec) break;
			sgf.gfStates.addStatesItem(sec);
		}
		return sgf;
	}
	
	/**
	 * query the next micro state by reuse previous result
	 * 
	 * @param inPreMics
	 *            : the previous micro state set, if without reuse, we use this
	 *            mics to query grid and get micro state
	 * @param inReuseMics
	 *            : the micro states queried by precious prediction process,
	 *            NOTED: inReuseMics are destroyed after being reused
	 * @param g
	 *            : grid
	 * @param inAC
	 *            : Agglomerative Cluster function
	 * @param r
	 *            : maximum bound of each micro state
	 * @return
	 */
	private ArrayList<MicroState> forwardQeryMicsReuse(
			ArrayList<MicroState> inPreMics, ArrayList<MicroState> inReuseMics,
			Grid g, AgglomerativeCluster inAC, double r) {
		
		// if there is nothing can be reused, query directly
		
		if (null == inReuseMics || 0>= inReuseMics.size()) {
			ArrayList<RelaxMicroState> rf = g.forwardQueryMics(inPreMics);
			return inAC.Releax2Mics(rf, r);// convert to MicroState
		}

			
			
		// build index, each (traId+time)-> MicroState
		HashMap<Long, MicroState> reuseMicsHash = hashTratimeMics(inReuseMics);

		for (MicroState preMic : inPreMics) {// for each previous Mics
			if (null != preMic.LTraCell && preMic.LTraCell.size() > 0) {
				// visit every trajectory id in each micro states
				Iterator<Long> preMicKeys = preMic.LTraCell.keySet().iterator();
				ArrayList<Long> preMicDelPoints=new ArrayList<Long>();
				while (preMicKeys.hasNext()) {
					Long keyItem = preMicKeys.next();// get traid+time
					int keyItemId = Configuration.getTraId(keyItem);
					int keyItemTime = Configuration.getTime(keyItem);
					Long keyNextTime = Configuration.getKey(keyItemId,
							keyItemTime + Configuration.T_Sample);// compute
																	// next time
					// find whether this traId+time has been stored in
					// reuseMicrostate
					MicroState keyHashMic = reuseMicsHash.get(keyNextTime);
					if (null != keyHashMic) {
						// delete thus traId+time, all the undeleted point in
						// preMics will be used to query grid
						//preMic.deletePoint(keyItem);
						preMicDelPoints.add(keyItem);
						
						// delete this traId+time, all the undeleted point in
						// this hash, will be deleted in reuse Mics, as they
						// are not part of the result of pre Mics
						reuseMicsHash.remove(keyNextTime);
					}
				}
				//delete at the same time, in order to reuse 
				for(Long preMicDelItem:preMicDelPoints){
					preMic.deletePoint(preMicDelItem);
				}
				//update density and minbound is not uesful for query
				//preMic.setMinBound();//set min Bound
				//preMic.updateDensity(g);//update density
			}
		}
		
		//filter the micro state whose size is zero
		ArrayList<MicroState> queryPreMics=new ArrayList<MicroState>();
		for(MicroState preMic:inPreMics){
			if(preMic.getSize()>0){
				queryPreMics.add(preMic);
			}
		}

		// get relax micro state from grid,quried by the rest points in preMics
		ArrayList<RelaxMicroState> rfRestPreMic = g.forwardQueryMics(queryPreMics);
		// convert to MicroState
		ArrayList<MicroState> queryMics = inAC.SplitMics(rfRestPreMic, r);

		// visit all the reuseMicHash, the elememts left in this hash are the
		// points that should not be part of
		// pre mics query result, thus, these points should be deleted from
		// reuse mics.
		Iterator<Entry<Long, MicroState>> reuseMicsHashItr = reuseMicsHash
				.entrySet().iterator();
		// delete the points from state
		while (reuseMicsHashItr.hasNext()) {
			Entry<Long, MicroState> eLM = reuseMicsHashItr.next();
			MicroState eLMS = eLM.getValue();
			Long eLMK = eLM.getKey();
			eLMS.deletePoint(eLMK);
		}

		//store the sum of micro states( query+ reuse)
		ArrayList<MicroState> sumMics=new ArrayList<MicroState>();
		// when delete point ,we donot update the density and the minbound, all
		// those work done by one scan
		Iterator<MicroState> reuseMicItr = inReuseMics.iterator();
	
		while (reuseMicItr.hasNext()) {
			MicroState reuseMic = reuseMicItr.next();
			if (reuseMic.getSize() >= 1) {
				reuseMic.updateDensity(g);
				reuseMic.setMinBound();
				sumMics.add(reuseMic);
			} 
		}
				
		// query and reuse result merge together
		if(null!=queryMics){
			
		sumMics.addAll(queryMics);
		}

		// get the maximum large micro state
		ArrayList<MicroState> res = inAC.mergeMics(sumMics, null, r);
		
		return res;
	}
	
	/**
	 * build index for (TraId+time)-> MicroState
	 * @param inReuseMics
	 * @return
	 */
	private HashMap<Long,MicroState> hashTratimeMics(ArrayList<MicroState> inReuseMics){
		
		HashMap<Long,MicroState> res=new HashMap<Long,MicroState>();
		if(null==inReuseMics) return null;
		
		for(MicroState ms:inReuseMics){//visit all micro states
			if(null!=ms.LTraCell&&ms.LTraCell.size()>0){
				Iterator<Long> keys=ms.LTraCell.keySet().iterator();//visit each traId+time in each micro state
				while(keys.hasNext()){
					Long keyItem=keys.next();
					res.put(keyItem, ms);//put into hashmap
				}
			}
		}
		
		return res;
	}
	
	/**
	 * continuous prediction by reuse cache result
	 * @param inRecentPath
	 * @param backLen
	 * @param g
	 * @param p
	 * @param minDelta
	 * @param r
	 * @return
	 */
	public ArrayList<StateGridFilter> continuousPathPredictionRC(
			ArrayList<RoICell> inRecentPath, int backLen, Grid g, double p,
			double minDelta, double r, int prd_num){
		ArrayList<StateGridFilter> resSGF=new ArrayList<StateGridFilter>();
		g.openQC();
		ArrayList<RoICell> firstRef = new ArrayList<RoICell>(
				inRecentPath.subList(0, backLen));// index backLen is exclusive,
		
		// query the result from grid for first step
		ArrayList<Entry<Long, GridLeafTraHashItem>> firstRes = g
				.queryRangeTimeSeqCells(firstRef);

		// store the micro state, it will be reused by next time stamp
		ArrayList<StatesDendrogram> toReuseSDList = new ArrayList<StatesDendrogram>();
		// prediction path
		StateGridFilter firstGF = PathPrediction(firstRes, g, p, minDelta, r,
				null, prd_num);//noted, we do not use toReuseSDList fore experiment currently, wait for forever...
		
		resSGF.add(firstGF);
		
		// start from time k, do prediction for eahc time step k
		for (int k = 1; k < inRecentPath.size() - backLen + 1; k++) {
			// for k to k+backLen-1, k is inclusive, and k+backLen is exclusive
			// get the recent trajectory, trace back trajectory
			ArrayList<RoICell> followRef = new ArrayList<RoICell>(
					inRecentPath.subList(k, k + backLen));
		
			
			
			
			ArrayList<Entry<Long, GridLeafTraHashItem>> followRes = g
					.queryRangeTimeSeqCells(followRef);// firstly, query result
					
			// store the micro state for this time,it will be reused by next
			// time stamp
			
			ArrayList<StatesDendrogram> toReusefollow = new ArrayList<StatesDendrogram>();
			// prediction
			StateGridFilter sgfFollow = this.PathPredictionReuse(followRes, g,
					p, minDelta, r, null, null);
			
			// reuse point to this time step micro states
			toReuseSDList = toReusefollow;
			resSGF.add(sgfFollow);
		}
		g.closeQC();//for debug
		return resSGF;
		
	}

	/**
	 * make continuous prediction from 0 to len-backLen
	 * Reuse micros: of previous micro states
	 * 
	 * @param inRecentPath
	 *            :a pre-defind prediction path
	 * @param backLen
	 *            :back trace path
	 * @param g
	 *            :grid map
	 * @param p
	 *            :probability to control the state generation
	 * @param minDelta
	 *            :min probability for MAP
	 * @param r
	 *            :maximum distance threshold for micro state
	 * @return: ArrayList<StateGridFilter>, the generated StateGridFilter for at
	 *          each time 0<=k<len-backLen
	 */
	public ArrayList<StateGridFilter> OLDcontinuousPathPredictionRM(
			ArrayList<RoICell> inRecentPath, int backLen, Grid g, double p,
			double minDelta, double r, int prd_num) {

		// result to return
		ArrayList<StateGridFilter> resSGF = new ArrayList<StateGridFilter>();

		ArrayList<RoICell> firstRef = new ArrayList<RoICell>(
				inRecentPath.subList(0, backLen));// index backLen is exclusive,
		// query the result from grid for first step
		ArrayList<Entry<Long, GridLeafTraHashItem>> firstRes = g
				.queryRangeTimeSeqCells(firstRef);

		// store the micro state, it will be reused by next time stamp
		ArrayList<StatesDendrogram> toReuseSDList = new ArrayList<StatesDendrogram>();
		// prediction path
		StateGridFilter firstGF = PathPrediction(firstRes, g, p, minDelta, r,
				toReuseSDList, prd_num);
		resSGF.add(firstGF);

		// start from time k, do prediction for eahc time step k
		for (int k = 1; k < inRecentPath.size() - backLen + 1; k++) {
			// for k to k+backLen-1, k is inclusive, and k+backLen is exclusive
			// get the recent trajectory, trace back trajectory
			ArrayList<RoICell> followRef = new ArrayList<RoICell>(
					inRecentPath.subList(k, k + backLen));

			ArrayList<Entry<Long, GridLeafTraHashItem>> followRes = g
					.queryRangeTimeSeqCells(followRef);// firstly, query result

			// store the micro state for this time,it will be reused by next
			// time stamp
			ArrayList<StatesDendrogram> toReusefollow = new ArrayList<StatesDendrogram>();
			// prediction
			StateGridFilter sgfFollow = this.PathPredictionReuse(followRes, g,
					p, minDelta, r, toReuseSDList, toReusefollow);
			
		
			
			
			// reuse point to this time step micro states
			toReuseSDList = toReusefollow;
			resSGF.add(sgfFollow);
		}
		return resSGF;
	}

	/**
	 * 
	 * @param inRecentPath
	 * @param g
	 * @param p://state generation control probability
	 * @param minDelta:MAP threshold
	 * @param r
	 * @return
	 */
	public StateGridFilter DirectPathPrediction(ArrayList<RoICell> inRecentPath,Grid g, double p,
			double minDelta, double r, int prd_num){
		ArrayList<Entry<Long, GridLeafTraHashItem>>  queryFirst=g.queryRangeTimeSeqCells(inRecentPath);
		if(null==queryFirst||queryFirst.size()==0) return null;
		 StateGridFilter sgf=PathPrediction(queryFirst, g,p,minDelta,r, prd_num);
		 
		 return sgf;
	}
	
	
	private HashMap<Integer, ArrayList<RoICell>> LoadTestCase(String testFile){
		
		HashMap<Integer,ArrayList<RoICell>> res=new HashMap<Integer,ArrayList<RoICell>>();
		try{
		FileInputStream in=new FileInputStream(testFile);
		DataInputStream ds=new DataInputStream(in);
		BufferedReader br=new BufferedReader(new InputStreamReader(ds));
		
		String str="";
		ArrayList<RoICell> tra=null;
		int id=-1;
		while((str=br.readLine())!=null){
			if(str.contains("#")){
				String idStr[]=str.split("#");
				id=Integer.parseInt(idStr[1]);
				tra=new ArrayList<RoICell>();
				if(null!=tra&&-1!=id){
					res.put(id, tra);
				}
			
			}else{
				String locStr[]=str.split(" ");
				if(locStr.length>=2){
				int x=Integer.parseInt(locStr[0]);
				int y=Integer.parseInt(locStr[1]);
				
				RoICell rc=new RoICell(x,y);
				tra.add(rc);
				}
			}
		}
		
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return res;
		
		
		
	}
	
	
	
	public void printSampleList(HashMap<Integer,ArrayList<RoICell>> inList){
		
		Iterator<Entry<Integer,ArrayList<RoICell>>> lItr=inList.entrySet().iterator();
		while(lItr.hasNext()){
			Entry<Integer,ArrayList<RoICell>> item=lItr.next();
			
			System.out.println("id:"+item.getKey());
			
			ArrayList<RoICell> rcList=item.getValue();
			
			for(RoICell rc:rcList){
				System.out.print(" x:"+rc.roiX+" y:"+rc.roiY+" ");
			}
			System.out.println("");
		}
		
	}
	
	public static void main(String args[]){
	//	Predictor pdr=new Predictor();
		//pdr.testToyQC();
		//pdr.testContinuous();
		//pdr.testTaxi();
	//	pdr.testTaxiContinuous();
		//pdr.testLoadSample();
		//pdr.testToyData();
		//pdr.testBrikoff();
		//pdr.testToyContinuous();
	
		
	}
	

	
}


/*//junk
//for debug
if(k+1<inRecentPath.size() - backLen + 1){
ArrayList<Entry<Long,GridLeafTraHashItem>> firstTra=g.queryRangeTimeSeqCells(followRef);

ArrayList<RoICell> secondRef=new ArrayList<RoICell>(inRecentPath.subList(k+1, k+1+backLen));
ArrayList<Entry<Long,GridLeafTraHashItem>> secondTra=g.queryRangeTimeSeqCells(secondRef);

HashSet<Long> firstSet=new HashSet<Long>();
HashSet<Integer> firstTraSet=new HashSet<Integer>();
if(null!=firstTra){
	System.out.println("first item:");
	for(Entry<Long,GridLeafTraHashItem> firstItem:firstTra){
		Long itemKey=firstItem.getKey();
		int itemId=Configuration.getTraId(itemKey);
		int itemTime=Configuration.getTime(itemKey);
		int itemTimeNext=itemTime+Configuration.T_Sample;
		firstSet.add(Configuration.getKey(itemId, itemTimeNext));
		
		firstTraSet.add(itemId);
		System.out.println("tra id:"+itemId+" time:"+itemTime);
		
	}
}

HashSet<Long> secondSet=new HashSet<Long>();
HashSet<Integer> secondTraSet=new HashSet<Integer>();
if(null!=secondSet){
	System.out.println("second time:");
	for(Entry<Long,GridLeafTraHashItem> secondItem:secondTra){
		secondSet.add(secondItem.getKey());
		secondTraSet.add(Configuration.getTraId(secondItem.getKey()));
		
		System.out.println("tra id:"+Configuration.getTraId(secondItem.getKey())+" time:"+Configuration.getTime(secondItem.getKey()));
	}
}

HashSet<Long> sumSet=new HashSet<Long>();
sumSet.addAll(firstSet);
sumSet.addAll(secondSet);
int reuseInt=firstSet.size()+secondSet.size()-sumSet.size();
double reusePercent=(double)reuseInt/secondSet.size();

HashSet<Integer> sumTraSet=new HashSet<Integer>();
sumTraSet.addAll(firstTraSet);
sumTraSet.addAll(secondTraSet);
int reuseTra=firstTraSet.size()+secondTraSet.size()-sumTraSet.size();
double reuseTraPercent=(double)reuseTra/secondTraSet.size();

System.out.println("reuseable id+time:"+reuseInt+" reuse percent:"+reusePercent+" reuse id:"+reuseTra+" reuse percent:"+reuseTraPercent);



}
//end for debug
*/


