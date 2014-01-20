/**
 * 
 */
package and146.projects.convolution;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import and146.projects.convolution.IConvolutionMatrixProcessor.MeasurementType;
import and146.projects.convolution.gui.ImageFrame;
import and146.projects.convolution.nativelibs.NativeLibrariesUtils;
import and146.projects.convolution.opencv.InMemoryGrayscaleImage;
import and146.projects.convolution.opencv.InMemoryImage;
import and146.projects.convolution.opencv.OpenCVHelper;

/**
 * @author neonards
 *
 */
public class ConvolutionToolApplication {
	
	/* possible command line options */
	public static final Option CLI_OPT_HELP = new Option("h", "help", false, "show this help");
	public static final Option CLI_OPT_INPUT_IMAGE = new Option("i", "input-image", true, "the input image's filaname");
	public static final Option CLI_OPT_SHOW_INPUT_IMAGE = new Option("si", "show-input-image", false, "show input image when loaded");
	public static final Option CLI_OPT_SHOW_OUTPUT_IMAGE = new Option("so", "show-output-image", false, "show input image(s) when loaded");
	public static final Option CLI_OPT_MATRIX_FILE = new Option("f", "matrix-file", true, "path to the XML containing the convolution matrices");
	public static final Option CLI_OPT_DEBUG = new Option("d", "debug", false, "enables the debug mode (maximal verbosity)");
	public static final Option CLI_OPT_HUMAN_READABLE = new Option("h", "human-readable", false, "enables the human readable time output");
	public static final Option CLI_OPT_LOG_TO_FILE = new Option("l", "log", true, "enables the logging to a file");
	public static final Option CLI_OPT_WORKING_LOOPS = new Option("wl", "measure-working-loops-only", false, "measures only the working loops");
	
	//
	private static Logger log = LoggerFactory.getLogger(ConvolutionToolApplication.class);
	/* default settings for logger */
    private static String LOG_PATTERN = "%d [%p|%c|%C{1}] %m%n";
    private static Level LOG_LEVEL = Level.INFO;
    //
	private static CommandLine commandLine = null;
	private static Options options = null;
	
	private static InMemoryImage inputImage;
	private static List<ConvolutionMatrix> matrices;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initLogger();
		
		log.info("Starting the application ...");
		log.info(String.format("System info: %s %s", NativeLibrariesUtils.getOperatingSystem(), NativeLibrariesUtils.getArchitecture()));
		
		// parse the command line arguments
		initCli(args);
		
		// update the logger appenders based on CLI settings
        updateLoggerAppenders();
		
		// load OpenCV's native libraries
		OpenCVHelper.loadNativeLibraries();
		
		log.info("Initialization done.");
		System.out.println();
		
		if (getCommandLine().hasOption(CLI_OPT_HELP.getOpt())) {
			printHelp(true);
		}
		
		checkNeccessaryArguments();
		
