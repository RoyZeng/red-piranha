package net.firstpartners.sample.DslRuleflow;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import net.firstpartners.drools.FileRuleLoader;
import net.firstpartners.drools.RuleRunner;
import net.firstpartners.drools.data.RuleSource;
import net.firstpartners.drools.log.SpreadSheetLogger;
import net.firstpartners.spreadsheet.Range;
import net.firstpartners.spreadsheet.RangeConvertor;
import net.firstpartners.spreadsheet.RangeHolder;
import net.firstpartners.spreadsheet.SpreadSheetOutputter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Sample showing how we can read and manipulate data from excel
 * Read Ranges from Excel, Convert to a format that rules can use
 * 
 * Based on Sample from Apache POI
 * 
 * @author paulbrowne
 * 
 */
public class DslChocolateTradingExample {

	private static Log log = LogFactory.getLog(DslChocolateTradingExample.class);

	private static final String EXCEL_DATA_FILE = "chocolate-data.xls";

	private static final String EXCEL_OUTPUT_FILE = "chocolate-output.xls";

	// the name of the sheet the we log files to
	private static final String EXCEL_LOG_WORKSHEET_NAME = "log";

	private static final String[] RULES_FILES = new String[] {
	"log-rules.dslr"};

	private static final String DSL_FILE="cell-logging.dsl";

	/**
	 * Read an excel file and spit out what we find.
	 * 
	 * @param args
	 *            Expect one argument that is the file to read.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// Open our Excel file using Apache Poi
		// This method searches for our file in a number of places on disk
		InputStream inputFromExcel = DslChocolateTradingExample.class
		.getClassLoader().getResourceAsStream(EXCEL_DATA_FILE);

		if (null == inputFromExcel) {
			throw new FileNotFoundException("Cannot find file:"
					+ EXCEL_DATA_FILE);
		} else {
			log.info("found file:" + EXCEL_DATA_FILE);
		}

		// Convert this into a (POI) Workbook
		HSSFWorkbook wb = new HSSFWorkbook(new POIFSFileSystem(inputFromExcel));

		// Convert the cell
		RangeHolder ranges = RangeConvertor.convertExcelToCells(wb);
		HashMap<String, Object> globals = new HashMap<String, Object>();

		// Create a new Excel Logging object
		SpreadSheetLogger excelLogger = new SpreadSheetLogger();

		// Log the cell contents
		log.debug("============ Excel Cell Contents In =========");
		for (Range r : ranges) {
			log.debug(r);
		}

		// Set Paramaters
		RuleSource ruleSource = new RuleSource();
		ruleSource.setDslFileLocation(DSL_FILE);
		ruleSource.setRulesLocation(RULES_FILES);
		ruleSource.setFacts(ranges.getAllRangesAndCells());
		ruleSource.setGlobals(globals);

		// Load and fire our rules files against the data
		new RuleRunner(new FileRuleLoader()).runStatelessRules(ruleSource,excelLogger);

		// Log the cell contents
		log.debug("============ Excel Cell Contents Out =========");
		for (Range r : ranges) {
			log.debug(r);
		}

		// update the excel spreadsheet with the result of our rules
		RangeConvertor.convertCellsToExcel(wb, ranges);

		// update the excel spreadsheet with our log file
		excelLogger.flush(wb, EXCEL_LOG_WORKSHEET_NAME);

		// Write out modified Excel sheet
		SpreadSheetOutputter.outputToFile(wb, EXCEL_OUTPUT_FILE);

		// Close our input work book
		inputFromExcel.close();

		// complete
		log.info("Finished");
	}

}
