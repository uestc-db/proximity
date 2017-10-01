package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.zart.AlgoZart;
import ca.pfv.spmf.frequentpatterns.zart.ContextZart;
import ca.pfv.spmf.frequentpatterns.zart.ItemsetZart;
import ca.pfv.spmf.frequentpatterns.zart.TFTableFrequent;
import ca.pfv.spmf.frequentpatterns.zart.TZTableClosed;
/**
 * Class for testing the Zart Algorithm
 * @author Philippe Fournier-Viger, 2008
 *
 */
public class MainTestZart_saveToFile {

	public static void main(String[] args) throws IOException {

		String input = fileToPath("contextZart.txt");  // the database
		String output = "C://patterns//zart_output.txt";  // the path for saving the frequent itemsets found
		
		// Load a binary context
		ContextZart context = new ContextZart();
		context.loadFile(input);

		// Apply the Zart algorithm
		double minsup = 0.4;
		AlgoZart zart = new AlgoZart();
		TZTableClosed results = zart.runAlgorithm(context, minsup);
		TFTableFrequent frequents = zart.getTableFrequent();
		zart.printStatistics();
		zart.saveResultsToFile(output);
			
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestZart_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
