/**
 * 
 */
package igpp.docgen;

/**
 * @author tking
 *
 */

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.HashMap;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

//import org.apache.commons.cli.*;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;

public class Process
{
	private String mVersion = "1.0.0";
	private String mOverview = "Defines variables that can be used to populate a Apache Velocity template.\n"
			 + "A Velocity template contains text and references to  variables. The pds.docgen executable is\n"
			 + "used to defined the value assigned to variables. Variables can be defined on the command line,\n" 
			 + "read from a PDS3 label or from a text file. Variables are placed into one or more named contexts.\n" 
			 + "When loading varaibles from a file the context name can be choosen. Variables from the command line\n"
			 + "are placed in a context	named \"options\". The syntax for specifying a named context that is\n"
			 + "populated from a file is:\n"
             + "\n"
			 + "   name:file\n"
             + "\n"
			 + "files ending in \".lbl\" are parsed as PDS3 label files. All others are parsed as text files\n"
             + "containing one keyword=value per line. Lines begining with \"#\" are considered comments."
			 ;
	private String mAcknowledge = "Development funded by NASA's PDS project at UCLA.";

	private boolean mVerbose= false;
	
	// create the Options
	Options mAppOptions = new org.apache.commons.cli.Options();

	public Process() {
		mAppOptions.addOption( "h", "help", false, "Dispay this text" );
		mAppOptions.addOption( "v", "verbose", false, "Verbose. Show status at each step." );
		mAppOptions.addOption( "o", "output", true, "Output. Output generated document to {file}. Default: System.out." );
		mAppOptions.addOption( "t", "template", true, "Template. The template folder to search for templates file." );
		mAppOptions.addOption( "i", "include", true, "Include Path. Path to look for files referenced with an INCLUDE or STRUCTURE pointer." );
		mAppOptions.addOption( "s", "separator", true, "Separator. Pattern that separates values in tabular files. Default: is a tab (\t)" );
		mAppOptions.addOption( "f", "format", true, "Format. Format output with a given style. Allowed values are PDS3 and XML. Default: XML" );
	}

	public static void main( String args[] )
    {
        /* first, we init the runtime engine.  Defaults are fine. */

        Process me = new Process();
        String outfile = null;
        String template = null;
        String templatePath = ".";
        String includePath = null;
        String separator = "\\t";
        String format = "xml";

        // Check if any options - if none show help and exit
    	if(args.length == 0) {
    		me.showHelp();
    		return;
    	}

		CommandLineParser parser = new PosixParser();
        try {
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("t")) {	// Handle this option early
   				templatePath = line.getOptionValue("t");
   			}
   	        Velocity.setProperty("file.resource.loader.path", templatePath);
   			Velocity.init();
        } catch(Exception e) {
            System.out.println(":: Problem initializing Velocity ::");
            e.printStackTrace(System.out);
            return;
        }

        /* Create Velocity Context to hold variable data */

        VelocityContext context = new VelocityContext();
        
        // Default context for working with transforms
        Integer contextI = new Integer(0);       
        context.put("Integer", contextI);
        Double contextD = new Double(0.0);       
        context.put("Double", contextD);
        igpp.util.File contextFile = new igpp.util.File();       
        context.put("File", contextFile);
       
        try
        {
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) me.mVerbose = true;
   			if(line.hasOption("o")) outfile = line.getOptionValue("o");
   			if(line.hasOption("t")) template = line.getOptionValue("t");
   			if(line.hasOption("i")) includePath = line.getOptionValue("i");
  			if(line.hasOption("s")) separator = line.getOptionValue("s");
  			if(line.hasOption("f")) format = line.getOptionValue("f").toLowerCase();

   	 		HashMap<String, String> options = new HashMap<String, String>();
   	 		
   	 		// Push options
  	 		options.put("verbose", igpp.util.Text.getYesNo(me.mVerbose));
  	 		if(outfile != null) { options.put("output", outfile); } else { options.put("output", ""); }
  	 		if(template != null) { options.put("template", template); } else { options.put("template", ""); }
 	 		if(includePath != null) { options.put("includePath", includePath); } else { options.put("includePath", ""); }
 	 		if(separator != null) { options.put("separator", separator); } else { options.put("separator", ""); }
   	 		
   			// Process arguments looking for variable context
   			for(String p : line.getArgs()) {
   				if(p.indexOf(':') != -1) {	// Load variable space from file.
   					String[] part = p.split(":", 2);
   					String name = part[0].trim();
   					String filename = part[1].trim();
   					if(part[1].toLowerCase().endsWith(".lbl")) { context.put(name, igpp.docgen.ParsePDS3.process(filename, includePath)); }
   					else if(part[1].toLowerCase().endsWith(".txt")) { context.put(name, igpp.docgen.ParseList.process(filename)); }
   					else if(part[1].toLowerCase().endsWith(".csv")) { context.put(name, igpp.docgen.ParseTable.process(filename, separator)); }
   					else if(part[1].toLowerCase().endsWith(".tab")) { context.put(name, igpp.docgen.ParseTable.process(filename, separator)); }
   					else { context.put(name, igpp.docgen.ParseTable.process(filename, separator)); }	// As table
   				} else if(p.indexOf('=') != -1) {	// An assignment x=y
   					String[] part = p.split("=", 2);
   					String name = part[0].trim();
   					String value = part[1].trim();
   					options.put(name, value);
   				} else {
   					template = p;
   				}
   			}
   	        context.put("options", options);
        } catch(Exception e) {
            System.out.println(":: Problem processing arguments ::" );
            e.printStackTrace(System.out);
            return;
        }

        
		if(me.mVerbose) {
			Object[] keys = context.getKeys();
			for(Object key : keys) {
				// Skip common context.
				if(key.equals("Integer")) continue;
				if(key.equals("Double")) continue;
				if(key.equals("File")) continue;
				
				// Show values in context.
				String keyname = String.valueOf(key);
				System.out.println("--------");
				System.out.println(keyname);
				System.out.println("--------");
				
				@SuppressWarnings("unchecked")			
				HashMap<String, Object> map = (HashMap<String, Object>) context.get(keyname);
				igpp.docgen.PrintMap.valueList(System.out, "", map);
			}
		}
    
        if(template == null) {       	
        	System.out.println("Template must be specified.");
        	return;
        }


        /* lets render a template */
        try
        {
        	// OutputStream outstream = System.out;
        	// if(outfile != null) {
        	//	outstream = new FileOutputStream(outfile);
        	// }
        	// Writer writer = new BufferedWriter(new OutputStreamWriter(outstream));
        	PrintStream outstream = System.out;
        	if(outfile != null) {
        		outstream = new PrintStream(outfile);
        	}
        	StringWriter writer = new StringWriter();
        	
            Velocity.mergeTemplate(template, "ISO-8859-1", context, writer);
            
            if(format.equals("xml")) {
            	outstream.print(igpp.xml.ToXML.stringToPlainXML(writer.toString()));
            } else if(format.equals("pds3")) {
            	pds.label.PDSLabel label = new pds.label.PDSLabel();
            	StringReader reader = new StringReader(writer.toString());
            	label.parse(new BufferedReader(reader));
            	label.print(outstream);
            } else {
            	outstream.print(writer.toString());
            }
            
            // writer.flush();
            // writer.close();
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
