package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.multidimensionalpatterns.AlgoDimApriori;
import ca.pfv.spmf.multidimensionalsequentialpatterns.AlgoSeqDim;
import ca.pfv.spmf.multidimensionalsequentialpatterns.MDSequenceDatabase;
import ca.pfv.spmf.sequentialpatterns.AlgoBIDEPlus;

/**
 * Test
 * @author Philippe Fournier-Viger
 */
public class MainTestMultiDimSequentialPatternMiningClosed {

	public static void main(String [] arg) throws IOException{    
		// Minimum absolute support = 50 %
		double minsupp = 0.50;
		String input = fileToPath("ContextMDSequenceNoTime.txt");
		String output = "C:\\patterns\\MDSPM.txt";
		
		// Load a sequence database
		MDSequenceDatabase contextMDDatabase  = new MDSequenceDatabase(); //
		contextMDDatabase.loadFile(input);
//		contextMDDatabase.printContext();
		
		// If the second boolean is true, the algorithm will use
		// CHARM instead of AprioriClose for mining frequent closed itemsets.
		// This options is offered because on some database, AprioriClose does not
		// perform very well. Other algorithms could be added.
		AlgoDimApriori algoDim = new AlgoDimApriori(false, true);
		
		AlgoSeqDim algoSeqDim = new AlgoSeqDim();
		
		// Apply algorithm
		AlgoBIDEPlus bideplus = new AlgoBIDEPlus(minsupp);  
		algoSeqDim.runAlgorithm(contextMDDatabase, bideplus, algoDim, true, output);
		
		// Print results
		algoSeqDim.printStatistics(contextMDDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMultiDimSequentialPatternMiningClosed.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}