		readInputFile();
		readConfigFile();
		processInput();
		
	}
	
	private static void initLogger() {
		BasicConfigurator.configure();
        
        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        rootLogger.getLoggerRepository().resetConfiguration();
        
        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = LOG_PATTERN;
        console.setLayout(new PatternLayout(PATTERN)); 
        console.setThreshold(LOG_LEVEL);
        console.activateOptions();
        //add appender to any Logger (here is root)
        rootLogger.addAppender(console);
	}
	
	private static void updateLoggerAppenders() {
        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        
        // debug mode?
        if (getCommandLine().hasOption(CLI_OPT_DEBUG.getOpt())) {
                rootLogger.getLoggerRepository().resetConfiguration();
                LOG_LEVEL = Level.DEBUG;
                
                ConsoleAppender console = new ConsoleAppender(); //create appender
                //configure the appender
                console.setLayout(new PatternLayout(LOG_PATTERN)); 
                console.setThreshold(LOG_LEVEL);
                console.activateOptions();
                //add appender to any Logger (here is root)
                rootLogger.addAppender(console);
        }
        
        // log to file?
        if (getCommandLine().hasOption(CLI_OPT_LOG_TO_FILE.getOpt())) {
                String logFile = getCommandLine().getOptionValue(CLI_OPT_LOG_TO_FILE.getOpt());
                
                FileAppender fa = new FileAppender();
                fa.setName("FileLogger");
                fa.setFile(logFile);
                fa.setLayout(new PatternLayout(LOG_PATTERN));
                fa.setThreshold(LOG_LEVEL);
                fa.setAppend(true);
                fa.activateOptions();

                //add appender to any Logger (here is root)
                rootLogger.addAppender(fa);
        }
	}
	
	/**
	 * Initializes Apache CLI to work with command line arguments
	 * @param args
	 */
	private static void initCli(String[] args) {
		log.debug("Parsing command line arguments...");
		options = new Options();
		
		fillCommandLineOptions();
		
		CommandLineParser parser = new BasicParser();
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			log.error("Unable to parse command line arguments: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Fills the list of possible command line options
	 */
	private static void fillCommandLineOptions() {
		getOptions().addOption(CLI_OPT_HELP);
		getOptions().addOption(CLI_OPT_INPUT_IMAGE);
		getOptions().addOption(CLI_OPT_SHOW_INPUT_IMAGE);
		getOptions().addOption(CLI_OPT_SHOW_OUTPUT_IMAGE);
		getOptions().addOption(CLI_OPT_MATRIX_FILE);
		getOptions().addOption(CLI_OPT_DEBUG);
        getOptions().addOption(CLI_OPT_LOG_TO_FILE);
        getOptions().addOption(CLI_OPT_HUMAN_READABLE);
        getOptions().addOption(CLI_OPT_WORKING_LOOPS);
	}
	
	/**
	 * Checks that the user put all neccessary arguments on command line
	 */
	private static void checkNeccessaryArguments() {
		boolean valid = true;
		
		if (
				// no input specified
				!getCommandLine().hasOption(CLI_OPT_INPUT_IMAGE.getOpt())
						
				// no matrix XML
				|| !getCommandLine().hasOption(CLI_OPT_MATRIX_FILE.getOpt())
				) {
			valid = false;
		}
		
		if (!valid) {
			System.out.println("Missing some arguments.");
			System.out.println();
			printHelp();
			System.exit(1);
		}
	}
	
	/**
	 * Attempts to load and possibly show the input image.
	 */
	private static void readInputFile() {
		log.info(String.format("Loading file '%s'", getCommandLine().getOptionValue(CLI_OPT_INPUT_IMAGE.getOpt())));
		File inputFile = new File(getCommandLine().getOptionValue(CLI_OPT_INPUT_IMAGE.getOpt()));
		if (!inputFile.exists() || !inputFile.isFile()) {
			log.error("Error while reading the input file.");
			System.exit(1);
		}
		inputImage = new InMemoryGrayscaleImage(inputFile.getAbsolutePath());
		
		ImageFrame inputImageFrame;
		
		if (getCommandLine().hasOption(CLI_OPT_SHOW_INPUT_IMAGE.getOpt()))
			inputImageFrame = new ImageFrame(inputImage);
		
		log.debug(String.format("Input file '%s' loaded", getCommandLine().getOptionValue(CLI_OPT_INPUT_IMAGE.getOpt())));
	}
	
	/**
	 * Parses the config XML file
	 */
	private static void readConfigFile() {
		matrices = new ArrayList<ConvolutionMatrix>();
		
		// read XML
		File xmlFile = new File(getCommandLine().getOptionValue(CLI_OPT_MATRIX_FILE.getOpt()));
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("matrix");
			
			// "matrix"
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					 
					Element eElement = (Element) nNode;
					
					String matrixName = eElement.getAttribute("name");
					List<List<Integer>> matrixRows = new LinkedList<List<Integer>>();
					
					if (matrixName == null)
						throw new IllegalStateException("No 'name' attribute found for matrix at index " + i);
					
					NodeList rows = eElement.getElementsByTagName("row");
					if (rows.getLength() == 0)
						throw new IllegalStateException(String.format("No rows found for matrix '%s'", matrixName));
					
					int colsCount = -1; // symetry check
					
					// "row"
					for (int j = 0; j < rows.getLength(); j++) {
						Node nRow = rows.item(j);
						if (nRow.getNodeType() == Node.ELEMENT_NODE) {
							Element row = (Element) nRow;
							
							NodeList cols = row.getElementsByTagName("col");
							
							if (cols.getLength() == 0)
								throw new IllegalStateException(String.format("No cols found for matrix '%s'", matrixName));
							
							if (colsCount == -1)
								colsCount = cols.getLength();
							
							if (cols.getLength() != colsCount)
								throw new IllegalStateException(String.format("Mismatching count of columns for matrix '%s'", matrixName));
							
							List<Integer> matrixRowCols = new LinkedList<Integer>();
							
							// "col"
							for (int k = 0; k < cols.getLength(); k++) {
								Node nCol = cols.item(k);
								if (nCol.getNodeType() == Node.ELEMENT_NODE) {
									Element col = (Element) nCol;
									matrixRowCols.add(Integer.parseInt(col.getTextContent()));
								}
							}
							
							matrixRows.add(matrixRowCols);
						}
					}
					
					// assemble the matrix
					int[][] matrix = new int[matrixRows.size()][matrixRows.get(0).size()];
					
					for (int m = 0; m < matrixRows.size(); m++) {
						for (int n = 0; n < matrixRows.get(0).size(); n++) {
							matrix[m][n] = matrixRows.get(m).get(n);
						}
					}
					
					ConvolutionMatrix convolutionMatrix = new ConvolutionMatrix(matrixName, matrix);
					matrices.add(convolutionMatrix);
				}
			}
			
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error("Unable to parse input XML: " + e.getMessage());
			System.exit(1);
		}
		
	}
	
	/**
	 * Processes the input and saves and shows the output
	 */
	private static void processInput() {
		
		MeasurementType measurementType = 
				getCommandLine().hasOption(CLI_OPT_WORKING_LOOPS.getOpt())
				? MeasurementType.WORKING_LOOPS_ONLY
						: MeasurementType.WHOLE_PROCESS;
		
		// serial processor
		log.info("Processing with serial processor...");
		IConvolutionMatrixProcessor serialProcessor = new ConvolutionMatrixProcessorSerialImpl(measurementType);
		serialProcessor.process(inputImage, 
				matrices, 
				getCommandLine().hasOption(CLI_OPT_SHOW_OUTPUT_IMAGE.getOpt()));
		
		System.out.println();
		
		// parallel (OpenCL) processor
		log.info("Processing with parallel processor...");
		IConvolutionMatrixProcessor openClProcessor = new ConvolutionMatrixProcessorOpenCLImpl(measurementType);
		openClProcessor.process(inputImage, 
				matrices, 
				getCommandLine().hasOption(CLI_OPT_SHOW_OUTPUT_IMAGE.getOpt()));
		
		// print comparison results
		Map<Class<? extends IConvolutionMatrixProcessor>, Long> totalTimes = new HashMap<Class<? extends IConvolutionMatrixProcessor>, Long>();
		totalTimes.put(serialProcessor.getClass(), Long.valueOf(0));
		totalTimes.put(openClProcessor.getClass(), Long.valueOf(0));
		StringBuilder results = new StringBuilder("\n");
		int normalizeMillisTo = 9;
		results.append("Comparison:\t|\tSerial algorithm (SA)\t|\tSA in millis\t|\tParallel OpenCL algorithm (OCL)\t|\tOCL in millis\t|\tMatrix name\n");
		for (String matrixName : serialProcessor.getProcessingTimes().keySet()) {
			results.append(String.format(
					"\t\t|\t%s\t|\t%s\t|\t%s\t\t|\t%s\t|\t%s\n", 
					formatMillis(serialProcessor.getProcessingTimes().get(matrixName)),
					normalize(serialProcessor.getProcessingTimes().get(matrixName), normalizeMillisTo),
					formatMillis(openClProcessor.getProcessingTimes().get(matrixName)),
					normalize(openClProcessor.getProcessingTimes().get(matrixName), normalizeMillisTo),
					matrixName));
			
			totalTimes.put(serialProcessor.getClass(), totalTimes.get(serialProcessor.getClass())+serialProcessor.getProcessingTimes().get(matrixName));
			totalTimes.put(openClProcessor.getClass(), totalTimes.get(openClProcessor.getClass())+openClProcessor.getProcessingTimes().get(matrixName));
		}
		results.append(String.format("Total:\t\t|\t%s\t|\t%s\t|\t%s\t\t|\t%s\t|\t\n",
				formatMillis(totalTimes.get(serialProcessor.getClass())),
				normalize(totalTimes.get(serialProcessor.getClass()), normalizeMillisTo),
				formatMillis(totalTimes.get(openClProcessor.getClass())),
				normalize(totalTimes.get(openClProcessor.getClass()), normalizeMillisTo)));
		long difference = totalTimes.get(serialProcessor.getClass()) - totalTimes.get(openClProcessor.getClass());
		DecimalFormat diffDecFormat = new DecimalFormat("#.##");
		diffDecFormat.setRoundingMode(RoundingMode.HALF_UP);
		String winningImpl = totalTimes.get(serialProcessor.getClass()) < totalTimes.get(openClProcessor.getClass()) ? "Serial" : "OpenCL";
		results.append("\n");
		results.append(String.format("Difference between serial and OpenCL implementation: %s (%sx). ",
				(difference < 0 ? "-" : "") + formatMillis(Math.abs(difference)),
				diffDecFormat.format((float)totalTimes.get(serialProcessor.getClass()) / (float)totalTimes.get(openClProcessor.getClass()))));
		results.append(winningImpl + " implementation wins.");
		results.append("\n");
		
		log.info(results.toString());
		
	}
	
	public static CommandLine getCommandLine() {
		return commandLine;
	}
	
	public static Options getOptions() {
		return options;
	}
	
	/**
	 * Prints help on command line
	 */
	public static void printHelp() {
		printHelp(false);
	}
	
	/**
	 * Prints help on command line
	 * @param isRequestedByUser Tells if the user requested it, or it's an error state (different formatting)
	 */
	public static void printHelp(boolean isRequestedByUser) {
		if (!isRequestedByUser) { // e.g. missing param
			System.out.println("----");
			System.out.println();
		}
		
		String jarName = ConvolutionToolApplication.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		jarName = jarName.substring(jarName.lastIndexOf(NativeLibrariesUtils.getOperatingSystem().getFilePathDelimiter()) + 1);
		if (jarName.length() == 0) // running from IDE
			jarName = ConvolutionToolApplication.class.getSimpleName();
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(jarName, getOptions());
		System.exit(isRequestedByUser ? 0 : 1);
	}
	
	/**
	 * Formats time in millis
	 * @param millis
	 * @return
	 */
	private static String formatMillis(long millis) {
		TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
		Calendar cal = Calendar.getInstance(utcTimeZone);
		cal.setTimeInMillis(millis);
		DateFormat formatter = new SimpleDateFormat("HH'h' mm'min' ss.SSS's'");
		formatter.setTimeZone(utcTimeZone);
		
		return formatter.format(cal.getTime());
	}
	
	/**
	 * Helper method for formatting the comparison output.
	 * Normalizes the number's length to a given length.
	 * @param num
	 * @param maxLength
	 * @return
	 */
	private static String normalize(long num, int maxLength) {
		StringBuilder sb = new StringBuilder();
		
		String numStr = String.valueOf(num);
		
		for (int i = 0; i < maxLength-numStr.length(); i++) {
			sb.append(" ");
		}
		
		for (int i = 0; i < numStr.length(); i++) {
			sb.append(numStr.charAt(i));
		}
		
		return sb.toString();
	}
}
