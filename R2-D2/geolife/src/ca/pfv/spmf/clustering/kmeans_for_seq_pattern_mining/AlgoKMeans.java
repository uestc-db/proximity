package ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ca.pfv.spmf.sequentialpatterns.ItemValued;

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

public class AlgoKMeans extends AbstractAlgoClustering {

	private int k;
	private final static Random random = new Random(System.currentTimeMillis());

	public AlgoKMeans(int k) {
		this.k = k;
	}

	/**
	 * 
	 * @param input
	 * @param k
	 *            : le nombre de clusters
	 * @return
	 */
	public List<Cluster> runAlgorithm(List<ItemValued> input) {
		List<Cluster> clusters = new ArrayList<Cluster>();

		// Cas particulier : 1 seul item
		if (input.size() == 1) {
			ItemValued item = input.get(0);
			Cluster cluster = new Cluster(item);
			cluster.addItem(item);
			clusters.add(cluster);
			return clusters;
		}

		// (1) Randomly generate k empty clusters with a random median (cluster
		// center)

		// (1.1) Choose the higher and lower values for generating a median
		double higher = input.get(0).getId();
		double lower = input.get(0).getId();
		for (ItemValued item : input) {
			if (item.getValue() > higher) {
				higher = item.getValue();
			}
			if (item.getValue() < lower) {
				lower = item.getValue();
			}
		}

		// Special case : all items have the same values, so we return only one
		// cluster.
		if (higher == lower) {
			Cluster cluster = new Cluster(input);
			clusters.add(cluster);
			return clusters;
		}

		// (1.2) Generate the k empty clusters with random median
		for (int i = 0; i < k; i++) {
			// generate random median

			double median = random.nextInt((int) (higher - lower)) + lower;
			// create the cluster
			Cluster cluster = new Cluster(median);
			clusters.add(cluster);
		}

		// (2) Repeat the two next steps until the assignment hasn't changed
		boolean changed;

		do {
			changed = false;
			// (2.1) Assign each point to the nearest cluster center.

			// / for each item
			for (ItemValued item : input) {
				// find the nearest cluster and the cluster containing the item
				Cluster nearestCluster = null;
				Cluster containingCluster = null;
				double distanceToNearestCluster = Double.MAX_VALUE;

				for (Cluster cluster : clusters) {
					double distance = medianDistance(cluster, item);
					if (distance < distanceToNearestCluster) {
						nearestCluster = cluster;
						distanceToNearestCluster = distance;
					}
					if (cluster.containsItem(item)) {
						containingCluster = cluster;
					}
				}

				if (containingCluster != nearestCluster) {
					if (containingCluster != null) {
						removeItem(containingCluster.getItems(), item);
						// fixed 2010 because before I was using "remove" from
						// List but ItemValued defines "equals".
					}
					nearestCluster.addItem(item);
					changed = true;
				}
			}

			// (2.2) Recompute the new cluster medians
			for (Cluster cluster : clusters) {
				cluster.recomputeClusterMedian();
			}

		} while (changed);

		// Computer min and max for all clusters
		for (Cluster cluster : clusters) {
			cluster.computeHigherAndLower();
		}

		return clusters;
	}

	private void removeItem(List<ItemValued> items, ItemValued item) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i) == item) {
				items.remove(i);
			}
		}
	}

	private double medianDistance(Cluster cluster1, ItemValued item) {
		return Math.abs(cluster1.getMedian() - item.getValue());
	}

	public void setK(int k) {
		this.k = k;
	}

}
