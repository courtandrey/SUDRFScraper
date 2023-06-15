package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.configuration.ApplicationConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.SearchPattern;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.service.CasesPipeLine;
import courtandrey.SUDRFScraper.service.SeleniumHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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

    @Override
    public void scrapTextsAndFlush(Set<Case> resultCases, CasesPipeLine casesPipeLine) {
        Set<Case> chunk = new HashSet<>();
        String chunkSizeString = ApplicationConfiguration.getInstance().getProperty("dev.pipeline.chunk_size");
        int chunkSize = 10000;
        if (chunkSizeString != null) {
            try {
                chunkSize = Integer.parseInt(chunkSizeString);
            } catch (Exception ignored) {}
        }
        while (!resultCases.isEmpty()) {
            for (Case _case:resultCases) {
                chunk.add(_case);
                if (chunk.size() == chunkSize) {
                    break;
                }
            }
            chunk = scrapTexts(chunk);
            casesPipeLine.offer(chunk);
            resultCases.removeAll(chunk);
            chunk.clear();
        }
    }

    protected String cleanUp(String dirtyString) {
        dirtyString = dirtyString.replaceAll("\\s{2,}", "");
        dirtyString = dirtyString.replaceAll("\n+", "\n");
        dirtyString = dirtyString.replaceAll(" ","");
        return dirtyString.replaceAll("\u200B","");
    }
}
