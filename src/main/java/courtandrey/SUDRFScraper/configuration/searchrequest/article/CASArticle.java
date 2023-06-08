package courtandrey.SUDRFScraper.configuration.searchrequest.article;

import courtandrey.SUDRFScraper.configuration.searchrequest.Field;
import lombok.Getter;

@Getter
@SuppressWarnings("all")
public class CASArticle implements Article{
    private final String partOfCas;
    private final String mosgorsudCode;

    public String getMosgorsudCode() {
        return mosgorsudCode;
    }

    public CASArticle(String partOfCas, String mosgorsudCode) {
        this.partOfCas = partOfCas;
        this.mosgorsudCode = mosgorsudCode;
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
