package courtandrey.SUDRFScraper.dump;

import courtandrey.SUDRFScraper.dump.model.Case;

import java.io.IOException;
import java.util.Collection;

public interface Updater {
    void startService();
    void update(Collection<Case> cases);
    void writeSummery(String summeryText);
    void joinService() throws InterruptedException;
    void registerEnding();
    void addMeta() throws IOException;
}
