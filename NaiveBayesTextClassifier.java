/*  
 *  Part of the Natural Lanugage Processing Course at Williams College
 *  Class Author: Bryan Bailey
 * 
 */  


import java.io.*;
import java.util.*;


/**
 *   This class performs text classification using the Naive Bayes method.
 */
public class NaiveBayesTextClassifier {

    /** The mapping from category identifiers to category names. */
    String[] catName;

    /** The mapping from category names to category identifiers. */
    HashMap<String,Integer> catIndex = new HashMap<String,Integer>();


    /** The index corresponds to the category, which the corresponding
     *  HashMap mapping words to their count in the category
     */
    ArrayList<HashMap<String,Integer>> wordsInCat;

    /** A HashMap of all the words in the dataset (HashMap for ease of access) **/
    HashMap<String, Boolean> inVocab;
    int sizeOfVocab;

    /**
     *  Model parameters: P(word|cat) 
     *  The index in the arraylist corresponds to the identifier
     *  of the category (i.e. the first element contains the 
     *  probabilities for category 1, the second for category 2,
     *  etc.
     */
    ArrayList<HashMap<String,Double>> likelihood = new ArrayList<HashMap<String,Double>>();
    
    /**  Prior probabilities P(cat) for all the categories. */
    double[] priorProb;

    /** 
     *   The probability of a word we haven't seen before 
     *	 ( should be -Math.log(size of vocabulary) ).
     */
    double unknownWordProb;
    
    /** The total number of words in each category**/
    int[] wordNumbers;
		
    // ---------------------------------------------------------- //


    /**
     *  Computes the posterior probability P(cat|d) = P(cat|w1 ... wn) =
     *  = P(cat) * P(w1|cat) * ... *vP(wn|cat), for all categories cat.
     *
     *  @return The name of the winning category (i.e. argmax P(cat|d) ).
     */
    String classifyDatapoint( Datapoint d ) {

	// The maximum probability of all the categories
	double maxProb = Double.POSITIVE_INFINITY;

	// The category with the maximum probability
	int maxCat = -1;
	
	// Iterator of datapoint bag of words
	Iterator<String> iterator;     

	// Index of the current category in likelihood iterator
	int x = 0;

	// Determine the category with the bagofwords apporach
	for( HashMap<String, Double> category: likelihood ) {

	    // Reset test probability to 0 for each category
	    double testProb = 0;
	    iterator = d.iterator();
	    double wordProb = 0;

	    // Go through each word in datapoint
	    while( iterator.hasNext() ) {
		String next = iterator.next();

		// If the word appears in the dataset category, get it's prob in the category
		if( category.containsKey( next ) ) {
		    wordProb = category.get( next );
		    
		    // Else if it's in the vocab at all
		} else if( inVocab.containsKey( next ) ) {
		    wordProb = -Math.log( (double)1/(wordNumbers[x]+sizeOfVocab));

		    // Else give it the unknown word prob
		} else {
		    wordProb = unknownWordProb;
		}
		testProb += wordProb;
	    }

	    // Update the test probability
	    testProb += priorProb[x];

	    // If this testProb is lower than the max, this category is now the most likely
	    if( testProb < maxProb ) {
		maxProb = testProb;
		maxCat = x;
	    }
	    x++;
	}
	
	// If we've found a category with the lowest probability
	if( maxCat >= 0 ) {
	    return catName[maxCat];
	} else return "no classification given";
	
    }
    
    
    // ---------------------------------------------------------- //


    /**
     *   Computes the prior probabilities P(cat) and likelihoods
     *   P(word|cat), for all words and categories (also for
     *   unseen words). To avoid underflow, log-probabilities are 
     *   used. 
     *
     *   Laplace smoothing is used in order to avoid that certain
     *   probabilities become zero.
     */
    void buildModel( Dataset training_set ) {

	// First copy some essential info from the training_set.
	catName = training_set.catName;
	catIndex = training_set.catIndex;
	wordNumbers = training_set.noOfWords;

	// Counts the number of unique words in the training_set
	wordsInCat = getCatWords( training_set );
	sizeOfVocab = getVocabSize( training_set );

	// Add hashmaps and establish prior probabilities for each category
	priorProb = new double[training_set.noOfCategories];

	for( int i=0; i<training_set.noOfCategories; i++ ) {
	    likelihood.add( i, new HashMap<String, Double>() );
	    priorProb[i] = -Math.log( (double)training_set.noOfDatapoints[i]/training_set.totNoOfDatapoints );	    
	}



	// Iterator of datapoints
	Iterator<Datapoint> iter = training_set.iterator();

	// Go through each datapoint in the trainingset
	Datapoint nextPoint;
	HashMap<String, Double> map = new HashMap<String, Double>();	    

	while( iter.hasNext() ) {

	    nextPoint = iter.next();
	    Iterator<String> pointIter = nextPoint.iterator();
	    String nextWord;
	    // The category index of the current datapoint
	    int identifier = catIndex.get(nextPoint.cat);
	    
	    // Go through each word in the datapoint
	    while( pointIter.hasNext() ) {

		nextWord = pointIter.next();

		// Get its count in the category
		int occurences = wordsInCat.get(identifier).get(nextWord);

		// Divide by the total number of words in the category plus the number of unique words in the vocab
		// Includes LaPlace smoothing
		double prob = -Math.log((double)(occurences+1)/(training_set.noOfWords[identifier] + sizeOfVocab));

		// The mapping from the word to its probability
		map = likelihood.get( identifier );

		// Put the word with its probability in the category hashmap
		map.put( nextWord, prob );
		
	    }
	   
	}

	// Update the probability of an unseen word
	unknownWordProb = -Math.log((double)1/sizeOfVocab);
    }


