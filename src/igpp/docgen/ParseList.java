package igpp.docgen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

public class ParseList {
	private String mVersion = "1.0.0";
	private String mOverview = "Parse a text file and generate a list values which can be used with Apache Velocity tools.\n"
						+ "When text files are parsed lines beginning with \"#\" are considered comments.\n" 
						+ "All other lines are parsed as a \"name=value\". \n"
						+ "If a line does not conform to the \"name=value\" syntax it is ignored.\n"
						+ "If a value is enclosed in curly braces {} the value is parsed as a comma separated list.\n"
						+ "\n";
	private String mAcknowledge = "Development funded by NASA's PDS project at UCLA.";

	private boolean mVerbose= false;
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public ParseList() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated document to {file}. Default: System.out." );
		mAppOptions.addOption( "s", "separator", true, "Separator. Pattern that separates values. Default: is any number of spaces ([ ]+)" );
	}

	public static void main( String args[] )
    {
        /* first, we init the runtime engine.  Defaults are fine. */

        ParseList me = new ParseList();
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
   				HashMap<String, Object> map = process(p);
   				igpp.docgen.ValueMap.print(outstream, "", map);
   		        if(me.mVerbose) {
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
	static public HashMap<String, Object> process(String filename)
	{
        // Parse table 
        HashMap<String, Object> map = new HashMap<String, Object>();
        if(filename == null)  return null;
        
    	String buffer;

        try {
	        BufferedReader data = new BufferedReader(new FileReader(filename));
	        while((buffer = data.readLine()) != null) {
	        	if(buffer.length() == 0) continue;
	        	if(buffer.startsWith("#")) continue;	// Skip comments
	        	
	        	String[] part = buffer.split("=", 2);
	        	String tag = part[0].trim();
	        	String val = "";
	        	if(part.length == 2) val = part[1].trim();
	        	ArrayList<String> list = new ArrayList<String>();
	        	if(val.startsWith("{") && val.endsWith("}")) { // Parse a comma separated list
	        		val = val.substring(1);	// Drop leading "{"
	        		val = val.substring(0, val.length()-1); // Drop trailing "{"
	        		String[] elem = val.split(",");
	        		for(String e : elem) {
	        			e = e.trim();
	        			list.add(e);
	        		}
	        		map.put(tag, list);
	        	} else {
	        		map.put(tag, val);
	        	}	        	
	        }
	        data.close();
        } catch (Exception e ) {
            System.out.println("Problem reading table : " + e );
            e.printStackTrace();
        }

        return map;
	}

}
