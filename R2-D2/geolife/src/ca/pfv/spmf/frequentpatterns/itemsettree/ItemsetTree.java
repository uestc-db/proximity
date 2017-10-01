package ca.pfv.spmf.frequentpatterns.itemsettree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * An implementation of the Itemset-tree
 * 
 * It is based on the description in:
 * 
 *     Kubat, M., Hafez, A., Raghavan, V. V., Lekkala, J. R., Chen, W. K. (2003) 
 *     Itemset Trees for Targeted Association Querying. Proc. of ICDE 2003.
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

public class ItemsetTree {
	
	ItemsetTreeNode root = null;

	// statistics about tree construction
	double maxMemory = 0;
	long startTimestamp;
	long endTimestamp;

	List<List<Integer>> database; // in-memory database

	public ItemsetTree() {
		
	}

	public void buildTree(String input)
			throws IOException {
		startTimestamp = System.currentTimeMillis();
		
		maxMemory = 0;
		
		root = new ItemsetTreeNode(null, 0);

		// scan the database
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		while (((line = reader.readLine()) != null)) { // for each transaction
			String[] lineSplited = line.split(" ");
			
			int[] itemset = new int[lineSplited.length];
			for (int i=0; i< lineSplited.length; i++) { // for each item in the transaction
				// increase the support count of the item
				itemset[i] = Integer.parseInt(lineSplited[i]);
			}
//			printTree();
			construct(root, itemset);
		}
		reader.close();
		
		checkMemory();
		// close the file
		endTimestamp = System.currentTimeMillis();
	}
	
	public void addTransaction(int[] transaction){
		construct(root, transaction);
	}


	private void construct(ItemsetTreeNode r, int[] s) {
		int[] sr = r.itemset;
		
		if(same(s, sr)){
			r.support++;
			return;
		}
		if(ancestorOf(s, sr)){
			ItemsetTreeNode newNode = new ItemsetTreeNode(s, r.support +1);
			newNode.childs.add(r);
			r.parent.childs.remove(r);
			r.parent.childs.add(newNode);
			r.parent = newNode;
			return;
		}
		
		// ELSE IF calculate largest common ancestor
		int[] l = getLargestCommonAncestor(s, sr);
		if(l != null){
			ItemsetTreeNode newNode = new ItemsetTreeNode(l, r.support +1);
			newNode.childs.add(r);
			r.parent.childs.remove(r);
			r.parent.childs.add(newNode);
			r.parent = newNode;
			// append second children
			ItemsetTreeNode newNode2 = new ItemsetTreeNode(s, 1);
			newNode.childs.add(newNode2);
			newNode2.parent = newNode;
			return;
		}
		
		// ELSE
		int indexLastItemOfR = (sr == null)? 0 : sr.length;
		r.support++;
		for(ItemsetTreeNode ci : r.childs){
			
			if(same(s, ci.itemset)){ // case 2
				ci.support++;
				return;
			}
			
			if(ancestorOf(s, ci.itemset)){ // case 3
				ItemsetTreeNode newNode = new ItemsetTreeNode(s, ci.support+ 1);
				newNode.childs.add(ci);
				newNode.parent = r;
				r.childs.remove(ci);
				ci.parent = newNode;
				return;
			}
			
			if(ancestorOf(ci.itemset, s)){ // case 4
				construct(ci, s);
				return;
			}

			// case 5
			// if ci and s have a common ancestor that is larger than r:
			if(ci.itemset[indexLastItemOfR] == s[indexLastItemOfR]){
				int[] ancestor = getLargestCommonAncestor(s, ci.itemset);
				ItemsetTreeNode newNode = new ItemsetTreeNode(ancestor, ci.support+ 1);
				newNode.parent = r;
				r.childs.add(newNode);
				newNode.childs.add(ci);
				ci.parent = newNode;
				r.childs.remove(ci);
				ItemsetTreeNode newNode2 = new ItemsetTreeNode(s, 1);
				newNode2.parent = newNode;
				newNode.childs.add(newNode2);
				return;
			}
			
		}
		
		// otherwise, case 1
		ItemsetTreeNode newNode = new ItemsetTreeNode(s, 1);
		newNode.parent = r;
		r.childs.add(newNode);
		
		
	}

	private int[] getLargestCommonAncestor(int[] itemset1, int[] itemset2) {
		if(itemset2 == null || itemset1 == null){
			return null;
		}
	
		int maxI = itemset1.length > itemset2.length ? itemset2.length : itemset1.length;
		int count = 0;
		for(int i=0; i< maxI - 1; i++){   //  HERE WE DO maxI-1 because we don't want that the maximum ancestor is equal to itemset1 or itemset2
			if(itemset1[i] != itemset2[i]){
				break;
			}else{
				count++;
			}
		}
		if(count >0){
			int[] common = new int[count];
			System.arraycopy(itemset1, 0, common, 0, count);
			return common;
		}
		else{
			return null;
		}
	}

	private boolean ancestorOf(int[] itemset1, int[] itemset2) {
		if(itemset2 == null){
			return false;
		}
		if(itemset1 == null){
			return true;
		}
		
		if(itemset1.length >= itemset2.length){
			return false;
		}
		for(int i=0; i< itemset1.length; i++){
			if(itemset1[i] != itemset2[i]){
				return false;
			}
		}
		return true;
	}

	private boolean same(int[] itemset1, int[] itemset2) {
		if(itemset2 == null || itemset1 == null){
			return false;
		}		
		
		if(itemset1.length != itemset2.length){
			return false;
		}
		for(int i=0; i< itemset1.length; i++){
			if(itemset1[i] != itemset2[i]){
				return false;
			}
		}
		return true;
	}

