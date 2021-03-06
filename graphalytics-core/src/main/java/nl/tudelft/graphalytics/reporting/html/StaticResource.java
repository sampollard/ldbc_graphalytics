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
package nl.tudelft.graphalytics.reporting.html;

import nl.tudelft.graphalytics.reporting.BenchmarkReportFile;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by tim on 12/14/15.
 */
public class StaticResource implements BenchmarkReportFile {

	private final URL resourceUrl;
	private final String relativeOutputPath;

	public StaticResource(URL resourceUrl, String relativeOutputPath) {
		this.resourceUrl = resourceUrl;
		this.relativeOutputPath = relativeOutputPath;
	}

	@Override
	public void write(Path reportPath) throws IOException {
		Path outputPath = reportPath.resolve(relativeOutputPath);
		// Ensure that the containing directory exists
		if (!outputPath.getParent().toFile().exists()) {
			Files.createDirectories(outputPath.getParent());
		} else if (!outputPath.getParent().toFile().isDirectory()) {
			throw new IOException("Could not write static resource to \"" + outputPath + "\": parent is not a directory.");
		}
		// Copy the resource to the output file
		FileUtils.copyInputStreamToFile(resourceUrl.openStream(), outputPath.toFile());
	}

}
