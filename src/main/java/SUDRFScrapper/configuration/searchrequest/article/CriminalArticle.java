package SUDRFScrapper.configuration.searchrequest.article;

import SUDRFScrapper.configuration.searchrequest.Field;

public class CriminalArticle implements Article{
    private int article;
    private int part = 0;
    private char letter = 0;
    private int subArticle = 0;
    private boolean hasNoPart = false;

    public int getSubArticle() {
        return subArticle;
    }

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

    public int getArticle() {
        return article;
    }

    public void setArticle(int article) {
        this.article = article;
    }

    public int getPart() {
        return part;
    }

    public char getLetter() {
        return letter;
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
