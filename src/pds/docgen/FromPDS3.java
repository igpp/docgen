/**
 * 
 */
package pds.docgen;

/**
 * @author tking
 *
 */

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.Writer;

import pds.label.PDSLabel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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
									 + "Values are placed in the \"$label\" context.";
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
		mAppOptions.addOption( "p", "print", false, "Print. Print the value stack from parsing the label to the output file." );
		mAppOptions.addOption( "i", "include", true, "Include Path. Path to look for files referenced with an INCLUDE or STRUCTURE pointer." );
	}

	public static void main( String args[] )
    {
        /* first, we init the runtime engine.  Defaults are fine. */

        FromPDS3 me = new FromPDS3();
        String outfile = null;
        String infile = null;
        String template = null;
        boolean print = false;
        String includePath = null;
        
		CommandLineParser parser = new PosixParser();
        try
        {
            Velocity.init();
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) me.mVerbose = true;
   			if(line.hasOption("o")) outfile = line.getOptionValue("o");
   			if(line.hasOption("p")) print = true;
   			if(line.hasOption("t")) template = line.getOptionValue("t");
   			if(line.hasOption("l")) infile = line.getOptionValue("l");
   			if(line.hasOption("i")) includePath = line.getOptionValue("i");
        }
        catch(Exception e)
        {
            System.out.println("Problem initializing Velocity : " + e );
            return;
        }
        
        if(template == null && ! print) {
        	System.out.println("Template (-t) must be specified.");
        	return;
        }

        if(infile == null) {
        	System.out.println("Label (-l) must be specified.");
        	return;
        }

        /* lets make a Context and put data into it */

        VelocityContext context = new VelocityContext();

        PDSLabel label = new PDSLabel();
        try {
        	label.parse(infile);
        } catch(Exception e) {
        	System.out.println("Unable to parse label file. " + e.getMessage());
        	return;
        }
        
        PDSLabel richLabel = null;
        try {
        	richLabel = label.expandPointers(includePath);
        } catch(Exception e) {
        	System.out.println("Unable to expand label file. " + e.getMessage());
        	return;
        }
        
        HashMap<String, Object> labelMap = richLabel.getHashMap(0);
        if(print) {
        	me.printMap("", labelMap);
        	return;
        }
        
        // Else process template
        context.put("label", labelMap);
        
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

	/**
	 * Print the map in template format
	 **/
	public void printMap(String prefix, HashMap<String, Object> map)
	{
		ArrayList<HashMap<String, Object>> arrayType = new ArrayList<HashMap<String, Object>>();
		
		Set<String> keySet = (Set<String>) map.keySet();
		for(String key : keySet) {
			if(map.get(key).getClass().isInstance(arrayType) ) {
				ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) map.get(key);
				for(HashMap<String, Object> mapItem : list) {
					printMap(prefix + key + ".",  mapItem);
				}
			} else {
				System.out.println(prefix + key + ":" + map.get(key));
			}
		}
	}
}
