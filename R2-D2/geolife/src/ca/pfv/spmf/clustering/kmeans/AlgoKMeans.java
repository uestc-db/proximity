package ca.pfv.spmf.clustering.kmeans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modified version of the K-means algorithm From Wikipedia : The K-means
 * algorithm steps are (J. MacQueen, 1967): * Choose the number of clusters, k.
 * * Randomly generate k clusters and determine the cluster centers, or directly
 * generate k random points as cluster centers. * Assign each point to the
 * nearest cluster center. * Recompute the new cluster centers. * Repeat the two
 * previous steps until some convergence criterion is met (usually that the
 * assignment hasn't changed).
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

public class AlgoKMeans {

	private List<Cluster> clusters = null;
	private final static Random random = new Random(System.currentTimeMillis());
	
	// stats
	private double maxMemory;
	private long startTimestamp;
	private long endTimestamp;
	private long iterationCount;

	public AlgoKMeans() { 
		
	}
	
	

	public List<Cluster> runAlgorithm(String inputFile, int k) throws NumberFormatException, IOException {
		startTimestamp =  System.currentTimeMillis();
		iterationCount =0;
		
		List<Vector> vectors = new ArrayList<Vector>();
		double minValue = Integer.MAX_VALUE;
		double maxValue = 0;
		
		// read the vectors from the input file
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String line;
		while (((line = reader.readLine()) != null)) { // for each transaction
			String[] lineSplited = line.split(" ");
			double [] vector = new double[lineSplited.length];
			for (int i=0; i< lineSplited.length; i++) { 
				double value = Double.parseDouble(lineSplited[i]);
				if(value < minValue){
					minValue = value;
				}
				if(value > maxValue){
					maxValue = value;
				}
				vector[i] = value;
			}
			vectors.add(new Vector(vector));
		}
		reader.close();

		clusters = new ArrayList<Cluster>();
		
		int vectorsSize = vectors.get(0).data.length;
		
		// Special case : only one item
		if (vectors.size() == 1) {
			Vector vector = vectors.get(0);
			Cluster cluster = new Cluster(vectorsSize);
			cluster.addVector(vector);
			clusters.add(cluster);
			return clusters;
		}
		
		// (1) Randomly generate k empty clusters with a random mean (cluster
		// center)
		for(int i=0; i< k; i++){
			Vector meanVector = generateRandomVector(minValue, maxValue, vectorsSize);
			Cluster cluster = new Cluster(vectorsSize);
			cluster.setMean(meanVector);
			clusters.add(cluster);
		}

		// (2) Repeat the two next steps until the assignment hasn't changed
		boolean changed;

		while(true) {
			iterationCount++;
			changed = false;
			// (2.1) Assign each point to the nearest cluster center.

			// / for each vector
			for (Vector vector : vectors) {
				// find the nearest cluster and the cluster containing the item
				Cluster nearestCluster = null;
				Cluster containingCluster = null;
				double distanceToNearestCluster = Double.MAX_VALUE;

				for (Cluster cluster : clusters) {
					double distance = euclideanDistance(cluster.getmean(), vector);
					if (distance < distanceToNearestCluster) {
						nearestCluster = cluster;
						distanceToNearestCluster = distance;
					}
					if (cluster.contains(vector)) {
						containingCluster = cluster;
					}
				}

				if (containingCluster != nearestCluster) {
					if (containingCluster != null) {
						containingCluster.remove(vector);
					}
					nearestCluster.addVector(vector);
					changed = true;
				}
			}

			checkMemory();
			
			if(!changed){     // exit condition for main loop
				break;
			}
			
			// (2.2) Recompute the new cluster means
			for (Cluster cluster : clusters) {
				cluster.recomputeClusterMean();
			}
		} 

		checkMemory();
		endTimestamp =  System.currentTimeMillis();
		
		return clusters;
	}

	private Vector generateRandomVector(double minValue, double maxValue,
			int vectorsSize) {
		double[] vector = new double[vectorsSize];
		for(int i=0; i < vectorsSize; i++){
			vector[i] = (random.nextDouble() * (maxValue - minValue)) + minValue;
		}
		return new Vector(vector);
	}

	private double euclideanDistance(Vector vector1, Vector vector2) {
		double sum =0;
		for(int i=0; i< vector1.data.length; i++){
			sum += Math.pow(vector1.data[i] - vector2.data[i], 2);
		}
		return Math.sqrt(sum);
	}

	public void printStatistics() {
		System.out.println("========== KMEANS - STATS ============");
		System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
				+ " ms");
		System.out.println(" Max memory:" + maxMemory + " mb ");
		System.out.println(" Iteration count: " + iterationCount);
		System.out.println("=====================================");
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

}
