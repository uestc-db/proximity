package ca.pfv.spmf.associationrules.pasquier;

import java.util.List;

import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * An implementation of the third algorithm of Pasquier et al. 1999
 * "Efficient mining..."
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
public class AlgoGDBasisForExactRulesFromFC {

	private Rules gdBasisForExactRules = new Rules("GD Basis for exact rules");

	public Rules runAlgorithm(Itemsets frequentPseudos,
			Itemsets frequentsClosed, int nbobjects, double minconf) {
		System.out
				.println("====  EXECUTION - GENERATING GD FOR EXACT A.RULES  ====");

		// STEP 2 : Generating the GD basis of association rules
		generateGuiguesDuquenneBasisForExactRules(minconf, frequentPseudos,
				frequentsClosed);

		return gdBasisForExactRules;
	}

	private void generateGuiguesDuquenneBasisForExactRules(double minconf,
			Itemsets frequentPseudos, Itemsets frequentsClosed) {
		// gdBasisForExactRules
		for (List<ItemsetApriori> level : frequentPseudos.getLevels()) {
			for (ItemsetApriori p : level) {
				if (p.size() > 0) {
					ItemsetApriori antecedent = p;
					ItemsetApriori consequent = p.getClosure()
							.cloneItemSetMinusAnItemset(p);
					calculateSupportOf(consequent, frequentsClosed);
					double confidence = ((double) p.getClosure()
							.getAbsoluteSupport())
							/ ((double) consequent.getAbsoluteSupport());
					Rule regle = new Rule(antecedent, consequent,
							p.getAbsoluteSupport(), confidence);
					if (regle.getConfidence() >= minconf) {
						gdBasisForExactRules.addRule(regle);
					}
				}
			}
		}
	}

	private void calculateSupportOf(ItemsetApriori consequent,
			Itemsets frequentsClosed) {
		// we calculate the support by checking which smallest
		// closed itemsets countains "consequent".
		for (List<ItemsetApriori> level : frequentsClosed.getLevels()) {
			for (ItemsetApriori itemset : level) {
				if (itemset.getItems().containsAll(consequent.getItems())) {
					System.out.println("the support of "
							+ consequent.toString() + " is "
							+ itemset.getAbsoluteSupport());
					consequent
							.setTransactioncount(itemset.getAbsoluteSupport());
					return;
				}
			}
		}
		throw new RuntimeException("calculateSupportOf : Error!");
	}

	public void printStats(int transactionsCount) {
		System.out
				.println("===== GENERATING GD FOR EXACT A.RULES STATISTIQUES =====");
		System.out.println(" GD basis association rules count : "
				+ gdBasisForExactRules.getRulesCount());
		gdBasisForExactRules.printRules(transactionsCount);
		System.out
				.println("===================================================");
	}

	public Rules getGdBasisForExactRules() {
		return gdBasisForExactRules;
	}
}
