package courtandrey.SUDRFScraper.configuration.searchrequest.article;

import courtandrey.SUDRFScraper.configuration.searchrequest.Field;
import lombok.Getter;

@Getter
public class CriminalArticle implements Article{
    private final int article;
    private int part = 0;
    private char letter = 0;
    private int subArticle = 0;
    private boolean hasNoPart = false;

    @SuppressWarnings("unused")
    public void setHasNoPart(boolean hasNoPart) {
        this.hasNoPart = hasNoPart;
    }

    public CriminalArticle(int article, int partOrSubArticle, boolean isSubArticlePresent) {
        this.article = article;
        if (isSubArticlePresent) {
            subArticle = partOrSubArticle;
        } else {
            part = partOrSubArticle;
        }
    }

    public CriminalArticle(int article, int subArticle, int part) {
        this.article = article;
        this.part = part;
        this.subArticle = subArticle;
    }

    public CriminalArticle(int article, int subArticle, int part, char letter) {
        this.article = article;
        this.part = part;
        this.letter = letter;
        this.subArticle = subArticle;
    }

    public CriminalArticle(int article, int part, char letter) {
        this.article = article;
        this.part = part;
        this.letter = letter;
    }

    public CriminalArticle(int article) {
        this.article = article;
    }

    public CriminalArticle(int article, int part) {
        this.article = article;
        this.part = part;
    }

    @Override
    public String getMainPart() {
        return subArticle == 0 ? String.valueOf(article) : article + "." + subArticle;
    }

    @Override
    public boolean hasNoPart() {
        return hasNoPart;
    }

    @Override
    public Field getField() {
        return Field.CRIMINAL;
    }

    @Override
    public String toString() {
       String returnString = "Уголовная Статья " + article;
       if (subArticle > 0) returnString += "." + subArticle;
       if (part > 0) returnString += " ч." + part;
       if (letter != 0) returnString += " п." + letter;
       return returnString;
    }
}
