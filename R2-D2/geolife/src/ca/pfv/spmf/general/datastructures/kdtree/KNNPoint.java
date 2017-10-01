package ca.pfv.spmf.general.datastructures.kdtree;

/**
 * This class represents a point for the KDTree
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

public class KNNPoint implements Comparable<KNNPoint>{
	
	double [] values;
	double distance;
	
	public KNNPoint(double values[], double distance){
		this.values = values;
		this.distance = distance;
	}

	public int compareTo(KNNPoint point2) {
		return (int)(this.distance  - point2.distance);
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("(");
		for(Double element : values ){
			buffer.append(" " + element);
		}
		buffer.append(")");
		return buffer.toString();
	}
	
	public boolean equals(Object point2){
		if(point2 == null){
			return false;
		}
		KNNPoint o2 = (KNNPoint)point2;
		for(int i=0; i < values.length; i++ ){
			if(o2.values[i] != values[i]){
				return false;
			}
		}
		return true;
	}
}
