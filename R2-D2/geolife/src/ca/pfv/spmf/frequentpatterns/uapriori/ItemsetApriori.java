package ca.pfv.spmf.frequentpatterns.uapriori;
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
	private List<ItemApriori> items = new ArrayList<ItemApriori>(); // ordered
	private double expectedsupport = 0;
	
	public ItemsetApriori(){
		
	}

	public double getExpectedSupport() {
		return expectedsupport;
	}
	
	public String getSupport() {
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(2); 
		return format.format(expectedsupport);
	}
	
	public void increaseTransactionCount(double supp) { 
		expectedsupport += supp;
	}
	
	public void addItem(ItemApriori value){
			items.add(value);
	}
	
	public List<ItemApriori> getItems(){
		return items;
	}
	
	public ItemApriori get(int index){
		return items.get(index);
	}
	
	public void print(){
		System.out.print(toString());
	}
	
	public void printWithoutSupport(){
		StringBuffer r = new StringBuffer ();
		for(ItemApriori attribute : items){
			r.append(attribute.getId());
			r.append(' ');
		}
		System.out.print(r);
	}
	
	
	public String toString(){
		StringBuffer r = new StringBuffer ();
		for(ItemApriori attribute : items){
			r.append(attribute.toString());
			r.append(' ');
		}
		return r.toString();
	}

	public boolean contains(ItemApriori item) {
		return items.contains(item);
	}

	
	public boolean isLexicallySmallerthan(ItemsetApriori itemset2){
		for(int i=0; i< items.size(); i++){
			if(items.get(i).getId() > itemset2.items.get(i).getId()){
				return false;
			}
			else if(items.get(i).getId() < itemset2.items.get(i).getId()){
				return true;
			}
		}
		return true;
	}
	
	
	public boolean isEqualTo(ItemsetApriori itemset2){
		if(items.size() != itemset2.items.size()){
			return false;
		}
		for(ItemApriori val : items){
			if(!itemset2.contains(val)){
				return false;
			}
		}
		return true;
	}


	public void setExpectedSupport(double expectedsupport) {
		this.expectedsupport = expectedsupport;
	}

	// pour Apriori
	public ItemsetApriori cloneItemSetMinusOneItem(ItemApriori itemsetToRemove){
		ItemsetApriori itemset = new ItemsetApriori();
		for(ItemApriori item : items){
			if(!item.equals(itemsetToRemove)){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	
	public ItemsetApriori cloneItemSetMinusAnItemset(ItemsetApriori itemsetToNotKeep){
		ItemsetApriori itemset = new ItemsetApriori();
		for(ItemApriori item : items){
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
	public ItemApriori allTheSameExceptLastItem(ItemsetApriori itemset2) {
		if(itemset2.size() != items.size()){
			return null;
		}
		for(int i=0; i< items.size(); i++){
			// if they are the last items
			if(i == items.size()-1){ 
				// the one from items should be smaller (lexical order) and different than the one of itemset2
				if(items.get(i).getId() >= itemset2.get(i).getId()){  
					return null;
				}
			}
			// if they are not the last items, they  should be different
			else if(items.get(i).getId() != itemset2.get(i).getId()){ 
				return null; 
			}
		}
		return itemset2.get(itemset2.size()-1);
	}

	public void setItems(List<ItemApriori> items) {
		this.items = items;
	}
}
