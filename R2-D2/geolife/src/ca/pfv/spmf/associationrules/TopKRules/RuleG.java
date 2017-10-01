package ca.pfv.spmf.associationrules.TopKRules;

import java.util.BitSet;

/**
 * This class is for representing a sequential rule.
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
public class RuleG implements Comparable<RuleG>{

	private Integer[] itemset1; // antecedent
	private Integer[] itemset2; // consequent
	public BitSet    tids1;
	public BitSet    common;
	public int maxLeft;
	public int maxRight;
	public boolean expandLR = false;
	
	private int count; // absolute support
		
	public RuleG(Integer[] itemset1, Integer[] itemset2, int count, BitSet tids1, BitSet common, int maxLeft, int maxRight){
		this.count = count;
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.common =  common;
		this.tids1 = tids1;
		this.maxLeft= maxLeft;
		this.maxRight= maxRight;
	}

	public Integer[] getItemset1() {
		return itemset1;
	}

	public Integer[] getItemset2() {
		return itemset2;
	}
	
	public int getAbsoluteSupport(){
		return count;
	}

	public double getConfidence() {
		return ((double)count) / tids1.cardinality();
	}

	public int compareTo(RuleG o) {
		if(o == this){
			return 0;
		}
		int compare = this.getAbsoluteSupport() - o.getAbsoluteSupport();
		if(compare !=0){
			return compare;
		}
		return this.hashCode() - o.hashCode();
	}
	
	public String toString(){
		return toString(itemset1) +  " ==> " + toString(itemset2);
	}

	private String toString(Integer[] itemset) {
		StringBuffer temp = new StringBuffer();
		for(int item : itemset){
			temp.append(item + ",");
		}
		return temp.toString();
	}
}
