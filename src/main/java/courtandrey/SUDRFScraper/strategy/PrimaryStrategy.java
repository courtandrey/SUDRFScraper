package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.SearchPattern;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue;


public class PrimaryStrategy extends ConnectionSUDRFStrategy {

    public PrimaryStrategy(CourtConfiguration cc) {
        super(cc);
    }

    @Override
    public void run() {
        createUrls();
        iterateThroughUrls();
        if (checkSuccessAndChangePattern(SearchPattern.SECONDARY_PATTERN,SearchPattern.DEPRECATED_SECONDARY_PATTERN) ||
                checkSuccessAndChangePattern(SearchPattern.DEPRECATED_SECONDARY_PATTERN,SearchPattern.SECONDARY_PATTERN)) {
            iterateThroughUrls();
        }
        finish();
    }

    private boolean checkSuccessAndChangePattern(SearchPattern src, SearchPattern trg) {
        if (finalIssue != Issue.SUCCESS && cc.getSearchPattern()==src) {
            cc.setSearchPattern(trg);
            createUrls();
            return true;
        }
        return false;
    }

    private void iterateThroughUrls() {
        for (; indexUrl < urls.length; indexUrl++) {
            super.run();
            if (finalIssue == Issue.SUCCESS) {
                indexUrl++;
                break;
            } else {
                clear();
            }
        }
        --indexUrl;
    }

}
