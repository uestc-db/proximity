package ca.pfv.spmf.frequentpatterns.fpgrowth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an implementation of the FPGROWTH algorithm (Han et al., 2004) 
 * based on the description in the book of Han & Kamber.
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
public class AlgoFPGrowth {

	protected Itemsets frequentItemsets = new Itemsets("FREQUENT ITEMSETS");
	private long startTimestamp; // for stats
	private long endTime; // for stats
	public int relativeMinsup;
	private int contextSize=0;

	public AlgoFPGrowth() {
		
	}

	public Itemsets runAlgorithm(Database context, double minsupp) {
		startTimestamp = System.currentTimeMillis();
		
		this.relativeMinsup = (int) Math.ceil(minsupp * context.size());
		
		// (1) PREPROCESSING: Initial database scan to determine the frequency of each item
		final Map<Integer, Integer> mapSupport = new HashMap<Integer, Integer>();
		for(Itemset itemset : context.getObjects()){
			for(Integer item : itemset.getItems()){
				if(mapSupport.get(item) == null){
					mapSupport.put(item, 1);
				}else{
					mapSupport.put(item, mapSupport.get(item) + 1);
				}
			}
		}
		
		// (2) PREPROCESSING:  All infrequent items are discarded from the database
		for(Itemset itemset : context.getObjects()){ // for each transactions
			for(int i=itemset.size() -1; i>=0; i--){ // for each items
				// remove the item if its support is lower than minsup 
				if(mapSupport.get(itemset.get(i)) < relativeMinsup){ 
					itemset.getItems().remove(i);
				}
			}
		}
		
		// (3) PREPROCESSING: Sort items in each transaction in a descending order 
		// according to their frequency in the database.
		for(Itemset itemset : context.getObjects()){
			Collections.sort(itemset.getItems(), new Comparator<Integer>(){
				public int compare(Integer item1, Integer item2){
					int compare = mapSupport.get(item2) - mapSupport.get(item1);
					if(compare ==0){ // if the same frequency, we check the lexical ordering!
						return (item1 - item2);
					}
					return compare;
				}
			});
		}
	
		// (4) Build the initial FP-TREE
		FPTree tree = new FPTree();
		// We add each transactions in the FP-Tree one by one
		for(Itemset transaction : context.getObjects()){
			tree.addTransaction(transaction); 
		}
		// We create the header table for the tree
		tree.createHeaderList(mapSupport);
		
		contextSize = context.size();
		context = null;

		
		// (5) We start to mine the FP-Tree by calling the recursive method.
		// Initially, the prefix alpha is empty.
		Itemset prefixAlpha = new Itemset();
		prefixAlpha.setTransactioncount(contextSize);		
		fpgrowth(tree, prefixAlpha, mapSupport);
		
		endTime= System.currentTimeMillis();
		
		return frequentItemsets; // Return all frequent itemsets found!
	}


	/**
	 * This method mines pattern from a Prefix-Tree recursively
	 * @param tree  The Prefix Tree
	 * @param prefix  The current prefix "alpha"
	 * @param mapSupport The frequency of each item in the prefix tree.
	 */
	private void fpgrowth(FPTree tree, Itemset prefixAlpha, Map<Integer, Integer> mapSupport) {
//		// (5)  Apply the FP-BONSAI optimization
//		pruning(tree, prefixAlpha.getAbsoluteSupport(), mapSupport);
		
		// We need to check if there is a single path in the prefix tree or not.
		// So first we check if there is only one item in the header table
		if(tree.headerList.size() == 1){
			FPNode node = tree.mapItemNodes.get(tree.headerList.get(0));
			// We need to check if this item has some node links.
			if(node.nodeLink == null){ 
				// That means that there is a single path, so we 
				// add all combinations of this path, concatenated with the prefix "alpha", to the set of patterns found.
				addAllCombinationsForPathAndPrefix(node, prefixAlpha); // CORRECT?
			}else{
				// There is more than one path
				fpgrowthMoreThanOnePath(tree, prefixAlpha, mapSupport);
			}
		}else{ // There is more than one path
			fpgrowthMoreThanOnePath(tree, prefixAlpha, mapSupport);
		}
	}
	
