package ca.pfv.spmf.clustering.kmeans;

public class Vector {

	double[] data;
	
	public Vector(double [] data){
		this.data = data;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(int i=0; i<data.length; i++){
			buffer.append(data[i]);
			if(i < data.length -1){
				buffer.append(",");
			}
		}
		return buffer.toString();
	}
}
