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
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.HelpFormatter;

public class Process
{
	private String mVersion = "1.0.13";
	private String mOverview = "Defines variables that can be used to populate a Apache Velocity template.\n"
			 + "A Velocity template contains text and references to  variables. The pds.docgen executable is\n"
			 + "used to defined the value assigned to variables. Variables can be defined on the command line,\n" 
			 + "read from a PDS3 label or from a text file. Variables are placed into one or more named contexts.\n" 
			 + "When loading varaibles from a file the context name can be choosen. Variables from the command line\n"
			 + "are placed in a context	named \"options\". The syntax for specifying a named context that is\n"
			 + "populated from a file is:\n"
             + "\n"
			 + "   [format]:name:file\n"
             + "\n"
			 + "Files ending in \".lbl\", \".cat\", or \".fmt\" are parsed as PDS3 label files. \n\n"
             + "Files ending in \".txt\" are parsed as variable lists containing one keyword=value per line. \n\n"
             + "Files ending in \".cdf\" are parsed as CDF files.\n\n"
			 + "Lines begining with \"#\" are considered comments. Files ending in \".csv\" or \n"
             + "\".tab\" are processed as delimited text files with the first line containing field names. \n\n"
			 + "\n"
             + "The format determined by the filename extension can be overriden with a \"format\" designator \n"
			 + "prefix in the context options. Supported format designators are \"pds3\" for PDS3 format \n"
             + "\"list\" for variable lists and \"csv\" for delimited text."
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
		mAppOptions.addOption( "f", "format", true, "Format. Format output with a given style. Allowed values are PDS3, XML, HTML and Plain. Default: XML" );
	}

	public static void main( String args[] )
    {
        /* first, we init the runtime engine.  Defaults are fine. */

        Process me = new Process();
        String outfile = null;
        String template = null;
        String templatePath = ".";
        String includePath = null;
        String separator = "\\t"; // ",";
        String format = "xml";

        // Check if any options - if none show help and exit
    	if(args.length == 0) {
    		me.showHelp();
    		return;
    	}

		CommandLineParser parser = new BasicParser();
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
        context.put("Long", new Long(0));
        context.put("Integer", new Integer(0));
        context.put("Double", new Double(0.0));
        context.put("String", new java.lang.String());
        context.put("Text", new igpp.util.Text());
        context.put("File", new igpp.util.File());
        context.put("Date", new igpp.util.Date());
        context.put("Calc", new igpp.util.Calc());
       
        try
        {
            CommandLine line = parser.parse(me.mAppOptions, args);

   			if(line.hasOption("h")) me.showHelp();
   			if(line.hasOption("v")) me.mVerbose = true;
   			if(line.hasOption("o")) outfile = line.getOptionValue("o");
   			if(line.hasOption("t")) templatePath = line.getOptionValue("t");
   			if(line.hasOption("i")) includePath = line.getOptionValue("i");
  			if(line.hasOption("s")) separator = line.getOptionValue("s");
  			if(line.hasOption("f")) format = line.getOptionValue("f").toLowerCase();

  			// Fix-up separator to support conversion of "\t" to tab
  			if(separator.equals("\\t")) separator = "\t";
  			
   	 		HashMap<String, String> options = new HashMap<String, String>();
   	 		HashMap<String, String> namespace = new HashMap<String, String>();
   	 		
   	 		// Push options
  	 		options.put("verbose", igpp.util.Text.getYesNo(me.mVerbose));
  	 		if(outfile != null) { options.put("output", outfile); } else { options.put("output", ""); }
  	 		if(templatePath != null) { options.put("templatePath", templatePath); } else { options.put("templatePath", ""); }
 	 		if(includePath != null) { options.put("includePath", includePath); } else { options.put("includePath", ""); }
 	 		if(separator != null) { options.put("separator", separator); } 
   	 		   			
   			// Process arguments looking for variable context
   			for(String p : line.getArgs()) {
				if(p.indexOf('=') != -1) {	// An assignment x=y
   					String[] part = p.split("=", 2);
   					String name = part[0].trim();
   					String value = part[1].trim();
   					options.put(name, value);
				} else if(p.indexOf(':') != -1) {	// A name space name - load from file.
   					String fmt = me.getFormat(p);
   					String name = me.getName(p);
   					String filename = me.getFile(p);
   					namespace.put(name, filename);
   					if(me.mVerbose) {  System.out.println("Namespace: " + name + "; parsing as " + fmt + " " + filename); }
   					if(fmt.equalsIgnoreCase("pds3")) { context.put(name, igpp.docgen.ParsePDS3.process(filename, includePath)); }
   					if(fmt.equalsIgnoreCase("list")) { context.put(name, igpp.docgen.ParseList.process(filename)); }
   					if(fmt.equalsIgnoreCase("csv")) { context.put(name, igpp.docgen.ParseTable.process(filename, separator)); }
   					if(fmt.equalsIgnoreCase("cdf")) { context.put(name, igpp.docgen.ParseCDF.process(filename)); }
   				} else {
   					template = p;
   					options.put("template", template);
   				}
   			}
   	        context.put("options", options);
   	        context.put("context", namespace);
        } catch(Exception e) {
            System.out.println(":: Problem processing arguments ::" );
            e.printStackTrace(System.out);
            return;
        }

        
		if(me.mVerbose) {
			System.out.println("--------------");
			System.out.println(" Defined Keys");
			System.out.println("--------------");
			System.out.println("* Internal *");
			System.out.println("");
			Object[] keys = context.getKeys();
			// Show keys for embedded classes
			for(Object key : keys) {
				
				String keyname = String.valueOf(key);
				if(context.get(keyname) instanceof HashMap<?, ?>) continue;
				
				// Show values in context.
				System.out.println(keyname + "[" + context.get(keyname).getClass().getName() + "]");
			}
			
			System.out.println("");
			System.out.println("*Generated*");
			System.out.println("");
			
			// Now show generated keys
			keys = context.getKeys();
			for(Object key : keys) {
				
				// Skip common context.
				if(key.equals("Long")) continue;
				if(key.equals("Integer")) continue;
				if(key.equals("Double")) continue;
				if(key.equals("String")) continue;
				if(key.equals("Text")) continue;
				if(key.equals("File")) continue;
				if(key.equals("Date")) continue;
				if(key.equals("Calc")) continue;
				
				// Show values in context.
				String keyname = String.valueOf(key);
				System.out.println(keyname + "[" + context.get(keyname).getClass().getName() + "]");

				if(context.get(keyname) instanceof HashMap<?, ?>) {
					@SuppressWarnings("unchecked")
					HashMap<String, Object> map = (HashMap<String, Object>) context.get(keyname);
					igpp.docgen.ValueMap.print(System.out, "   ", map);
				}
			}
			System.out.println("--------------");
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

            if(format.equals("xml")) {	// Fix-up all values
             	Object keys[] = context.getKeys();
            	for(Object key : keys) {
    				// Skip common context. Also not a HashMap
    				if(key.equals("Long")) continue;
    				if(key.equals("Integer")) continue;
    				if(key.equals("Double")) continue;
    				if(key.equals("Text")) continue;
    				if(key.equals("String")) continue;
    				if(key.equals("File")) continue;
    				if(key.equals("Date")) continue;
    				if(key.equals("Calc")) continue;

    				String keyname = String.valueOf(key);
    				if(context.get(keyname) instanceof HashMap<?, ?>) {
    					@SuppressWarnings("unchecked")			
    					HashMap<String, Object> map = (HashMap<String, Object>) context.get(keyname);
    					igpp.docgen.ValueMap.encodeForXML(map);
    				}
            	}
            }
            
            Velocity.mergeTemplate(template, "ISO-8859-1", context, writer);
            
            if(format.equals("xml")) {
            	outstream.print(igpp.xml.ToXML.stringToXML(writer.toString()));
            } else if(format.equals("html")) {
                outstream.print(igpp.xml.ToXHTML.stringToXHTML(writer.toString()));
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
            System.out.println("Problem while merging template : " + e );
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

	
	/**
	 * Retrieve the name space of file spec. 
	 **/
	public String getFormat(String spec)
	{
		String filename = "";
		String[] part = spec.trim().split(":", 3);
		if(part.length == 3) return(part[0]);
		// Otherwise inspect extension
		filename = part[part.length-1];
		if(filename.toLowerCase().endsWith(".lbl")) { return("pds3"); }
		if(filename.toLowerCase().endsWith(".cat")) { return("pds3"); }
		if(filename.toLowerCase().endsWith(".fmt")) { return("pds3"); }
		if(filename.toLowerCase().endsWith(".txt")) { return("list"); }
		if(filename.toLowerCase().endsWith(".lst")) { return("list"); }
		if(filename.toLowerCase().endsWith(".csv")) { return("csv"); }
		if(filename.toLowerCase().endsWith(".tab")) { return("csv"); }
		if(filename.toLowerCase().endsWith(".cdf")) { return("cdf"); }
		
		return("csv");	// default
	}
	
	/**
	 * Retrieve the name space of file spec. 
	 **/
	public String getName(String spec)
	{
		String name = "";
		String[] part = spec.split(":", 3);
		if(part.length > 1) {
			name = part[part.length-2].trim();
		}
		return name.trim();
	}
	
	/**
	 * Retrieve the file name of file spec. 
	 **/
	public String getFile(String spec)
	{
		String[] part = spec.split(":", 3);
		String filename = part[part.length-1].trim();
		return filename.trim();
	}
	
}
