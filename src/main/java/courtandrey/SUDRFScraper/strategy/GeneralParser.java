package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.SearchPattern;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.service.ThreadHelper;
import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class GeneralParser extends ConnectorParser{
    private final Set<Case> cases = new HashSet<>();
    protected boolean isTextFound = false;

    GeneralParser(CourtConfiguration cc) {
        super(cc);
    }

    @Override
    public boolean isTextFound() {
        return isTextFound;
    }

    @Override
    public Set<Case> scrap(Document document, String currentUrl) {
        Element table = document.getElementById("tablcont");
        if (table == null) {
            table = document.getElementById("resultTable");
        }
        if (table == null) {
            Elements elements = document.getElementsByClass("bgs-result relative sudrf-dt");
            if (elements.size() != 0) table = elements.get(0);
        }
        if (table == null) {
            table = document.getElementById("search_results");
        }
        if (table == null) {
            return null;
        }

        Elements rows = table.getElementsByTag("tr");

        rows.remove(0);

        for (Element row : rows) {
            Elements caseParams = row.getElementsByTag("td");

            Case _case = new Case();

            _case.setRegion(cc.getRegion());

            if (cc.getName() == null) {
                String name = getName(document);
                _case.setName(name);
                cc.setName(name);
            } else {
                _case.setName(cc.getName());
            }

            if (!caseParams.get(0).text().replace(" ", "").equals("")) {
                _case.setCaseNumber(caseParams.get(0).text());
            }
            if (!caseParams.get(1).text().replace(" ", "").equals("")) {
                _case.setEntryDate(caseParams.get(1).text());
            }
            if (!caseParams.get(2).text().replace(" ", "").equals("")) {
                _case.setNames(caseParams.get(2).text());
            }
            if (!caseParams.get(3).text().replace(" ", "").equals("")) {
                _case.setJudge(caseParams.get(3).text());
            }
            if (!caseParams.get(4).text().replace(" ", "").equals("")) {
                _case.setResultDate(caseParams.get(4).text());
            }
            if (!caseParams.get(5).text().replace(" ", "").equals("")) {
                _case.setDecision(caseParams.get(5).text());
            }

            int textParam = 7;

            if (caseParams.size() > 7) {
                if (!caseParams.get(6).text().replace(" ", "").equals("")) {
                    _case.setEndDate(caseParams.get(6).text());
                }
            } else {
                textParam = 6;
            }

            Elements els = caseParams.get(textParam).getElementsByTag("a");
            String href = "";
            if (els.size() != 0)
                href = els.get(0).attr("href");
            if (!href.equals("")) {
                if (href.charAt(0) == '/') href = currentUrl + href;
                _case.setText(href);
            }
            cases.add(_case);
        }
        return cases;
    }
    @Override
    public Set<Case> scrapTexts(Set<Case> cases) {
        if (cases.size() == 0) return cases;
        SimpleLogger.log(LoggingLevel.INFO, String.format(Message.COLLECTING_TEXTS.toString(),cases.size(),cc.getName()));
        int i = 1;
        for (Case _case:cases) {
            String url = _case.getText();
            if (url != null) {
                _case.setText(null);
                String text = getText(url);
                if (text != null && !text.equals("Malformed case")) {
                    isTextFound = true;
                    _case.setText(text);
                } else if (text == null) {
                    SimpleLogger.log(LoggingLevel.DEBUG, Message.DOCUMENT_NOT_PARSED + url);
                }
            }
            if (i % 25 == 0) {
                SimpleLogger.log(LoggingLevel.INFO,String.format(Message.COLLECTED_TEXTS.toString(),i,cases.size(),cc.getName()));
            }
            i+= 1;
        }
        return cases;
    }

    private String getText(String href) {
        if (cc.getConnection() == Connection.REQUEST) {
            String text = null;
            for (int i = 0; i < 10; i++) {
                try {
                    text = getJsoupText(href);
                    break;
                } catch (HttpStatusException | SocketException | UnknownHostException | SocketTimeoutException e) {
                    ThreadHelper.sleep(5);
                }
                catch (IOException e) {
                    SimpleLogger.log(LoggingLevel.ERROR,
                            String.format(Message.EXCEPTION_OCCURRED_WHILE_PARSING.toString(),e) + href);
                    break;
                }
            }
            return text;
        }
        else if (cc.getConnection() == Connection.SELENIUM) {
            String text = null;
            for (int i = 0; i < 10; i++) {
                try {
                    text = getSeleniumText(href);
                    break;
                } catch (WebDriverException e) {
                    ThreadHelper.sleep(10);
                }
            }

            return text;
        }
        return null;
    }
    @Override
    public String parseText(Document doc) {
        if (cc.getSearchPattern() == SearchPattern.SECONDARY_PATTERN ||
                cc.getSearchPattern() == SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            Elements text = doc.getElementsByClass("doc-content marginTop10");
            if (text.size() == 0) {
                return checkMalformed(doc);
            }
            return text.get(0).text();
        }
        else {
            StringBuilder text = new StringBuilder();
            Element content = doc.getElementById("content");

            if (content == null) {
                content = doc.getElementById("tab_content_Document1");
            }
            if (content == null) {
                content = doc.getElementById("tab_content_Docs");
            }
            if (content == null) {
                return checkMalformed(doc);
            }
            for (Element el:content.getElementsByTag("p")) {
                text.append(el.text());
                text.append("\n");
            }
            return text.toString();
        }
    }



    private String checkMalformed(Document doc) {
        Elements malformedElements = doc.getElementsByClass("grayColor empty-field one-value");
        if (malformedElements.size()>0 && malformedElements.get(0).text()
                .contains("Не заполнено")) return "Malformed case";
        Element malformedElement = doc.getElementById("search_results");
        if (malformedElement!=null &&
                malformedElement.text().contains("Warning: pg_query()")) return "Malformed case";
        return null;
    }

    private String getName(Document document) {
        if (cc.getName() != null) return cc.getName();
        Element el = document.getElementsByClass("heading heading_caps heading_title").get(0);
        return el.text();
    }
}
