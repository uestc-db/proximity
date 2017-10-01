package ca.pfv.spmf.clustering.hierarchical_clustering;

import java.util.ArrayList;
import java.util.List;
/**
* This class represents a cluster.
 *
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SPMF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SPMF.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Cluster {
	private List<Vector> vectors;
	private Vector mean;
	
	private Vector sum; // FOR MEAN
	
	public Cluster(int vectorsSize){
		this.vectors = new ArrayList<Vector>();
		sum = new Vector(new double[vectorsSize]);
	}
	
	public void setMean(Vector mean){
		this.mean = mean;
	}
	
	public void addVector(Vector vector) {
		vectors.add(vector);
		for(int i=0; i < vector.data.length; i++){
			sum.data[i] += vector.data[i];
		}
	}

	public Vector getmean() {
		return mean;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		if(vectors.size() >=1){
			for(Vector vector : vectors){
				buffer.append("[");
				buffer.append(vector.toString());
				buffer.append("]");
			}
		}
		return buffer.toString();
	}
	

	public void recomputeClusterMean() {
		for(int i=0; i < sum.data.length; i++){
			mean.data[i] = sum.data[i] / vectors.size();
		}
	}

	public List<Vector> getVectors(){
		return vectors;
	}
}
