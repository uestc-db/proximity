package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.aprioriTID_bitset_saveToFile.AlgoAprioriTID_Bitset_File;

/**
 * Class to test the AprioriTID algorithm (bitset version) .
 * @author Philippe Fournier-Viger 
 */
public class MainTestAprioriTID_bitset_saveToFile {

	public static void main(String [] arg) throws NumberFormatException, IOException{
		// Loading the binary context
		String input = fileToPath("contextPasquier99.txt");  // the database
		String output = "C://frequent_itemsets.txt";  // the path for saving the frequent itemsets found
		
		double minsup = 0.4; // means a minsup of 2 transaction (we used a relative support)
		
		// Applying the AprioriTID algorithm
		AlgoAprioriTID_Bitset_File apriori = new AlgoAprioriTID_Bitset_File();
		apriori.runAlgorithm(input, output, minsup);
		apriori.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriTID_bitset_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
