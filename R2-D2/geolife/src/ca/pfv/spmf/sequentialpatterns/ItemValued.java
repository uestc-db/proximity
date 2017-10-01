package ca.pfv.spmf.sequentialpatterns;

import ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining.Cluster;

/**
 * This class is an Item that can have an integer value.
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
public class ItemValued extends Item{
	private double value;
	private int sequenceID =-1; // used for clustering to recognize items that are from sequences.
	
	private double min;
	private double max;

	private Cluster cluster = null; // used for clustering
	
	public ItemValued(int id){
		this(id, 0);
	}
	
	public ItemValued(int id, double value){
		super(id);
		this.value = value;
		this.min = value;
		this.max = value;
	}
	
	// pour clustering
	public ItemValued(int id, double value, double min, double max){
		super(id);
		this.value = value;
		this.min = min;
		this.max = max;
	}
	
	// Constructeur pour tests dans clustering
	public ItemValued(int id, double value, int sequenceID){
		super(id);
		this.value = value;
		min = value;
		max = value;
		this.sequenceID = sequenceID;
	}

	public double getValue() {
		return value;
	}

	public String toString(){
		StringBuffer temp = new StringBuffer();
		temp.append(getId());
		temp.append(" (");
		temp.append(getValue());
		if(min !=0 && max !=0){
			temp.append(", min=");
			temp.append(getMin());
			temp.append(" max=" );
			temp.append(getMax());
		}
		temp.append(')');
		if(getCluster() != null){
			temp.append('[');
			temp.append(getCluster().getMedian());
			temp.append(']');
		}
		return temp.toString();
	}
	
//	public int hashCode()
//	{
//		String string = getId() + " " + getValeur(); // ON POURRAIT AMÉLIORER!
//		return string.hashCode();
//	}

	
	//------------ Pour clustering ----//
	public int getSequenceID() {
		return sequenceID;
	}

	public void setSequenceID(int sequenceID) {
		this.sequenceID = sequenceID;
	}
	
	public void setValue(double value) {
		this.value = value;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public void setMax(double max) {
		this.max = max;
	}
	
}
