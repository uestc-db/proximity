package ca.pfv.spmf.associationrules.agrawal_Apriori_version;

import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;

/**
 * This class is for representing an association rule. An association rule has an antecedent, a consequent,
 * a support and a confidence.

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

public class RuleAgrawal {
	private ItemsetApriori itemset1; // antecedent
	private ItemsetApriori itemset2; // consequent
	private int transactionCount; // absolute support
	private double confidence;
	
	public RuleAgrawal(ItemsetApriori itemset1, ItemsetApriori itemset2, int transactionCount, double confidence){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.transactionCount =  transactionCount;
		this.confidence = confidence;
	}
	
	public double getRelativeSupport(int objectCount) {
		return ((double)transactionCount) / ((double) objectCount);
	}
	
	public int getSupportAbsolu(){
		return transactionCount;
	}

	public double getConfidence() {
		return confidence;
	}
	
	public void print(){
		System.out.println(toString());
	}
	
	public String toString(){
		return itemset1.toString() +  " ==> " + itemset2.toString();
	}

	public ItemsetApriori getItemset1() {
		return itemset1;
	}

	public ItemsetApriori getItemset2() {
		return itemset2;
	}


}
