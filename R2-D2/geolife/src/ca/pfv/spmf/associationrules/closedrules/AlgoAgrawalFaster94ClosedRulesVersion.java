package ca.pfv.spmf.associationrules.closedrules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.associationrules.agrawal_Apriori_version.RuleAgrawal;
import ca.pfv.spmf.associationrules.agrawal_Apriori_version.RulesAgrawal;
import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;
/**
 * This is an implementation of the "faster algorithm" described in 
 * Agrawal & al. 1994, IBM Research Report RJ9839, June 1994. 
 * It has been slightly modified for being applied to mine all "closed association rules"
 * as defined in Szathmary's thesis (2006).
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

public class AlgoAgrawalFaster94ClosedRulesVersion {
	private Itemsets patterns;
	private RulesAgrawal rules;
	
	private double minconf;
	
	public AlgoAgrawalFaster94ClosedRulesVersion(double minconf){
		this.minconf = minconf;
	}

	public RulesAgrawal runAlgorithm(Itemsets patterns) {
		this.patterns = patterns;
		rules = new RulesAgrawal("Closed association rules");
		
		//For each frequent itemset of size >=2
		for(int k=2; k< patterns.getLevels().size(); k++){
			for(ItemsetApriori lk : patterns.getLevels().get(k)){
				// create H1
				Set<ItemsetApriori> H1 = new HashSet<ItemsetApriori>();
				for(Integer item : lk.getItems()){  // THIS PART WAS CHANGED
					ItemsetApriori itemset = new ItemsetApriori();
					itemset.addItem(item);
					H1.add(itemset);
				}
				
				Set<ItemsetApriori> H1_for_recursion  = new HashSet<ItemsetApriori>();
				for(ItemsetApriori hm_P_1 : H1){
					ItemsetApriori itemset_Lk_minus_hm_P_1 = lk.cloneItemSetMinusAnItemset(hm_P_1);

					calculateSupport(itemset_Lk_minus_hm_P_1);   //  THIS COULD BE OPTIMIZED ? OR DONE ANOTHER WAY ?
					calculateSupport(lk);  
					double conf = ((double)lk.getAbsoluteSupport()) / ((double)itemset_Lk_minus_hm_P_1.getAbsoluteSupport());
					
					if(conf >= minconf){
						RuleAgrawal rule = new RuleAgrawal(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
						rules.addRule(rule);
						H1_for_recursion.add(hm_P_1);// for recursion
					}
				}

				// call apGenRules
				apGenrules(k, 1, lk, H1_for_recursion);
			}
		}
		
		return rules;
	}

	private void apGenrules(int k, int m, ItemsetApriori lk, Set<ItemsetApriori> Hm) {
//		System.out.println(" " + lk.toString() + "  " + Hm.toString());
		if(k > m+1){
			Set<ItemsetApriori> Hm_plus_1 = generateCandidateSizeK(Hm);
			Set<ItemsetApriori> Hm_plus_1_for_recursion = new HashSet<ItemsetApriori>();
			for(ItemsetApriori hm_P_1 : Hm_plus_1){
				ItemsetApriori itemset_Lk_minus_hm_P_1 = lk.cloneItemSetMinusAnItemset(hm_P_1);

//				calculateSupport(hm_P_1);   
				calculateSupport(itemset_Lk_minus_hm_P_1);   // THIS COULD BE DONE ANOTHER WAY ?
				calculateSupport(lk);                                            // IT COULD PERHAPS BE IMPROVED....
				double conf = ((double)lk.getAbsoluteSupport()) / ((double)itemset_Lk_minus_hm_P_1.getAbsoluteSupport());
				
				if(conf >= minconf){
					RuleAgrawal rule = new RuleAgrawal(itemset_Lk_minus_hm_P_1, hm_P_1, lk.getAbsoluteSupport(), conf);
					rules.addRule(rule);
					Hm_plus_1_for_recursion.add(hm_P_1);
				}
			}
			apGenrules(k, m+1, lk, Hm_plus_1_for_recursion);
		}
	}

	private void calculateSupport(ItemsetApriori itemsetToTest) {  // THIS WAS CHANGED
		for(List<ItemsetApriori> list : patterns.getLevels()){
			if(list.size() == 0  || list.get(0).size() < itemsetToTest.size()){
				continue; // it is not useful to consider itemsets that are smaller  
				          // than itemsetToTest.size
			}
			for(ItemsetApriori itemset : list){
				if(itemsetToTest.includedIn(itemset)){
					itemsetToTest.setTransactioncount(itemset.getAbsoluteSupport());
					return;
				}
			}
		}
	}

	protected Set<ItemsetApriori> generateCandidateSizeK(Set<ItemsetApriori> levelK_1) {
		Set<ItemsetApriori> candidates = new HashSet<ItemsetApriori>();

		// For each itemset I1 and I2 of level k-1
		for(ItemsetApriori itemset1 : levelK_1){
			for(ItemsetApriori itemset2 : levelK_1){
				// If I1 is smaller than I2 according to lexical order and
				// they share all the same items except the last one.
				Integer missing = itemset1.allTheSameExceptLastItem(itemset2);
				if(missing != null ){
					// Create a new candidate by combining itemset1 and itemset2
					ItemsetApriori candidate = new ItemsetApriori();
					for(Integer item : itemset1.getItems()){
						if(item > missing && missing != null){
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
		for(Integer item : candidate.getItems()){
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
	
}
