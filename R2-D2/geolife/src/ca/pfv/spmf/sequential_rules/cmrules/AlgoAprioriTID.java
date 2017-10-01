package ca.pfv.spmf.sequential_rules.cmrules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.general.datastructures.triangularmatrix.TriangularMatrix;

/**
 * This is an implementation of the Apriori algorithm as described by :
 * 
 *   Pasquier, N., Bastide, Y., Taouil, R., Lakhal, L., (1999). Efficient Mining of Association Rules using 
 *   Closed Itemset Lattices. Information Systems, Elsevier Science, 24(1), pages 25-46 .
 *   
 * and the original Apriori article:
 * 
 *   Agrawal R, Srikant R. "Fast Algorithms for Mining Association Rules", VLDB. Sep 12-15 1994, Chile, 487-99,
 * 
 * The Apriori algorithm finds all the frequents itemsets and their support
 * in a binary context.
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
public class AlgoAprioriTID {

	protected Itemsets frequentItemsets = new Itemsets("FREQUENT ITEMSETS");
	protected Context context;
	protected int k; // level
	TriangularMatrix matrix;
	
	int minSuppRelative;
	
	// Special parameter to set the maximum size of itemsets to be discovered
	int maxItemsetSize = Integer.MAX_VALUE;

	public AlgoAprioriTID(Context context, TriangularMatrix matrix) {
		this.context = context;
		this.matrix = matrix;
	}

	public Itemsets runAlgorithm(int minsuppRelative, List<Integer> listFrequentsSize1, Map<Integer, Set<Integer>> mapItemCount) {
		
		this.minSuppRelative = minsuppRelative;

		// Generate candidates with size k = 1 (all itemsets of size 1)
		k=1;

		// To build level 1, we keep only the frequent candidates.
		// We scan the database one time to calculate the support of each candidate.
		List<Itemset> level = createLevelWithFrequentCandidates1(listFrequentsSize1, mapItemCount);

		k = 2;
		// While the level is not empty
		while (!level.isEmpty()  && k <= maxItemsetSize) {
			// Generate candidates of size K
			level = generateCandidateSizeK(level);; // We keep only the last level... 
			k++;
		}
		return frequentItemsets; // Return all frequent itemsets found!
	}


	protected List<Itemset> createLevelWithFrequentCandidates1(List<Integer> listFrequentsSize1, Map<Integer, Set<Integer>> mapItemCount) {
		List<Itemset> levelK = new ArrayList<Itemset>();

		for(Integer item : listFrequentsSize1){
			Itemset itemset = new Itemset();
			itemset.addItem(item);
			itemset.setTransactioncount(mapItemCount.get(item));
			levelK.add(itemset);
			frequentItemsets.addItemset(itemset, k);
		}
		return levelK;
	}

	/**
	 * Add the empty set to level 0 if no itemset of level 1 have a support
	 * that is equal to the number of transactions in the binary context.
	 * @param candidatesSize1 Candidates of level 1.
	 */
	protected void addEmptySetToLevel0IfNecessary(List<Itemset> candidatesSize1) {
		for(Itemset candidate : candidatesSize1){
			if(candidate.getAbsoluteSupport() == context.size()){
				return;
			}
		}
		Itemset emptySet = new Itemset();
		Set<Integer> transactionIds = new HashSet<Integer>(context.getObjects().size());
		for(int i=0; i < context.getObjects().size(); i++){
			transactionIds.add(i);
		}
		emptySet.setTransactioncount(transactionIds);
		frequentItemsets.addItemset(emptySet, 0); // ajout ensemble vide
	}

	// Based on the description of Pasquier 99: "Efficient mining..."
	protected List<Itemset> generateCandidateSize1() {
		List<Itemset> candidates = new ArrayList<Itemset>(); // liste  d'itemsets
		for (Integer item : context.getAttributes()) {
			Itemset itemset = new Itemset();
			itemset.addItem(item);
			candidates.add(itemset);
		}
		return candidates;
	}

	protected List<Itemset> generateCandidateSizeK(List<Itemset> levelK_1) {
		List<Itemset> candidates = new ArrayList<Itemset>();

// For each itemset I1 and I2 of level k-1
loop1:	for(int i=0; i< levelK_1.size(); i++){
			Itemset itemset1 = levelK_1.get(i);
loop2:		for(int j=i+1; j< levelK_1.size(); j++){
				Itemset itemset2 = levelK_1.get(j);
				
				for(int k=0; k< itemset1.size(); k++){
					// if they are the last items
					if(k == itemset1.size()-1){ 
						// the one from itemset1 should be smaller (lexical order) 
						// and different from the one of itemset2
						if(itemset1.getItems().get(k) >= itemset2.get(k)){  
							continue loop1;
						}
					}
					// if they are not the last items, and 
					else if(itemset1.getItems().get(k) < itemset2.get(k)){ 
						continue loop2; // we continue searching
					}
					else if(itemset1.getItems().get(k) > itemset2.get(k)){ 
						continue loop1;  // we stop searching:  because of lexical order
					}
				}
				// NOW COMBINE ITEMSET 1 AND ITEMSET 2
				Integer missing = itemset2.get(itemset2.size()-1);

				// create list of common tids
				Set<Integer> list = new HashSet<Integer>();
				for(Integer val1 : itemset1.getTransactionsIds()){
					if(itemset2.getTransactionsIds().contains(val1)){
						list.add(val1);
					}
				}
			
				if(list.size() >= minSuppRelative){
					// Create a new candidate by combining itemset1 and itemset2
					Itemset candidate = new Itemset();
					for(int k=0; k < itemset1.size(); k++){
						candidate.addItem(itemset1.get(k));
					}
					candidate.addItem(missing);
					candidate.setTransactioncount(list);
					candidates.add(candidate);
					frequentItemsets.addItemset(candidate, k);
				}
			}
		}
		return candidates;
	}

	protected boolean allSubsetsOfSizeK_1AreFrequent(Itemset candidate, List<Itemset> levelK_1) {
		// To generate all the set of size K-1, we will proceed
		// by removing each item, one by one.
		if(candidate.size() == 1){
			return true;
		}
		for(Integer item : candidate.getItems()){
			Itemset subset = candidate.cloneItemSetMinusOneItem(item);
			boolean found = false;
			for(Itemset itemset : levelK_1){
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

	public Itemsets getItemsets() {
		return frequentItemsets;
	}

	public void setMaxItemsetSize(int maxItemsetSize) {
		this.maxItemsetSize = maxItemsetSize;
	}
}
