package ca.pfv.spmf.sequential_rules.cmrules;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining.Cluster;
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

	// Contexte
	private final List<Sequence> sequences = new ArrayList<Sequence>();

	// for clustering, the last item that was used to do the projection that
	// results in this database.
	private Cluster cluster = null;

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

	public void addSequence(String[] entiers) { //
		Sequence sequence = new Sequence(sequences.size());
		Itemset itemset = new Itemset();
		for (String entier : entiers) {
			if (entier.codePointAt(0) == '<') { // Timestamp
				//String valeur = entier.substring(1, entier.length() - 1);
			} else if (entier.equals("-1")) { // séparateur d'itemsets
				sequence.addItemset(itemset);
				itemset = new Itemset();
			} else if (entier.equals("-2")) { // indicateur de fin de séquence
				sequences.add(sequence);
			} else { // un item au format : id(valeurentiere) ou format : id
				// si l'item à une valeur entière, extraire la valeur
				// extraire la valeur associée à un item
				itemset.addItem(Integer.parseInt(entier));
			}
		}
	}

	public void addSequence(Sequence sequence) {
		sequences.add(sequence);
	}

	public void printContext() {
		System.out.println("============  CONTEXTE ==========");
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
		List<Integer> sizes = new ArrayList<Integer>();
		List<Integer> itemsetsizes = new ArrayList<Integer>();
		List<Integer> differentitems = new ArrayList<Integer>();
		List<Integer> appearXtimesbySequence = new ArrayList<Integer>();
		for(Sequence sequence : sequences){
			sizes.add(sequence.size());
			HashMap<Integer, Integer> mapIntegers = new HashMap<Integer, Integer>();
			for(Itemset itemset : sequence.getItemsets()){
				itemsetsizes.add(itemset.size());
				for(Integer item : itemset.getItems()){
					Integer count = mapIntegers.get(item);
					if(count == null){
						count = 0;
					}
					count = count +1;
					mapIntegers.put(item, count);
				}
			}
			differentitems.add(mapIntegers.entrySet().size());
			
			for(Entry<Integer, Integer> entry: mapIntegers.entrySet()){
				appearXtimesbySequence.add(entry.getValue());
			}
		}
		System.out.println("SIZE mean : " + calculateMean(sizes) 
				+ " stdD: " + calculateStdDeviation(sizes)+
				" var: " + calculateVariance(sizes));
		System.out.println("DIFFERENT ITEMS COUNT BY SEQUENCE mean : " + calculateMean(differentitems) 
				+ " stdD: " + calculateStdDeviation(differentitems)+
				" var: " + calculateVariance(differentitems));
		System.out.println("EACH ITEM APPEAR HOW MANY TIMES BY SEQUENCE mean : " + calculateMean(appearXtimesbySequence) 
				+ " stdD: " + calculateStdDeviation(appearXtimesbySequence)+
				" var: " + calculateVariance(appearXtimesbySequence));
		System.out.println("ITEMSET SIZE mean : " + calculateMean(itemsetsizes) 
				+ " stdD: " + calculateStdDeviation(itemsetsizes)+
				" var: " + calculateVariance(itemsetsizes));
	}
	
	public static double calculateMean(List<Integer> list){
		double sum=0;
		for(Integer val : list){
			sum += val;
		}
		return sum / list.size();
	}
	
	public static double calculateStdDeviation(List<Integer> list){
		double deviation =0;
		double mean = calculateMean(list);
		for(Integer val : list){
			deviation += Math.pow(mean - val, 2);
		}
		return Math.sqrt(deviation / list.size());
	}
	
	public static double calculateVariance(List<Integer> list){
		double deviation =0;
		double mean = calculateMean(list);
		for(Integer val : list){
			deviation += Math.pow(mean - val, 2);
		}
		return Math.pow(Math.sqrt(deviation / list.size()), 2);
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

	// --------------- For clustering
	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	
	public void loadFileSignLanguage(String fileToPath, int i) {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(fileToPath));
			myInput = new BufferedReader(new InputStreamReader(fin));
			String oldUtterance = "-1";
			Sequence sequence = null;
			int seqid =0;
			while ((thisLine = myInput.readLine()) != null) {
				if(thisLine.length() >= 1 && thisLine.charAt(0) != '#'){
					String []tokens = thisLine.split(" ");
					String currentUtterance = tokens[0];
					if(!currentUtterance.equals(oldUtterance)){
						if(sequence != null){
							sequences.add(sequence);
						}
						sequence = new Sequence(seqid++);
						oldUtterance = currentUtterance;
					}
					for(int j=1; j< tokens.length; j++){
						int character = Integer.parseInt(tokens[j]);
						if(character == -11 || character == -12){
							continue;
						}
						Itemset itemset = new Itemset();
						itemset.addItem(character);
						sequence.addItemset(itemset);
					}
				}
			}
			sequences.add(sequence);
			System.out.println(sequence.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
