package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;


import ca.pfv.spmf.associationrules.agrawal_FPGrowth_version_saveToFile.AlgoAgrawalFaster94_FPGrowth_version_saveToFile;
import ca.pfv.spmf.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.frequentpatterns.fpgrowth.Database;
import ca.pfv.spmf.frequentpatterns.fpgrowth.Itemsets;
/**
 * Class to test the algorithms AlgoGDBasisForExactRulesFromFC,  AlgoProperBasisForApproxRulesFromFC
 * and AlgoStructuralBasisForApproxRulesFromFC.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAllAssociationRules_FPGrowth_saveToFile_withLift {

	public static void main(String [] arg) throws IOException{
		String input = fileToPath("contextIGB.txt");
		String output = "C:\\patterns\\association_rules.txt";
		
		// Loading the binary context
		Database database = new Database();
		database.loadFile(input);

		int databaseSize = database.size();
		
		// STEP 1: Applying the FP-GROWTH algorithm to find frequent itemsets
		double minsupp = 0.5;
		AlgoFPGrowth fpgrowth = new AlgoFPGrowth();
		Itemsets patterns = fpgrowth.runAlgorithm(database, minsupp);
//		patterns.printItemsets(database.size());
		fpgrowth.printStats();
		
		// STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
		double  minlift = 0.1;
		double  minconf = 0.50;
		AlgoAgrawalFaster94_FPGrowth_version_saveToFile algoAgrawal = new AlgoAgrawalFaster94_FPGrowth_version_saveToFile(minconf, minlift);
		algoAgrawal.runAlgorithm(patterns, output, databaseSize);
		algoAgrawal.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAllAssociationRules_FPGrowth_saveToFile_withLift.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