	/**
	 * Mine an FP-Tree having more than one path.
	 * @param tree  the FP-tree
	 * @param prefix  the current prefix, named "alpha"
	 * @param mapSupport the frequency of items in the FP-Tree
	 */
	private void fpgrowthMoreThanOnePath(FPTree tree, Itemset prefixAlpha, Map<Integer, Integer> mapSupport) {
		// We process each frequent item in the header table list of the tree in reverse order.
		for(int i= tree.headerList.size()-1; i>=0; i--){
			Integer item = tree.headerList.get(i);
			
			int support = mapSupport.get(item);
			// if the item is not frequent, we skip it
			if(support <  relativeMinsup){
				continue;
			}
			// Create Beta by concatening Alpha with the current item
			// and add it to the list of frequent patterns
			Itemset beta = prefixAlpha.cloneItemset();
			beta.addItem(item);
			if(prefixAlpha.getAbsoluteSupport() < support){
				beta.setTransactioncount(prefixAlpha.getAbsoluteSupport());
			}else{
				beta.setTransactioncount(support);
			}
			
			sortItemset(beta);
			frequentItemsets.addItemset(beta, beta.size());
			
			// === Construct beta's conditional pattern base ===
			// It is a subdatabase which consists of the set of prefix paths
			// in the FP-tree co-occuring with the suffix pattern.
			List<List<FPNode>> prefixPaths = new ArrayList<List<FPNode>>();
			FPNode path = tree.mapItemNodes.get(item);
			while(path != null){
				// if the path is not just the root node
				if(path.parent.itemID != -1){
					// create the prefixpath
					List<FPNode> prefixPath = new ArrayList<FPNode>();
					// add this node.
					prefixPath.add(path);   // NOTE: we add it just to keep its support,
					// actually it should not be part of the prefixPath
					
					//Recursively add all the parents of this node.
					FPNode parent = path.parent;
					while(parent.itemID != -1){
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
			for(List<FPNode> prefixPath : prefixPaths){
				// the support of the prefixpath is the support of its first node.
				int pathCount = prefixPath.get(0).counter;  
				for(int j=1; j<prefixPath.size(); j++){  // for each node, except the first one, we count the frequency
					FPNode node = prefixPath.get(j);
					if(mapSupportBeta.get(node.itemID) == null){
						mapSupportBeta.put(node.itemID, pathCount);
					}else{
						mapSupportBeta.put(node.itemID, mapSupportBeta.get(node.itemID) + pathCount);
					}
				}
			}
			
			// (B) Construct beta's conditional FP-Tree
			FPTree treeBeta = new FPTree();
			// add each prefixpath in the FP-tree
			for(List<FPNode> prefixPath : prefixPaths){
				treeBeta.addPrefixPath(prefixPath, mapSupportBeta, relativeMinsup); 
			}  
			treeBeta.createHeaderList(mapSupportBeta); 
			
			// Mine recursively the Beta tree.
			if(treeBeta.root.childs.size() > 0){
				fpgrowth(treeBeta, beta, mapSupportBeta);
			}
		}
		
	}

	/**
	 * This method is for adding recursively all combinations of nodes in a path, concatenated with a given prefix,
	 * to the set of patterns found.
	 * @param nodeLink the first node of the path
	 * @param prefix  the prefix
	 * @param minsupportForNode the support of this path.
	 */
	private void addAllCombinationsForPathAndPrefix(FPNode node, Itemset prefix) {
		// We add the node to the prefix
		Itemset itemset = prefix.cloneItemset();
		itemset.addItem(node.itemID);

		itemset.setTransactioncount(node.counter); 

		sortItemset(itemset);
		frequentItemsets.addItemset(itemset, itemset.size());
		
		// recursive call if there is a node link
		if(node.nodeLink != null){
			addAllCombinationsForPathAndPrefix(node.nodeLink, prefix);
			addAllCombinationsForPathAndPrefix(node.nodeLink, itemset);
		}
	}
	
	public void sortItemset(Itemset itemset){
		Collections.sort(itemset.getItems(), new Comparator<Integer>() {
			public int compare(Integer o1,Integer o2) {
				return o1 - o2;
			}
		});
	}

	public void printStats() {
		System.out
				.println("=============  FP-GROWTH - STATS =============");
		long temps = endTime - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ contextSize);
		System.out.println(" Frequent itemsets count : " + frequentItemsets.getItemsetsCount()); 
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}
	
	public Itemsets getItemsets() {
		return frequentItemsets;
	}
}
