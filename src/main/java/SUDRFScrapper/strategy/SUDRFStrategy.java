package SUDRFScrapper.strategy;

import SUDRFScrapper.configuration.courtconfiguration.CourtConfiguration;
import SUDRFScrapper.configuration.courtconfiguration.Issue;
import SUDRFScrapper.configuration.courtconfiguration.SearchPattern;
import SUDRFScrapper.configuration.searchrequest.SearchRequest;
import SUDRFScrapper.dump.model.Case;
import SUDRFScrapper.service.*;
import SUDRFScrapper.service.logger.LoggingLevel;
import SUDRFScrapper.service.logger.Message;
import SUDRFScrapper.service.logger.SimpleLogger;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;

public abstract class SUDRFStrategy extends Thread{
    protected int srv_num = 1;
    protected int page_num = 1;
    protected Document currentDocument = null;
    protected int unravel = 10;
    protected boolean timeToStopRotatingSrv = false;
    protected boolean timeToStopRotatingBuild = false;
    protected boolean timeToStopRotatingPage = false;

    final CourtConfiguration cc;
    protected URLCreator urlCreator;
    protected Issue issue = null;
    protected Issue finalIssue = null;
    protected SearchRequest request = SearchRequest.getInstance();

    protected int indexUrl = 0;
    protected int build = 1;
    protected int prevSize = -1;
    private int prevSrvSize = -1;
    private int prevBuildSize = -1;

    protected Set<Case> resultCases = new HashSet<>();

    public Set<Case> getResultCases() {
        return resultCases;
    }

    protected String[] urls;

    public SUDRFStrategy(CourtConfiguration cc) {
        this.cc = cc;
        
        this.setName(cc.getStrategyName().name());

        setPage_num();

        if (cc.getSearchPattern() != SearchPattern.VNKOD_PATTERN) timeToStopRotatingBuild = true;
    }

    private void setPage_num() {
        if (cc.getSearchPattern() == SearchPattern.SECONDARY_PATTERN ||
            cc.getSearchPattern() == SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            page_num = 0;
        }
        else {
            page_num = 1;
        }
    }

    private void resetPage() {
        if (cc.getSearchPattern()!=SearchPattern.SECONDARY_PATTERN
                && cc.getSearchPattern()!=SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            urls[indexUrl] = urls[indexUrl].replace("page="+page_num,"page="+1);
        }
        else {
            urls[indexUrl] = urls[indexUrl].replace("\"start\":"+page_num,"\"start\":"+0);
        }
        setPage_num();
    }

    protected void rotate() {
        if (timeToStopRotatingPage) {
            resetPage();
            timeToStopRotatingPage = false;
            if (timeToStopRotatingBuild && (build > 1)) {
                urls[indexUrl] = urls[indexUrl].replace("num_build=" + build, "num_build=" + 1);
                build = 1;
                timeToStopRotatingBuild = false;
                prevSrvSize = resultCases.size();
                rotateSrv();
            } else if (timeToStopRotatingBuild) {
                prevSrvSize = resultCases.size();
                rotateSrv();
            } else {
                prevBuildSize = resultCases.size();
                rotateBuild();
            }
        }
        else {
            rotatePage();
        }
    }

    private void rotatePage() {
        if (cc.getSearchPattern()!=SearchPattern.SECONDARY_PATTERN
                && cc.getSearchPattern()!=SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            urls[indexUrl] = urls[indexUrl].replace("page="+page_num,"page="+(page_num+1));
            ++page_num;
        }
        else {
            urls[indexUrl] = urls[indexUrl].replace("\"start\":"+page_num,"\"start\":"+(page_num+10));
            page_num = page_num + 10;
        }
    }

    protected void clear() {
        timeToStopRotatingSrv = false;
        srv_num = 1;
        unravel = 10;
        if (cc.getSearchPattern() != SearchPattern.VNKOD_PATTERN) timeToStopRotatingBuild = true;
        else {
            timeToStopRotatingBuild = false;
            build = 1;
        }
        prevSize = -1;
        prevSrvSize = -1;
        setPage_num();
        currentDocument = null;
    }

    protected void rotateBuild() {
        if (cc.getSearchPattern()==SearchPattern.VNKOD_PATTERN) {
            urls[indexUrl]=urls[indexUrl].replace("num_build="+build,"num_build="+(build+1));
            ++build;
        }
    }

