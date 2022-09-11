package SUDRFScrapper.strategy;

import SUDRFScrapper.configuration.courtconfiguration.CourtConfiguration;

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
