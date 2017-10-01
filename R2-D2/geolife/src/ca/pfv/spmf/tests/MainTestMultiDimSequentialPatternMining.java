package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.multidimensionalpatterns.AlgoDimApriori;
import ca.pfv.spmf.multidimensionalsequentialpatterns.AlgoSeqDim;
import ca.pfv.spmf.multidimensionalsequentialpatterns.MDSequenceDatabase;
import ca.pfv.spmf.sequentialpatterns.prefixspan_for_use_with_multidimensional_pattern_mining.AlgoPrefixSpanMDSPM;

/**
 * Test
 * @author Philippe Fournier-Viger
 */
public class MainTestMultiDimSequentialPatternMining {

	public static void main(String [] arg) throws IOException{  
		// Minimum absolute support = 75 %
		double minsupp = 0.75;
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
		AlgoDimApriori algoDim = new AlgoDimApriori(false, false);
		
		AlgoSeqDim algoSeqDim = new AlgoSeqDim();
		
		// Apply algorithm
		AlgoPrefixSpanMDSPM prefixSpan = new AlgoPrefixSpanMDSPM(minsupp);  
		algoSeqDim.runAlgorithm(contextMDDatabase, prefixSpan, algoDim, false, output);
		
		// Print results
		algoSeqDim.printStatistics(contextMDDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestMultiDimSequentialPatternMining.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}


