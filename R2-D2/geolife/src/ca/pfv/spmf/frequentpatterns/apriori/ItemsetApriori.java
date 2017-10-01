package ca.pfv.spmf.frequentpatterns.apriori;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an itemset (a set of items)
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
public class ItemsetApriori{
	private List<Integer> items = new ArrayList<Integer>(); // ordered
	private int transactioncount = 0;
	
	private boolean close = false;
	private boolean pseudoclose = false;
	private ItemsetApriori closure = null;
	
	public ItemsetApriori(){
	}

	public double getRelativeSupport(int nbObject) {
		return ((double)transactioncount) / ((double) nbObject);
	}
	
	public String getSupportRelatifFormatted(int nbObject) {
		double frequence = ((double)transactioncount) / ((double) nbObject);
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(2); 
		return format.format(frequence);
	}
	
	public int getAbsoluteSupport(){
		return transactioncount;
	}

	// APRIORI
	public void increaseTransactionCount() { 
		transactioncount++;
	}
	
	public boolean includedIn(ItemsetApriori itemset2) {
		return itemset2.items.containsAll(items);
	}
	
	public void addItem(Integer value){
			items.add(value);
	}
	
	public void addItemOrderedWithNoDuplicate(Integer value){
		for(int i=0; i< items.size(); i++){
			if(value == items.get(i) ){
				return; // already there!
			}
			if(value < items.get(i)){
				if(i ==0){
					items.add(0,value);
				}else{
					items.add(i,value);
				}
				return;
			}
		}
		addItem(value);
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
	
//	public boolean containsAll(ItemsetApriori itemset) {
//		for(Integer item : itemset.getItems()){
//			if(!contains(item)){
//				return false;
//			}
//		}
//		return true;
//	}
	
	public boolean isLexicallySmallerthan(ItemsetApriori itemset2){
		for(int i=0; i< items.size(); i++){
			if(items.get(i) > itemset2.items.get(i)){
				return false;
			}
			else if(items.get(i) < itemset2.items.get(i)){
				return true;
			}
		}
		return true;
	}
	
//	// Pour l'algorithme Apriori
//	public Integer shareFirstIItemsOnly(ItemsetApriori itemset2){
//		// Pourrait être optimisé... en sachant que les listes sont ordonnées.
//		boolean shareIminus1= true;
//		// ils doivent avoir les premiers i-1 éléments en commun
//		for(int j=0; j< items.size()-1; j++){ 
//			if(!itemset2.get(j).equals(get(j))){
//				shareIminus1 = false;
//				break;
//			}
//		}
//		if(!shareIminus1){
//			return null;
//		}
//		if(itemset2.items.get(items.size()-1).equals(items.get(items.size()-1))){ // le dernier élément doit être différent!
//			return null;
//		}
//		if(items.get(items.size()-1) > itemset2.items.get(items.size()-1)){
//			return null;
//		}
//		return itemset2.get(items.size()-1);
//	}
	
//	// For Apriori
//	public Integer haveOneItemDifferent(ItemsetApriori itemset2){
//		Integer missingFromItemset2 = null;
//		for(Integer item : items){
//			if(!itemset2.contains(item)){
//				if(missingFromItemset2 != null){
//					return null;  // more than one item is different
//				}else{
//					missingFromItemset2 = item;
//				}
//			}
//		}
//		return missingFromItemset2;
//	}
	
	public boolean isEqualTo(ItemsetApriori itemset2){
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


	public void setTransactioncount(int transactioncount) {
		this.transactioncount = transactioncount;
	}

	public boolean isClose() {
		return close;
	}

	public void setClose(boolean close) {
		this.close = close;
		this.closure = this;
	}

	public boolean isPseudoclose() {
		return pseudoclose;
	}

	public void setPseudoclose(boolean pseudoclose) {
		this.pseudoclose = pseudoclose;
	}

	public ItemsetApriori getClosure() {
		return closure;
	}

	public void setClosure(ItemsetApriori closure) {
		this.closure = closure;
	}

	// pour Apriori
	public ItemsetApriori cloneItemSetMinusOneItem(Integer itemsetToRemove){
		ItemsetApriori itemset = new ItemsetApriori();
		for(Integer item : items){
			if(!item.equals(itemsetToRemove)){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	
	public ItemsetApriori cloneItemSetMinusAnItemset(ItemsetApriori itemsetToNotKeep){
		ItemsetApriori itemset = new ItemsetApriori();
		for(Integer item : items){
			if(!itemsetToNotKeep.contains(item)){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	
	public int size(){
		return items.size();
	}

	/** 
	* check if the item from this itemset are all the same as those of itemset2 
	* except the last item 
	* and that itemset2 is lexically smaller than this itemset. If all these conditions are satisfied,
	* this method return the last item of itemset2. Otherwise it returns null.
	* @return the last item of itemset2, or null.
	* */
	public Integer allTheSameExceptLastItem(ItemsetApriori itemset2) {
		if(itemset2.size() != items.size()){
			return null;
		}
		for(int i=0; i< items.size(); i++){
			// if they are the last items
			if(i == items.size()-1){ 
				// the one from items should be smaller (lexical order) and different than the one of itemset2
				if(items.get(i) >= itemset2.get(i)){  
					return null;
				}
			}
			// if they are not the last items, they  should be different
			else if(items.get(i) != itemset2.get(i)){ 
				return null; 
			}
		}
		return itemset2.get(itemset2.size()-1);
	}
	
	/**
	 * This method compare this itemset with another itemset to see if they are equal.
	 * The method assume that the two itemsets are lexically ordered.
	 * @return  true or false
	 */
	public boolean allTheSame(ItemsetApriori itemset2) {
		if(itemset2.size() != items.size()){
			return false;
		}
		for(int i=0; i< itemset2.size(); i++){
			if(itemset2.getItems().get(i) != getItems().get(i)){
				return false;
			}
		}
		return true;
	}
	
	// used by the CloStream algorithm
	public ItemsetApriori intersection(ItemsetApriori itemset2){
		ItemsetApriori intersection = new ItemsetApriori();
		for(Integer item : items){
			if(itemset2.contains(item)){
				intersection.addItem(item);
			}
		}
		return intersection;
	}
	
	/**
	 * for Eclat/charm
	 * Could be optimized if we used set instead of a list of items.
	 */
	public ItemsetApriori union(ItemsetApriori itemset) {
//		ItemsetApriori union = new ItemsetApriori(); 
//		for(Integer item : items){
//			union.addItem(item);
//		}
//		union.addItem(itemset.get(itemset.size()-1));
//		return union;
		
		ItemsetApriori union = new ItemsetApriori(); 
		union.getItems().addAll(items);
		for(Integer item : itemset.getItems()){
			if(items.contains(item) == false){
				union.addItem(item);
			}
		}
		
		return union;
	}

	public void setItems(List<Integer> items) {
		this.items = items;
	}
	



}
