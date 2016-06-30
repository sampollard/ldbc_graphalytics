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
package nl.tudelft.graphalytics.granula;

import nl.tudelft.granula.util.FileUtil;
import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import nl.tudelft.graphalytics.domain.BenchmarkSuite;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.plugin.Plugin;
import nl.tudelft.graphalytics.reporting.BenchmarkReportGenerator;
import nl.tudelft.graphalytics.reporting.BenchmarkReportWriter;
import nl.tudelft.graphalytics.reporting.html.HtmlBenchmarkReportGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class GranulaPlugin implements Plugin {

	private static final Logger LOG = LogManager.getLogger();

	private static final String LOG_DIR = "log";

	private final GranulaAwarePlatform platform;
	private final BenchmarkReportWriter reportWriter;
	private final GranulaDriver granulaDriver;

	public GranulaPlugin(GranulaAwarePlatform platform, BenchmarkReportWriter reportWriter) {
		this.platform = platform;
		this.reportWriter = reportWriter;
		this.granulaDriver = new GranulaDriver(platform);
	}

	@Override
	public String getPluginName() {
		return "granula";
	}

	@Override
	public String getPluginDescription() {
		return "Granula: Fine Grained Performance Analysis for Big Data Processing Systems";
	}

	@Override
	public void preBenchmarkSuite(BenchmarkSuite benchmarkSuite) {
		// No operation
	}

	@Override
	public void preBenchmark(Benchmark benchmark) {
		if(GranulaDriver.enabled) {
			if(GranulaDriver.platformLogEnabled) {
				granulaDriver.storeDriverLog(platform, benchmark, getLogDirectory(benchmark));
				platform.preBenchmark(benchmark, getLogDirectory(benchmark));
			}
		}

	}


	@Override
	public void postBenchmark(Benchmark benchmark, BenchmarkResult benchmarkResult) {
		if (GranulaDriver.enabled) {
			if (GranulaDriver.platformLogEnabled) {
				platform.postBenchmark(benchmark, getLogDirectory(benchmark));
			}
		}
	}

	@Override
	public void postBenchmarkSuite(BenchmarkSuite benchmarkSuite, BenchmarkSuiteResult benchmarkSuiteResult) {
		if (GranulaDriver.enabled) {
			if (GranulaDriver.archivingEnabled) {
				try {
                    granulaDriver.setReportDirPath(reportWriter.getOrCreateOutputDataPath());
                    granulaDriver.generateArchive(benchmarkSuiteResult);
                } catch (Exception ex) {
                    LOG.error("Failed to generate Granula archives for the benchmark results:", ex);
                }
			}
		}
	}


	@Override
	public void preReportGeneration(BenchmarkReportGenerator reportGenerator) {
		if (GranulaDriver.enabled) {
			if (GranulaDriver.archivingEnabled) {
				if (reportGenerator instanceof HtmlBenchmarkReportGenerator) {
					HtmlBenchmarkReportGenerator htmlReportGenerator = (HtmlBenchmarkReportGenerator)reportGenerator;
					htmlReportGenerator.registerPlugin(new GranulaHtmlGenerator());
				}
			}
		}
	}

	@Override
	public void shutdown() {

	}

	private Path getLogDirectory(Benchmark benchmark) {
		try {
			return reportWriter.getOrCreateOutputDataPath().resolve(LOG_DIR).resolve(benchmark.getBenchmarkIdentificationString());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


}
