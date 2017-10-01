package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.MSApriori_optimized.AlgoMSApriori_saveToFile;

/**
 * Class to test the MSAPRIORI algorithm .
 */
public class MainTestMSApriori_saveToFile {

	public static void main(String [] arg) throws IOException{

		String input = fileToPath("contextIGB.txt");
		String output = "C://patterns//MSApriori_result.txt";  // the path for saving the frequent itemsets found
		
		double beta=0.4;
		double LS=0.2;
		
		// Applying the MSApriori algorithm
		AlgoMSApriori_saveToFile apriori = new AlgoMSApriori_saveToFile();
		apriori.runAlgorithm(input, output, beta, LS);
		apriori.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMSApriori_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