    protected void rotateSrv() {
        if (cc.getSearchPattern() != SearchPattern.SECONDARY_PATTERN
                && cc.getSearchPattern()!=SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            urls[indexUrl]=urls[indexUrl].replace("srv_num="+srv_num,"srv_num="+(srv_num+1));
            ++srv_num;
        } else {
            timeToStopRotatingSrv = true;
        }
    }
    protected void setVnkod(Document document) {
        if (cc.getSearchPattern()!=SearchPattern.SECONDARY_PATTERN &&
            cc.getSearchPattern()!=SearchPattern.DEPRECATED_SECONDARY_PATTERN) {
            ConfigurationHelper.setVnkodForNonSecondaryPatterns(cc,document);
        }
        else {
            setVnkodForSecondaryPattern();
        }
        if (cc.getVnkod() == null) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.VNKOD_NOT_FOUND+cc.getSearchString());
        }
    }

    private void setVnkodForSecondaryPattern() {
        try {
            ConfigurationHelper.findElementAndSetVnkod(cc);
        } catch (Exception ignored) {}
    }

    protected boolean checkConditions() {
        if (prevSize == resultCases.size()) {
            if (!timeToStopRotatingPage) {
                timeToStopRotatingPage = true;
            }
            if (srv_num > 2 && prevSrvSize == resultCases.size()) {
                timeToStopRotatingSrv = true;
                return true;
            } else {
                if (build > 2 && prevBuildSize == resultCases.size()) {
                    timeToStopRotatingBuild = true;
                    if (prevSrvSize == resultCases.size()) {
                        timeToStopRotatingSrv = true;
                        return true;
                    }
                }
                rotate();
            }
            return true;
        }
        return false;
    }

    protected void checkText(String text) {
        if (text.contains("код с картинки") || text.contains("Время жизни сессии закончилось")) {
            issue = Issue.CAPTCHA;
        }

        else if (text.contains("для получения полной информации по делу или материалу, нажмите на номер")) {
            finalIssue = Issue.compareAndSetIssue(Issue.URL_ERROR, finalIssue);
            issue = Issue.URL_ERROR;
        }

        else if (text.contains("Данных по запросу не обнаружено") || text.contains("Данных по запросу не найдено")) {
            finalIssue = Issue.compareAndSetIssue(Issue.NOT_FOUND_CASE, finalIssue);
            issue = Issue.NOT_FOUND_CASE;
        }

        else if (text.contains("№ дела") || text.contains("Дата поступления") || text.contains("Номер дела")
                || text.contains("Всего по запросу найдено")) {
            issue = Issue.SUCCESS;
        }

        else if (text.contains("Информация временно недоступна") || text.contains("Warning: pg_connect():")) {
            finalIssue = Issue.compareAndSetIssue(Issue.INACTIVE_COURT,finalIssue);
            issue = Issue.INACTIVE_COURT;
            unravel = unravel - 5;
        }

        else if (text.contains("Данный модуль неактивен")) {
            finalIssue = Issue.compareAndSetIssue(Issue.INACTIVE_MODULE, finalIssue);
            issue = Issue.INACTIVE_MODULE;
            unravel = unravel - 5;
        }

        else if (text.contains("503")
                && text.contains("В настоящее время сайт временно недоступен. Обратитесь к данной странице позже")) {
            --unravel;
            finalIssue = Issue.compareAndSetIssue(Issue.CONNECTION_ERROR,finalIssue);
            issue = Issue.CONNECTION_ERROR;
        }

        else if (text.contains("ERROR LEVEL 2")) {
            finalIssue = Issue.compareAndSetIssue(Issue.NOT_SUPPORTED_REQUEST,finalIssue);
            issue = Issue.NOT_SUPPORTED_REQUEST;
            unravel = unravel - 5;
        }

        else if (cc.getConnection() != Connection.SELENIUM){
            finalIssue = Issue.compareAndSetIssue(Issue.UNDEFINED_ISSUE,finalIssue);
            issue = Issue.UNDEFINED_ISSUE;
        }

        else {
            unravel = unravel - 5;
            finalIssue = Issue.compareAndSetIssue(Issue.CONNECTION_ERROR, finalIssue);
            issue = Issue.CONNECTION_ERROR;
        }
    }

    protected void createUrls() {
        indexUrl = 0;
        urlCreator = new URLCreator(cc);
        urls = urlCreator.createUrls();
    }

    private void filterCases() {
        String textToFind = request.getText();
        if (textToFind != null) {
            Set<Case> cases = new HashSet<>();
            for (Case _case:resultCases) {
                if (_case.getText() != null && _case.getText().contains(textToFind)) {
                    cases.add(_case);
                }
            }
            resultCases = cases;
            if (cases.size() == 0) {
                issue = Issue.NOT_FOUND_CASE;
                finalIssue = Issue.NOT_FOUND_CASE;
            }
        }

        if (request.getArticle() != null) {
            Set<Case> cases = new HashSet<>();
            for (Case _case:resultCases) {
                String mainPart = request.getArticle().getMainPart();
                if (_case.getNames() != null && _case.getNames().matches("(.*)"+mainPart+"[^\\d.](.*)")) {
                    cases.add(_case);
                }
                resultCases = cases;
                if (cases.size() == 0) {
                    issue = Issue.NOT_FOUND_CASE;
                    finalIssue = Issue.NOT_FOUND_CASE;
                }
            }
        }
    }

    protected void finish() {
        if (finalIssue == Issue.SUCCESS) {
            filterCases();
        }
        setFinalInfo();
        logFinalInfo();
    }

    protected void logFinalInfo() {
        if (cc.getIssue() != Issue.SUCCESS && cc.getIssue() != Issue.NOT_FOUND_CASE) {
            SimpleLogger.log(LoggingLevel.DEBUG,cc.getIssue() + " " + urls[indexUrl]);
        }

        try {
            SimpleLogger.addToCourtHistory(cc);
        } catch (IOException e) {
            SimpleLogger.log(LoggingLevel.ERROR, String.format(Message.IOEXCEPTION_OCCURRED.toString(), e));
        }
    }

     protected void checkIfWorkingUrlDoesntWork() {
        if ((finalIssue != Issue.SUCCESS && finalIssue != Issue.NOT_FOUND_CASE
                && finalIssue != Issue.NOT_SUPPORTED_REQUEST && finalIssue != Issue.CONNECTION_ERROR)
                && cc.getWorkingUrl().get(request.getField()) != null) {
            cc.putWorkingUrl(request.getField(),null);
        }
    }

    protected void setFinalInfo() {
        cc.setIssue(Objects.requireNonNullElseGet(finalIssue, () -> Objects.requireNonNullElse(issue, Issue.ERROR)));

        checkIfWorkingUrlDoesntWork();

        putWorkingUrl();
    }

    private void putWorkingUrl() {
        if (finalIssue == Issue.SUCCESS && cc.getWorkingUrl().get(request.getField()) == null) {
            cc.putWorkingUrl(request.getField(),urlCreator.returnEnding(indexUrl));
        }
    }
}
