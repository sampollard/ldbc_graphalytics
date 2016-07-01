package nl.tudelft.graphalytics.granula;

import nl.tudelft.granula.modeller.entity.Runner;
import nl.tudelft.graphalytics.reporting.html.HtmlBenchmarkReportGenerator;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by wlngai on 6/30/16.
 */
public class VisualizerManager {

    public static final String STATIC_RESOURCES[] = new String[]{
            "granula/css/granula.css",
            "granula/js/chart.js",
            "granula/js/data.js",
            "granula/js/environmentview.js",
            "granula/js/job.js",
            "granula/js/operation-chart.js",
            "granula/js/operationview.js",
            "granula/js/view.js",
            "granula/js/util.js",
            "granula/js/overview.js",
            "granula/lib/bootstrap.css",
            "granula/lib/bootstrap.js",
            "granula/lib/jquery.js",
            "granula/lib/d3.min.js",
            "granula/lib/nv.d3.css",
            "granula/lib/nv.d3.js",
            "granula/lib/snap.svg-min.js",
            "granula/lib/underscore-min.js",
            "granula/visualizer.htm"
    };


    public static void addVisualizerResource(Runner runner) {
        for (String resource : VisualizerManager.STATIC_RESOURCES) {
            URL resourceUrl = HtmlBenchmarkReportGenerator.class.getResource("/" + resource);

            Path resArcPath = Paths.get(runner.getArcPath()).resolve(resource);
            try {
                if (!resArcPath.getParent().toFile().exists()) {
                    Files.createDirectories(resArcPath.getParent());
                } else if (!resArcPath.getParent().toFile().isDirectory()) {
                    throw new IOException("Could not write static resource to \"" + resArcPath + "\": parent is not a directory.");
                }
                FileUtils.copyInputStreamToFile(resourceUrl.openStream(), resArcPath.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
