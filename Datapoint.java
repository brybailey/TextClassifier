/*
 * This file is a part of the computer assignment 
 * for the Natural Lanugage Processing course at Williams
 *  Class Author: Johan Boye                                                                         *
 */
import java.util.*;
import java.io.*;


/**
 *   This class represents a text as a bag-of-words + a category. 
 */
public class Datapoint {

    /** The text represented as a map from the word to the number of occurrences. */
    private HashMap<String,Integer> word = new HashMap<String,Integer>();

    /** The total number of words in the text. */
    public int noOfWords;

    /** The category. */
    public String cat;

    public Datapoint( String text, String cat ) {
	this.cat = cat;
	try {
	    StringReader reader = new StringReader( text + "\n" );
	    Tokenizer tok = new Tokenizer( reader, false, false, null );
	    while ( tok.hasMoreTokens() ) { 
		noOfWords++;
		String w = tok.nextToken();
		Integer count = word.get( w );
		if ( count == null ) {
		    count = 1;
		}
		else {
		    count++;
		}
		word.put( w, count );
	    }
	}
	catch ( IOException e ) {
	    e.printStackTrace();
	}
    }


    public Iterator<String> iterator() {
	return word.keySet().iterator();
    }

    public Integer count( String w ) {
	return word.get( w );
    }
    
    public String toString() {
	return word.toString();
    }

}
