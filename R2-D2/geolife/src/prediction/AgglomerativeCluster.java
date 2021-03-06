package prediction;

import grid.Configuration;
import grid.Grid;
import grid.GridCell;
import grid.GridLeafTraHashItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Map.Entry;

import utl.kdtree.KDTree;
import utl.kdtree.KeyDuplicateException;
import utl.kdtree.KeyMissingException;
import utl.kdtree.KeySizeException;

public class AgglomerativeCluster {
	Grid g;
	public AgglomerativeCluster(Grid inG){
		g=inG;
	}

	/**
	 * dendrogram绯荤粺鏍戝浘鐨勬剰鎬�
	 * @param queryRes: a set of query result form grid
	 * @param r
	 * @return
	 */
	public StatesDendrogram getDendrogram(ArrayList<Entry<Long, GridLeafTraHashItem>> queryRes,
			double r) {

		ArrayList<MicroState> mics = InitialMicroState(queryRes, r);
		ArrayList<ArrayList<MacroState>> den= buildDendrogram(mics, r);
		
		StatesDendrogram sd=new StatesDendrogram(mics,den);
		
		return sd;
	}
	
	/**
	 * get dendrogram from micro states directly
	 * @param mics
	 * @param r
	 * @return
	 */
	public StatesDendrogram getDendrogramMics(ArrayList<MicroState> mics,double r){
		
		ArrayList<ArrayList<MacroState>> den= buildDendrogram(mics, r);//build the dendrogram
		StatesDendrogram sd=new StatesDendrogram(mics,den);
		
		return sd;
	}
	
	/**
	 * 
	 * @param rMics: a set of relax micro state, which is query by a set of micro states
	 * @param r
	 * @return
	 */
	public StatesDendrogram getDendrogramRelaxMics(ArrayList<RelaxMicroState> rMics,double r){
		
		ArrayList<MicroState> maxMics=null;
		StatesDendrogram sd=null;
		maxMics= Releax2Mics( rMics, r);
		
		ArrayList<ArrayList<MacroState>> den= buildDendrogram(maxMics, r);//build the dendrogram
		
		sd=new StatesDendrogram(maxMics,den);
		
		return sd;
	}
	
	/**
	 * convert RelaxMicroState into Micro State by split and merge operation
	 * @param rMics
	 * @param r
	 * @return
	 */
	public  ArrayList<MicroState> Releax2Mics(ArrayList<RelaxMicroState> rMics,double r){
		ArrayList<MicroState> initalMics=SplitMics(rMics, r);//split the relax micro state
		ArrayList<MicroState> maxMics=null;
		maxMics= mergeMics(initalMics, null, r);//merge the micro state
		
		return maxMics;
	}
	
	/**
	 * 
	 * @param mics
	 * @param r
	 * @return
	 */
	private ArrayList<ArrayList<MacroState>> buildDendrogram(
			ArrayList<MicroState> mics, double r) {

		ArrayList<ArrayList<MacroState>> res = new ArrayList<ArrayList<MacroState>>();
		if(null==mics){
			return res;
		}

		// initialize the k-d tree. It is a quad-tree in fact
		KDTree<MacroState> kt = new KDTree<MacroState>(2);// build quad-tree for
															// nearest query
		HashMap<Integer, MacroState> macs = new HashMap<Integer, MacroState>();// stateId->
																				// macro_state
		invertPQTuple invertPQT = new invertPQTuple();// invert list, index the
														// tuple in priority
														// queue
		PriorityQueue<PQTuple> pqLow = new PriorityQueue<PQTuple>(mics.size(),
				new ComparatorPQTuple());// lower level pq, if this queue is 0,
											// this level is finished
		PriorityQueue<PQTuple> pqHigh = new PriorityQueue<PQTuple>(mics.size(),
				new ComparatorPQTuple());// high level pq

		initialStateUpward(mics, kt, macs, invertPQT, pqLow);// initialize all

		double R = r;
		do {
			R = Configuration.AlphaRadius * R;
			
			if(R>Configuration.MaxRadius) break;

			ArrayList<MacroState> level = StateUpward(kt, macs, invertPQT,
					pqLow, pqHigh, R);

			res.add(level);

			pqLow = pqHigh;
			int c = (pqLow.size() > 1) ? pqLow.size() : 2;// minimum of pq is 2
			pqHigh = new PriorityQueue<PQTuple>(c, new ComparatorPQTuple());

		} while (pqLow.size() > 1);

		return res;
	}
	
	
	/**
	 * initialize the micro state. Given a set of cells, this function create a
	 * set of micro state. there are two steps step 1) construct the micro
	 * state,2) set the minimum bound for each micro state
	 * 
	 * @param cellArray
	 * @param r
	 * @return a set of micro state, which is store in ArrayList
	 */
	private ArrayList<MicroState> InitialMicroState(
			ArrayList<Entry<Long, GridLeafTraHashItem>> queryRes, double r) {

		// ArrayList<Entry<Long,GridLeafTraHashItem>>
		// queryRes=g.queryRangeTimeSeqCells(cellArray);//query all the cells
		// for next time stamp

		// result storage
		ArrayList<MicroState> res = null;

		// initialize the k-d tree. It is a quad-tree in fact
		KDTree<MicroState> kt = new KDTree<MicroState>(2);

		if (null==queryRes||0 == queryRes.size())
			return res;

		try {

			ArrayList<MicroState> inMics = createMics(queryRes, kt);// create
																	// initial
																	// micro
																	// states
																	// from
																	// points

			res = mergeMics(inMics, kt, r);// merge thus micro states to maximum
											// micro states

		} catch (KeySizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyMissingException e) {
			e.printStackTrace();
		}

		return res;// return result
	}
	
