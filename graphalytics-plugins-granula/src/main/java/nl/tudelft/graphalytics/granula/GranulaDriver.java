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

import nl.tudelft.granula.modeller.entity.Runner;
import nl.tudelft.granula.modeller.job.JobModel;
import nl.tudelft.granula.modeller.platform.PlatformModel;
import nl.tudelft.granula.util.FileUtil;
import nl.tudelft.granula.util.json.JsonUtil;
import nl.tudelft.graphalytics.Graphalytics;
import nl.tudelft.graphalytics.GraphalyticsLoaderException;
import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.reporting.html.HtmlBenchmarkReportGenerator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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



	public GranulaDriver() {
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
	}


	public void preserveDriverLog(GranulaAwarePlatform platform, Benchmark benchmark, Path benchmarkLogDir) {
		Path backupPath = benchmarkLogDir.resolve("runner");
		backupPath.toFile().mkdirs();

		Path backFile = backupPath.resolve("runner-log.js");


		Runner runner = new Runner();
		runner.setPlatform(platform.getName());
		runner.setAlgorithm(benchmark.getAlgorithm().getName());
		runner.setDataset(benchmark.getGraph().getName());
		runner.setJobId(benchmark.getId());
		runner.setLogPath(benchmarkLogDir.toAbsolutePath().toString());
		runner.setStartTime(System.currentTimeMillis());

		FileUtil.writeFile(JsonUtil.toJson(runner), backFile);
	}

	public static void buildJobArchive(Runner runner) throws IOException {

		PlatformArchiver platformArchiver = new PlatformArchiver();
		platformArchiver.buildArchive(runner);

		EnvironmentArchiver environmentArchiver = new EnvironmentArchiver();
		environmentArchiver.buildArchive(runner);

		VisualizerManager.addVisualizerResource(runner);
	}

	public static PlatformModel getPlatformModel(String platformName) {

		InputStream platformFileStream = Graphalytics.class.getResourceAsStream("/" + platformName + ".model");
		if (platformFileStream == null) {
			throw new GraphalyticsLoaderException("Missing resource \"" + platformName + ".model\".");
		}

		String modelClassName;
		try (Scanner platformScanner = new Scanner(platformFileStream)) {
			String line = null;
			if (!platformScanner.hasNext()) {
				throw new GraphalyticsLoaderException("Expected a single line with a class name in \"" + platformName +
						".model\", got an empty file.");
			}
			line = platformScanner.next();
			while(line.trim().equals("")) {
				line = platformScanner.next();
			}
			modelClassName = line;
		}

		Class<? extends PlatformModel> modelClass;
		try {
			Class<?> modelClassUncasted = Class.forName(modelClassName);
			modelClass = modelClassUncasted.asSubclass(PlatformModel.class);
		} catch (ClassNotFoundException e) {
			throw new GraphalyticsLoaderException("Could not find class \"" + modelClassName + "\".", e);
		}

		PlatformModel platformModel = null;

		try {
			platformModel = modelClass.newInstance();
		} catch (Exception e) {
			throw new GraphalyticsLoaderException("Could not load class \"" + modelClassName + "\".", e);
		}

		return platformModel;
	}




}
