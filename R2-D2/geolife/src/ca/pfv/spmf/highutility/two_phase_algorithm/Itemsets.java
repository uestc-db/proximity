package ca.pfv.spmf.highutility.two_phase_algorithm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a set of itemsets. They are ordered by size. For
 * example, "level 1" means the itemsets of size 1 (containing 1 item).
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
public class Itemsets {
	// itemsets ordered by size
	private final List<List<Itemset>> levels = new ArrayList<List<Itemset>>(); 
	private int itemsetsCount = 0;
	public String name;

	public Itemsets(String name) {
		this.name = name;
		levels.add(new ArrayList<Itemset>()); // We create an empty level 0 by
												// default.
	}

	public void printItemsets(int nbObject) {
		System.out.println(" ------- " + name + " -------");
		int patternCount = 0;
		int levelCount = 0;
		for (List<Itemset> level : levels) {
			System.out.println("  L" + levelCount + " ");
			for (Itemset itemset : level) {
				System.out.print("  pattern " + patternCount + "  ");
				itemset.print();
				System.out.print("support : "
						+ itemset.getSupportRelatifFormatted(nbObject));
				System.out.print(" utility : " + itemset.getUtility());
				System.out.print(" tidset : " + itemset.getTransactionsIds());
				patternCount++;
				System.out.println("");
			}
			levelCount++;
		}
		System.out.println(" --------------------------------");
	}


	public void saveResultsToFile(String output, int nbObject) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));

		for (List<Itemset> level : levels) {
			for (Itemset itemset : level) {
				writer.write(itemset.toString());
				writer.write(" support : "
						+ itemset.getSupportRelatifFormatted(nbObject));
				writer.write(" utility : " + itemset.getUtility());
				writer.write(" tidset : " + itemset.getTransactionsIds());
				writer.newLine();
			}
		}
		
		writer.close();
		
	}	

	
	
	
	public void addItemset(Itemset itemset, int k) {
		while (levels.size() <= k) {
			levels.add(new ArrayList<Itemset>());
		}
		levels.get(k).add(itemset);
		itemsetsCount++;
	}

	public List<List<Itemset>> getLevels() {
		return levels;
	}

	public int getItemsetsCount() {
		return itemsetsCount;
	}

	public void decreaseCount() {
		itemsetsCount--;
	}

}
