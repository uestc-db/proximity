package ca.pfv.spmf.associationrules.TopKRules;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;

import ca.pfv.spmf.general.datastructures.redblacktree.RedBlackTree;

/**
 * An algorithm for mining the TOP-K association rules with a pattern growth
 * approach and several optimizations.
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
public class AlgoTopKRules {
	long timeStart = 0;
	long timeEnd = 0;

	double minConfidence;
	int minsuppRelative;
	int k = 0;
	Database database;

	BitSet[] tableItemTids; // id item, tids-set
	int[] tableItemCount; // id item, tids-set-size
	PriorityQueue<RuleG> kRules; // the top k rules found until now
	RedBlackTree<RuleG> candidates; // the candidates for expansion

	double maxMemory = 0;
	int maxCandidateCount = 0;

	public AlgoTopKRules() {
	}

	public void runAlgorithm(int k, double minConfidence, Database database) {
		maxMemory = 0;
		maxCandidateCount = 0;
		this.minConfidence = minConfidence;
		this.database = database;
		this.k = k;

		this.minsuppRelative = 1;

		tableItemTids = new BitSet[database.maxItem + 1]; // id item, count
		tableItemCount = new int[database.maxItem + 1];
		kRules = new PriorityQueue<RuleG>();
		candidates = new RedBlackTree<RuleG>();

		timeStart = System.currentTimeMillis(); // for stats
		scanDatabase(database);
		start(tableItemTids);
		timeEnd = System.currentTimeMillis(); // for stats
	}

	private void start(BitSet[] tableItemTids) {

		main: for (int itemI = 0; itemI <= database.maxItem; itemI++) {
			if (tableItemCount[itemI] < minsuppRelative) {
				continue main;
			}
			BitSet tidsI = tableItemTids[itemI];

			main2: for (int itemJ = itemI + 1; itemJ <= database.maxItem; itemJ++) {
				if (tableItemCount[itemJ] < minsuppRelative) {
					continue main2;
				}
				BitSet tidsJ = tableItemTids[itemJ];

				// (1) Build list of common tids
				BitSet commonTids = (BitSet) tidsI.clone();
				commonTids.and(tidsJ);
				int support = commonTids.cardinality();
				if (support >= minsuppRelative) {
					generateRuleSize11(itemI, tidsI, itemJ, tidsJ, commonTids,
							support);
				}
			}
		}
		// RECURSIVELY EXPAND ALL CANDIDATES, THE MOST PROMISING CANDIDATES
		// FIRST...
		while (candidates.size() > 0) {
			RuleG rule = candidates.popMaximum();
			// if there is no more candidates with enough support, then we stop
			if (rule.getAbsoluteSupport() < minsuppRelative) {
				// candidates.remove(rule);
				break;
			}
			if (rule.expandLR) {
				expandLR(rule);
			} else {
				expandR(rule);
			}
			// candidates.remove(rule);
		}
	}

	private void generateRuleSize11(Integer item1, BitSet tid1, Integer item2,
			BitSet tid2, BitSet commonTids, int cardinality) {
		Integer[] itemset1 = new Integer[1];
		itemset1[0] = item1;
		Integer[] itemset2 = new Integer[1];
		itemset2[0] = item2;

		double confidenceIJ = ((double) cardinality) / (tableItemCount[item1]);
		RuleG ruleLR = new RuleG(itemset1, itemset2, cardinality, tid1,
				commonTids, item1, item2);

		// if rule i->j has minimum confidence
		if (confidenceIJ >= minConfidence) {
			save(ruleLR, cardinality);
		}
		registerAsCandidate(true, ruleLR);

		// if rule j->i has minimum confidence
		double confidenceJI = ((double) cardinality) / (tableItemCount[item2]);
		RuleG ruleRL = new RuleG(itemset2, itemset1, cardinality, tid2,
				commonTids, item2, item1);
		if (confidenceJI >= minConfidence) {
			save(ruleRL, cardinality);
		}
		registerAsCandidate(true, ruleRL);

	}

	private void registerAsCandidate(boolean expandLR, RuleG ruleLR) {
		ruleLR.expandLR = expandLR;
		candidates.add(ruleLR);

		if (candidates.size() >= maxCandidateCount) {
			maxCandidateCount = candidates.size();
		}
		checkMemory();
	}

	private void expandLR(RuleG ruleG) {
		Map<Integer, BitSet> mapCountLeft = new HashMap<Integer, BitSet>();
		Map<Integer, BitSet> mapCountRight = new HashMap<Integer, BitSet>();

		for (int tid = ruleG.common.nextSetBit(0); tid >= 0; tid = ruleG.common
				.nextSetBit(tid + 1)) {
			Iterator<Integer> iter = database.getTransactions().get(tid)
					.getItems().iterator();
			while (iter.hasNext()) {
				Integer item = iter.next();
				// CAN DO THIS BECAUSE TRANSACTIONS ARE SORTED BY DESCENDING
				// ITEM IDS (see Database.Java)
				if (item < ruleG.maxLeft && item < ruleG.maxRight) { //
					break;
				}
				if (tableItemCount[item] < minsuppRelative) {
					iter.remove();
					continue;
				}
				if (item > ruleG.maxLeft
						&& !containsLEX(ruleG.getItemset2(), item,
								ruleG.maxRight)) {
					BitSet tidsItem = mapCountLeft.get(item);
					if (tidsItem == null) {
						tidsItem = new BitSet();
						mapCountLeft.put(item, tidsItem);
					}
					tidsItem.set(tid);
				}
				if (item > ruleG.maxRight
						&& !containsLEX(ruleG.getItemset1(), item,
								ruleG.maxLeft)) {
					BitSet tidsItem = mapCountRight.get(item);
					if (tidsItem == null) {
						tidsItem = new BitSet();
						mapCountRight.put(item, tidsItem);
					}
					tidsItem.set(tid);
				}
			}
		}

		// for each item c found, we create each a rule
		for (Entry<Integer, BitSet> entry : mapCountRight.entrySet()) {
			BitSet tidsRule = entry.getValue();
			int ruleSupport = tidsRule.cardinality();

			if (ruleSupport >= minsuppRelative) {
				Integer itemC = entry.getKey();

				// create new right part of rule
				Integer[] newRightItemset = new Integer[ruleG.getItemset2().length + 1];
				System.arraycopy(ruleG.getItemset2(), 0, newRightItemset, 0,
						ruleG.getItemset2().length);
				newRightItemset[ruleG.getItemset2().length] = itemC;

				int maxRight = (itemC >= ruleG.maxRight) ? itemC
						: ruleG.maxRight;

				double confidence = ((double) ruleSupport)
						/ ruleG.tids1.cardinality();
				RuleG candidate = new RuleG(ruleG.getItemset1(),
						newRightItemset, ruleSupport, ruleG.tids1, tidsRule,
						ruleG.maxLeft, maxRight);

				if (confidence >= minConfidence) {
					save(candidate, ruleSupport);
				}
				registerAsCandidate(false, candidate);
			}
		}

		// for each item c found, we create each a rule
		for (Entry<Integer, BitSet> entry : mapCountLeft.entrySet()) {
			BitSet tidsRule = entry.getValue();
			int ruleSupport = tidsRule.cardinality();

			if (ruleSupport >= minsuppRelative) {
				Integer itemC = entry.getKey();

				// The tidset of the left itemset is calculated
				BitSet tidsLeft = (BitSet) ruleG.tids1.clone();
				tidsLeft.and(tableItemTids[itemC]);

				// create new left part of rule
				Integer[] newLeftItemset = new Integer[ruleG.getItemset1().length + 1];
				System.arraycopy(ruleG.getItemset1(), 0, newLeftItemset, 0,
						ruleG.getItemset1().length);
				newLeftItemset[ruleG.getItemset1().length] = itemC;

				int maxLeft = itemC >= ruleG.maxLeft ? itemC : ruleG.maxLeft;

				double confidence = ((double) ruleSupport)
						/ tidsLeft.cardinality();
				RuleG candidate = new RuleG(newLeftItemset,
						ruleG.getItemset2(), ruleSupport, tidsLeft, tidsRule,
						maxLeft, ruleG.maxRight);

				if (confidence >= minConfidence) {
					save(candidate, ruleSupport);
				}
				registerAsCandidate(true, candidate);
			}
		}
	}

	private void expandR(RuleG ruleG) {
		Map<Integer, BitSet> mapCountRight = new HashMap<Integer, BitSet>();

		for (int tid = ruleG.common.nextSetBit(0); tid >= 0; tid = ruleG.common
				.nextSetBit(tid + 1)) {
			Iterator<Integer> iter = database.getTransactions().get(tid)
					.getItems().iterator();
			while (iter.hasNext()) {
				Integer item = iter.next();
				if (tableItemCount[item] < minsuppRelative) {
					iter.remove();
					continue;
				}
				// CAN DO THIS BECAUSE TRANSACTIONS ARE SORTED BY DESCENDING
				// ITEM IDS (see Database.Java)
				if (item < ruleG.maxRight) {
					break;
				}

				if (item > ruleG.maxRight
						&& !containsLEX(ruleG.getItemset1(), item,
								ruleG.maxLeft)) {
					BitSet tidsItem = mapCountRight.get(item);
					if (tidsItem == null) {
						tidsItem = new BitSet();
						mapCountRight.put(item, tidsItem);
					}
					tidsItem.set(tid);
				}
			}
		}

		// for each item c found, we create a rule
		for (Entry<Integer, BitSet> entry : mapCountRight.entrySet()) {
			BitSet tidsRule = entry.getValue();
			int ruleSupport = tidsRule.cardinality();

			if (ruleSupport >= minsuppRelative) {
				Integer itemC = entry.getKey();

				// create new right part of rule
				Integer[] newRightItemset = new Integer[ruleG.getItemset2().length + 1];
				System.arraycopy(ruleG.getItemset2(), 0, newRightItemset, 0,
						ruleG.getItemset2().length);
				newRightItemset[ruleG.getItemset2().length] = itemC;

				int maxRight = itemC >= ruleG.maxRight ? itemC : ruleG.maxRight;

				double confidence = ((double) ruleSupport)
						/ ruleG.tids1.cardinality();
				RuleG candidate = new RuleG(ruleG.getItemset1(),
						newRightItemset, ruleSupport, ruleG.tids1, tidsRule,
						ruleG.maxLeft, maxRight);

				if (confidence >= minConfidence) {
					save(candidate, ruleSupport);
				}
				registerAsCandidate(false, candidate); // IMPORTANT: WAS MISSING
														// IN PREVIOUS VERSION
														// !!!!
			}
		}
	}

	private void save(RuleG rule, int support) {
		kRules.add(rule);
		if (kRules.size() > k) {
			if (support > this.minsuppRelative) {
				RuleG lower;
				do {
					kRules.poll();
				} while (kRules.size() > k);
			}
			this.minsuppRelative = kRules.peek().getAbsoluteSupport();
		}
	}

	/**
	 * This method checks if the item "item" is in the itemset. It asumes that
	 * items in the itemset are sorted in lexical order
	 * 
	 * @param item
	 * @param maxItemInArray
	 * @return
	 */
	public boolean containsLEX(Integer array[], Integer item, int maxItemInArray) {
		if (item > maxItemInArray) {
			return false;
		}
		for (Integer itemI : array) {
			if (itemI.equals(item)) {
				return true;
			} else if (itemI > item) {
				return false; // <-- xxxx
			}
		}
		return false;
	}

	private void scanDatabase(Database database) {
		// (1) count the support of each item in the database in one database
		// pass
		for (int j = 0; j < database.getTransactions().size(); j++) {
			Transaction transaction = database.getTransactions().get(j);
			for (Integer item : transaction.getItems()) {
				BitSet ids = tableItemTids[item];
				if (ids == null) {
					tableItemTids[item] = new BitSet(database.tidsCount);
				}
				tableItemTids[item].set(j);
				tableItemCount[item] = tableItemCount[item] + 1;
			}
		}
	}

	private void checkMemory() {
		double currentMemory = ((double) ((double) (Runtime.getRuntime()
				.totalMemory() / 1024) / 1024))
				- ((double) ((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
		if (currentMemory > maxMemory) {
			maxMemory = currentMemory;
		}
	}

	public void printStats() {
		System.out.println("=============  TOP-K RULES - STATS =============");
		System.out.println("Minsup : " + minsuppRelative);
		System.out.println("Rules count: " + kRules.size());
		System.out.println("Memory : " + maxMemory + " mb");
		System.out.println("Total time : " + (timeEnd - timeStart) + " ms");
		System.out
				.println("===================================================");
	}

	public void writeResultTofile(String path) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		Iterator<RuleG> iter = kRules.iterator();
		while (iter.hasNext()) {
			RuleG rule = (RuleG) iter.next();
			StringBuffer buffer = new StringBuffer();
			buffer.append(rule.toString());
			// write separator
			buffer.append("  sup= ");
			// write support
			buffer.append(rule.getAbsoluteSupport());
			// write separator
			buffer.append("  conf= ");
			// write confidence
			buffer.append(rule.getConfidence());
			writer.write(buffer.toString());
			writer.newLine();
		}

		writer.close();
	}
}
