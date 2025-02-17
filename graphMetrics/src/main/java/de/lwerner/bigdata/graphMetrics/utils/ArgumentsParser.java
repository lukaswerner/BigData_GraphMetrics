package de.lwerner.bigdata.graphMetrics.utils;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.cli.*;

/**
 * Simple class for parsing the command line parameters into the desired format.
 * 
 * Utilizes the Apache Commons CLI Library
 * 
 * @author Lukas Werner
 */
public abstract class ArgumentsParser {

	/**
	 * Parses the command line arguments
	 * 
	 * @param className the classname of the calling class
	 * @param outputFilename the default output filename
	 * @param arguments the given arguments
	 * @return the command line arguments as an object of CommandLineArguments
	 * @throws ParseException if the parsing progress throws an error
	 * @throws IllegalArgumentException if any arguments are given in the wrong format
	 */
	public static CommandLineArguments parseArguments(String className, String outputFilename, String[] arguments) throws ParseException, IllegalArgumentException {
		Options options = new Options();
		options.addOption("m", "maxIterations", true, "max iterations on converging algorithms");
		options.addOption("r", "reader", true, "choose reader [<simple|food>]");
		options.addOption("g", "graphs", true, "absolute path to graph file");
		options.addOption("v", "vertices", true, "absolute path to vertices json file");
		options.addOption("e", "edges", true, "absolute path to edges json file");
		options.addOption("o", "output", true, "absolute path to output file");
		
		CommandLineArguments args = new CommandLineArguments();
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine cli = parser.parse(options, arguments);
			if (cli.hasOption("maxIterations")) {
				try {
					args.setMaxIterations(Integer.parseInt(cli.getOptionValue("maxIterations")));
				} catch (Exception e) {
					throw new IllegalArgumentException();
				}
			}
			if (cli.hasOption("reader")) {
				String reader = cli.getOptionValue("reader");
				switch (reader) {
					case "simple":
					case "food":
						args.setReader(reader);
						break;
					default:
						throw new IllegalArgumentException();
				}
			}
			if (cli.hasOption("graph")) {
				args.setGraphPath(cli.getOptionValue("graph"));
			}
			if (cli.hasOption("vertices")) {
				args.setVerticesPath(cli.getOptionValue("vertices"));
			}
			if (cli.hasOption("edges")) {
				args.setEdgesPath(cli.getOptionValue("edges"));
			}
			if (cli.hasOption("output")) {
				args.setOutputPath(cli.getOptionValue("output"));
			} else {
				args.setOutputPath(System.getProperty("user.dir") + File.separator + outputFilename);
			}
		} catch(IllegalArgumentException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.setOptionComparator(new GraphMetricsOptionComparator());
			formatter.printHelp(100, "java " + className, "", options, "", true);
			throw e;
		} catch (ParseException e) {
			System.err.println("Parsing failed. Reason: " + e.getMessage());
			throw e;
		}
		return args;
	}
	
	/**
	 * Comparator class for setting the order of the arguments in the usage help output
	 * 
	 * @author Lukas Werner
	 */
	public static final class GraphMetricsOptionComparator implements Comparator<Option> {

		/**
		 * Simple order map with an integer to set order
		 */
		private HashMap<String, Integer> orderMap;
		
		/**
		 * Sets the order map
		 */
		public GraphMetricsOptionComparator() {
			orderMap = new HashMap<>();
			orderMap.put("v", 1);
			orderMap.put("r", 2);
			orderMap.put("g", 3);
			orderMap.put("e", 4);
			orderMap.put("o", 5);
			orderMap.put("m", 6);
		}
		
		@Override
		public int compare(Option o1, Option o2) {
			return Integer.compare(orderMap.get(o1.getOpt()), orderMap.get(o2.getOpt()));
		}
		
	}
	
}