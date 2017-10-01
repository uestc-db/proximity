package ca.pfv.spmf.tests;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.associationrules.TopKRules.AlgoTopKRules;
import ca.pfv.spmf.associationrules.TopKRules.Database;

/**
 * Class to test the TopKRules algorithm
 * @author Philippe Fournier-Viger (Copyright 2010)
 */
public class MainTestTopKRules {

	public static void main(String [] arg) throws Exception{
		// Load database into memory
		Database database = new Database(); 
		database.loadFile(fileToPath("contextIGB.txt")); 
		
		int k = 2; 
		double minConf = 0.8; //
		
		AlgoTopKRules algo = new AlgoTopKRules();
		algo.runAlgorithm(k, minConf, database);

		algo.printStats();
		algo.writeResultTofile("C:\\Patterns\\result.txt");   // to save results to file

	}

	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTopKRules.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
