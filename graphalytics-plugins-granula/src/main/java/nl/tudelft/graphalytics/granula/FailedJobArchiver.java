package nl.tudelft.graphalytics.granula;

import nl.tudelft.granula.modeller.job.JobModel;
import nl.tudelft.granula.modeller.platform.PlatformModel;
import nl.tudelft.graphalytics.Graphalytics;
import nl.tudelft.graphalytics.GraphalyticsLoaderException;
import nl.tudelft.graphalytics.reporting.html.HtmlBenchmarkReportGenerator;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class FailedJobArchiver {

    public static void main(String[] args) {
        String platformName = args[0];
        Path logPath = Paths.get(args[1]);
        Path archPath = Paths.get("./iffailed");
        String jobId = args[2];
        long startTime = Long.parseLong(args[3]);
        long endTime = System.currentTimeMillis();

        JobModel jobModel =  new JobModel(getPlatformModel(platformName));

        try {
            generateFailedJobArchive(jobId, jobModel, startTime, endTime, logPath, archPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void generateFailedJobArchive(String jobId, JobModel model, long startTime, long endTime, Path logPath, Path arcPath) throws IOException {
        // Ensure the log and archive directories exist
        Files.createDirectories(logPath);
        // make sure the log path(s) exists.

        Path logDataPath = logPath;
        Files.createDirectories(logDataPath.resolve("platform"));
        Files.createDirectories(logDataPath.resolve("environment"));


        PlatformArchiver platformArchiver = new PlatformArchiver();
        platformArchiver.createPlatformArchive(logDataPath, arcPath.resolve("granula/data"), startTime, endTime, model);

        EnvironmentArchiver environmentArchiver = new EnvironmentArchiver();
        environmentArchiver.creatEnvArchive(logDataPath, arcPath.resolve("granula/data"), jobId);


        for (String resource : GranulaHtmlGenerator.STATIC_RESOURCES) {
            URL resourceUrl = HtmlBenchmarkReportGenerator.class.getResource("/" + resource);


            Path outputPath = arcPath.resolve(resource);
            if (!outputPath.getParent().toFile().exists()) {
                Files.createDirectories(outputPath.getParent());
            } else if (!outputPath.getParent().toFile().isDirectory()) {
                throw new IOException("Could not write static resource to \"" + outputPath + "\": parent is not a directory.");
            }
            // Copy the resource to the output file
            FileUtils.copyInputStreamToFile(resourceUrl.openStream(), outputPath.toFile());
        }

    }

    private static PlatformModel getPlatformModel(String platformName) {

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
