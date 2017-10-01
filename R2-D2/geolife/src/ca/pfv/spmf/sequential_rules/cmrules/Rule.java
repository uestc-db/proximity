package ca.pfv.spmf.sequential_rules.cmrules;

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
public class Rule {
	private Itemset itemset1; // antecedent

	private Itemset itemset2; // consequent
	private int transactionCount; // absolute support
	private double confidence;
	int sequentialTransactionCount;
	
	public Rule(Itemset itemset1, Itemset itemset2, int transactionCount, double confidence){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.transactionCount =  transactionCount;
		this.confidence = confidence;
	}
	
	public Rule(Rule rule	) {
		itemset1 = rule.getItemset1();
		itemset2 = rule.getItemset2();
		confidence = rule.getConfidence();
		this.transactionCount =  rule.getAbsoluteSupport();
	}

	public Itemset getItemset1() {
		return itemset1;
	}

	public Itemset getItemset2() {
		return itemset2;
	}
	
	public double getCausality() {
		return ((double)sequentialTransactionCount) / ((double) transactionCount);
	}
	
	public double getRelativeSupport(int objectCount) {
		return ((double)transactionCount) / ((double) objectCount);
	}
	
	public int getAbsoluteSupport(){
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
	
	public int getAbsoluteSeqSupport() {
		return sequentialTransactionCount;
	}

	public double getSequentialSupport(int objectCount) {
		return ((double)sequentialTransactionCount) / ((double) objectCount);
	}

	public double getSequentialConfidence() {
		return ((double)sequentialTransactionCount) / ((double) itemset1.getAbsoluteSupport());
	}

	public void incrementTransactionCount() {
		transactionCount++;
	}

}
