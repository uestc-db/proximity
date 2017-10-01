package ca.pfv.spmf.sequential_rules.trulegrowth_with_strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of a sequence database. Each sequence should have a unique id.
 * See examples in /test/ directory for the format of input files.
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
public class SequenceDatabase {
	
	int seqid = 0;

	private final List<Sequence> sequences = new ArrayList<Sequence>();

	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				// si la ligne n'est pas un commentaire
				if (thisLine.charAt(0) != '#') {
					// ajoute une séquence
					addSequence(thisLine.split(" "));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}
	
	public void addSequence(String[] symbols) { //
		Sequence sequence = new Sequence(sequences.size());
		Itemset itemset = new Itemset();
		for (String item : symbols) {
			if (item.codePointAt(0) == '<') { // Timestamp
//				String valeur = entier.substring(1, entier.length() - 1);
			} else if (item.equals("-1")) { // itemset separator
				sequence.addItemset(itemset);
				itemset = new Itemset();
			} else if (item.equals("-2")) { // end of a sequence
				sequences.add(sequence);
			} else { // an item
				itemset.addItem(item);
				// make sure that itemsets are sorted by lexical order (by hashCode())
				Collections.sort(itemset.getItems(), new Comparator<String>() {
					public int compare(String arg0, String arg1) {
						return arg0.hashCode() - arg1.hashCode();
					}
				});
			}
		}
	}
	
//	public void addSequence(String[] symbols) { //
//		Sequence sequence = new Sequence(sequences.size());
//		for (String item : symbols) {
//			sequence.addItemset(new Itemset(item));
//		}
//		sequences.add(sequence);
//	}

	public void addSequence(Sequence sequence) {
		sequences.add(sequence);
	}

	public void printDatabase() {
		System.out.println("============  Database ==========");
		for (Sequence sequence : sequences) { // pour chaque objet
			System.out.print(sequence.getId() + ":  ");
			sequence.print();
			System.out.println("");
		}
	}
	
	public void printDatabaseStats() {
		System.out.println("============  STATS ==========");
		System.out.println("Number of sequences : " + sequences.size());
		// average size of sequence
		long size = 0;
		for(Sequence sequence : sequences){
			size += sequence.size();
		}
		double meansize = ((float)size) / ((float)sequences.size());
		System.out.println("mean size" + meansize);
	}

	public String toString() {
		StringBuffer r = new StringBuffer();
		for (Sequence sequence : sequences) { // pour chaque objet
			r.append(sequence.getId());
			r.append(":  ");
			r.append(sequence.toString());
			r.append('\n');
		}
		return r.toString();
	}

	public int size() {
		return sequences.size();
	}

	public List<Sequence> getSequences() {
		return sequences;
	}

	public Set<Integer> getSequenceIDs() {
		Set<Integer> ensemble = new HashSet<Integer>();
		for (Sequence sequence : getSequences()) {
			ensemble.add(sequence.getId());
		}
		return ensemble;
	}



}
