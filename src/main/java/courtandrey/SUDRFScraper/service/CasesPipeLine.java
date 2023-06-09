package courtandrey.SUDRFScraper.service;

import courtandrey.SUDRFScraper.dump.Updater;
import courtandrey.SUDRFScraper.dump.model.Case;

import java.util.Collection;

public class CasesPipeLine {
    private final Updater updater;

    public CasesPipeLine(Updater updater) {
        this.updater = updater;
    }

    public void offer(Collection<Case> _case) {
        updater.update(_case);
    }


}
