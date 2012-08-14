/**
 * 
 */
package igpp.docgen;

/**
 * @author tking
 *
 */

import java.io.BufferedWriter;
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


public class FromPDS3
{
	private String	mVersion = "1.0.0";
	private String mOverview = "FromPDS3 scans a PDS3 label file and generates a list values which can be used to populate a Apache Velocity template.\n"
									 + "Values are placed in the \"$label\" context."
									 + "A supplemental data file may be provided as a flat table. The name for each field is taken from the first line."
									 + "Lines begining with \"#\" are considered comments. ";
	private String mAcknowledge = "Development funded by NASA's PDS project at UCLA.";

	private boolean mVerbose= false;
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public FromPDS3() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated document to {file}. Default: System.out." );
		mAppOptions.addOption( "t", "template", true, "Template. The template for the output using Apache Velocity Template Language." );
		mAppOptions.addOption( "l", "label", true, "PDS3 Label. The PDS3 label to parse." );
		mAppOptions.addOption( "d", "data", true, "The name of the file containing the table data." );
		mAppOptions.addOption( "i", "include", true, "Include Path. Path to look for files referenced with an INCLUDE or STRUCTURE pointer." );
	}

	public static void main( String args[] )
    {
        /* first, we init the runtime engine.  Defaults are fine. */

        FromPDS3 me = new FromPDS3();
        String outfile = null;
        String labelFile = null;
        String template = null;
        String dataFile = null;
        String includePath = null;
        
		CommandLineParser parser = new PosixParser();
        try
        {
            Velocity.init();
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) me.mVerbose = true;
   			if(line.hasOption("o")) outfile = line.getOptionValue("o");
   			if(line.hasOption("t")) template = line.getOptionValue("t");
   			if(line.hasOption("l")) labelFile = line.getOptionValue("l");
   			if(line.hasOption("d")) dataFile = line.getOptionValue("l");
   			if(line.hasOption("i")) includePath = line.getOptionValue("i");
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

        if(labelFile == null) {
        	System.out.println("Label (-l) must be specified.");
        	return;
        }

        /* lets make a Context and put data into it */

        VelocityContext context = new VelocityContext();

        HashMap<String, Object> labelMap = igpp.docgen.ParsePDS3.process(labelFile, includePath);
        if(me.mVerbose) {
        	igpp.docgen.PrintMap.valueList(System.out, "", labelMap);
        }
        
        // Save map in "label" context
        context.put("label", labelMap);
 
        // If a table is also supplied - parse and map 
        if(dataFile != null) {
        	HashMap<String, Object> dataMap = igpp.docgen.ParseList.process(dataFile);
            if(dataMap != null) context.put("data", dataMap);
            if(me.mVerbose) {
            	igpp.docgen.PrintMap.valueList(System.out, "", dataMap);
            }
        }

        /* lets render a template */
        try
        {
        	OutputStream outstream = System.out;
        	if(outfile != null) {
        		outstream = new FileOutputStream(outfile);
        	}
        	Writer writer = new BufferedWriter(new OutputStreamWriter(outstream));
        	
            Velocity.mergeTemplate(template, "ISO-8859-1", context, writer);
            
            writer.flush();
            writer.close();
        }
        catch (Exception e )
        {
            System.out.println("Problem merging template : " + e );
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
