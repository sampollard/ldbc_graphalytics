package nl.tudelft.graphalytics.granula;

import nl.tudelft.granula.archiver.GranulaArchiver;
import nl.tudelft.granula.modeller.entity.BasicType;
import nl.tudelft.granula.modeller.entity.Runner;
import nl.tudelft.granula.modeller.job.JobModel;
import nl.tudelft.granula.modeller.job.Overview;
import nl.tudelft.granula.modeller.source.JobDirectorySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by wlngai on 6/20/16.
 */
public class PlatformArchiver {

    public void buildArchive(Runner runner) {

        Overview overview = new Overview();
        overview.setStartTime(runner.getStartTime());
        overview.setEndTime(runner.getEndTime());
        overview.setAlgorithm(runner.getAlgorithm());
        overview.setDataset(runner.getDataset());
        overview.setName(String.format("%s %s-%s [Proof of concepts]",
                runner.getPlatform().toUpperCase(), runner.getAlgorithm(), runner.getDataset()));
        overview.setDescription("Description not available yet.");


        JobDirectorySource jobDirSource = new JobDirectorySource(runner.getLogPath());
        jobDirSource.load();
        String arcPath = Paths.get(runner.getArcPath()).resolve("granula/data").toAbsolutePath().toString();
        JobModel jobModel =  new JobModel(GranulaDriver.getPlatformModel(runner.getPlatform()));
        GranulaArchiver granulaArchiver = new GranulaArchiver(jobDirSource, jobModel, arcPath, BasicType.ArchiveFormat.JS);
        granulaArchiver.setOverview(overview);
        granulaArchiver.archive();
    }


}
