package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.configuration.ApplicationConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.SearchPattern;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.StrategyName;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.service.CasesPipeLineFactory;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.SeleniumHelper;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;
import courtandrey.SUDRFScraper.service.ThreadHelper;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Set;

import static courtandrey.SUDRFScraper.service.Constant.UA;

public abstract class ConnectionSUDRFStrategy extends SUDRFStrategy {
    boolean isInTestingMode = false;

    protected Parser parser;

    public ConnectionSUDRFStrategy(CourtConfiguration cc) {
        super(cc);
        if (cc.getStrategyName() != StrategyName.MOSGORSUD_STRATEGY) {
            parser = new GeneralParser(cc);
        } else {
            parser = new MosGorSudParser(cc);
        }
        if (ApplicationConfiguration.getInstance().getProperty("dev.test") != null
                && ApplicationConfiguration.getInstance().getProperty("dev.test").equals("true")) {
            isInTestingMode = true;
        }
    }

    @Override
    public void run() {
       do {
           String message = String.format(Message.EXECUTION_STATUS_BEGINNING.toString(),cc.getName(),urls[indexUrl]);
           doCircle();
           SimpleLogger.log(LoggingLevel.INFO,message + " " + String.format(Message.EXECUTION_STATUS_MID.toString(),issue));
       } while (!timeToStopRotatingSrv);
    }
    private void doCircle() {
        if (isInTestingMode && finalIssue == Issue.SUCCESS) {
            timeToStopRotatingSrv = true;
            return;
        }
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
        unravel = unravel - 5;
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
            int waitTime = 14 + (10-unravel);
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
                    .userAgent(UA.toString())
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
        if (issue == Issue.SUCCESS)
            resultCases = filterCases();
        if (resultCases.size() > 150_000) {
            parser.scrapTextsAndFlush(resultCases, CasesPipeLineFactory.getInstance().getPipeLine());
            resultCases.clear();
        }
        resultCases = parser.scrapTexts(resultCases);
        super.finish();
    }

    @Override
    protected void logFinalInfo() {
        if (!parser.isTextFound() && resultCases.size() != 0 && resultCases.size() >= 25) {
            SimpleLogger.log(LoggingLevel.DEBUG, Message.NO_TEXT_FOUND + urls[indexUrl]);
        }
        if ((resultCases.size() % 25 == 0 || resultCases.size() % 20 == 0) && resultCases.size() != 0 && !isInTestingMode) {
            SimpleLogger.log(LoggingLevel.DEBUG, Message.SUSPICIOUS_NUMBER_OF_CASES + urls[indexUrl]);
        }
        super.logFinalInfo();
    }

    private boolean checkPreventable() {
        return unravel <= 0;
    }

    private void getCases() {
        if (cc.getVnkod() == null) setVnkod(currentDocument);
        Set<Case> cases = parser.scrap(currentDocument,cc.getSearchString());

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