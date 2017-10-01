package ca.pfv.spmf.associationrules.pasquier;

import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * Algorithm 4 from Pasquier et al. 1999 "Efficient mining..."
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
public class AlgoProperBasisForApproxRulesFromFC {

	private Rules basis = new Rules("Proper Basis for approximative rules");

	public Rules runAlgorithm(Itemsets frequentsClosed, double minconf) {
		System.out
		.println("===  EXECUTION - GENERATING PROPER BASIS FOR APPROX A.RULES  ===");
		
		int k = frequentsClosed.getLevels().size()-1;
		
		for(int i=2; i<= k; i++){
			System.out.println("k =" + i);
			for(ItemsetApriori l : frequentsClosed.getLevels().get(i)){
				for(int j = i-1; j>0; j--){
					for(ItemsetApriori lprime : frequentsClosed.getLevels().get(j)){
						System.out.print(" l = ");
						l.print();
						System.out.print("    l' = ");
						lprime.print();
						if(lprime.includedIn(l)){
							System.out.println("");
							double confidence = ((double)l.getAbsoluteSupport()) 
								/ ((double)lprime.getAbsoluteSupport());
							if(confidence >= minconf){
								System.out.print(" rule : ");
								Rule rule = new Rule(lprime, 
										l.cloneItemSetMinusAnItemset(lprime),
										l.getAbsoluteSupport(), confidence);
								rule.print();
								System.out.println(" found");
								basis.addRule(rule);
							}else{
								System.out.println(" rejected because confidence " + confidence + " is lower than minconf " + minconf);
							}
						}else{
							System.out.println(" rejected, because l' is not included in l");
						}
					}
				}
			}
		}

		return basis;
	}


	public void printStats(int transactionsCount) {
		System.out
				.println("===== GENERATING PB FOR APPROX A.RULES STATISTIQUES =====");
		System.out.println(" Prober basis association rules count : " + basis.getRulesCount()); 
		basis.printRules(transactionsCount);
		System.out
				.println("===================================================");
	}

	public Rules getProperBasisForApproximativeRules() {
		return basis;
	}
}
