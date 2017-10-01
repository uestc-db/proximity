package prediction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class PQTuple {
	double d;
	int host;
	int nbs;
	
	//host-> nbs; nbs is te nearest neighbor of host
	public PQTuple(int inHost,int inNeighbor,double inDist){
		host=inHost;
		nbs=inNeighbor;
		d=inDist;
	}
	
	public static void main(String args[]){
		PQTuple t1=new PQTuple(1,2,0.5);
		PQTuple t2=new PQTuple(3,4,0.6);
		
		PriorityQueue<PQTuple> pq=new PriorityQueue<PQTuple>(4,new ComparatorPQTuple());
		
		pq.add(t1);
		pq.add(t2);
		
		pq.remove(t1);
		System.out.println(pq.size());
		
	}
};
//compare distance
class ComparatorPQTuple implements Comparator<PQTuple>{

	@Override
	public int compare(PQTuple arg0, PQTuple arg1) {
		// TODO Auto-generated method stub
		if(arg0.d<arg1.d) return -1;
		if(arg0.d==arg1.d) return 0;
		if(arg0.d>arg1.d) return 1;
		return 0;
	}
	
};

