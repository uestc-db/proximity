package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.clustering.kmeans.AlgoKMeans;

public class MainTestKMeans_saveToFile {
	
	public static void main(String []args) throws NumberFormatException, IOException{
		
		String input = fileToPath("configKmeans.txt");
		String output = "c:\\patterns\\clusters.txt";
		int k=3;
		
		// Apply the algorithm
		AlgoKMeans algoKMeans = new AlgoKMeans();  // we request 3 clusters
		algoKMeans.runAlgorithm(input, k);
		algoKMeans.printStatistics();
		algoKMeans.saveToFile(output);

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestKMeans_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
	
	
}
