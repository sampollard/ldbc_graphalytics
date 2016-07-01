package nl.tudelft.graphalytics.granula;

import nl.tudelft.granula.modeller.entity.Runner;
import nl.tudelft.granula.util.FileUtil;
import nl.tudelft.granula.util.json.JsonUtil;

import java.io.IOException;
import java.nio.file.Paths;

public class FailedJobArchiver {

    public static void main(String[] args) {
        String driverLogPath = args[0];
        Runner runner = (Runner) JsonUtil.fromJson(FileUtil.readFile(Paths.get(driverLogPath)), Runner.class);
        runner.setEndTime(System.currentTimeMillis());
        runner.setArcPath(Paths.get("./iffailed").toAbsolutePath().toString());

        try {
            GranulaDriver.buildJobArchive(runner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
