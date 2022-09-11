package SUDRFScrapper.configuration.searchrequest.article;

import SUDRFScrapper.configuration.searchrequest.Field;

public class AdminArticle implements Article {
    private int chapter;
    private int article;
    private int subArticle;
    private boolean isSubArticlePresent = false;
    private int part;
    private int subPart;
    private boolean hasNoPart = false;


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

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public int getArticle() {
        return article;
    }

    public void setArticle(int article) {
        this.article = article;
    }

    public int getSubArticle() {
        return subArticle;
    }

    public void setSubArticle(int subArticle) {
        this.subArticle = subArticle;
    }

    public boolean isSubArticlePresent() {
        return isSubArticlePresent;
    }

    public void setSubArticlePresent(boolean subArticlePresent) {
        isSubArticlePresent = subArticlePresent;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public AdminArticle(int chapter, int article) {
        this.chapter = chapter;
        this.article = article;
    }

    public AdminArticle(int chapter, int article, int subArticle) {
        this.chapter = chapter;
        this.article = article;
        this.subArticle = subArticle;
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

    public AdminArticle(int chapter, int article, int subArticle, int part) {
        this.chapter = chapter;
        this.article = article;
        this.subArticle = subArticle;
        this.part = part;
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
