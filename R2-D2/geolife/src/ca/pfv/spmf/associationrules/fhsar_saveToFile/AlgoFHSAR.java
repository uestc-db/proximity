package ca.pfv.spmf.associationrules.fhsar_saveToFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ca.pfv.spmf.general.datastructures.redblacktree.RedBlackTree;

/**
 * An implementation of the FHSAR algorithm for hiding sensitive association rules in a
 * transaction database. This algorithm is described in the paper:
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
public class AlgoFHSAR {

	// variables for statistics
	int tidcount = 0;
	long startTimestamp = 0;
	long endTimeStamp = 0;
	
	// the minimum suport
	private int minSuppRelative;

	public void runAlgorithm(String input, String inputSAR, String output,
			double minsup, double minconf) throws IOException {
		startTimestamp = System.currentTimeMillis();
		
		List<Rule> sensitiveRules = new ArrayList<Rule>();  // the sensitive rules
		List<Set<Integer>> transactions = new ArrayList<Set<Integer>>();   // the transactions from the database
		
		// a red-black tree to store the transactions ordered by their wi value
		RedBlackTree<Transaction> PWT = new RedBlackTree<Transaction>();
		
		// STEP1 : Read the sensitive association rules from the file into memory
		readSensitiveRulesIntoMemory(inputSAR, sensitiveRules);
		
		// STAGE 1 of the FHSAR algorithm
		// Read the database into memory.
		// At the same time, we will calculate the wi for each transaction in the database
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(input));
		tidcount=0;
		while( ((line = reader.readLine())!= null)){ // for each transaction
			// we ignore lines starting with "#" because they are comments in the file
			if(line.charAt(0) == '#'){
				continue;
			}
			
			String[] lineSplited = line.split(" ");
			
			// we will check if each association rule is contained in the transaction.
			Set<Integer> transaction = new HashSet<Integer>(lineSplited.length); // the items in this transaction
			boolean thereIsARuleSupportedByTransaction = false;  // flag to know if at least one sensitive rule appear in this transaction
			List<Rule> rulesContained = new ArrayList<Rule>();  // the list of sensitive rules contained in this transaction
			
			for(Rule rule : sensitiveRules){
				Set<Integer> matchLeft = new HashSet<Integer>();
				Set<Integer> matchRight = new HashSet<Integer>();
				// for each item
loop:			for(int i=0; i<lineSplited.length; i++){
					int item = Integer.parseInt(lineSplited[i]);
					transaction.add(item);
					
					// if the left side of this sensitive rule matches with this transaction
					if(matchLeft.size() != rule.leftSide.size() && rule.leftSide.contains(item)){
						matchLeft.add(item);
						if(matchLeft.size() == rule.leftSide.size()){
							rule.leftSideCount++;
						}
					}  // else if the right side of this sensitive rule matches with this transaction
					else if(matchRight.size() != rule.rightSide.size() && rule.rightSide.contains(item)){
						matchRight.add(item);
					}
					// if the rule completely matches with this transaction...
					if(matchLeft.size() == rule.leftSide.size()  && matchRight.size() == rule.rightSide.size()){
						rule.count++;
						rulesContained.add(rule);
						thereIsARuleSupportedByTransaction = true;
						break loop;
					}
				}
			}
			
			// if at least a rule is supported by this transaction,
			//  we calculate the wi for the transaction and then
			// we will insert the transaction with its wi into PWT.
			if(thereIsARuleSupportedByTransaction){
				
				// (1) calculate MIC
				Map<Integer, Integer> mapItemCount = new HashMap<Integer,Integer>();
				for(Rule rule : rulesContained){
					for(Integer item : rule.leftSide){
						Integer count = mapItemCount.get(item);
						if(count == null){
							count = 0;
						}
						mapItemCount.put(item, count+1);
					}
					for(Integer item : rule.rightSide){
						Integer count = mapItemCount.get(item);
						if(count == null){
							count = 0;
						}
						mapItemCount.put(item, count+1);
					}
				}
				//  find the item that has the max count (represented as max(|rk| in the paper) and keep it
				int MIC = -1;
				int maxItem = -1;
				for(Entry<Integer, Integer> entry: mapItemCount.entrySet()){
					if(entry.getValue() > MIC){
						maxItem = entry.getKey();
						MIC = entry.getValue();
					}
				}
				double wi = MIC / Math.pow(2, transaction.size() - 1);
				// add transaction to PWT
				PWT.add(new Transaction(transaction, wi, maxItem));	
			}
			tidcount++;
			transactions.add(transaction);
		}
		reader.close();
		
		// we transform the minsup parameter into a relative value.
		minSuppRelative = (int) Math.ceil(minsup * tidcount);
		
		// STAGE 2 of the FHSAR algorithm
		// This part is not well-explained in the paper so it might not be exactly like
		// what the authors did. But the main idea is the same.
		// We will delete items until the sensitive association rules fell below the threshold.
		while(sensitiveRules.isEmpty() == false){
			// We take the transaction that has the highest wi from PWT.
			Transaction td = PWT.popMaximum();
			// Select the item with the maximum |Rk| for this transaction
			// (we don't choose randomly in my implementation)
			int maxItem = td.maxItem;
			
			// Now we calculate the new wi for this transaction if we remove the item.
			// At the same time we will update the support count of the sensitive association rules.
			Map<Integer, Integer> mapItemCount = new HashMap<Integer,Integer>();
			boolean atLeastOneRule = false;
			// for each sensitive rule remaining
			for(Rule rule : sensitiveRules){
				if(td.items.containsAll(rule.leftSide) && td.items.containsAll(rule.rightSide)){
					if(rule.leftSide.contains(maxItem)){
						rule.count--;
						rule.leftSideCount--;
					}else if(rule.rightSide.contains(maxItem)){
						rule.count--;
					}else{
						atLeastOneRule = true;
						for(Integer item : rule.leftSide){
							Integer count = mapItemCount.get(item);
							if(count == null){
								count = 0;
							}
							mapItemCount.put(item, count+1);
						}
						for(Integer item : rule.rightSide){
							Integer count = mapItemCount.get(item);
							if(count == null){
								count = 0;
							}
							mapItemCount.put(item, count+1);
						}
					}
				}
			}
			
			// we remove the item
//			System.out.println("remove " + maxItem + " from " + td.items);
			td.items.remove(maxItem);  
			
			// remove rules that are below the thresholds
			Iterator<Rule> iter = sensitiveRules.iterator();
			while (iter.hasNext()) {
				Rule rule = (Rule) iter.next();
				if(rule.count < minSuppRelative  || ((rule.count / (double)rule.leftSideCount) < minconf)){
					iter.remove();
				}
			}

			// if at least one sensitive rule is STILL contained in this transaction, we need to update
			// the wi  and add this transaction again in PWT.
			if(atLeastOneRule){
				int MIC = -1;
				int newMaxItem = -1;
				for(Entry<Integer, Integer> entry: mapItemCount.entrySet()){
					if(entry.getValue() > MIC){
						newMaxItem = entry.getKey();
						MIC = entry.getValue();
					}
				}
				double wi = MIC / Math.pow(2, td.items.size() - 1);
				td.wi = wi;
				td.maxItem = newMaxItem;
				PWT.add(td);	
			}
		}
		
		// Now, write the transformed transaction database to disk!
		BufferedWriter writer = new BufferedWriter(new FileWriter(output)); 
		for(Set<Integer> transaction : transactions){
			List<Integer> sorted = new ArrayList<Integer>(transaction);
			Collections.sort(sorted);
			for(int i=0; i< sorted.size(); i++){
				if(i > 0){
					writer.write(" " + sorted.get(i));
				} else{
					writer.write("" + sorted.get(i));
				}
			}
			writer.newLine();
		}
		writer.close();
		
		endTimeStamp = System.currentTimeMillis();
	}

	private void readSensitiveRulesIntoMemory(String inputSAR, List<Rule> rules)
			throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(inputSAR));
		String line;
		while( ((line = reader.readLine())!= null)){ // for each rule   4 ==> 5
			String[] lineSplited = line.split("==> ");
			// read left side
			String [] leftStrings = lineSplited[0].split(" ");
			String [] rightStrings = lineSplited[1].split(" ");
			Rule rule = new Rule();
			for(String string : leftStrings){
				rule.leftSide.add(Integer.parseInt(string));
			}
			for(String string : rightStrings){
				rule.rightSide.add(Integer.parseInt(string));
			}
			rules.add(rule);
		}
		reader.close();
	}

	public void printStats() {
		System.out.println("=============  FSHAR - STATS =============");
		System.out.println(" Transactions count from original database : " + tidcount);
		System.out.println(" minsup : " + minSuppRelative + " transactions");
		System.out.println(" Total time ~ " + (endTimeStamp - startTimestamp)+ " ms");
		System.out.println("============================================");
	}
}