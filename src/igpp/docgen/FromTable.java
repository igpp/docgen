/**
 * 
 */
package igpp.docgen;

/**
 * @author tking
 *
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.Writer;

import java.util.HashMap;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

//import org.apache.commons.cli.*;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;


public class FromTable
{
	private String	mVersion = "1.0.0";
	private String mOverview = "FromTable scans a file containing a delimited table and for each row generates a document "
			 + "using an Apache Velocity template.\n"
			 + "The name for each field is taken from the first line. Lines begining with \"#\" are considered comments."
			 + "Values are placed in the \"$table\" context in Velocity. The output file name can contain field name references"
			 + "using the syntax (i.e. precede field name with a \"$\" to create customize file names for each record.";
	private String mAcknowledge = "Development funded by NASA's PDS project at UCLA.";

	private boolean mVerbose= false;
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public FromTable() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "o", "output", true, "Output. Output placed in document {file}. Default: System.out." );
		mAppOptions.addOption( "t", "template", true, "Template. The template for the output using Apache Velocity Template Language." );
		mAppOptions.addOption( "d", "data", true, "The name of the file containing the table data." );
		mAppOptions.addOption( "p", "print", false, "Print. Print the value stack from parsing the label to the output file." );
		mAppOptions.addOption( "s", "separator", true, "Separator. Pattern that separates values. Default: is any number of spaces ([ ]+)" );
	}

	public static void main( String args[] )
    {
        /* first, we init the runtime engine.  Defaults are fine. */

        FromTable me = new FromTable();
        String outfile = null;
        String infile = null;
        String template = null;
        String separator = "[ ]+";
        
		CommandLineParser parser = new PosixParser();
        try
        {
            Velocity.init();
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) me.mVerbose = true;
   			if(line.hasOption("o")) outfile = line.getOptionValue("o");
   			if(line.hasOption("t")) template = line.getOptionValue("t");
   			if(line.hasOption("d")) infile = line.getOptionValue("d");
        }
        catch(Exception e)
        {
            System.out.println("Problem initializing Velocity : " + e );
            return;
        }
        
        if(template == null) {
        	System.out.println("Template (-t) must be specified.");
        	return;
        }

        if(infile == null) {
        	System.out.println("Data file (-d) must be specified.");
        	return;
        }

        /* lets make a Context and put data into it */
        String 	buffer;
        String[] fieldNames = null;
        
        VelocityContext context = new VelocityContext();

        try {
        	System.out.println("Reading: " + infile);
	        BufferedReader data = new BufferedReader(new FileReader(infile));
	        try {
	        	buffer = data.readLine();
	        	fieldNames = buffer.split("[ ]+");
	        } catch(Exception e) {
	        	System.out.println("Unable to parse data table file. " + e.getMessage());
	        	return;
	        }
	        
	        HashMap<String, Object> dataMap = new HashMap<String, Object>();
	        
	        while((buffer = data.readLine()) != null) {
	        	if(buffer.length() == 0) continue;
	        	if(buffer.startsWith("#")) continue;	// Skip comments
	        	
	        	String[] values = buffer.split(separator);
	        	if(values.length != fieldNames.length) 	continue;	// Maybe an error?
	        	
	        	for(int i = 0; i < fieldNames.length; i++) {
	        		dataMap.put(fieldNames[i], values[i]);
	        	}
	        	
		        if(me.mVerbose) {
		        	igpp.docgen.PrintMap.valueList(System.out, "", dataMap);
		        }
		        
		        // Else process template
		        context.put("data", dataMap);
		        
		        /* Render document with a template */
		        try
		        {
		        	OutputStream outstream = System.out;
		        	// Replace pattern in file name
		        	String outname = org.apache.velocity.util.StringUtils.stringSubstitution(outfile, dataMap).toString();
		        	if(outfile != null) {
		        		outstream = new FileOutputStream(outname);
		        	}
		        	Writer writer = new BufferedWriter(new OutputStreamWriter(outstream));
		        	
		            Velocity.mergeTemplate(template, "ISO-8859-1", context, writer);
		            
		            if( ! outstream.equals(System.out)) {
		            	writer.flush();
		            	writer.close();
		            }
		        } catch (Exception e ) {
		            System.out.println("Problem merging template : " + e );
		        }
	        }
        } catch (Exception e ) {
            System.out.println("Problem reading table : " + e );
            e.printStackTrace();
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
}
