package ca.pfv.spmf.clustering.hierarchical_clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Based on the description of Hierarchical Clustering Algorithms from
 * http://home.dei.polimi.it/matteucc/Clustering/tutorial_html/hierarchical.html
 * 
 * This is a Hierarchical Clustering with a constant "threshold" that indicate
 * the maximal distance between two clusters to group them.
 * 
 * The distance between two clusters is calculated as the distance between the
 * medians of the two clusters.
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

public class AlgoHierarchicalClustering {

	private double maxDistance =0;
	List<Cluster> clusters = null;
	
	// stats
	private double maxMemory;
	private long startTimestamp;
	private long endTimestamp;
	private long iterationCount;

	public AlgoHierarchicalClustering() {
	}

	public List<Cluster> runAlgorithm(String inputFile, double maxDistance) throws NumberFormatException, IOException {
		startTimestamp = System.currentTimeMillis();
		this.maxDistance = maxDistance;
		
		clusters = new ArrayList<Cluster>();
		
		// read the vectors from the input file
		// and add each vector to an individual cluster.
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String line;
		while (((line = reader.readLine()) != null)) { // for each transaction
			String[] lineSplited = line.split(" ");
			double [] vector = new double[lineSplited.length];
			for (int i=0; i< lineSplited.length; i++) { 
				double value = Double.parseDouble(lineSplited[i]);
				vector[i] = value;
			}
			Vector theVector = new Vector(vector);
			Cluster cluster = new Cluster(vector.length);
			cluster.addVector(theVector);
			cluster.setMean(theVector);
			clusters.add(cluster);
		}
		reader.close();

		// (2) Loop to combine the two closest clusters into a bigger cluster
		// until no clusters can be combined.
		boolean changed = false;
		do {
			changed = mergeTheClosestCluster();
			checkMemory();
		} while (changed);

		endTimestamp = System.currentTimeMillis();
		return clusters;
	}

	private boolean mergeTheClosestCluster() {
		Cluster clusterToMerge1 = null;
		Cluster clusterToMerge2 = null;
		double minClusterDistance = Integer.MAX_VALUE;

		// find the two closest clusters with distance > threshold
		for (int i = 0; i < clusters.size(); i++) {
			for (int j = i + 1; j < clusters.size(); j++) {
				
				double distance = euclideanDistance(clusters.get(i).getmean(), clusters.get(j).getmean());
				if (distance < minClusterDistance && distance < maxDistance) {
					minClusterDistance = distance;
					clusterToMerge1 = clusters.get(i);
					clusterToMerge2 = clusters.get(j);
				}
			}
		}

		// if no close clusters were found, return false
		if (clusterToMerge1 == null) {
			return false;
		}

		// else, merge the two closest clusters
		for(Vector vector : clusterToMerge2.getVectors()){
			clusterToMerge1.addVector(vector);
		}
		clusterToMerge1.recomputeClusterMean();
		clusters.remove(clusterToMerge2);

		iterationCount++;
		return true;
	}

	private double euclideanDistance(Vector vector1, Vector vector2) {
		double sum =0;	
		for(int i=0; i< vector1.data.length; i++){
			sum += Math.pow(vector1.data[i] - vector2.data[i], 2);
		}
		return Math.sqrt(sum);
	}
	
	private void checkMemory() {
		double currentMemory = ((double) (Runtime.getRuntime()
				.totalMemory() / 1024) / 1024)
				- ((double) (Runtime.getRuntime().freeMemory() / 1024) / 1024);
		if (currentMemory > maxMemory) {
			maxMemory = currentMemory;
		}
	}
	

	public void saveToFile(String output) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		for(int i=0; i< clusters.size(); i++){
			if(clusters.get(i).getVectors().size() >= 1){
				writer.write(clusters.get(i).toString());
				if(i < clusters.size()-1){
					writer.newLine();
				}
			}
		}
		writer.close();
	}

	
	public void printStatistics() {
		System.out.println("========== HIERARCHICAL CLUSTERING - STATS ============");
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:" + maxMemory + " mb ");
		System.out.println(" Iteration count: " + iterationCount);
		System.out.println("=====================================");
	}

}
