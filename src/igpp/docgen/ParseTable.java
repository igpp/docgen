package igpp.docgen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class ParseTable {
	private String mVersion = "1.0.0";
	private String mOverview = "Parse text file containing a delimited table of values and generate a\n"
								+ "a HashMap list values which can be used with Apache Velocity tools.\n"
								+ "Field names are taken from the first record. Comments are lines that\n"
								+ "begin with \"#\" and are ignore. The array of records is assigned the\n"
								+ "name \"record\"."
								+ "\n";
	private String mAcknowledge = "Development funded by NASA's PDS project at UCLA.";

	private static boolean mVerbose= false;
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public ParseTable() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated document to {file}. Default: System.out." );
		mAppOptions.addOption( "s", "separator", true, "Separator. Pattern that separates values. Default: is any number of spaces (\\t" );
	}

	public static void main( String args[] )
    {
        /* first, we init the runtime engine.  Defaults are fine. */

        ParseTable me = new ParseTable();
        String outfile = null;
        String separator = "\\t";

		CommandLineParser parser = new PosixParser();
        try
        {
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) setVerbose(true);
   			if(line.hasOption("o")) outfile = line.getOptionValue("o");
   			if(line.hasOption("s")) separator = line.getOptionValue("s");

        	PrintStream outstream = System.out;
        	if(outfile != null) {
        		outstream = new PrintStream(outfile);
        	}
   			
   			// Process arguments looking for variable context
   			for(String p : line.getArgs()) {
   				HashMap<String, Object> map = process(p, separator);
   				igpp.docgen.ValueMap.print(outstream, "", map);
   		        if(getVerbose()) {
   		        	ValueMap.print(System.out, "", map);
   		        }
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
	 * Process a flat textual table.
	 **/
	static public HashMap<String, Object> process(String filename, String separator)
	{
        HashMap<String, Object> map = new HashMap<String, Object>();
        
        try {
        	if(mVerbose) System.out.println("Reading: " + filename);
	        BufferedReader data = new BufferedReader(new FileReader(filename));
	        String buffer;
	        String[] fieldNames = null;
	        
	        try {
	        	buffer = data.readLine();
	        	fieldNames = buffer.split(separator);
	        } catch(Exception e) {
	        	System.out.println("Unable to parse data table file. " + e.getMessage());
	        	return map;
	        }
	        
	        ArrayList<HashMap<String, Object>> records = new ArrayList<HashMap<String, Object>>();
	        
	        int lineCnt = 0;
	        while((buffer = data.readLine()) != null) {
	        	lineCnt++;
	        	if(buffer.length() == 0) continue;
	        	if(buffer.startsWith("#")) continue;	// Skip comments
	        	
	        	String[] values = buffer.split(separator);
	        	if(values.length != fieldNames.length) 	{ // Maybe an error?
	        		System.out.println("Wrong number of fields on line " + lineCnt);
	        		continue;
	        	}

	            HashMap<String, Object> recordMap = new HashMap<String, Object>();
	            records.add(recordMap);
	            
	        	for(int i = 0; i < fieldNames.length; i++) {
	        		recordMap.put(fieldNames[i], values[i]);
	        	}
	        }
	        map.put("record", records);
        } catch (Exception e ) {
            System.out.println("Problem reading table : " + e );
            e.printStackTrace();
        }
        
        return map;
	}

	public static void setVerbose(boolean state) { mVerbose = state; }
	public static boolean getVerbose() { return mVerbose; }
	
}
