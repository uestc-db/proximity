package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.highutility.two_phase_algorithm.AlgoTwoPhase;
import ca.pfv.spmf.highutility.two_phase_algorithm.Database;
import ca.pfv.spmf.highutility.two_phase_algorithm.Itemsets;

/**
 * Class to test the Two-Phase algorithm (Liu, Liao & Choudhari, PAKDD 2005).
 * @author Philippe Fournier-Viger, 2010
 */
public class MainTestTwoPhaseAlgorithm {

	public static void main(String [] arg) throws IOException{
		
		String input = fileToPath("DB_Utility.txt");
		String output = "C:\\patterns\\hui.txt";

		int min_utility = 30;  // 

		// Loading the database into memory
		Database database = new Database();
		database.loadFile(input);
		database.printContext();
		
		// Applying the Two-Phase algorithm
		AlgoTwoPhase twoPhase = new AlgoTwoPhase(database);
		Itemsets highUtilityItemsets = twoPhase.runAlgorithm(min_utility);
		
		// print the frequent itemsets found
		highUtilityItemsets.printItemsets(database.size());
		
		twoPhase.printStats();

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTwoPhaseAlgorithm.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
