package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.general.datastructures.redblacktree.RedBlackTree;
import ca.pfv.spmf.sequential_rules.topseqrules.AlgoTopSeqRules;
import ca.pfv.spmf.sequential_rules.topseqrules.Rule;
import ca.pfv.spmf.sequential_rules.topseqrules.SequenceDatabase;

/**
 * -Xmx1024${build_files}m
 * @author Philippe Fournier-Viger (Copyright 2012)
 */
public class MainTestTopSeqRules {

	public static void main(String [] arg) throws IOException{
		// load database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		try {
			sequenceDatabase.loadFile(fileToPath("contextPrefixSpan.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		sequenceDatabase.printDatabaseStats();
		
		int k = 3;
		double minconf = 0.8;
//
		AlgoTopSeqRules algo = new AlgoTopSeqRules();
		RedBlackTree<Rule> rules = algo.runAlgorithm(k, sequenceDatabase, minconf);
		algo.printStats();
		algo.writeResultTofile("C:\\Patterns\\result.txt");   // to save results to file
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTopSeqRules.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
