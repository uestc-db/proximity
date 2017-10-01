package ca.pfv.spmf.frequentpatterns.uapriori;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * This is an implementation of the U-Apriori algorithm as described by :
 * 
 *   Chui, C., Kao, B., Hung, E. (2007), Mining Frequent Itemsets fomr Uncertain Data, PAKDD 2007,  pp 47-58.
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
public class AlgoUApriori_saveToFile {

	protected ContextApriori context;
	protected int k; // level

	// stats
	protected int totalCandidateCount = 0;
	protected int databaseScanCount = 0;
	protected long startTimestamp;
	protected long endTimestamp;
	private int itemsetCount;
	
	// write to file
	BufferedWriter writer = null;	
	
	public AlgoUApriori_saveToFile(ContextApriori context) {
		this.context = context;
	}

	public void runAlgorithm(double minsupp, String output) throws IOException {
		startTimestamp = System.currentTimeMillis();
		totalCandidateCount = 0;
		databaseScanCount = 0;
		writer = new BufferedWriter(new FileWriter(output)); 
		itemsetCount=0;
		
		// Generate candidates with size k = 1 (all itemsets of size 1)
		k=1;
		Set<ItemsetApriori> candidatesSize1 = generateCandidateSize1();
		totalCandidateCount+=candidatesSize1.size();

		calculateSupportForEachCandidate(candidatesSize1);
		
//		addEmptySetToLevel0IfNecessary(candidatesSize1);

		// To build level 1, we keep only the frequent candidates.
		// We scan the database one time to calculate the support of each candidate.
		Set<ItemsetApriori> level = createLevelWithFrequentCandidates(minsupp,
				candidatesSize1);

		k = 2;
		// While the level is not empty
		while (!level.isEmpty()  ) {
			// Generate candidates of size K
			Set<ItemsetApriori> candidatesK = generateCandidateSizeK(level);
			totalCandidateCount+=candidatesK.size();

			// We scan the database one time to calculate the support
			// of each candidates.
			calculateSupportForEachCandidate(candidatesK);

			// We build the level k+1 with all the candidates that have
			// a support higher than the minsup threshold.
			Set<ItemsetApriori> levelK = createLevelWithFrequentCandidates(
					minsupp, candidatesK);
			level = levelK; // We keep only the last level... 
			k++;
		}
		writer.close();
		endTimestamp = System.currentTimeMillis();
	}

	public void saveItemsetToFile(ItemsetApriori itemset) throws IOException{
		writer.write(itemset.toString() + " Support: " + itemset.getExpectedSupport());
		writer.newLine();
		itemsetCount++;
	}
	
	protected Set<ItemsetApriori> createLevelWithFrequentCandidates(double minsupp,Set<ItemsetApriori> candidatesK) throws IOException {
		Set<ItemsetApriori> levelK = new HashSet<ItemsetApriori>();
		for (ItemsetApriori candidate : candidatesK) { 
			if (candidate.getExpectedSupport() >= minsupp) {
				levelK.add(candidate);
				saveItemsetToFile(candidate);
			}
		}
		return levelK;
	}

	protected void calculateSupportForEachCandidate(
			Set<ItemsetApriori> candidatesK) {
		databaseScanCount++;
		for (ItemsetApriori transaction : context.getObjects()) {
			// For each candidate of level K, we increase its support
			// if it is included in the transaction.
		candidateLoop : for (ItemsetApriori candidate : candidatesK) {
				double expectedSupport = 0;
				for(ItemApriori item : candidate.getItems()){
					boolean found = false;
					for(ItemApriori itemT : transaction.getItems()){
						if(item.getId() == itemT.getId()){
							found = true;
							if(expectedSupport == 0){
								expectedSupport = itemT.getProbability();
							}else{
								expectedSupport *= itemT.getProbability();
							}
							break;
						}
						else if (item.getId() < itemT.getId()){
							break;
						}
					}	
					if(found == false){
						continue candidateLoop;
					}
				}
				candidate.increaseTransactionCount(expectedSupport);
			}
		}
	}

	// Based on the description of Pasquier 99: "Efficient mining..."
	protected Set<ItemsetApriori> generateCandidateSize1() {
		Set<ItemsetApriori> candidates = new HashSet<ItemsetApriori>(); // liste  d'itemsets
		for (ItemApriori item : context.getAttributes()) {
			ItemsetApriori itemset = new ItemsetApriori();
			itemset.addItem(item);
			candidates.add(itemset);
		}
		return candidates;
	}

	protected Set<ItemsetApriori> generateCandidateSizeK(Set<ItemsetApriori> levelK_1) {
		Set<ItemsetApriori> candidates = new HashSet<ItemsetApriori>();

		// For each itemset I1 and I2 of level k-1
		Object[] itemsets = levelK_1.toArray();
		for(int i=0; i< levelK_1.size(); i++){
			ItemsetApriori itemset1 = (ItemsetApriori)itemsets[i];
			for(int j=0; j< levelK_1.size(); j++){
				ItemsetApriori itemset2 = (ItemsetApriori)itemsets[j];
				// If I1 is smaller than I2 according to lexical order and
				// they share all the same items except the last one.
				ItemApriori missing = itemset1.allTheSameExceptLastItem(itemset2);
				if(missing != null ){
					// Create a new candidate by combining itemset1 and itemset2
					ItemsetApriori candidate = new ItemsetApriori();
					for(ItemApriori item : itemset1.getItems()){
						if(item.getId() > missing.getId() && missing != null){
							candidate.addItem(missing);
							missing = null;
						}
						candidate.addItem(item);
					}
					if(missing != null){
						candidate.addItem(missing);
					}
//					System.out.println(" " + itemset1.toString() + " + " + itemset2.toString() + " = " + candidate.toString());
	
					// The candidate is tested to see if its subsets of size k-1 are included in
					// level k-1 (they are frequent).
					if(allSubsetsOfSizeK_1AreFrequent(candidate,levelK_1)){
						candidates.add(candidate);
					}
				}
			}
		}
		return candidates;
	}

	protected boolean allSubsetsOfSizeK_1AreFrequent(ItemsetApriori candidate, Set<ItemsetApriori> levelK_1) {
		// To generate all the set of size K-1, we will proceed
		// by removing each item, one by one.
		if(candidate.size() == 1){
			return true;
		}
		for(ItemApriori item : candidate.getItems()){
			ItemsetApriori subset = candidate.cloneItemSetMinusOneItem(item);
			boolean found = false;
			for(ItemsetApriori itemset : levelK_1){
				if(itemset.isEqualTo(subset)){
					found = true;
					break;
				}
			}
			if(found == false){
				return false;
			}
		}
		return true;
	}


	public void printStats() {
		System.out
				.println("=============  U-APRIORI - STATS =============");
		long temps = endTimestamp - startTimestamp;
//		System.out.println(" Total time ~ " + temps + " ms");
		System.out.println(" Transactions count from database : "
				+ context.size());
		System.out.println(" Candidates count : " + totalCandidateCount);
		System.out.println(" Database scan count : " + databaseScanCount);
		System.out.println(" The algorithm stopped at size " + (k - 1)
				+ ", because there is no candidate");
		System.out.println(" Uncertain itemsets count : " + itemsetCount);

		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}
}
