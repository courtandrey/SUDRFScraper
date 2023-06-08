package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.dump.model.Case;
import org.jsoup.nodes.Document;

import java.util.Collection;
import java.util.Set;

public interface Parser {
    boolean isTextFound();
    Set<Case> scrap(Document document, String currentUrl);

    Set<Case> scrapTexts(Set<Case> resultCases);
    String parseText(Document decision);
}
