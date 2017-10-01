package ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.sequentialpatterns.ItemValued;


/**
* This class represents a Cluster.
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
	private List<ItemValued> items;
	private double median;
	private double higher =0;
	private double lower = Double.MAX_VALUE;
	
	private double sum = 0; // FOR MEAN
	
	private Set<Integer> sequenceIDs = null;
	
	//Constructor of empty cluster with a specified median (for  K-means)
	public Cluster(double median){
		this.items = new ArrayList<ItemValued>();
		this.median = median;
	}
	
	public Cluster(List<ItemValued> newItems){
		this.items = new ArrayList<ItemValued>(newItems);
		calculateMedian();
	}
	
	public Cluster(List<ItemValued> newItems, List<ItemValued> newItems2){
		this.items = new ArrayList<ItemValued>(newItems);
		items.addAll(newItems2);
		calculateMedian();
	}
	
	public Cluster(ItemValued item){
		this.items = new ArrayList<ItemValued>();
		this.items.add(item);
		sum+= item.getValue();
		// calculate median
		this.median = item.getValue();
	}
	
	
	// To perform merges
	public void addItemsFromCluster(Cluster cluster2){
		for(ItemValued item : cluster2.getItems()){
			getItems().add(item);
			sum+= item.getValue();
		}
	}
	
	public void addItem(ItemValued item) {
		getItems().add(item);
		sum += item.getValue();
	}
	
	public void addItems(List<ItemValued> newItems) {
		for(ItemValued item : newItems){
			this.getItems().add(item);
			sum += item.getValue();
		}
	}

	public List<ItemValued> getItems() {
		return items;
	}
	
	public int size(){
		return getItems().size();
	}

	public double getMedian() {
		return median;
	}
	
	public String toString(){
		String retour = "(";
		for(ItemValued item : getItems()){
			retour = retour + item.getValue() + " ";
		}
		retour = retour + ")      <" + median + ", min=" + getLower() + " max=" + getHigher() + ">";
		return retour;
	}
	
	
	private void calculateMedian() {
		if(getItems().isEmpty()){
			return;
		}
		
		if(getItems().size() ==1){
			median = getItems().get(0).getValue();
			return;
		}
		// VERSION USING THE AVERAGE
		median = sum /((double)items.size());

		
		// VERSION USING THE MEDIAN VALUE 
//		median = 0;
//		// sort the list
//		Collections.sort(getItems(), new Comparator<ItemValued>(){
//			public int compare(ItemValued obj1, ItemValued obj2)
//            {
//                    return (int) (obj1.getValue() - obj2.getValue());
//            }
//		});
//		
//		// calculate median 
//		if((getItems().size() % 2) == 0){ // if even
//			int index1 = (getItems().size() / 2); // upper
//			int index2 = (getItems().size() / 2) - 1; // lower
//			double somme = getItems().get(index1).getValue() + getItems().get(index2).getValue();
//			median = somme /2;
//		}
//		else{ // if odd
//			int index1 = (int)Math.floor(items.size() / 2);
//			median = items.get(index1).getValue(); // middle
//		}
	}

	public void recomputeClusterMedian() {
		calculateMedian();
	}
	
	public void computeHigherAndLower(){
		for(ItemValued item : getItems()){
			if(item.getValue() > higher){
				higher = item.getValue();
			}
			if(item.getValue() < lower){
				lower = item.getValue();
			}
		}
	}

	public boolean containsItem(ItemValued item2) {
		for(ItemValued item : getItems()){
			if(item == item2){
				return true;
			}
		}
		return false;
	}

	public double getHigher() {
		return higher;
	}

	public double getLower() {
		return lower;
	}

	public int getItemId() {
		return getItems().get(0).getId();
	}

	public Set<Integer> getSequenceIDs() {
		return sequenceIDs;
	}

	public void setSequenceIDs(Set<Integer> sequenceIDs) {
		this.sequenceIDs = sequenceIDs;
	}
}
