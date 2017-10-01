package ca.pfv.spmf.frequentpatterns.apriori;

/**
 * Finding frequent pseudo-closed itemsets from frequent closed itemsets and
 * frequent itemsets based on Pasquier et al. 1999 "Efficient mining..."
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
public class AlgoPseudoClosedItemsets {

	private Itemsets frequentPseudos = new Itemsets("FREQUENTS PSEUDO-CLOSED");

	public Itemsets runAlgorithm(Itemsets frequents, Itemsets frequentsClosed,
			int objectsCount) {

//		/// Step 1 : Calculating all Frequent pseudo-closed itemsets");
		// if FC0 is empty, then we add empty set to pseudo-closed
		if (frequentsClosed.getLevels().get(0).isEmpty()) {
			ItemsetApriori emptyset = new ItemsetApriori();
			
			emptyset.setPseudoclose(true);
			frequentPseudos.addItemset(emptyset, 0);
			// calculate closure of the emptyset
			calculateClosureForPseudo(emptyset, frequentsClosed, 1);
		}
		int k = frequents.getLevels().size();

		// For each level i=1 à k
		for (int i = 1; i < k; i++) {
           // Calculating pseudo-closed  for level " + i);
			for (ItemsetApriori itemset : frequents.getLevels().get(i)) {
				// We only consider frequent non closed itemsets
				if (!itemset.isClose()) {
					checkIfPseudoClosed(itemset, i);
					if(itemset.isPseudoclose()) {
						calculateClosureForPseudo(itemset, frequentsClosed, i);
					}
				}
			}
		}

		return frequentPseudos;
	}

	private void calculateClosureForPseudo(ItemsetApriori itemset,
			Itemsets frequentsClosed, int i) {
		// we have to find the maximal closed in which this item
		// is included.
		for (int j = i + 1; j < frequentsClosed.getLevels().size()
				&& itemset.getClosure() == null; j++) {
			for (ItemsetApriori fc : frequentsClosed.getLevels().get(j)) {
				if (itemset.includedIn(fc)) {
					itemset.setClosure(fc);
					break;
				}
			}
		}
	}

	private void checkIfPseudoClosed(ItemsetApriori l, int i) {
		l.setPseudoclose(true);
		for (int j = 0; j < i && l.isPseudoclose(); j++) {
			if (!(j >= frequentPseudos.getLevels().size())) {
				for (ItemsetApriori p : frequentPseudos.getLevels().get(j)) {
					
					if (p.includedIn(l) && !p.getClosure().includedIn(l)) {

						l.setPseudoclose(false);
						break;
					}
				}
			}
		}
		if (l.isPseudoclose()) {
			frequentPseudos.addItemset(l, i);
		}
	}

	public void printStatistics(int nbObjects) {
		System.out.println("===== GENERATING PSEUDO-CLOSED =====");
		System.out.println(" Frequent pseudo-closed itemsets count : "
				+ frequentPseudos.getItemsetsCount());
		System.out
				.println("===================================================");
	}

	public Itemsets getFrequentPseudos() {
		return frequentPseudos;
	}
}
