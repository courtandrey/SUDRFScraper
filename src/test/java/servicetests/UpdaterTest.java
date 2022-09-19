package servicetests;

import courtandrey.SUDRFScraper.dump.JSONUpdaterService;
import courtandrey.SUDRFScraper.dump.Updater;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.service.Constants;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class UpdaterTest {
    @Test
    public void successJSONUpdaterTest() throws IOException, InterruptedException {
        String dumpName = "dumpName";
        Updater updater = new JSONUpdaterService(dumpName, (e, t) -> e.printStackTrace());
        updater.startService();
        Collection<Case> cases = new ArrayList<>();
        cases.add(new Case());
        cases.add(new Case());
        updater.update(cases);
        updater.writeSummery("Updated by JSONUpdaterService");
        updater.addMeta();
        updater.joinService();

        Path resultPath = Path.of(String.format(Constants.PATH_TO_RESULT_JSON, dumpName, dumpName));
        Path summeryPath = Path.of(String.format(Constants.PATH_TO_SUMMERY, dumpName, dumpName));
        Path metaPath = Path.of(String.format(Constants.PATH_TO_RESULT_META, dumpName, dumpName));

        assert Files.exists(resultPath) && Files.size(resultPath) > 0;
        assert Files.exists(summeryPath) && Files.size(summeryPath) > 0;
        assert Files.exists(metaPath) && Files.size(metaPath) > 0;

        Files.deleteIfExists(resultPath);
        Files.deleteIfExists(summeryPath);
        Files.deleteIfExists(metaPath);
        Files.deleteIfExists(Path.of(String.format(Constants.PATH_TO_RESULT_DIRECTORY, dumpName)));
    }
}
