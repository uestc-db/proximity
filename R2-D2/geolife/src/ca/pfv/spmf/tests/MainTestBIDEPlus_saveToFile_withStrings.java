package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile_withStrings.AlgoBIDEPlus_withStrings;
import ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile_withStrings.SequenceDatabase;
/**
 * Class for testing the BIDE+ algorithm
 * @author Philippe Fournier-Viger
 */
public class MainTestBIDEPlus_saveToFile_withStrings {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextPrefixSpanStrings.txt"));
		sequenceDatabase.print();
		
		int minsup = 2; // we use a minsup of 2 sequences (50 % of the database size)
		
		AlgoBIDEPlus_withStrings algo  = new AlgoBIDEPlus_withStrings();  //
		
		// execute the algorithm
		algo.runAlgorithm(sequenceDatabase, "C://patterns//closed_sequential_patterns.txt", minsup);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestBIDEPlus_saveToFile_withStrings.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}