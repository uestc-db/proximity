package ca.pfv.spmf.sequential_rules.cmdeogun;

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
	private int transactioncount; // absolute support
	
	public Rule(Itemset itemset1, Itemset itemset2){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
	}

	public Itemset getItemset1() {
		return itemset1;
	}

	public Itemset getItemset2() {
		return itemset2;
	}
	
	public double getRelativeSupport(int sequencecount) {
		return ((double)transactioncount) / ((double) sequencecount);
	}
	
	public int getSupportAbsolu(){
		return transactioncount;
	}

	public double getConfidence() {
		return ((double)transactioncount) / ((double) itemset1.getAbsoluteSupport());
	}
	
	public void print(){
		System.out.println(toString());
	}
	
	public String toString(){
		return itemset1.toString() +  " ==> " + itemset2.toString();
	}

	public void incrementTransactionCount() {
		this.transactioncount++;
	}

	public int getTransactionCount() {
		return transactioncount;
	}

	public void setTransactioncount(int transactioncount) {
		this.transactioncount = transactioncount;
	}
}
