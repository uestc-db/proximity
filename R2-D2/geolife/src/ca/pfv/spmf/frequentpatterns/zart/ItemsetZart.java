package ca.pfv.spmf.frequentpatterns.zart;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * An itemset (a set of items).
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
public class ItemsetZart{
	private final List<Integer> items = new ArrayList<Integer>(); // ordonnée
	private int transactioncount = 0;
	
	public ItemsetZart(){
	}
	
	public double getRelativeSupport(int nbObject) {
		return ((double)transactioncount) / ((double) nbObject);
	}
	
	public ItemsetZart cloneItemSetMinusAnItemset(ItemsetZart itemsetToNotKeep){
		ItemsetZart itemset = new ItemsetZart();
		for(Integer item : items){
			if(!itemsetToNotKeep.contains(item)){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	
	public String getRelativeSupportAsString(int nbObject) {
		double frequence = ((double)transactioncount) / ((double) nbObject);
		// We create a DecimalFormat to format double value:
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(2); 

		// We return double as a String value
		return format.format(frequence);
	}
	
	public int getAbsoluteSupport(){
		return transactioncount;
	}

	public void increaseTransactionCount() { 
		transactioncount++;
	}
	
	public boolean includedIn(ItemsetZart itemset2) {
		return itemset2.items.containsAll(items);
	}
	
	public void addItem(Integer value){
			items.add(value);
	}
	
	public List<Integer> getItems(){
		return items;
	}
	
	public Integer get(int index){
		return items.get(index);
	}
	
	
	public void print(){
		System.out.print(toString());
	}
	
	public String toString(){
		StringBuffer r = new StringBuffer ();
		for(Integer attribute : items){
			r.append(attribute.toString());
			r.append(' ');
		}
		return r.toString();
	}

	public boolean contains(Integer item) {
		return items.contains(item);
	}

	
	// Pour l'algorithme Apriori
	public Integer shareFirstIItemsOnly(ItemsetZart itemset2){
		// Pourrait être optimisé... en sachant que les listes sont ordonnées.
		boolean shareIminus1= true;
		// ils doivent avoir les premiers i-1 éléments en commun
		for(int j=0; j< items.size()-1; j++){ 
			if(!itemset2.get(j).equals(get(j))){
				shareIminus1 = false;
				break;
			}
		}
		if(!shareIminus1){
			return null;
		}
		if( itemset2.items.get(items.size()-1).equals(items.get(items.size()-1))){ // le dernier élément doit être différent!
			return null;
		}
		if( items.get(items.size()-1) > itemset2.items.get(items.size()-1)){
			return null;
		}
		return itemset2.get(items.size()-1);
	}
	
	public boolean isEqualTo(ItemsetZart itemset2){
		if(items.size() != itemset2.items.size()){
			return false;
		}
		for(Integer val : items){
			if(!itemset2.contains(val)){
				return false;
			}
		}
		return true;
	}
	
	// pour Apriori
	public ItemsetZart cloneItemSetMinusOneItem(Integer itemsetToRemove){
		ItemsetZart itemset = new ItemsetZart();
		for(Integer item : items){
			if(!item.equals(itemsetToRemove)){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	
	public boolean contains(ItemsetZart item) {
		return items.contains(item);
	}

	public void setTransactioncount(int transactioncount) {
		this.transactioncount = transactioncount;
	}
	
	public int size(){
		return items.size();
	}
}
