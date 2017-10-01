package ca.pfv.spmf.sequential_rules.cmrules;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is not currently used.
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
	
	private int size;
	private List<Itemset>[] table;
	
	public HashTable(int size){
		this.size = size;
		table = new ArrayList[size];
	}
	public boolean containsSupersetOf(Itemset itemset) {
		int hashcode = hashCode(itemset);
		if(table[hashcode] ==  null){
			return false;
		}
		for(Object object : table[hashcode]){
			Itemset itemset2 = (Itemset)object;
			if(itemset2.getItems().contains(itemset.getItems())){
				return true;
			}
		}
		return false;
	}
	public void put(Itemset itemset) {
		int hashcode = hashCode(itemset);
		if(table[hashcode] ==  null){
			table[hashcode] = new ArrayList<Itemset>();
		}
		table[hashcode].add(itemset);
	}
	
	public int hashCode(Itemset itemset){
		int hashcode =0;
		for(Integer tid : itemset.getTransactionsIds()){
			hashcode += tid;
		}
		return (hashcode % size);
	}

}
