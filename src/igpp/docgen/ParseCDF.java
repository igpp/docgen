/**
 * 
 */
package igpp.docgen;

/**
 * @author tking
 *
 */

import pds.cdf.CDF;

import java.io.PrintStream;

import java.util.HashMap;

//import org.apache.commons.cli.*;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;


public class ParseCDF
{
	private String	mVersion = "1.0.0";
	private String mOverview = "Parse a CDF label file and generate a list values which can be used with Apache Velocity tools.\n"
									 + "";
	private String mAcknowledge = "Development funded by NASA's PDS project at UCLA.";

	private boolean mVerbose= false;
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public ParseCDF() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated document to {file}. Default: System.out." );
	}

	public static void main( String args[] )
    {
        /* first, we init the runtime engine.  Defaults are fine. */

        ParseCDF me = new ParseCDF();
        String outfile = null;

		CommandLineParser parser = new PosixParser();
        try
        {
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) me.mVerbose = true;
   			if(line.hasOption("o")) outfile = line.getOptionValue("o");

        	PrintStream outstream = System.out;
        	if(outfile != null) {
        		outstream = new PrintStream(outfile);
        	}
   			
   			// Process arguments looking for variable context
   			for(String p : line.getArgs()) {
   				CDF cdf = process(p);
   				cdf.dump(true, true);
   				/*
   				HashMap<String, Object> map = process(p);
   				igpp.docgen.ValueMap.print(outstream, "", map);
   		        if(me.mVerbose) {
   		        	ValueMap.print(System.out, "", map);
   		        }
   		        */
   			}
        } catch(Exception e) {
            System.out.println("Problem processing arguments : " + e );
            return;
        }
    }
	
 	/**
	 * Display help information.
	 **/
	public void showHelp()
	{
		System.out.println("");
		System.out.println(getClass().getName() + "; Version: " + mVersion);
		System.out.println(mOverview);
		System.out.println("");
		System.out.println("Usage: java " + getClass().getName() + " [options] [file...]");
		System.out.println("");
		System.out.println("Options:");

		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(getClass().getName(), mAppOptions);

		System.out.println("");
		System.out.println("Acknowledgements:");
		System.out.println(mAcknowledge);
		System.out.println("");
	}
	
	/**
	 * Process a CDF file and create a variable space in the Velocity Context
	 */
	static public CDF process(String pathname)
	{    
        try {
        	CDF cdf = new CDF(pathname);
        	return cdf;
        } catch(Exception e) {
        	System.out.println("Unable to parse CDF file. " + e.getMessage());
        	return null;
        }
 	}
	
	/*
	static public HashMap<String, Object> process(String pathname)
	{    
        HashMap<String, Object> map = null;

        try {
        	CDF cdf = new CDF(pathname);
            
            map = cdf.getHashMap();
            
        } catch(Exception e) {
        	System.out.println("Unable to parse CDF file. " + e.getMessage());
        	return null;
        }
        
        return map;
 	}
 	*/
}
