package SUDRFScrapper.configuration.searchrequest.article;

import SUDRFScrapper.configuration.searchrequest.Field;

public interface Article {
    boolean hasNoPart();
    Field getField();
    String getMainPart();
}
