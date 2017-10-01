package ca.pfv.spmf.frequentpatterns.itemsettree;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a hash table
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
public class HashTable {
	
	int size;
	public List<Itemset>[] table;
	
	public HashTable(int size){
		this.size = size;
		table = new ArrayList[size];
	}
	

	public void put(int[] items, int support) {
		if(items[0] == 4 && items[1] == 5){
			System.out.println();
		}
		int hashcode = hashCode(items);
		if(table[hashcode] ==  null){
			table[hashcode] = new ArrayList<Itemset>();
			Itemset itemset = new Itemset();
			itemset.itemset = items;
			itemset.support = support;
			table[hashcode].add(itemset);
		}else{
			for(Itemset existingItemset : table[hashcode]){
				if(same(items, existingItemset.itemset)){
					existingItemset.support += support; //update the support
					return;
				}
			}
			Itemset itemset = new Itemset();
			itemset.itemset = items;
			itemset.support = support;
			table[hashcode].add(itemset);
		}
	}

	
	public int hashCode(int[] items){
		int hashcode =0;

		for (int i=0; i< items.length; i++) {
			hashcode += (items[i] + (i*10));
	    }
		return (hashcode % size);
	}
	
	private boolean same(int[] itemset1, int[] itemset2) {
		if(itemset2 == null || itemset1 == null){
			return false;
		}		
		
		if(itemset1.length != itemset2.length){
			return false;
		}
		for(int i=0; i< itemset1.length; i++){
			if(itemset1[i] != itemset2[i]){
				return false;
			}
		}
		return true;
	}

}