    /* Returns the an arraylist with each index being a category, mapping the words in the category
     * To the count in the category
     **/
    public ArrayList<HashMap<String,Integer>> getCatWords( Dataset testSet ) {
	ArrayList<HashMap<String,Integer>> returnedWords = new ArrayList<HashMap<String,Integer>>();
	for( int i = 0; i < testSet.noOfCategories; i++ ) {
	    returnedWords.add( new HashMap<String, Integer>() );
	}

	Iterator<Datapoint> pointIter = testSet.iterator();
	while( pointIter.hasNext() ) {
	    Datapoint nextPoint = pointIter.next();
	    Iterator<String> wordIter = nextPoint.iterator();
	    int catID = catIndex.get( nextPoint.cat );
	    while( wordIter.hasNext() ) {
		String nextWord = wordIter.next();
		HashMap<String,Integer> catWords = returnedWords.get(catID);
		int count;
		int nextCount = nextPoint.count(nextWord);

		// If we've already seen this word, update its count with the count in this datapoint
		if( catWords.containsKey(nextWord) ) {
		    int currentCount = catWords.get( nextWord );
		    count = currentCount+nextCount;

		    // Otherwise its count is just the count in this datapoint
		} else { 
		    count = nextCount;
		}

		catWords.put( nextWord, count );
	    }
	    
	}
	return returnedWords;
    }
	
    // Returns the number of unique words in the data set    
    public int getVocabSize( Dataset testSet ) {
	inVocab = new HashMap<String, Boolean>();
	int vocabSize = 0;
	Iterator<Datapoint> pointIter = testSet.iterator();
	while( pointIter.hasNext() ) {
	    Datapoint point = pointIter.next();
	    Iterator<String> wordIter = point.iterator();
	    while( wordIter.hasNext() ) {
		String nextWord = wordIter.next();
		// If we haven't seen this word before, add one to the size of the vocab
		// Add the word to the unique words HashMap
		if( !inVocab.containsKey(nextWord ) ){
		    vocabSize++;
		    inVocab.put(nextWord,true);
		}
	    }
	    
	}
	return vocabSize;
    }
    
    // ---------------------------------------------------------- //


    /**
     *   Goes through a testset, classifying each datapoint according 
     *   to the model.
     */
    void classifyTestset( Dataset testset ) {
	Iterator<Datapoint> iter = testset.iterator();
	while ( iter.hasNext() ) {
	    Datapoint dp = iter.next();
	    String cat = classifyDatapoint( dp );
	    System.out.println( cat );
	}
    }


    // ---------------------------------------------------------- //


    /**
     *   Constructor. Read the training file and, possibly, the test 
     *   file. If test file is null, read input from the keyboard.
     */
    public NaiveBayesTextClassifier( String training_file, String test_file ) {
	buildModel( new Dataset( training_file ));
	classifyTestset( new Dataset( test_file ));
    }


    // ---------------------------------------------------------- //


    /** Prints usage information. */
    static void printHelpMessage() {
	System.err.println( "The following parameters are available: " );
	System.err.println( "  -d <filename> : training file (mandatory)");
	System.err.println( "  -t <filename> : test file (mandatory)" );
    }


    // ---------------------------------------------------------- //


    public static void main( String[] args ) {
	// Parse command line arguments 
	String training_file = null;
	String test_file = null;
	int i=0; 
	while ( i<args.length ) {
	    if ( args[i].equals( "-d" )) {
		i++;
		if ( i<args.length ) {
		    training_file = args[i++];
		}
		else {
		    printHelpMessage();
		    return;
		}
	    }
	    else if ( args[i].equals( "-t" )) {
		i++;
		if ( i<args.length ) {
		    test_file = args[i++];
		}
		else {
		    printHelpMessage();
		    return;
		}
	    }
	    else {
		printHelpMessage();
		return;
	    }
	}
	if ( training_file != null && test_file != null ) {
	    new NaiveBayesTextClassifier( training_file, test_file );
	}
	else {
	    printHelpMessage();
	}
    }
    



}
