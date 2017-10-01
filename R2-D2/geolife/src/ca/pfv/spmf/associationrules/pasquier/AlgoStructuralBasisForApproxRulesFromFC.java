package ca.pfv.spmf.associationrules.pasquier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * Algorithm from Pasquier et al. 1999 "Efficient mining..."
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
public class AlgoStructuralBasisForApproxRulesFromFC {

	private Rules basis = new Rules("Structural Basis for approximative rules");


	public Rules runAlgorithm(Itemsets frequentsClosed, double minconf) {
		System.out
		.println("===  EXECUTION - GENERATING STRUCTURAL BASIS FOR APPROX A.RULES  ===");

		int k = frequentsClosed.getLevels().size()-1;
		
		Set<ItemsetApriori> itemsetsToIgnore = new HashSet<ItemsetApriori>();
		
		for(int i=2; i<= k; i++){
			System.out.println("k =" + i);
			for(ItemsetApriori l : frequentsClosed.getLevels().get(i)){
				// (1) Find Sn, the subsets of L.
				List<List<ItemsetApriori>> sn =  generateSn(l, i, frequentsClosed);
				
				// (2) find all candidates rules
				List<Rule> candidates =  new ArrayList<Rule>();
				for(int j = i-1; j>0; j--){
					for(ItemsetApriori lprime : sn.get(j)){
						System.out.print(" l = ");
						l.print();
						System.out.print("    l' = ");
						lprime.print();
						if(itemsetsToIgnore.contains(lprime)){
							System.out.println(" ignored, because subset of a l' that is already used to generate a candidate");
						}else{
							double condifence = ((double)l.getAbsoluteSupport()) 
							/ ((double)lprime.getAbsoluteSupport());
							if(condifence >= minconf){
								System.out.print(" candidate : ");
								Rule Rule = new Rule(lprime, 
										l.cloneItemSetMinusAnItemset(lprime),
										l.getAbsoluteSupport(), condifence);
								Rule.print();
								System.out.println();
								candidates.add(Rule);
								// remove subsets of L' from Sn
								updateSubsetOfLprimeToIgnore(lprime, sn, j, itemsetsToIgnore);
							}
							else{
								System.out.println(" rejected, because confidence" + condifence + " is lower than minconf " + minconf);
							}
						}
					}
				}
				
				// (3) Keep only one rule
				if(candidates.size()>0){
					// Find the max support
					int supportmax=0;
					for(Rule Rule : candidates){
						supportmax = Math.max(supportmax, Rule.getAbsoluteSupport());
					}
					System.out.println("   Max support  : " + supportmax);
					// Keep the first rule according to lexical order
					Rule smallest = null;
					for(Rule Rule : candidates){
						if( Rule.getAbsoluteSupport() == supportmax){
							if(smallest == null){
								smallest=Rule;
								System.out.print("    smallest:");
								smallest.print();
								System.out.println();
							}else if(Rule.isLexicallySmallerthan(smallest)){
								smallest = Rule;
								System.out.print("    smallest:");
								smallest.print();
								System.out.println();
							}
						}
					}

					System.out.print("   We add the following rule to the basis :");
					smallest.print();
					basis.addRule(smallest);
					System.out.println("");
					break;
				}

			}
		}
		return basis;
	}

	private void updateSubsetOfLprimeToIgnore(ItemsetApriori lprime, List<List<ItemsetApriori>> sn, int j, Set<ItemsetApriori> itemsetsToIgnore) {
		// could be improved, i think
		for(int k = j-1; j>=0; j--){  
			for(ItemsetApriori itemset : sn.get(k)){  
				if(itemset.includedIn(lprime)){
					itemsetsToIgnore.add(itemset);
				}
			}
		}
	}

	private List<List<ItemsetApriori>> generateSn(ItemsetApriori l, int i, Itemsets frequentsClosed) {
		List<List<ItemsetApriori>> Sn = new ArrayList<List<ItemsetApriori>>();
		for(int k=0; k< i-1; k++){
			Sn.add(new ArrayList<ItemsetApriori>());
		}
		for(int j = i-1; j>0; j--){
			Sn.add(new ArrayList<ItemsetApriori>());
			for(ItemsetApriori lprime : frequentsClosed.getLevels().get(j)){
					if(lprime.includedIn(l)){
						Sn.get(j).add(lprime);
					}
			}
		}
		return Sn;
	}


	public void printStats(int transactionsCount) {
		System.out
				.println("===== GENERATING SB FOR APPROX A.RULES STATS =====");
		System.out.println(" Structural basis association rules count: " + basis.getRulesCount()); 
		basis.printRules(transactionsCount);
		System.out
				.println("===================================================");
	}

	public Rules getProperBasisForApproximativeRules() {
		return basis;
	}
}
