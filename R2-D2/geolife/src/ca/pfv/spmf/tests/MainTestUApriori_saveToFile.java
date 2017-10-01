package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.uapriori.AlgoUApriori_saveToFile;
import ca.pfv.spmf.frequentpatterns.uapriori.ContextApriori;

/**
 * Class to test the U-APRIORI algorithm .
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestUApriori_saveToFile {

	public static void main(String [] arg) throws IOException{

		// Loading the binary context
		ContextApriori context = new ContextApriori();
		try {
			context.loadFile(fileToPath("contextUncertain.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		String output = "C://patterns//uncertain_itemsets.txt"; 
		
		// Applying the UApriori algorithm
		AlgoUApriori_saveToFile apriori = new AlgoUApriori_saveToFile(context);
		apriori.runAlgorithm(0.1, output);
		apriori.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestUApriori_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
