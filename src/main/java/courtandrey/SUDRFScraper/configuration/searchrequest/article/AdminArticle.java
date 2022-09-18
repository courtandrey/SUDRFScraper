package courtandrey.SUDRFScraper.configuration.searchrequest.article;

import courtandrey.SUDRFScraper.configuration.searchrequest.Field;
import lombok.Getter;

@Getter
public class AdminArticle implements Article {
    private final int chapter;
    private final int article;
    private int subArticle;
    private boolean isSubArticlePresent = false;
    private int part;
    private int subPart;
    private boolean hasNoPart = false;

    @SuppressWarnings("unused")
    public void setHasNoPart(boolean hasNoPart) {
        this.hasNoPart = hasNoPart;
    }

    public AdminArticle(int chapter, int article, int subArticle, int part, int subPart) {
        this.chapter = chapter;
        this.article = article;
        this.subArticle = subArticle;
        this.part = part;
        this.subPart = subPart;
    }

    public AdminArticle(int chapter, int article, int partOrSubArticle, int partOrSubPart , boolean isSubArticlePresent) {
        this.chapter = chapter;
        this.article = article;
        this.isSubArticlePresent = isSubArticlePresent;
        if (isSubArticlePresent) {
            this.subArticle = partOrSubArticle;
            this.part = partOrSubPart;
        } else {
            this.part = partOrSubArticle;
            this.subPart = partOrSubPart;
        }
    }
    @Override
    public String getMainPart() {
        String returnString = chapter+"." + article;
        if (subArticle != 0 ) returnString += "." + subArticle;
        return returnString;
    }

    public AdminArticle(int chapter, int article) {
        this.chapter = chapter;
        this.article = article;
    }

    public AdminArticle(int chapter, int article, int partOrSubArticle, boolean isSubArticlePresent) {
        this.chapter = chapter;
        this.article = article;
        this.isSubArticlePresent = isSubArticlePresent;
        if (isSubArticlePresent) {
            this.subArticle = partOrSubArticle;
        }
        else {
            this.part = partOrSubArticle;
        }
    }
    @Override
    public boolean hasNoPart() {
        return hasNoPart;
    }

    @Override
    public Field getField() {
        return Field.ADMIN;
    }

    @Override
    public String toString() {
        String returnString = "Статья КоАП " + chapter+"." + article;
        if (subArticle != 0 ) returnString += "." + subArticle;
        if (part != 0) returnString += " ч." + part;
        if (subPart != 0) returnString += "." + subPart;
        return returnString;
    }

}
