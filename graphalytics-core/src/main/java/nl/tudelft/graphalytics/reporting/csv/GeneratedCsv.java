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

import nl.tudelft.graphalytics.reporting.BenchmarkReportFile;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implements the write method for a CSV file, same as HTML.
 * @author Samuel Pollard (https://github.com/sampollard)
 */
public class GeneratedCsv implements BenchmarkReportFile {

	private String csvData;
	private String relativePath;
	private String baseFilename;

	/**
	 * @param csvData the raw data for this page
	 * @param relativePath the path relative to the report root to write this page to, or "." for root
	 * @param baseFilename the filename (excluding extension) of this page
	 */
	public GeneratedCsv(String csvData, String relativePath, String baseFilename) {
		this.csvData = csvData;
		this.relativePath = relativePath;
		this.baseFilename = baseFilename;
	}

	@Override
	public void write(Path reportPath) throws IOException {
		Path outputDirectory = reportPath.resolve(relativePath);
		Path outputPath = outputDirectory.resolve(baseFilename + ".csv");
		// Ensure that the output directory exists
		if (!outputDirectory.toFile().exists()) {
			Files.createDirectories(outputDirectory);
		} else if (!outputDirectory.toFile().isDirectory()) {
			throw new IOException("Could not write static resource to \"" + outputPath + "\": parent is not a directory.");
		}
		// Write the CSV data to a file
		FileUtils.writeStringToFile(outputPath.toFile(), csvData);
	}
}
