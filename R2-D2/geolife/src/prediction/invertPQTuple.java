package prediction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class invertPQTuple{
	HashMap<Integer,ArrayList<PQTuple>> invertNbs=new HashMap<Integer,ArrayList<PQTuple>>();//record neighborhood, all the PQTuple, whose nbs is "key:nbs"
	HashMap<Integer,PQTuple> invertHost=new HashMap<Integer,PQTuple>();//record host, all the PQTuple, whose host is "key:host"
	
	public void addNbsTuple(int id,PQTuple t){
		ArrayList<PQTuple> al=invertNbs.get(id);
		if(null==al){
			al=new ArrayList<PQTuple>();
			al.add(t);
			invertNbs.put(id, al);
		}else{
			al.add(t);
		}
	}
	
	public void addHostTuple(int id, PQTuple t){
		PQTuple al=invertHost.get(id);
		invertHost.put(id, t);
	}
	
	public ArrayList<PQTuple> getNbsTuples(int id){
		return invertNbs.get(id);
	}
	
	public PQTuple getHostTuples(int id){
		return invertHost.get(id);
	}
	
	public void deleteNbsId(int id){
		invertNbs.remove(id);
	}
	public void deleteHostId(int id){
		invertHost.remove(id);
	}
	
}
