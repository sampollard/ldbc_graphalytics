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

import nl.tudelft.granula.modeller.job.JobModel;
import nl.tudelft.granula.util.FileUtil;
import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GranulaDriver {

	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Property key for enabling or disabling Granula.
	 */
	private static final String GRANULA_ENABLED = "benchmark.run.granula.enabled";
	private static final String PLATFORM_LOGGING_ENABLED = "benchmark.run.granula.platform-logging";
	private static final String ENVIRONMENT_LOGGING_ENABLED = "benchmark.run.granula.environment-logging";
	private static final String ARCHIVING_ENABLED = "benchmark.run.granula.archiving";


	public static boolean enabled;
	public static boolean platformLogEnabled;
	public static boolean envLogEnabled;
	public static boolean archivingEnabled;


	JobModel model;
	Path reportDataPath;

	public GranulaDriver(GranulaAwarePlatform platform) {
		// Load Granula configuration
		PropertiesConfiguration granulaConfig;
		try {
			granulaConfig = new PropertiesConfiguration("granula.properties");
			enabled = granulaConfig.getBoolean(GRANULA_ENABLED, false);
			platformLogEnabled = granulaConfig.getBoolean(PLATFORM_LOGGING_ENABLED, false);
			envLogEnabled = granulaConfig.getBoolean(ENVIRONMENT_LOGGING_ENABLED, false);
			archivingEnabled = granulaConfig.getBoolean(ARCHIVING_ENABLED, false);

			if(enabled) {
				LOG.info("Granula plugin is found, and is enabled.");
				LOG.info(String.format(" - Logging is %s for Granula.", (platformLogEnabled) ? "enabled" : "disabled"));
				LOG.info(String.format(" - Archiving is %s for Granula.", (archivingEnabled) ? "enabled" : "disabled"));
			} else {
				LOG.info("Granula plugin is found, but is disabled.");
			}

			if (archivingEnabled && !platformLogEnabled) {
				LOG.error(String.format("The archiving feature (%s) is not usable while logging feature (%s) is not enabled. " +
						"Turning off the archiving feature of Granula. ", ARCHIVING_ENABLED, PLATFORM_LOGGING_ENABLED));
				enabled = false;
			}
		} catch (ConfigurationException e) {
			LOG.info("Could not find or load granula.properties.");
		}
		setModel(platform.getJobModel());
	}


	public void storeDriverLog(GranulaAwarePlatform platform, Benchmark benchmark, Path benchmarkLogDir) {
		Path backupPath = benchmarkLogDir.resolve("GraphalyticsLog");
		backupPath.toFile().mkdirs();

		Path backFile = backupPath.resolve("graphalytics-log.txt");
		try {
			backFile.toFile().createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String benchmarkLog = "";
		benchmarkLog += platform.getName() + " ";
		benchmarkLog += benchmarkLogDir + " ";
		benchmarkLog += benchmark.getId() + " ";
		benchmarkLog += System.currentTimeMillis() + " ";
		FileUtil.writeFile(benchmarkLog, backFile);
	}

	public void generateArchive(BenchmarkSuiteResult benchmarkSuiteResult) throws IOException {


		// Ensure the log and archive directories exist
		Path logPath = reportDataPath.resolve("log");
		Path stdArcPath = reportDataPath.resolve("archive"); //not used atm
		Path usedArcPath = logPath.getParent().getParent().resolve("html").resolve("granula").resolve("data");
		Files.createDirectories(logPath);
		Files.createDirectories(stdArcPath);  //not used atm

		for (BenchmarkResult benchmarkResult : benchmarkSuiteResult.getBenchmarkResults()) {

			// make sure the log path(s) exists.
			Path logDataPath = logPath.resolve(benchmarkResult.getBenchmark().getBenchmarkIdentificationString());
			Files.createDirectories(logDataPath.resolve("platform"));
			Files.createDirectories(logDataPath.resolve("environment"));

			Benchmark benchmark = benchmarkResult.getBenchmark();
			long startTime = benchmarkResult.getStartOfBenchmark().getTime();
			long endTime = benchmarkResult.getEndOfBenchmark().getTime();
			String jobId = benchmark.getId();

			PlatformArchiver platformArchiver = new PlatformArchiver();
			platformArchiver.createPlatformArchive(logDataPath, usedArcPath, startTime, endTime, model);

			EnvironmentArchiver environmentArchiver = new EnvironmentArchiver();
			environmentArchiver.creatEnvArchive(logDataPath, usedArcPath, jobId);
		}
	}




	public void setModel(JobModel model) {
		this.model = model;
	}

	public void setReportDirPath(Path reportDataPath) {
		this.reportDataPath = reportDataPath;
	}

}
