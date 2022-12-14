package courtandrey.SUDRFScraper.configuration.searchrequest;

import courtandrey.SUDRFScraper.configuration.searchrequest.article.AdminArticle;
import courtandrey.SUDRFScraper.configuration.searchrequest.article.Article;
import courtandrey.SUDRFScraper.configuration.searchrequest.article.CASArticle;
import courtandrey.SUDRFScraper.configuration.searchrequest.article.CriminalArticle;
import courtandrey.SUDRFScraper.exception.SearchRequestException;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class SearchRequest {
    private String resultDateFrom;
    private String resultDateTill;
    private String text;
    private Article article;
    private String publishedDateTill;
    private String entryDateTill;

    public String getEntryDateTill() {
        return entryDateTill;
    }

    public void setEntryDateTill(LocalDate entryDateTill) {
        this.entryDateTill = getDateToString(entryDateTill);
    }

    private courtandrey.SUDRFScraper.configuration.searchrequest.Field field = courtandrey.SUDRFScraper.configuration.searchrequest.Field.CRIMINAL;

    private static SearchRequest instance;

    public synchronized String getPublishedDateTill() {
        return publishedDateTill;
    }

    public void setPublishedDateTill(LocalDate publishedDateTill) {
        this.publishedDateTill = getDateToString(publishedDateTill);
    }

    public void setStringPublishedDateTill(String dateTill) {
        this.publishedDateTill = dateTill;
    }

    public synchronized String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public synchronized Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        if (article instanceof CriminalArticle) setField(courtandrey.SUDRFScraper.configuration.searchrequest.Field.CRIMINAL);
        if (article instanceof AdminArticle) setField(courtandrey.SUDRFScraper.configuration.searchrequest.Field.ADMIN);
        if (article instanceof CASArticle) setField(courtandrey.SUDRFScraper.configuration.searchrequest.Field.CAS);
        this.article = article;
    }

    /**
     * @param date first day of period when the case ended.
     */
    public void setResultDateFrom(LocalDate date) {
        this.resultDateFrom = getDateToString(date);
    }

    public void setField(courtandrey.SUDRFScraper.configuration.searchrequest.Field field) {
        if (article != null && article.getField() != field) throw new SearchRequestException("Article does not match field");
        this.field=field;
    }

    public courtandrey.SUDRFScraper.configuration.searchrequest.Field getField() {
        return field;
    }

    private String getDateToString(LocalDate date) {
        int dom = date.getDayOfMonth();
        int m = date.getMonthValue();
        String day = dom>=10 ? String.valueOf(dom) : ("0"+dom);
        String month = m>=10 ? String.valueOf(m) : ("0"+m);
        return day+"."+month+"."+date.getYear();
    }

    /**
     * @param date last day of period when the case ended.
     */
    public void setResultDateTill(LocalDate date) {
        this.resultDateTill = getDateToString(date);
    }

    private SearchRequest() {}

    public static SearchRequest getInstance() {
        if (instance==null) instance = new SearchRequest();
        return instance;
    }

    public boolean checkFields() {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field f:fields) {
            try {
                if (!f.getName().equals("instance") && f.get(this) != null
                        && !f.getName().equals("field")) return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        if (article != null) {
            builder.append("article = ").append(article).append(";");
        } else {
            builder.append("field = ").append(field).append(";");
        }
        if (resultDateFrom != null) {
            builder.append("resultDateFrom = ").append(resultDateFrom).append(";");
        }
        if (resultDateTill != null) {
            builder.append("resultDateTill = ").append(resultDateTill).append(";");
        }
        if (text != null) {
            builder.append("text = ").append(text).append(";");
        }
        builder.append("}");
        return builder.toString();
    }

    public synchronized String getResultDateFrom() {
        return resultDateFrom;
    }

    public synchronized String getResultDateTill() {
        return resultDateTill;
    }

}
