/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.reporting.csv;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.domain.GraphSet;
import nl.tudelft.graphalytics.reporting.BenchmarkReport;
import nl.tudelft.graphalytics.reporting.BenchmarkReportData;
import nl.tudelft.graphalytics.reporting.BenchmarkReportFile;
import nl.tudelft.graphalytics.reporting.BenchmarkReportGenerator;

import java.util.*;
import java.lang.StringBuffer;

/**
 * Utility class for generating an CSV-based BenchmarkReport from a BenchmarkSuiteResult.
 * This is used instead of the html benchmark generator to facilitate machine-readability
 * of results. For each metric used, one CSV is created where the rows are the datasets
 * and the columns are the algorithm abbreviations.
 * @author Samuel Pollard
 */
public class CsvBenchmarkReportGenerator implements BenchmarkReportGenerator {

	public static final String REPORT_TYPE_IDENTIFIER = "csv";
	private final List<Plugin> plugins = new LinkedList<>();
	private Map<Benchmark, String> pluginPageLinks;
	private static final String delim = ",";

	private String createCsvFromResult(BenchmarkSuiteResult result) {
		StringBuffer csv = new StringBuffer();
		BenchmarkReportData reportData = new BenchmarkReportData(result);
		boolean header = true;
		Collection<GraphSet> allGraphSets = reportData.getGraphSets();
		for (Algorithm alg : reportData.getAlgorithms()) {
			if (header) { // Header row: Print out all the dataset names
				for (GraphSet graphset : allGraphSets) {
					csv.append(delim + graphset.getName());
				}
				header = false;
			} else { // Subsequent rows: Print out the algorithms and all the results
				csv.append(alg.getAcronym());
				Map<GraphSet, BenchmarkResult> resultsByAlgorithm = reportData.getResults(alg);
				for (GraphSet graphset : allGraphSets) {
					long runtime = resultsByAlgorithm.get(graphset).getElapsedTimeInMillis();
					csv.append(delim + Long.toString(runtime));
				}
			}
			csv.append("\n");
		}
		return csv.toString();
	}

	@Override
	public BenchmarkReport generateReportFromResults(BenchmarkSuiteResult result) {
		// Callback to plugins before generation
		pluginPageLinks = new HashMap<>();
		for (Plugin plugin : plugins) {
			plugin.preGenerate(this, result);
		}

		// Generate the report files, one table (csv file) per metric (e.g. runtime, TEPS).
		Collection<BenchmarkReportFile> reportFiles = new LinkedList<>();
		// First add runtime in seconds
		String csvString = createCsvFromResult(result);
		reportFiles.add(new GeneratedCsv(csvString, "../", "runtime"));

		// Callback to plugins after generation for additional files
		for (Plugin plugin : plugins) {
			Collection<BenchmarkReportFile> additionalFiles = plugin.generateAdditionalReportFiles(this, result);
			if (additionalFiles != null) {
				reportFiles.addAll(additionalFiles);
			}
		}

		return new BenchmarkReport(REPORT_TYPE_IDENTIFIER, reportFiles);
	} 
	/**
	 * Adds a plugin instance to the list of plugins that will receive callbacks throughout the generation process.
	 *
	 * @param plugin the plugin instance to add
	 */
	public void registerPlugin(Plugin plugin) {
		plugins.add(plugin);
	}

	/**
	 * Callback interface for plugins to inject custom CSV files and resources into the benchmark report.
	 */
	public interface Plugin {

		/**
		 * Callback before generation of the default Graphalytics benchmark report starts.
		 *
		 * @param generator the benchmark report generator instance
		 * @param result    the results of running a benchmark suite
		 */
		void preGenerate(CsvBenchmarkReportGenerator generator, BenchmarkSuiteResult result);

		/**
		 * Callback during benchmark report generation to add additional pages and resources to the report.
		 *
		 * @param generator the benchmark report generator instance
		 * @param result    the results of running a benchmark suite from which a report is to be generated
		 * @return a collection of additional tables and resources
		 */
		Collection<BenchmarkReportFile> generateAdditionalReportFiles(CsvBenchmarkReportGenerator generator,
				BenchmarkSuiteResult result);

	}

}
