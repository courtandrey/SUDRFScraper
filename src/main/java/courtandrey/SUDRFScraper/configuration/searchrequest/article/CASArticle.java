package courtandrey.SUDRFScraper.configuration.searchrequest.article;

import courtandrey.SUDRFScraper.configuration.searchrequest.Field;
import lombok.Getter;

@Getter
@SuppressWarnings("all")
public class CASArticle implements Article{
    private final String partOfCas;

    public CASArticle(String partOfCas) {
        this.partOfCas = partOfCas;
    }

    @Override
    public boolean hasNoPart() {
        return true;
    }

    @Override
    public Field getField() {
        return Field.CAS;
    }

    @Override
    public String getMainPart() {
        return partOfCas;
    }

    @Override
    public String toString() {
        return "Административное статья, включающая в себя " + partOfCas;
    }
}
