package courtandrey.SUDRFScraper.configuration.searchrequest.article;

import courtandrey.SUDRFScraper.configuration.searchrequest.Field;

public interface Article {
    boolean hasNoPart();
    Field getField();
    String getMainPart();
}
