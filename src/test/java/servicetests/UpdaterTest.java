package servicetests;

import com.fasterxml.jackson.databind.ObjectMapper;
import courtandrey.SUDRFScraper.configuration.ApplicationConfiguration;
import courtandrey.SUDRFScraper.dump.JSONUpdaterService;
import courtandrey.SUDRFScraper.dump.Updater;
import courtandrey.SUDRFScraper.dump.model.Case;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static courtandrey.SUDRFScraper.service.Constant.*;

public class UpdaterTest {
    @SuppressWarnings("unchecked")
    @Test
    public void successJSONUpdaterTest() throws IOException, InterruptedException {
        ApplicationConfiguration.getInstance().setProperty("basic.result.path", "./");
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

        Path resultPath = Path.of(String.format(PATH_TO_RESULT_JSON.toString(), dumpName, dumpName));
        Path summeryPath = Path.of(String.format(PATH_TO_SUMMERY.toString(), dumpName, dumpName));
        Path metaPath = Path.of(String.format(PATH_TO_RESULT_META.toString(), dumpName, dumpName));

        assert Files.exists(resultPath) && Files.size(resultPath) > 0;
        assert Files.exists(summeryPath) && Files.size(summeryPath) > 0;
        assert Files.exists(metaPath) && Files.size(metaPath) > 0;

        HashMap<String, String> map = (HashMap<String, String>) (new ObjectMapper()).readValue(metaPath.toFile(), HashMap.class);

        assert map.get("string_count").equals("2");

        Files.deleteIfExists(resultPath);
        Files.deleteIfExists(summeryPath);
        Files.deleteIfExists(metaPath);
        Files.deleteIfExists(Path.of(String.format(PATH_TO_RESULT_DIRECTORY.toString(), dumpName)));
    }
}
