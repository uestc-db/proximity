package ca.pfv.spmf.frequentpatterns.eclat_and_charm_saveToMemory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation of the CHARM-MFI algorithm (szathmary06) that is a
 * simple extension that take as input the ouptput of CHARM algorithm by Zaki.
 * But event if it is called Charm-MFI it could be used with AprioriClose or any
 * other algorithms for mining frequent closed itemsets.
 * 
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
 * 
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 * 
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
public class AlgoCharmMFI {

	protected Itemsets maximalItemsets;
	private long startTimestamp; // for stats
	private long endTimestamp; // for stats

	public AlgoCharmMFI() {
	}

	public Itemsets runAlgorithm(Itemsets frequentClosed) {
		startTimestamp = System.currentTimeMillis();
		this.maximalItemsets = frequentClosed;

		int maxItemsetLength = frequentClosed.getLevels().size();
		List<Itemset> t1 = frequentClosed.getLevels().get(1);
		for (int i = 1; i < maxItemsetLength - 1; i++) {
			List<Itemset> ti = frequentClosed.getLevels().get(i);
			List<Itemset> tip1 = frequentClosed.getLevels().get(i + 1);
			findMaximal(ti, tip1);
		}

		endTimestamp = System.currentTimeMillis();
		return maximalItemsets; // Return all frequent itemsets found!
	}

	private void findMaximal(List<Itemset> ti, List<Itemset> tip1) {
		for (Itemset sup : tip1) {
			List<Itemset> s = subset(ti, sup);
			for (Itemset sub : s) {
				sub.maximal = false;
			}
		}

	}

	// in a set of itemsets finds the subsets of a given itemset
	private List<Itemset> subset(List<Itemset> ti, Itemset sup) {
		List<Itemset> result = new ArrayList<Itemset>();
		for (Itemset itemset : ti) {
			if (sup.getItems().containsAll(itemset.getItems())) {
				result.add(itemset);
			}
		}
		return result;
	}

	public void printStats(int transactionCount) {
		System.out.println("=============  CHARM-MFI - STATS =============");
		long temps = endTimestamp - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ transactionCount);
		System.out.println(" Frequent itemsets count : "
				+ maximalItemsets.getItemsetsCount());
		maximalItemsets.printItemsets(transactionCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}

	public Itemsets getItemsets() {
		return maximalItemsets;
	}

}
