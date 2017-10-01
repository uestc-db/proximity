package ca.pfv.spmf.associationrules.agrawal_FPGrowth_version_saveToFile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ca.pfv.spmf.associationrules.agrawal_FPGrowth_version.RuleAgrawal;
import ca.pfv.spmf.frequentpatterns.fpgrowth.Itemset;
import ca.pfv.spmf.frequentpatterns.fpgrowth.Itemsets;

/**
 * This is an implementation of the "faster algorithm" described in Agrawal &
 * al. 1994, IBM Research Report RJ9839, June 1994.
 * 
 * This version saves the results to a file.
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

public class AlgoAgrawalFaster94_FPGrowth_version_saveToFile {
	private Itemsets patterns;
	BufferedWriter writer = null;
	long startTimestamp = 0;
	long endTimeStamp = 0;

	private double minconf;
	private int ruleCount = 0;
	private int databaseSize = 0;
	private double minlift;
	private boolean usingLift = true;

	public AlgoAgrawalFaster94_FPGrowth_version_saveToFile(double minconf) {
		this.minconf = minconf;
		this.minlift = 0;
		usingLift = false;
	}

	public AlgoAgrawalFaster94_FPGrowth_version_saveToFile(double minconf,
			double minlift) {
		this.minconf = minconf;
		this.minlift = minlift;
		usingLift = true;
	}

	public void runAlgorithm(Itemsets patterns, String output, int databaseSize)
			throws IOException {
		startTimestamp = System.currentTimeMillis();
		ruleCount = 0;
		this.patterns = patterns;
		this.databaseSize = databaseSize;
		writer = new BufferedWriter(new FileWriter(output));

		// For each frequent itemset of size >=2
		for (int k = 2; k < patterns.getLevels().size(); k++) {
			for (Itemset lk : patterns.getLevels().get(k)) {
				// create H1
				Set<Itemset> H1 = new HashSet<Itemset>();
				for (Itemset itemsetSize1 : patterns.getLevels().get(1)) {
					if (lk.contains(itemsetSize1.getItems().get(0))) {
						H1.add(itemsetSize1);
					}
				}

				// / ================ I ADDED THIS BECAUSE THE ALGORITHM AS
				// DESCRIBED BY AGRAWAL94
				// / ================ DID NOT GENERATE ALL THE ASSOCIATION RULES
				Set<Itemset> H1_for_recursion = new HashSet<Itemset>();
				for (Itemset hm_P_1 : H1) {
					Itemset itemset_Lk_minus_hm_P_1 = lk
							.cloneItemSetMinusAnItemset(hm_P_1);

					// double conf = supp(lk) / supp (lk - hm+1)
					calculateSupport(itemset_Lk_minus_hm_P_1); // THIS COULD BE
																// OPTIMIZED ?
																// OR DONE
																// ANOTHER WAY ?
					double conf = ((double) lk.getAbsoluteSupport())
							/ ((double) itemset_Lk_minus_hm_P_1
									.getAbsoluteSupport());

					if(conf < minconf){
						continue;
					}
					
					if(minlift >0){
						calculateSupport(hm_P_1);  // if we want to calculate the lift, we need to add this.
						double lift = conf / hm_P_1.getAbsoluteSupport();
						if(lift < minlift){
							continue;
						}
					}
					
					RuleAgrawal rule = new RuleAgrawal(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
					saveToFile(rule);
					H1_for_recursion.add(hm_P_1);// for recursion
					
				}
				// ================ END OF WHAT I HAVE ADDED

				// call apGenRules
				apGenrules(k, 1, lk, H1_for_recursion);
			}
		}

		// close the file
		writer.close();
		endTimeStamp = System.currentTimeMillis();
	}

	private void saveToFile(RuleAgrawal rule) throws IOException {
		ruleCount++;
		StringBuffer buffer = new StringBuffer();
		// write itemset 1
		for (int i = 0; i < rule.getItemset1().size(); i++) {
			buffer.append(rule.getItemset1().get(i));
			if (i != rule.getItemset1().size() - 1) {
				buffer.append(",");
			}
		}
		// write separator
		buffer.append(" ==> ");
		// write itemset 2
		for (int i = 0; i < rule.getItemset2().size(); i++) {
			buffer.append(rule.getItemset2().get(i));
			if (i != rule.getItemset2().size() - 1) {
				buffer.append(",");
			}
		}
		// write separator
		buffer.append("  sup= ");
		// write support
		buffer.append(rule.getRelativeSupport(databaseSize) + " %");
		// write separator
		buffer.append("  conf= ");
		// write confidence
		buffer.append(rule.getConfidence());
		if(usingLift){
			buffer.append("  lift= ");
			buffer.append(rule.getLift());
		}
		
		writer.write(buffer.toString());
		writer.newLine();
		writer.flush();
	}

	// apGenRules, p14. from Agrawal
	private void apGenrules(int k, int m, Itemset lk, Set<Itemset> Hm)
			throws IOException {
		// System.out.println(" " + lk.toString() + "  " + Hm.toString());
		if (k > m + 1) {
			Set<Itemset> Hm_plus_1 = generateCandidateSizeK(Hm);
			Set<Itemset> Hm_plus_1_for_recursion = new HashSet<Itemset>();
			for (Itemset hm_P_1 : Hm_plus_1) {
				Itemset itemset_Lk_minus_hm_P_1 = lk
						.cloneItemSetMinusAnItemset(hm_P_1);

				calculateSupport(itemset_Lk_minus_hm_P_1); // THIS COULD BE DONE
															// ANOTHER WAY ?
															// IT COULD PERHAPS
															// BE IMPROVED....
				double conf = ((double) lk.getAbsoluteSupport())
						/ ((double) itemset_Lk_minus_hm_P_1
								.getAbsoluteSupport());

				if(conf < minconf){
					continue;
				}
				
				// IT COULD PERHAPS BE IMPROVED....
				if(minlift >0){
					calculateSupport(hm_P_1);  // if we want to calculate the lift, we need to add this.
					double lift = conf / hm_P_1.getAbsoluteSupport();
					if(lift < minlift){
						continue;
					}
				}
				
				RuleAgrawal rule = new RuleAgrawal(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
				saveToFile(rule);
				Hm_plus_1_for_recursion.add(hm_P_1);
			}
			apGenrules(k, m + 1, lk, Hm_plus_1_for_recursion);
		}
	}

	/**
	 * Calculate the support of an itemset by looking at the frequent patterns
	 * of the same size.
	 * 
	 * @param itemset_Lk_minus_hm_P_1
	 *            The itemset.
	 */
	private void calculateSupport(Itemset itemset_Lk_minus_hm_P_1) {
		// loop over all the patterns of the same size.
		for (Itemset itemset : patterns.getLevels().get(
				itemset_Lk_minus_hm_P_1.size())) {
			// If the pattern is found
			if (itemset.allTheSame(itemset_Lk_minus_hm_P_1)) {
				// set its support to the same value.
				itemset_Lk_minus_hm_P_1.setTransactioncount(itemset
						.getAbsoluteSupport());
				return;
			}
		}
	}

	/**
	 * Generating candidate itemsets of size k from frequent itemsets of size
	 * k-1. This is called "apriori-gen" in the paper by agrawal. This method is
	 * also used by the Apriori algorithm for generating candidates.
	 * 
	 * @param levelK_1
	 * @return
	 */
	protected Set<Itemset> generateCandidateSizeK(Set<Itemset> levelK_1) {
		Set<Itemset> candidates = new HashSet<Itemset>();

		// For each itemset I1 and I2 of level k-1
		for (Itemset itemset1 : levelK_1) {
			for (Itemset itemset2 : levelK_1) {
				// If I1 is smaller than I2 according to lexical order and
				// they share all the same items except the last one.
				Integer missing = itemset1.allTheSameExceptLastItem(itemset2);
				if (missing != null) {
					// Create a new candidate by combining itemset1 and itemset2
					Itemset candidate = new Itemset();
					for (Integer item : itemset1.getItems()) {
						if (item > missing && missing != null) {
							candidate.addItem(missing);
							missing = null;
						}
						candidate.addItem(item);
					}
					if (missing != null) {
						candidate.addItem(missing);
					}

					// The candidate is tested to see if its subsets of size k-1
					// are included in
					// level k-1 (they are frequent).
					if (allSubsetsOfSizeK_1AreFrequent(candidate, levelK_1)) {
						candidates.add(candidate);
					}
				}
			}
		}
		return candidates;
	}

	/**
	 * This method checks if all the subsets of size "k" of the itemset
	 * "candidate" are frequent.
	 * 
	 * @param candidate
	 *            An itemset of size "k".
	 * @param levelK_1
	 *            The frequent itemsets of size "k-1".
	 * @return
	 */
	protected boolean allSubsetsOfSizeK_1AreFrequent(Itemset candidate,
			Set<Itemset> levelK_1) {
		// To generate all the set of size K-1, we will proceed
		// by removing each item, one by one.
		if (candidate.size() == 1) {
			return true;
		}
		for (Integer item : candidate.getItems()) {
			Itemset subset = candidate.cloneItemSetMinusOneItem(item);
			boolean found = false;
			for (Itemset itemset : levelK_1) {
				if (itemset.isEqualTo(subset)) {
					found = true;
					break;
				}
			}
			if (found == false) {
				return false;
			}
		}
		return true;
	}

	public void printStats() {
		System.out
				.println("=============  ASSOCIATION RULE GENERATION - STATS =============");
		System.out.println(" Number of association rules generated : "
				+ ruleCount);
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)
				+ " ms");
		System.out
				.println("===================================================");
	}
	
	public void saveTextToFile(String text) throws IOException{
		writer.write(text);
	}
	
	public void saveStatsToFile() throws IOException {
		saveTextToFile("=============  ASSOCIATION RULE GENERATION - STATS =============\n");
		saveTextToFile(" Number of itemsets generated : "
				+ patterns.getItemsetsCount() + "\n");
		saveTextToFile(" Number of association rules generated : "
				+ ruleCount + "\n");
		saveTextToFile(" Total time ~ " + (endTimeStamp - startTimestamp)
				+ " ms\n");
		saveTextToFile("===================================================\n");
	}
}
