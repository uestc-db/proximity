package ca.pfv.spmf.highutility.two_phase_algorithm;
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
public class Transaction{
	private final List<Integer> items; // ordered
	private final List<Integer> itemsUtilities; // ordered
	private final int transactionUtility;
	
	public Transaction(List<Integer> items, List<Integer> itemsUtilities, int transactionUtility){
		this.items =  items;
		this.itemsUtilities = itemsUtilities;
		this.transactionUtility = transactionUtility;
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
		for(int i=0; i< items.size(); i++){
			r.append(items.get(i) + " ");
			if(i == items.size() -1){
				r.append(":");
			}
		}
		r.append(transactionUtility + ": ");
		for(int i=0; i< itemsUtilities.size(); i++){
			r.append(itemsUtilities.get(i) + " ");
		}

		return r.toString();
	}

	public boolean contains(Integer item) {
		for(Integer itemI : items){
			if(itemI == item){
				return true;
			}else if(itemI > item){
				return false;
			}
		}
		return false;
	}
	
	public boolean contains(int item) {
		for(int i=0; i<items.size(); i++){
			if(items.get(i) == item){
				return true;
			}else if(items.get(i) > item){
				return false;
			}
		}
		return false;
	}

	
	public boolean isEqualTo(Transaction itemset2){
		if(items.size() != itemset2.items.size()){
			return false;
		}
		return items.containsAll(itemset2.items);
	}

	
	public int size(){
		return items.size();
	}


	public List<Integer> getItemsUtilities() {
		return itemsUtilities;
	}

	public int getTransactionUtility() {
		return transactionUtility;
	}

}
