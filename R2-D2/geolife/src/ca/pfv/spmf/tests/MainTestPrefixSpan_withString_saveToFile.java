package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.sequentialpatterns.prefixspan_with_strings_saveToFile.AlgoPrefixSpanStrings_saveToFile;
import ca.pfv.spmf.sequentialpatterns.prefixspan_with_strings_saveToFile.SequenceDatabase;

/**
 * Class for testing the PrefixSpan algorithm
 * @author Philippe Fournier-Viger
 */
public class MainTestPrefixSpan_withString_saveToFile {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpanStrings.txt"));
		// print the database to console
//		sequenceDatabase.print();
		
		// Create an instance of the algorithm with minsup = 50 %
		AlgoPrefixSpanStrings_saveToFile algo = new AlgoPrefixSpanStrings_saveToFile(); 
		
		int minsup = 2; // we use a minimum support of 2 sequences.
		
		// execute the algorithm
		algo.runAlgorithm(sequenceDatabase, "C://patterns//sequential_patterns.txt", minsup);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestPrefixSpan_withString_saveToFile.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}