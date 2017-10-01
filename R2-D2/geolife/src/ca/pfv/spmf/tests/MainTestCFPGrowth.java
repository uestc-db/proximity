package ca.pfv.spmf.tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.cfpgrowth.AlgoCFPGrowth_saveToFile;

/**
 * Class to test the CFPGROWTH algorithm
 */
public class MainTestCFPGrowth {

	public static void main(String[] arg) throws FileNotFoundException,
			IOException {
		String database = fileToPath("contextCFPGrowth.txt");
		String output = "C://patterns//frequent_itemsets_by_CFPGrowth.txt";
		String MISfile = fileToPath("MIS.txt");

		// Applying the CFPGROWTH algorithmMainTestFPGrowth.java
		AlgoCFPGrowth_saveToFile algo = new AlgoCFPGrowth_saveToFile();
		algo.runAlgorithm(database, output, MISfile);
		algo.printStats();
	}

	public static String fileToPath(String filename)
			throws UnsupportedEncodingException {
		URL url = MainTestCFPGrowth.class.getResource(filename);
		return java.net.URLDecoder.decode(url.getPath(), "UTF-8");
	}
}
