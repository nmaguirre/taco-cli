package ui;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import verifier.BasicProgramVerifier;
import verifier.JMLAnnotatedClass;

/**
 * RepairCLI is a command line interface for Stryker. It is a very simple interface, that receives only minimal
 * input: class name, method to fix, qualified path to class, and max depth for search.
 * @author aguirre
 *
 */
public class VerifyCLI {

	/**
	 * Main method of CLI interface to TACO. It uses Apache CLI to parse command line options:
	 * -p for qualified path to class
	 * -c for class name
	 * -m for method to fix
	 * All arguments are mandatory
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();
		
		Option path = new Option("p", "path", true, "qualified path e.g.: src/ or /Users/johndoe/Documents/workspace/taco/src/");
		path.setRequired(true);
		
		Option className = new Option("c", "class-name", true, "qualified class name e.g.: main.util.Pair");
		className.setRequired(true);
		
		Option method = new Option("m", "method", true, "method to verify e.g.: add");
		method.setRequired(true);
		
		Option scope = new Option("s", "scope", true, "scope");
		scope.setRequired(false);
		
		Option help = new Option("h", "help", false, "print commands");
		help.setRequired(false);
		
		Option classes = new Option("n", "needed-classes", true, "class dependencies of the class defined with c/class-name argument");
		classes.setRequired(false);
		classes.setArgs(Option.UNLIMITED_VALUES);
		classes.setValueSeparator(',');
		
		options.addOption(help);
		options.addOption(path);
		options.addOption(className);
		options.addOption(method);
		options.addOption(classes);
		options.addOption(scope);

		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "taco", options );
				return;
			}
			String qualifiedPath = cmd.getOptionValue("p");
			String clazz = cmd.getOptionValue("c");
			String methodToFix = cmd.getOptionValue("m");
			
			String[] dependenciesArgs = new String[]{};
			
			if (cmd.hasOption("n")) {
				dependenciesArgs = cmd.getOptionValues('n');
			}

			JMLAnnotatedClass subject = new JMLAnnotatedClass(qualifiedPath, clazz);		
			BasicProgramVerifier verifier = new BasicProgramVerifier(subject, methodToFix, dependenciesArgs);
			
			if (cmd.hasOption("s")) {
				String typeScope = cmd.getOptionValue('s');
				verifier.setScope(typeScope);
			}
			verifier.verify();
		}
		catch (ParseException e) {
			System.err.println( "Incorrect options.  Reason: " + e.getMessage() );
			return;
		}
		catch(NumberFormatException e) { 
			System.err.println(e.getMessage());
			return;
		}
		catch(IllegalArgumentException e) {
			System.err.println(e.getMessage());
		}

	}

}