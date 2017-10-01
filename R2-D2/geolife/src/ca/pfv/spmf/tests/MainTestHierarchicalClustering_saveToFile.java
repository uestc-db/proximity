package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.clustering.hierarchical_clustering.AlgoHierarchicalClustering;

public class MainTestHierarchicalClustering_saveToFile {
	
	public static void main(String []args) throws NumberFormatException, IOException{
		
		String input = fileToPath("configKmeans.txt");
		String output = "c:\\patterns\\clusters.txt";
		int maxdistance = 4;
		
		// Apply the algorithm
		AlgoHierarchicalClustering algo = new AlgoHierarchicalClustering();  // we request 3 clusters
		algo.runAlgorithm(input, maxdistance);
		algo.printStatistics();
		algo.saveToFile(output);

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestHierarchicalClustering_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
	
	
}
