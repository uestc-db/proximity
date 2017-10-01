package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.apriori.*;


/**
 * Class to test the AprioriRare algorithm
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAprioriRare_saveToFile {

	public static void main(String [] arg) throws IOException{
		// Loading the binary context
		ContextApriori context = new ContextApriori();
		try {
			context.loadFile(fileToPath("contextZart.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		String output = "C://patterns//minimal_rare_itemsets.txt";  // the path for saving the frequent itemsets found

		// Applying the Apriori algorithm
		AlgoAprioriRare_saveToFile apriori = new AlgoAprioriRare_saveToFile(context);
		apriori.runAlgorithm(0.6, output);
		apriori.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriRare_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
