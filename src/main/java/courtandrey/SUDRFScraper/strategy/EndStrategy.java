package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;

public class EndStrategy extends SUDRFStrategy {
    public EndStrategy(CourtConfiguration cc) {
        super(cc);
    }

    @Override
    public void run() {
        finish();
    }

    @Override
    protected void finish() {
    }
}
