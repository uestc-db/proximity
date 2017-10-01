package ca.pfv.spmf.frequentpatterns.cfpgrowth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is an implementation of the CFPGrowth algorithm (Y. Hu et Y. Chen.,
 * 2004).
 * 
 * This implementation was made by Azadeh Soltani based on the FPGrowth
 * implementation by Philippe Fournier-Viger
 * 
 * Copyright (c) 2008-2012 Azadeh Soltani, Philippe Fournier-Viger
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
public class AlgoCFPGrowth_saveToFile {

	private long startTimestamp; // for stats
	private long endTime; // for stats
	private int transactionCount = 0; // for stats

	private int itemsetCount;
	BufferedWriter writer = null;

	// the comparator that is used to compare the item ordering
	final Comparator<Integer> itemComparator;
	int MIS[], minMIS;

	// constructor
	public AlgoCFPGrowth_saveToFile() {
		itemComparator = new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				int compare = MIS[o2] - MIS[o1]; // pfv
				if (compare == 0) { // if the same MIS, we check the lexical
									// ordering!
					return (o1 - o2);
				}
				return compare;
			}
		};
	}

	public void runAlgorithm(String input, String output, String MISIn)
			throws FileNotFoundException, IOException {
		startTimestamp = System.currentTimeMillis();
		// (1) PREPROCESSING: Initial database scan to determine the MIS of each
		// item
		final Map<Integer, Integer> mapSupport = new HashMap<Integer, Integer>();

		// az---initializing MISs--------------
		initMISfromFile(MISIn);

		itemsetCount = 0;
		writer = new BufferedWriter(new FileWriter(output));

		// (2) Scan the database to build the initial FP-Tree
		// Before inserting a transaction in the FPTree, we sort the items
		// by decreasing order of MIS.
		MISTree tree = new MISTree();

		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		while (((line = reader.readLine()) != null)) { // for each transaction
			String[] lineSplited = line.split(" ");
			List<Integer> transaction = new ArrayList<Integer>();
			for (String itemString : lineSplited) { // for each item in the
													// transaction
				Integer item = Integer.parseInt(itemString);
				Integer count = mapSupport.get(item);
				if (count == null) {
					mapSupport.put(item, 1);
				} else {
					mapSupport.put(item, ++count);
				}
				// all items are added to transactions
				transaction.add(item);
			}
			transactionCount++;
			// sort item in the transaction by non increasing order of MIS
			Collections.sort(transaction, this.itemComparator);
			// add the sorted transaction to the MISTree.
			tree.addTransaction(transaction);
		}// while
		reader.close();
		// tree.print(tree.root);

		// We create the header table for the tree
		tree.createHeaderList(this.itemComparator);

		// We search for fBar items with support smaller than minMIS and remove
		// them from the tree
		boolean sw = false;
		for (Entry<Integer, Integer> entry : mapSupport.entrySet()) {
			if (entry.getValue() < minMIS) {
				tree.deleteFromHeaderList(entry.getKey(), itemComparator);
				// System.out.println(entry.getKey());

				tree.MISPrunning(entry.getKey());
				// System.out.println(entry.getKey());
				// tree.print(tree.root);
				sw = true;
			}// if
		}// for
			// merge child node with the same item id
		if (sw == true) {
			tree.MISMerge(tree.root);
		}
		// tree.print(tree.root);

		// (5) We start to mine the FP-Tree by calling the recursive method.
		// Initially, the prefix alpha is empty.
		int[] prefixAlpha = new int[0];
		fpgrowth(tree, prefixAlpha, transactionCount, mapSupport);

		writer.close();
		endTime = System.currentTimeMillis();
	}

	// az--------------------------------------------------------
	private void initMISfromFile(String input) throws FileNotFoundException,
			IOException {
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		minMIS = Integer.MAX_VALUE;
		int maxItemID = 0;
		final Map<Integer, Integer> mapMIS = new HashMap<Integer, Integer>();
		while (((line = reader.readLine()) != null)) { // for each transaction
			String[] lineSplited = line.split(" ");
			Integer item = Integer.parseInt(lineSplited[0]);
			Integer itemMIS = Integer.parseInt(lineSplited[1]);
			if ((minMIS > itemMIS) && (itemMIS != 0)) {
				minMIS = itemMIS;
			}
			mapMIS.put(item, itemMIS);
			if (item > maxItemID) {
				maxItemID = item;
			}
		}
		MIS = new int[maxItemID + 1];
		for (Entry<Integer, Integer> entry : mapMIS.entrySet()) {
			MIS[entry.getKey()] = entry.getValue();
		}
	}

	// -----------------------------------
	private int initMISfromFrequency(String input,
			final Map<Integer, Integer> mapSupport, double beta, double LS)
			throws FileNotFoundException, IOException {
		int maxItemID = 0;
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		while (((line = reader.readLine()) != null)) { // for each transaction
			String[] lineSplited = line.split(" ");
			for (String itemString : lineSplited) { // for each item in the
													// transaction
				// increase the support count of the item
				Integer item = Integer.parseInt(itemString);
				Integer count = mapSupport.get(item);
				if (count == null) {
					mapSupport.put(item, 1);
					// az
					if (maxItemID < item)
						maxItemID = item;
				} else {
					mapSupport.put(item, ++count);
				}
			}
			transactionCount++;
		}
		reader.close();
		MIS = new int[maxItemID + 1];
		minMIS = 1;
		int LSRelative = (int) Math.ceil(LS * transactionCount);
		for (Entry<Integer, Integer> entry : mapSupport.entrySet()) {
			// calculate the MIS value
			MIS[entry.getKey()] = (int) (beta * entry.getValue());
			if (MIS[entry.getKey()] < LSRelative) {
				MIS[entry.getKey()] = LSRelative;
			}// if
			if (MIS[entry.getKey()] < minMIS) {
				minMIS = MIS[entry.getKey()];
			}// if
		}// for

		return minMIS;
	}

	// end az-------------------------------------------------------------------

	/**
	 * This method mines pattern from a Prefix-Tree recursively
	 * 
	 * @param tree
	 *            The Prefix Tree
	 * @param prefix
	 *            The current prefix "alpha"
	 * @param mapSupport
	 *            The frequency of each item in the prefix tree.
	 * @throws IOException
	 */
	private void fpgrowth(MISTree tree, int[] prefixAlpha, int prefixSupport,
			Map<Integer, Integer> mapSupport) throws IOException {

		// String test = "";
		// for(int item : prefixAlpha){
		// test += item + " ";
		// }
		// System.out.println(test);

		// We check if there is only one item in the header table
		if (tree.headerList.size() == 1) {
			MISNode node = tree.mapItemNodes.get(tree.headerList.get(0));
			// If this node has no child
			if (node.nodeLink == null) {
				// pfv : moved some code here and cleaned it
				// az
				if (node.counter >= MIS[prefixAlpha[0]]) {
					writeItemsetToFile(prefixAlpha, node.itemID, node.counter);
				}
				// end of code that i moved
			} else {
				cfpgrowthMoreThanOnePath(tree, prefixAlpha, prefixSupport,
						mapSupport);
			}
		} else { // There is more than one path
			cfpgrowthMoreThanOnePath(tree, prefixAlpha, prefixSupport,
					mapSupport);
		}
	}

	/**
	 * Mine an FP-Tree having more than one path.
	 * 
	 * @param tree
	 *            the FP-tree
	 * @param prefix
	 *            the current prefix, named "alpha"
	 * @param mapSupport
	 *            the frequency of items in the FP-Tree
	 * @throws IOException
	 */
	private void cfpgrowthMoreThanOnePath(MISTree tree, int[] prefixAlpha,
			int prefixSupport, Map<Integer, Integer> mapSupport)
			throws IOException {
		// We process each frequent item in the header table list of the tree in
		// reverse order.
		for (int i = tree.headerList.size() - 1; i >= 0; i--) {
			Integer item = tree.headerList.get(i);

			int support = mapSupport.get(item);
			// if the item is not frequent, we skip it
			int mis = (prefixAlpha.length == 0) ? MIS[item]
					: MIS[prefixAlpha[0]]; // pfv
			if (support < mis)
				continue;
			// Let's Beta be the concatenation of Alpha with the current item

			int betaSupport = (prefixSupport < support) ? prefixSupport
					: support;
			// az
			// int mis = (prefixAlpha.length == 0) ? MIS[item] :
			// MIS[prefixAlpha[0]]; // pfv

			if (support >= mis) {
				writeItemsetToFile(prefixAlpha, item, betaSupport); // pfv
			}

			// === Construct beta's conditional pattern base ===
			// It is a subdatabase which consists of the set of prefix paths
			// in the FP-tree co-occuring with the suffix pattern.
			List<List<MISNode>> prefixPaths = new ArrayList<List<MISNode>>();
			MISNode path = tree.mapItemNodes.get(item);
			while (path != null) {
				// if the path is not just the root node
				if (path.parent.itemID != -1) {
					// create the prefixpath
					List<MISNode> prefixPath = new ArrayList<MISNode>();
					// add this node.
					prefixPath.add(path); // NOTE: we add it just to keep its
											// support,
					// actually it should not be part of the prefixPath

					// Recursively add all the parents of this node.
					MISNode parent = path.parent;
					while (parent.itemID != -1) {
						prefixPath.add(parent);
						parent = parent.parent;
					}
					prefixPaths.add(prefixPath);
				}
				// We will look for the next prefixpath
				path = path.nodeLink;
			}

			// (A) Calculate the frequency of each item in the prefixpath
			Map<Integer, Integer> mapSupportBeta = new HashMap<Integer, Integer>();
			// for each prefixpath
			for (List<MISNode> prefixPath : prefixPaths) {
				// the support of the prefixpath is the support of its first
				// node.
				int pathCount = prefixPath.get(0).counter;
				for (int j = 1; j < prefixPath.size(); j++) { // for each node,
																// except the
																// first one, we
																// count the
																// frequency
					MISNode node = prefixPath.get(j);
					if (mapSupportBeta.get(node.itemID) == null) {
						mapSupportBeta.put(node.itemID, pathCount);
					} else {
						mapSupportBeta.put(node.itemID,
								mapSupportBeta.get(node.itemID) + pathCount);
					}
				}
			}

			// (B) Construct beta's conditional FP-Tree
			MISTree treeBeta = new MISTree();
			// add each prefixpath in the FP-tree
			for (List<MISNode> prefixPath : prefixPaths) {
				treeBeta.addPrefixPath(prefixPath, mapSupportBeta, minMIS); // az
			}
			treeBeta.createHeaderList(itemComparator); // az

			// System.out.println();
			// treeBeta.print(treeBeta.root);

			// Mine recursively the Beta tree.
			if (treeBeta.root.childs.size() > 0) {
				// create beta
				int[] beta = new int[prefixAlpha.length + 1];
				System.arraycopy(prefixAlpha, 0, beta, 0, prefixAlpha.length);
				beta[prefixAlpha.length] = item;

				fpgrowth(treeBeta, beta, betaSupport, mapSupportBeta);
			}
		}
	}

	/**
	 * Write a frequent itemset that is found to the output file.
	 * 
	 * @param counter
	 */
	private void writeItemsetToFile(int[] itemset, int lastItem, int support)
			throws IOException {
		itemsetCount++;
		StringBuffer buffer = new StringBuffer();
		// WRITE ITEMS
		for (int i = 0; i < itemset.length; i++) {
			buffer.append(itemset[i]);
			buffer.append(' ');
		}
		buffer.append(lastItem);
		buffer.append(':');
		// WRITE SUPPORT
		buffer.append(support);
		writer.write(buffer.toString());
		writer.newLine();
	}

	public void printStats() {
		System.out.println("=============  CFP-GROWTH - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ transactionCount);
		System.out.println(" Frequent itemsets count : " + itemsetCount);
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}
}
