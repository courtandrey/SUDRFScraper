package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.SearchPattern;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.service.Constants;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.SeleniumHelper;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;
import courtandrey.SUDRFScraper.service.ThreadHelper;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class ConnectionSUDRFStrategy extends SUDRFStrategy {

    protected Scrapper scrapper;

    public ConnectionSUDRFStrategy(CourtConfiguration cc) {
        super(cc);
        scrapper = new Scrapper(cc);
    }

    @Override
    public void run() {
       do {
           doCircle();
       } while (!timeToStopRotatingSrv);
    }
    private void doCircle() {
        connect();

        if (checkPreventable()) {
            timeToStopRotatingSrv = true;
            return;
        }

        String text = currentDocument.text();

        prevSize = resultCases.size();

        checkText(text);
        if (issue == Issue.SUCCESS) {
            getCases();
            if (checkConditions()) return;
            rotate();
        }

        else if (Issue.isPreventable(issue)) {
            if (checkPreventable()) {
                timeToStopRotatingSrv = true;
                return;
            }
            refresh();
        }

        else if (issue == Issue.NOT_FOUND_CASE) {
            if (checkConditions()) return;
            rotate();
        }

        else {
            timeToStopRotatingSrv = true;
        }
    }

    private void refresh() {
        if (cc.getConnection() == Connection.SELENIUM) {
            SeleniumHelper.getInstance().refresh();
        }
    }

    static class Scrapper {
        private final Set<Case> cases = new HashSet<>();
        protected boolean isTextFound = false;
        private final CourtConfiguration cc;

        private Scrapper(CourtConfiguration cc) {
            this.cc = cc;
        }

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

        protected void scrapTexts(Collection<Case> cases) {
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
            }
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

                if (text == null) {
                    SimpleLogger.log(LoggingLevel.ERROR, Message.DOCUMENT_NOT_PARSED + href);
                }

                return text;
            }
            return null;
        }

        private String getSeleniumText(String href) {
            Document doc = Jsoup.parse(SeleniumHelper.getInstance().getPage(href,setWaitTime()));
            return parseText(doc);
        }

        private int setWaitTime() {
            if (cc.getSearchPattern() == SearchPattern.DEPRECATED_SECONDARY_PATTERN ||
                    cc.getSearchPattern() == SearchPattern.SECONDARY_PATTERN) {
                return 3;
            } else {
                return 10;
            }
        }

        private String parseText(Document doc) {
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
                    if (content == null) {
                        return checkMalformed(doc);
                    }
                }

                for (Element el:content.getElementsByTag("p")) {
                    text.append(el.text());
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
            Element el = document.getElementsByClass("heading heading_caps heading_title").get(0);
            return el.text();
        }

        private String getJsoupText(String url) throws IOException {
            Document decision = Jsoup.connect(url)
                    .userAgent(Constants.UA)
                    .timeout(1000 * 60 * 2)
                    .get();
            return parseText(decision);
        }

    }

    protected void connect() {
        if (cc.getConnection() == Connection.REQUEST) {
            connectJsoup();
        } else if (cc.getConnection() == Connection.SELENIUM) {
            connectSelenium();
        }
    }

    private void connectSelenium() {
        try {
            int waitTime = 14;
            if (page_num > 1 && (cc.getSearchPattern() == SearchPattern.SECONDARY_PATTERN ||
                    cc.getSearchPattern() == SearchPattern.DEPRECATED_SECONDARY_PATTERN)) {
                waitTime = 5;
            }
            SeleniumHelper sh = SeleniumHelper.getInstance();
            String page = sh.getPage(urls[indexUrl], waitTime);

            currentDocument = Jsoup.parse(page);
        }
        catch (WebDriverException e) {
            if (unravel > 0) {
                ThreadHelper.sleep(5);
                unravel = unravel - 2;
                connectSelenium();
            }
            else {
                if (e instanceof TimeoutException) {
                    finalIssue = Issue.compareAndSetIssue(Issue.CONNECTION_ERROR, finalIssue);
                    issue = Issue.CONNECTION_ERROR;
                } else {
                    finalIssue = Issue.compareAndSetIssue(Issue.URL_ERROR, finalIssue);
                    issue = Issue.URL_ERROR;
                }
            }
        }
    }

    private void connectJsoup() {
        try {
            currentDocument = Jsoup
                    .connect(urls[indexUrl])
                    .userAgent(Constants.UA)
                    .timeout(1000 * 60 * 2)
                    .get();
        } catch (SocketException | HttpStatusException | UnknownHostException e) {
            if (unravel > 0) {
                ThreadHelper.sleep(5);
                --unravel;
                connectJsoup();
            } else {
                finalIssue = Issue.compareAndSetIssue(Issue.CONNECTION_ERROR, finalIssue);
                issue = Issue.CONNECTION_ERROR;
            }
        } catch (SocketTimeoutException e) {
            if (unravel>0) {
                ThreadHelper.sleep(5);
                unravel = unravel - 2;
                connectJsoup();
            } else {
                finalIssue = Issue.compareAndSetIssue(Issue.URL_ERROR, finalIssue);
                issue = Issue.URL_ERROR;
            }
        } catch (IOException e) {
            unravel = 0;
            finalIssue = Issue.compareAndSetIssue(Issue.CONNECTION_ERROR, finalIssue);
            issue = Issue.CONNECTION_ERROR;
        }
    }

    @Override
    protected void finish() {
        scrapper.scrapTexts(resultCases);
        super.finish();
    }

    @Override
    protected void logFinalInfo() {
        if (!scrapper.isTextFound && resultCases.size() != 0 && resultCases.size() >= 25) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.NO_TEXT_FOUND + urls[indexUrl]);
        }
        if (resultCases.size() % 25 == 0 && resultCases.size() != 0) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.SUSPICIOUS_NUMBER_OF_CASES + urls[indexUrl]);
        }
        super.logFinalInfo();
    }

    private boolean checkPreventable() {
        return unravel <= 0;
    }

    private void getCases() {
        if (cc.getVnkod() == null) setVnkod(currentDocument);

        Set<Case> cases = scrapper.scrap(currentDocument,urls[indexUrl].split("/modules")[0]);

        if (cases != null) {
            resultCases.addAll(cases);
        }

        if (resultCases != null && resultCases.size() > 0) {
            finalIssue = Issue.compareAndSetIssue(Issue.SUCCESS,finalIssue);
            issue = Issue.SUCCESS;
        } else {
            finalIssue = Issue.compareAndSetIssue(Issue.NOT_FOUND_CASE,finalIssue);
            issue = Issue.NOT_FOUND_CASE;
        }
    }

}