package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.SearchPattern;
import courtandrey.SUDRFScraper.service.SeleniumHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import static courtandrey.SUDRFScraper.service.Constant.UA;

public abstract class ConnectorParser implements Parser{
    protected CourtConfiguration cc;

    public ConnectorParser(CourtConfiguration cc) {
        this.cc = cc;
    }

    String getJsoupText(String url) throws IOException {
        Document decision = Jsoup.connect(url)
                .userAgent(UA.toString())
                .timeout(1000 * 60 * 2)
                .get();
        return parseText(decision);
    }

    String getSeleniumText(String href) {
        Document doc = Jsoup.parse(SeleniumHelper.getInstance().getPage(href,setWaitTime()));
        return parseText(doc);
    }

    private int setWaitTime() {
        if (cc.getSearchPattern() == SearchPattern.DEPRECATED_SECONDARY_PATTERN ||
                cc.getSearchPattern() == SearchPattern.SECONDARY_PATTERN) {
            return 3;
        } else {
            return 10;
        }
    }
}
