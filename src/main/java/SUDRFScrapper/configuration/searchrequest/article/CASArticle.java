package SUDRFScrapper.configuration.searchrequest.article;

import SUDRFScrapper.configuration.searchrequest.Field;

public class CASArticle implements Article{
    private final String partOfCas;

    public CASArticle(String partOfCas) {
        this.partOfCas = partOfCas;
    }

    public String getPartOfCas() {
        return partOfCas;
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
