package ca.pfv.spmf.associationrules.pasquier;

import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;

/**
 * Association rule
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
	private ItemsetApriori itemset1;
	private ItemsetApriori itemset2;
	private int transactioncount;
	private double confidence;
	
	public Rule(ItemsetApriori itemset1, ItemsetApriori itemset2, int transactionCount, double confidence){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.transactioncount =  transactionCount;
		this.confidence = confidence;
	}
	
	public void print(){
		itemset1.print();
		System.out.print(" ==> ");
		itemset2.print();
	}

	public double getRelativeSupport(int objectsCount) {
		return ((double)transactioncount) / ((double) objectsCount);
	}
	
	public int getAbsoluteSupport(){
		return transactioncount;
	}

	public double getConfidence() {
		return confidence;
	}

	public boolean isLexicallySmallerthan(Rule regle) {
		return itemset1.isLexicallySmallerthan(regle.itemset1);
	}
}