	private void checkMemory() {
		double currentMemory = ((double) ((double) (Runtime.getRuntime()
				.totalMemory() / 1024) / 1024))
				- ((double) ((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024));
		if (currentMemory > maxMemory) {
			maxMemory = currentMemory;
		}
	}


	public void printStatistics() {

		System.out.println("========== ITEMSET TREE CONSTRUCTION - STATS ============");
		System.out.println(" Tree construction time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:" + maxMemory);
		System.out.println("=====================================");
	}

	public void printTree() {
		System.out.println(root.toString(new StringBuffer(),""));
	}
	
	public String toString() {
		return root.toString(new StringBuffer(), "");
	}

	public int getSupportOfItemset(int[] s) {
		return count(s, root);
	}

	// This is implemented based on the algorithm "count" of Table 2 in the paper by Kubat et al.
	// Note that there was a few problem in the algorithm in the paper.
	// I had to change > by < in :  ci.itemset[ci.itemset.length -1] < s[s.length -1]){ 
	// also the count was not correct so i had to change the way it counted the support a little bit
	// by using += instead of return.
	private int count(int[] s, ItemsetTreeNode root) {
		int count =0;
		for(ItemsetTreeNode ci : root.childs){
			if(ci.itemset[0]  <= s[0]){
				if(includedIn(s, ci.itemset)){
					count += ci.support;
				}else if(ci.itemset[ci.itemset.length -1] < s[s.length -1]){  
					count += count(s, ci);
				}
			}
		}
		return count;
	}
	
	public HashTable getFrequentItemsetSubsuming(int [] s){
		// create a hash table to contain the itemsets to be more efficient
		HashTable hash = new HashTable(1000);
		
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}
		selectiveMining(s, seti, root, hash);
		return hash;
	}

	private void selectiveMining(int[] s, HashSet<Integer> seti,  ItemsetTreeNode t, HashTable hash) {
		for(ItemsetTreeNode ci : t.childs){
			if(ci.itemset[0]  <= s[0]){
				if(includedIn(s, ci.itemset)){
					// This part is not explained correctly in the paper, 
					// i had to figure it out by myself and fix it.
					if(ci.childs.size() ==0){
						hash.put(s, ci.support);
						recursiveAdd(s, seti, ci.itemset, ci.support, hash, 0);
					}else{
						selectiveMining(s, seti, ci, hash);
					}
					
				}else if(ci.itemset[ci.itemset.length -1] < s[s.length -1]){  
					selectiveMining(s, seti, ci, hash);
				}
			}
		}
	}

	private void recursiveAdd(int[] s, HashSet<Integer> seti, int[] ci, int cisupport, HashTable hash, int pos) {
		if(pos >= ci.length){
			return;
		}
		
		if(!seti.contains(ci[pos])){
			int[] newS = new int[s.length+1];
			int j=0;
			boolean added = false;
			for(Integer item : s){
				if(added || item < ci[pos]){
					newS[j++] = item;
				}else{
					newS[j++] = ci[pos];
					newS[j++] = item;
					added = true;
				}
			}
			if(j < s.length+1){
				newS[j++] = ci[pos];
			}
			
			hash.put(newS, cisupport);
			recursiveAdd(newS, seti, ci, cisupport, hash, pos+1);
		}
		recursiveAdd(s, seti, ci, cisupport, hash, pos+1);
		
	}

	private boolean includedIn(int[] itemset1, int[] itemset2) {
		int count = 0;
		for(int i=0; i< itemset2.length; i++){
			if(itemset2[i] == itemset1[count]){
				count++;
				if(count == itemset1.length){
					return true;
				}
			}
		}
		return false;
	}

	public HashTable getFrequentItemsetSubsuming(int[] is, int minsup) {
		HashTable hashTable = getFrequentItemsetSubsuming(is);
		// after finding the itemsets we do a loop to remove those with a support lower than minsup,
		// This does not seems efficient but that is how the authors of the paper do it.
		for(List<Itemset> list : hashTable.table){
			if(list != null){
				Iterator<Itemset> it = list.iterator();
				while (it.hasNext()) {
					Itemset itemset = (Itemset) it.next();
					if(itemset.support < minsup){
						it.remove();
					}
				}
			}
		}
		return hashTable;
	}

	public List<AssociationRule> generateRules(int[] s, int minsup, double minconf) {
		List<AssociationRule> rules = new ArrayList<AssociationRule>();
		
		HashSet<Integer> seti = new HashSet<Integer>();
		for(int i=0; i< s.length; i++){
			seti.add(s[i]);
		}

		int suppS = getSupportOfItemset(s);
		
		 HashTable frequentItemsets = getFrequentItemsetSubsuming(s, minsup);
		 for(List<Itemset> list : frequentItemsets.table){
			if(list != null){
				for(Itemset c : list){
					if(c.size() == s.length){  // if the same itemset
						continue;
					}
					// try to generate some rules
					int[] l = new int[c.itemset.length - s.length];
					int pos =0;
					for(Integer item : c.itemset){
						if(!seti.contains(item)){
							l[pos++] = item;
						}
					}
					// calculate confidence
					int suppC = getSupportOfItemset(c.itemset);
					
					// Note: the formula for calculating the confidence is wrong in the paper.
					// It is not g(l) / g(c) but it should be g(c) / g(s).
					double conf = (double)suppC / suppS;  
					if(conf >= minconf){
						AssociationRule rule = new AssociationRule();
						rule.itemset1 = s;
						rule.itemset2 = l;
						rule.support = suppC;
						rule.confidence = conf;
						rules.add(rule);
					}
					
				}
			}
		}
		return rules;
	}
}
