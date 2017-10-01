package ca.pfv.spmf.tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.fpgrowth_saveToFile.AlgoFPGrowth;

/**
 * Class to test the FPGROWTH algorithm (this version saves the result to a file)
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestFPGrowth_saveToFile {

	public static void main(String [] arg) throws FileNotFoundException, IOException{
		// Loading the binary context
		String input = fileToPath("contextPasquier99.txt");  // the database
		String output = "C://patterns//frequent_itemsets.txt";  // the path for saving the frequent itemsets found
		
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)

		// Applying the FPGROWTH algorithmMainTestFPGrowth.java
		AlgoFPGrowth algo = new AlgoFPGrowth();
		algo.runAlgorithm(input, output, minsup);
		algo.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestFPGrowth_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
