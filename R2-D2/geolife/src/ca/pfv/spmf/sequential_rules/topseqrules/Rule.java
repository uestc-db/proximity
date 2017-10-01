package ca.pfv.spmf.sequential_rules.topseqrules;

import java.util.Map;
import java.util.Set;


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
public class Rule implements Comparable<Rule>{
	
	private int[] itemset1; // antecedent
	private int[] itemset2; // consequent
	public int transactioncount; // absolute support
	Set<Integer> tidsI;
	Set<Integer> tidsJ;
	Set<Integer> tidsIJ;
	Map<Integer, Short> occurencesIfirst;
	Map<Integer, Short> occurencesJlast;
	public boolean expandLR = false;

	private double confidence;
	
	public Rule(int[] itemset1, int[] itemset2, double confidence, int transactioncount, 
			Set<Integer> tidsI, Set<Integer> tidsJ, Set<Integer> tidsIJ, 
			Map<Integer, Short> occurencesIfirst,
			Map<Integer, Short> occurencesJlast){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.confidence = confidence;
		this.transactioncount = transactioncount;
		this.tidsI = tidsI;
		this.tidsJ = tidsJ;
		this.tidsIJ = tidsIJ;
		this.occurencesJlast = occurencesJlast;
		this.occurencesIfirst = occurencesIfirst;
	}

	public int[] getItemset1() {
		return itemset1;
	}

	public int[] getItemset2() {
		return itemset2;
	}
	
	public int getAbsoluteSupport(){
		return transactioncount;
	}
	
	public double getRelativeSupport(int sequencecount) {
		return ((double)transactioncount) / ((double) sequencecount);
	}

	public void print(){
		System.out.println(toString());
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for(int i=0; i< itemset1.length; i++){
			buffer.append(itemset1[i]);
			if(i != itemset1.length-1){
				buffer.append(",");
			}
		}
		buffer.append(" ==> ");
		for(int i=0; i< itemset2.length; i++){
			buffer.append(itemset2[i]);
			if(i != itemset2.length-1){
				buffer.append(",");
			}
		}
		return buffer.toString();
	}

	public double getConfidence() {
		return confidence;
	}
	
	public int compareTo(Rule o) {
		if(o == this){
			return 0;
		}
		int compare = this.getAbsoluteSupport() - o.getAbsoluteSupport();
		if(compare !=0){
			return compare;
		}
//		compare = (int)(this.confidence  - o.confidence);
//		if(compare !=0){
//			return compare;
//		}
		return this.hashCode() - o.hashCode();
	}
}