	/**
	 * given a set of micro states, and merge them into a set of maximum micro
	 * states
	 * 
	 * @param inMics
	 * @param inkt
	 *            : if inkt is  null( inMics are stored in inkt at the same
	 *            time), create a new k-d tree, and store each state into k-d
	 *            tree
	 * @param r
	 * @return
	 * @throws KeySizeException
	 * @throws KeyMissingException
	 */
	public ArrayList<MicroState> mergeMics(ArrayList<MicroState> inMics,
			KDTree<MicroState> inkt, double r) {
		// result storage
		ArrayList<MicroState> res = null;

		if (null == inMics) {
			return res;
		}
		try {
		// store all the micro state and expend them, until the map is empty
		HashMap<Integer, MicroState> ml = new HashMap<Integer, MicroState>();
		if (null == inkt) {// if inkt is null, create a new k-d tree to store
							// all micro states
			inkt = new KDTree<MicroState>(2);
			for (MicroState inMic : inMics) {
				// if inkt is null, create new k-d tree and insert micro states into it
					KDTreeHashInsertMic(inMic, ml, inkt);
				
			}
		} else {
			for (MicroState inMic : inMics) {
				// if inkt have been created, only insert all the states into hashmap
				KDTreeHashInsertMic(inMic, ml, null);
			}
		}

		assert (ml.size() == inkt.size());

		// until there is no micro state can be expanded
		while (ml.size() > 0) {
			// get one micro state from hashmap
			Collection<MicroState> col = ml.values();
			Iterator<MicroState> itrMS = col.iterator();
			MicroState col_e = itrMS.next();

			// remove from hashmap, if it cannot be expanded, it will not be
			// added into the map
			itrMS.remove();
			// ml.remove(col_e.id);

			// query all the possible micro states
			List<MicroState> ms_sets = inkt.nearestEuclidean(col_e.getCenter(),
					2 * r - col_e.minBound);
			if (null == ms_sets || 0 == ms_sets.size())
				continue;

			// visit all the possible nearest neighbors by iterator
			MicroState[] a = new MicroState[1];
			MicroState[] ms_sets_A = ms_sets.toArray(a);

			for (int j = ms_sets_A.length - 1; j >= 0; j--) {
				MicroState nbs = ms_sets_A[j]; // get one micro state
				if (nbs.id == col_e.id)
					continue;// ignore themselves
				// if true, this state is expanded
				if (nbs.getDisCenter(col_e) - nbs.minBound - col_e.minBound <= 2 * r) {
					// delete this micro state, as it will be changed,noted that
					// col_e has been removed from hashmap ml
					inkt.delete(col_e.getCenter());
					// delete nbs, as it will be absorbed
					//if(!equalDoubleArray(col_e.getCenter(),nbs.getCenter()))
					inkt.delete(nbs.getCenter());
					// remove it from hashmap ml
					ml.remove(nbs.id);
					ml.remove(col_e.id);

					// col_e.addMicroState(nbs);
					// ml.put(col_e.id,col_e);

					MicroState ms = new MicroState(Configuration.getStateId(),
							col_e, nbs);
					this.KDTreeHashInsertMic(ms, ml,false, inkt,true);// note that we donot insert it into hashmap again.

					// kt.replaceInsert(col_e.getCenter(), col_e);

					break;
				}

			}
		}
		} catch (KeySizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyMissingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(inkt.size()>Configuration.minNumPerMic){
		res = inkt.toArrayListValue();// to arrayList
		return res;
		}else{
			return null;
		}
	}

	private boolean equalDoubleArray(double[] a,double b[]){
		if(null==a||null==b) return false;
		if(a.length!=b.length) return false;
		for(int i=0;i<a.length;i++){
			if(a[i]!=b[i]) return false;
		}
		return true;
	}
	/**
	 * given a set of query result, initialize them into a set of micro states.
	 * Most of time, just initialize each point as a state however, some points
	 * are within the same cells are merged
	 * 
	 * @param queryRes
	 * @param outkt: if outkt is not null, the micro states also are stored into k-d tree
	 * @return
	 * @throws KeySizeException
	 * @throws KeyMissingException
	 */
	private ArrayList<MicroState> createMics(
			ArrayList<Entry<Long, GridLeafTraHashItem>> queryRes,
			KDTree<MicroState> outkt) throws KeySizeException,
			KeyMissingException {
		if (null == outkt) {
			outkt = new KDTree<MicroState>(2);
		}

		if (0 == queryRes.size())
			return null;

		// initialize the micro state, consider each cell as a micro state
		for (int i = 0; i < queryRes.size(); i++) {
			// get cell
			int txi = queryRes.get(i).getValue().getCellX();
			int tyi = queryRes.get(i).getValue().getCellY();
			//System.out.println("creatmics");
			GridCell tgci=g.getGridCell(txi, tyi);

			// consider each cell as a state
			// idCount++;
			MicroState ms = new MicroState(Configuration.getStateId());
			 ms.addPoint(txi,tyi , tgci.density,queryRes.get(i)); 
			 //ms.addPoint(txi, tyi, 1, queryRes.get(i));//for debug

			// insert into k-d tree and hashmap
			this.KDTreeHashInsertMic(ms, null, outkt);

			// insert into k-d tree
			// kt.replaceInsert(ms.getCenter(), ms);
			// put into label hashmap
			// ml.put(ms.id, ms);
		}
		if(outkt.size()>2){
		ArrayList<MicroState> inMics = outkt.toArrayListValue();

		return inMics;
		}else {
			return null;
		}
	}
	
	
	/**
	 * split the relax micro state into a ast of micro states
	 * @param rMics
	 * @param r
	 * @return
	 */
	public ArrayList<MicroState> SplitMics(ArrayList<RelaxMicroState> rMics,double r){
		
		if(null==rMics||0==rMics.size()){
			return null;
		}
		
		ArrayList<MicroState> res=new ArrayList<MicroState>();//store result
		
		for(RelaxMicroState rmic:rMics){
			ArrayList<MicroState> rmicResItem=rmic.generateMics(r, g);//each relax mics are splited by themselves
			if(null!=rmicResItem){
				res.addAll(rmicResItem);
			}
		}
		
		return res;
	}
	
	/**
	 * go to up level to get good state
	 * 
	 * @param mics
	 * @param r
	 * @param p
	 * @return
	 */
	public ArrayList<MacroState> StateUpward(KDTree<MacroState> kt,HashMap<Integer, MacroState> macs,
			invertPQTuple invertPQT,PriorityQueue<PQTuple> pqLow,PriorityQueue<PQTuple> pqHigh,
			double R) {

			while (pqLow.size() > 0) {//if there are state can be merged
				PQTuple closest = findMergable(macs, pqLow, pqHigh, R);
				if (null == closest)//cannot find any mergable state,null is possible
					break;

				MacroState H = Agglomeration(macs, closest, kt);//generate new larger state
				assert(null!=H);
				if(kt.size()>1){//if the pqLow is smaller or equal to 1, we do not need to do reflection
				Reflection(H, closest, macs, invertPQT, kt, pqLow, pqHigh);
				}else{
					break;
				}

			}

		return 	kt.toArrayListValue();//to arrayList
		

	}
	
	/**
	 * the update of priority queue
	 * @param H
	 * @param inClosest
	 * @param inMacs
	 * @param inInvertPQT
	 * @param inkt
	 * @param inoutPqLow
	 * @param inPqHigh
	 */
	private void Reflection(MacroState H, PQTuple inClosest,
			HashMap<Integer, MacroState> inMacs, invertPQTuple inInvertPQT,
			KDTree<MacroState> inkt, PriorityQueue<PQTuple> inoutPqLow,
			PriorityQueue<PQTuple> inPqHigh) {

		inoutPqLow.remove(inClosest);// remove host itself
		inInvertPQT.deleteHostId(inClosest.host);//remove the host from index
		
		//if any macro state point to host
		ArrayList<PQTuple> hTuples = inInvertPQT.getNbsTuples(inClosest.host);
	
		// remove all point to host
		if (null != hTuples) {
			for (PQTuple item : hTuples) {
				if (!inoutPqLow.remove(item)) {//remove from low, if not in low, remove from high
					boolean flag=inPqHigh.remove(item);
					
				}

				MacroState itemHost = inMacs.get(item.host);
				if(null!=itemHost){//itemHost may be null, as it may be deleted
					findNewTuple( itemHost, inkt,
							 inoutPqLow, inInvertPQT);
				}
			}
			inInvertPQT.deleteNbsId(inClosest.host);//delete as host has been deleted
		}
		
		//find tuple, whose host is nbs, as nbs is disappeared
		PQTuple nbsHost = inInvertPQT.getHostTuples(inClosest.nbs);
		if (null != nbsHost) {
			if (!inoutPqLow.remove(nbsHost)) {
				inPqHigh.remove(nbsHost);
			}
			inInvertPQT.deleteHostId(inClosest.nbs);
		}

		ArrayList<PQTuple> nbsTuples = inInvertPQT.getNbsTuples(inClosest.nbs);//find all tuple points to nbs, as nbs is disappeared

		// remove all point to nbs
		if (null != nbsTuples) {
			for (PQTuple item : nbsTuples) {
				if (!inoutPqLow.remove(item)) {
					boolean flag=inPqHigh.remove(item);
					//assert(flag);
				}

				MacroState itemHost = inMacs.get(item.host);
				if(null!=itemHost){//itemHost may be null, as it may be deleted
					findNewTuple( itemHost, inkt,
							 inoutPqLow, inInvertPQT);
				}
			}
			
			inInvertPQT.deleteNbsId(inClosest.nbs);
		}

		//add new macro state H into PQ
		MacroState HNbs = this.getClosestMac(H, inkt);
		PQTuple HPqt = new PQTuple(H.id, HNbs.id, H.getDisCenter(HNbs));
		inoutPqLow.add(HPqt);
		inInvertPQT.addHostTuple(H.id, HPqt);
		inInvertPQT.addNbsTuple(HNbs.id, HPqt);

	}
	
	/**
	 * find a new clost tuple for a host, and update the corresponding DS
	 * @param itemHost
	 * @param inkt
	 * @param inoutPqLow
	 * @param inInvertPQT
	 */
	private void findNewTuple(MacroState itemHost, KDTree<MacroState> inkt,
			PriorityQueue<PQTuple> inoutPqLow, invertPQTuple inInvertPQT) {
		
		MacroState itemNbs = this.getClosestMac(itemHost, inkt);
		PQTuple itemPqt = new PQTuple(itemHost.id, itemNbs.id,
				itemHost.getDisCenter(itemNbs));
		inoutPqLow.add(itemPqt);//add to lowPQ
		
		inInvertPQT.addHostTuple(itemHost.id, itemPqt);//add to invert index
		inInvertPQT.addNbsTuple(itemNbs.id, itemPqt);//add to invert index
		
		

	}
	
	/**
	 * find the nearest macro state of host
	 * @param host 
	 * @param inkt
	 * @return
	 */
	private MacroState getClosestMac(MacroState host, KDTree<MacroState> inkt) {

		List<MacroState> stj;
		try {
			stj = inkt.nearest(host.getCenter(), 2);// find 2, exception itself
			assert (stj != null);
			MacroState a[] = new MacroState[1];
			MacroState stjA[] = stj.toArray(a);
			
			int ni=getNearestState( stjA, host);
			return stjA[ni];
			

		} catch (KeySizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * 
	 * @param host
	 * @param inkt
	 * @return
	 */
	private MicroState getClosestMic(MicroState host,KDTree<MicroState> inkt){
		List<MicroState> stj;
		try{
			stj=inkt.nearest(host.getCenter(), 2);
			assert(stj!=null);
			MicroState a[]=new MicroState[1];
			MicroState stjA[]=stj.toArray(a);
			int ni=getNearestState(stjA,host);
			return stjA[ni];
		}catch (KeySizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private int getNearestState(State[] stjA,State host){
		for (int k = stjA.length - 1; k >= 0; k--) {
			State nbs = stjA[k];
			if(nbs.id==host.id) continue;//skip itself

			return k;
		}
		
		return -1;
	}

	/**
	 * 
	 * @param inMacs
	 * @param inClosest
	 * @param inkt
	 * @return
	 */
	private MacroState Agglomeration(HashMap<Integer, MacroState> inMacs,
			PQTuple inClosest, KDTree<MacroState> inkt) {

		MacroState host = inMacs.get(inClosest.host);
		MacroState nbs = inMacs.get(inClosest.nbs);

		try {
			//remove two smaller macro state from k-d tree and hashmap
			inkt.delete(host.getCenter());
			inkt.delete(nbs.getCenter());
			inMacs.remove(nbs.id);
			inMacs.remove(host.id);

			//generate new larger macro state
			MacroState com=new MacroState(Configuration.getStateId(),host,nbs);
			
			//insert into hashmap and k-d tree
			KDTreeHashInsertMac(com,inMacs,inkt);
			//inMacs.put(com.id, com);
			//inkt.replaceInsert(com.getCenter(), com);

			return com;

		} catch (KeySizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyMissingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return null;

	}
	
	/**
	 * /insert into hashmap and k-d tree
	 * @param ms
	 * @param inMacs
	 * @param inkt
	 * @throws KeySizeException
	 * @throws KeyMissingException
	 */
	public void KDTreeHashInsertMac(MacroState ms,
			HashMap<Integer, MacroState> inMacs, KDTree<MacroState> inkt)
			throws KeySizeException, KeyMissingException {

		MacroState insertMac = ms;
		try {
			if(null!=inkt){
			inkt.insert(insertMac.getCenter(), insertMac);
			}
		} catch (KeyDuplicateException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			MacroState ms0 = inkt.search(ms.getCenter());
			inkt.delete(ms0.getCenter());
			insertMac = new MacroState(Configuration.getStateId(), ms, ms0);
			try {
				inkt.insert(insertMac.getCenter(), insertMac);
			} catch (KeyDuplicateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(null!=inMacs){
		inMacs.put(insertMac.id, insertMac);
		}

	}
	
	/**
	 * 
	 * @param ms
	 * @param inMacs: if it is null, just ignore it
	 * @param inkt:if it is null, just ignore it
	 * @throws KeySizeException
	 * @throws KeyMissingException
	 */
	public void KDTreeHashInsertMic(MicroState ms,
			HashMap<Integer, MicroState> inMacs,KDTree<MicroState> inkt)
			throws KeySizeException, KeyMissingException {
		
		KDTreeHashInsertMic( ms,
				inMacs, true, inkt,true);
	}
	
	
	
	/**
	 * 
	 * @param ms
	 * @param inMacs
	 * @param insertHash
	 * @param inkt
	 * @param insertKT
	 * @throws KeySizeException
	 * @throws KeyMissingException
	 */
	public void KDTreeHashInsertMic(MicroState ms,
			HashMap<Integer, MicroState> inMacs, boolean insertHash,KDTree<MicroState> inkt,boolean insertKT)
			throws KeySizeException, KeyMissingException {

		MicroState insertMac = ms;
		try {
			if(null!=inkt&&insertKT){
			inkt.insert(insertMac.getCenter(), insertMac);
			}
		} catch (KeyDuplicateException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			MicroState ms0 = inkt.search(ms.getCenter());
			inkt.delete(ms0.getCenter());
			if(null!=inMacs){
				inMacs.remove(ms0.id);
			}
			insertMac = new MicroState(Configuration.getStateId(), ms, ms0);
			try {
				if(insertKT){
				inkt.insert(insertMac.getCenter(), insertMac);
				}
			} catch (KeyDuplicateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(null!=inMacs&&insertHash){
//		System.out.println(inMacs.put(insertMac.id, insertMac)); //why here always null
		}

	}
	
	/**
	 * find a mergable state
	 * @param inMacs: state_id->macro state
	 * @param inPqLow:
	 * @param outPqHigh:
	 * @param inR:  radius threshold
	 * @return
	 */
	private PQTuple findMergable(HashMap<Integer, MacroState> inMacs,
			PriorityQueue<PQTuple> inPqLow, PriorityQueue<PQTuple> outPqHigh,
			double inR) {

		while (inPqLow.size() > 1) {
			PQTuple pqt = inPqLow.poll();
			MacroState host = inMacs.get(pqt.host);
			MacroState nbs = inMacs.get(pqt.nbs);

			double sr = MacroState.getRadius(host, nbs);
			double cr =host.getDisCenter(nbs);
			if (sr > inR||cr>Configuration.MaxStateDis) {//remove this state into high
				outPqHigh.add(pqt);
			} else {
				return pqt;
			}
		}
		return null;
	}
	
	/**
	 * upward initialize
	 * @param mics  record all micro states
	 * @param outkt index the macro state for find nearest neighbor
	 * @param outMacs record macro_state_id->macro_state
	 * @param outInvertPQT index pq
	 * @param outPQ pq
	 */
	private void initialStateUpward(ArrayList<MicroState> mics,
			KDTree<MacroState> outkt, HashMap<Integer, MacroState> outMacs,
			invertPQTuple outInvertPQT, PriorityQueue<PQTuple> outPQ) {
		try {
			//create macro state for each micro state
			for (int i = 0; i < mics.size(); i++) {
				MacroState mac = new MacroState(Configuration.getStateId(),mics.get(i));//generate new state id
				this.KDTreeHashInsertMac(mac, outMacs, outkt);//insert into map and k-d tree
				//outkt.replaceInsert(mac.getCenter(), mac);//add it into k-d tree
				//outMacs.put(mac.id, mac);
			}

			Collection<MacroState> values = outMacs.values();//all macro state
			
			if(null!=values&&values.size()>2){
			for (MacroState mac : values) {
				
				findNewTuple( mac, outkt,
						outPQ, outInvertPQT);

			}
			}
		} catch (KeySizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyMissingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static StatesDendrogram  testCase1(){
		Grid g=null;
		AgglomerativeCluster ac=new AgglomerativeCluster(g);
		
		ArrayList<Entry<Long,GridLeafTraHashItem>> queryRes=new ArrayList<Entry<Long,GridLeafTraHashItem>>();
		
		HashMap<Long,GridLeafTraHashItem> hm=new HashMap<Long,GridLeafTraHashItem>();
		
		GridLeafTraHashItem gti[]=new GridLeafTraHashItem[27];
		gti[0]=new GridLeafTraHashItem(2,2);
		gti[1]=new GridLeafTraHashItem(2,3);
		gti[2]=new GridLeafTraHashItem(2,4);
		gti[3]=new GridLeafTraHashItem(2,5);
		gti[4]=new GridLeafTraHashItem(3,2);
		gti[5]=new GridLeafTraHashItem(3,3);
		gti[6]=new GridLeafTraHashItem(3,4);
		gti[7]=new GridLeafTraHashItem(3,5);
		gti[8]=new GridLeafTraHashItem(4,2);
		gti[9]=new GridLeafTraHashItem(4,3);
		gti[10]=new GridLeafTraHashItem(4,4);
		gti[11]=new GridLeafTraHashItem(4,5);
		gti[12]=new GridLeafTraHashItem(5,2);
		gti[13]=new GridLeafTraHashItem(5,3);
		gti[14]=new GridLeafTraHashItem(5,4);
		gti[15]=new GridLeafTraHashItem(5,5);
		
		gti[16]=new GridLeafTraHashItem(9,3);
		gti[17]=new GridLeafTraHashItem(10,3);
		gti[18]=new GridLeafTraHashItem(10,4);
		gti[19]=new GridLeafTraHashItem(10,5);
		gti[20]=new GridLeafTraHashItem(10,6);
		gti[21]=new GridLeafTraHashItem(10,7);
		gti[22]=new GridLeafTraHashItem(9,7);
		
		
		gti[23]=new GridLeafTraHashItem(3,7);
		gti[24]=new GridLeafTraHashItem(4,8);
		gti[25]=new GridLeafTraHashItem(5,9);
		gti[26]=new GridLeafTraHashItem(6,10);
		
		double sumx=0,sumy=0;
		
		for(int i=0;i<27;i++){
			Long key=Configuration.getKey(i, i);
			hm.put(key, gti[i]);
		}
		
		int start=16,end=26;
		
		for(int i=start;i<=end;i++){
			sumx+=gti[i].getCellX();
			sumy+=gti[i].getCellY();
		}
		sumx/=(end-start+1);
		sumy/=(end-start+1);
		
		double radius=0;
		for(int i=start;i<=end;i++){
			double x=gti[i].getCellX();
			double y=gti[i].getCellY();
			
			double r=(x-sumx)*(x-sumx)+(y-sumy)*(y-sumy);
			
			radius+=r;
		}		
		ArrayList<Entry<Long,GridLeafTraHashItem>> query=new ArrayList<Entry<Long,GridLeafTraHashItem>>(hm.entrySet());
		//System.out.println(res.size());
		query=new ArrayList<Entry<Long,GridLeafTraHashItem>>(hm.entrySet());
		
		return ac.getDendrogram(query, 2.58/4);
		
	}
	
	
	public static StatesDendrogram  testCase2(){
		Grid g=null;
		AgglomerativeCluster ac=new AgglomerativeCluster(g);
		
		ArrayList<Entry<Long,GridLeafTraHashItem>> queryRes=new ArrayList<Entry<Long,GridLeafTraHashItem>>();
		
		HashMap<Long,GridLeafTraHashItem> hm=new HashMap<Long,GridLeafTraHashItem>();
		
		GridLeafTraHashItem gti[]=new GridLeafTraHashItem[27];
		gti[0]=new GridLeafTraHashItem(2,2);
		gti[1]=new GridLeafTraHashItem(2,3);
		gti[2]=new GridLeafTraHashItem(2,4);
		gti[3]=new GridLeafTraHashItem(2,5);
		gti[4]=new GridLeafTraHashItem(3,2);
		gti[5]=new GridLeafTraHashItem(3,3);
		gti[6]=new GridLeafTraHashItem(3,4);
		gti[7]=new GridLeafTraHashItem(3,5);
		gti[8]=new GridLeafTraHashItem(4,2);
		gti[9]=new GridLeafTraHashItem(4,3);
		gti[10]=new GridLeafTraHashItem(4,4);
		gti[11]=new GridLeafTraHashItem(4,5);
		gti[12]=new GridLeafTraHashItem(5,2);
		gti[13]=new GridLeafTraHashItem(5,3);
		gti[14]=new GridLeafTraHashItem(5,4);
		gti[15]=new GridLeafTraHashItem(5,5);
		
		gti[16]=new GridLeafTraHashItem(9,1);
		gti[17]=new GridLeafTraHashItem(10,1);
		gti[18]=new GridLeafTraHashItem(10,2);
		gti[19]=new GridLeafTraHashItem(10,3);
		
		gti[20]=new GridLeafTraHashItem(10,7);
		gti[21]=new GridLeafTraHashItem(10,8);
		gti[22]=new GridLeafTraHashItem(9,8);
		
		
		gti[23]=new GridLeafTraHashItem(3,7);
		gti[24]=new GridLeafTraHashItem(4,8);
		gti[25]=new GridLeafTraHashItem(5,9);
		gti[26]=new GridLeafTraHashItem(6,10);
		
		double sumx=0,sumy=0;
		
		for(int i=0;i<27;i++){
			Long key=Configuration.getKey(i, i);
			hm.put(key, gti[i]);

		}
		
		int start=16,end=26;
		
		for(int i=start;i<=end;i++){
			sumx+=gti[i].getCellX();
			sumy+=gti[i].getCellY();
		}
		sumx/=(end-start+1);
		sumy/=(end-start+1);
		
		double radius=0;
		for(int i=start;i<=end;i++){
			double x=gti[i].getCellX();
			double y=gti[i].getCellY();
			
			double r=(x-sumx)*(x-sumx)+(y-sumy)*(y-sumy);
			
			radius+=r;
		}		
		ArrayList<Entry<Long,GridLeafTraHashItem>> query=new ArrayList<Entry<Long,GridLeafTraHashItem>>(hm.entrySet());
		//System.out.println(res.size());
		query=new ArrayList<Entry<Long,GridLeafTraHashItem>>(hm.entrySet());
		
		return ac.getDendrogram(query, 2.58/4);
		
	}
	
	public static void main(String args[]){
		//for dubug
		//Grid g=new Grid();
		
		Grid g=null;
		AgglomerativeCluster ac=new AgglomerativeCluster(g);
		
		ArrayList<Entry<Long,GridLeafTraHashItem>> queryRes=new ArrayList<Entry<Long,GridLeafTraHashItem>>();
		
		HashMap<Long,GridLeafTraHashItem> hm=new HashMap<Long,GridLeafTraHashItem>();
		
		GridLeafTraHashItem gti[]=new GridLeafTraHashItem[27];
		gti[0]=new GridLeafTraHashItem(2,2);
		gti[1]=new GridLeafTraHashItem(2,3);
		gti[2]=new GridLeafTraHashItem(2,4);
		gti[3]=new GridLeafTraHashItem(2,5);
		gti[4]=new GridLeafTraHashItem(3,2);
		gti[5]=new GridLeafTraHashItem(3,3);
		gti[6]=new GridLeafTraHashItem(3,4);
		gti[7]=new GridLeafTraHashItem(3,5);
		gti[8]=new GridLeafTraHashItem(4,2);
		gti[9]=new GridLeafTraHashItem(4,3);
		gti[10]=new GridLeafTraHashItem(4,4);
		gti[11]=new GridLeafTraHashItem(4,5);
		gti[12]=new GridLeafTraHashItem(5,2);
		gti[13]=new GridLeafTraHashItem(5,3);
		gti[14]=new GridLeafTraHashItem(5,4);
		gti[15]=new GridLeafTraHashItem(5,5);
		
		gti[16]=new GridLeafTraHashItem(9,3);
		gti[17]=new GridLeafTraHashItem(10,3);
		gti[18]=new GridLeafTraHashItem(10,4);
		gti[19]=new GridLeafTraHashItem(10,5);
		gti[20]=new GridLeafTraHashItem(10,6);
		gti[21]=new GridLeafTraHashItem(10,7);
		gti[22]=new GridLeafTraHashItem(9,7);
		
		
		gti[23]=new GridLeafTraHashItem(3,7);
		gti[24]=new GridLeafTraHashItem(4,8);
		gti[25]=new GridLeafTraHashItem(5,9);
		gti[26]=new GridLeafTraHashItem(6,10);
		
		double sumx=0,sumy=0;
		
		for(int i=0;i<27;i++){
			Long key=Configuration.getKey(Configuration.getStateId(), i);
			hm.put(key, gti[i]);

		}
		
		int start=16,end=26;
		
		for(int i=start;i<=end;i++){
			sumx+=gti[i].getCellX();
			sumy+=gti[i].getCellY();
		}
		sumx/=(end-start+1);
		sumy/=(end-start+1);
		
		double radius=0;
		for(int i=start;i<=end;i++){
			double x=gti[i].getCellX();
			double y=gti[i].getCellY();
			
			double r=(x-sumx)*(x-sumx)+(y-sumy)*(y-sumy);
			
			radius+=r;
		}
		
		System.out.println(""+Math.sqrt(radius/27));
		
		
		ArrayList<Entry<Long,GridLeafTraHashItem>> query=new ArrayList<Entry<Long,GridLeafTraHashItem>>(hm.entrySet());
		
		ArrayList<MicroState> res=ac.InitialMicroState(query, Math.sqrt(2)*2);
		System.out.println(res.get(0).getDisCenter(res.get(1)));
		System.out.println(res.get(1).getDisCenter(res.get(2)));

		System.out.println(res.get(0).getDisCenter(res.get(2)));
		
		System.out.println(MacroState.getRadius(res.get(0), res.get(1)));
		System.out.println(MacroState.getRadius(res.get(1), res.get(2)));
		
		//System.out.println(res.size());
		query=new ArrayList<Entry<Long,GridLeafTraHashItem>>(hm.entrySet());
		
		//ArrayList<ArrayList<MacroState>> macRes=ac.getDendrogram(query, 2.58/2);
		StatesDendrogram sd=ac.getDendrogram(query, 2.59/4);
		System.out.println(sd.macsTree.size());
	}
	
}
