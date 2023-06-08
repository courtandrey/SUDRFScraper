package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;

public class MosGorSudStrategy extends ConnectionSUDRFStrategy{
    public MosGorSudStrategy(CourtConfiguration cc) {
        super(cc);
    }

    @Override
    public void run() {
        createUrls();
        super.run();
        finish();
    }


}
