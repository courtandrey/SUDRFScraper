package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.service.Converter;
import courtandrey.SUDRFScraper.service.Downloader;
import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MosGorSudParser extends ConnectorParser{
    private final Set<Case> cases = new HashSet<>();
    protected boolean isTextFound = false;
    private final Downloader downloader = new Downloader();
    private final Converter converter = new Converter();
    MosGorSudParser(CourtConfiguration cc) {
        super(cc);
    }
    @Override
    public boolean isTextFound() {
        return isTextFound;
    }

    @Override
    public Set<Case> scrap(Document document, String currentUrl) {
        Elements table = document.getElementsByClass("custom_table");
        if (table.size() == 0) return cases;
        for (Element element:table.get(0).getElementsByTag("tr")) {
            try{
                if (element.getElementsByTag("th").size() !=0) continue;
                Elements rowParts = element.getElementsByTag("td");
                Case _case = new Case();
                _case.setCaseNumber(rowParts.get(0).getElementsByTag("nobr").get(0).text());
                _case.setName(cc.getName());
                _case.setNames(rowParts.get(1).text()+"&"+rowParts.get(4).text()+"&"+rowParts.get(5).text());
                _case.setDecision(rowParts.get(2).text());
                _case.setJudge(rowParts.get(3).text());
                _case.setRegion(cc.getRegion());
                _case.setText(currentUrl+rowParts.get(0).getElementsByTag("a").attr("href"));
                cases.add(_case);
            } catch (Exception e) {
                SimpleLogger.log(LoggingLevel.DEBUG, Message.MOSGORSUD_PARSING_EXCEPTION + e.getLocalizedMessage());
            }
        }
        return cases;
    }

    @Override
    public Set<Case> scrapTexts(Set<Case> resultCases) {
        if (cases.size() == 0) return resultCases;
        SimpleLogger.log(LoggingLevel.INFO, String.format(Message.COLLECTING_TEXTS.toString(),cases.size(),cc.getName()));
        int i = 1;
        Set<Case> newCases = new HashSet<>();
        for (Case _case:cases) {
            String url = _case.getText();
            if (url != null) {
                _case.setText(null);
                try{
                    String text = getJsoupText(url);
                    if (text != null) {
                        isTextFound = true;
                        String[] splits = text.split("\\$DELIMITER");
                        if (splits.length == 1) {
                            if (!text.equals("MALFORMED"))
                                _case.setText(text);
                            else
                                _case.setText(null);
                        }
                        else {
                            _case.setText(splits[0]);
                            for (int j = 1; j < splits.length; j++) {
                                Case newCase = new Case();
                                newCase.setCaseNumber(_case.getCaseNumber()+" ("+j+")");
                                newCase.setNames(_case.getNames());
                                newCase.setJudge(_case.getJudge());
                                newCase.setRegion(_case.getRegion());
                                newCase.setName(_case.getName());
                                newCase.setDecision(_case.getDecision());
                                newCase.setText(splits[j]);
                                newCases.add(newCase);
                            }

                            _case.setCaseNumber(_case.getCaseNumber() + " ("+0+")");
                        }
                    }
                }catch (Exception e) {
                    SimpleLogger.log(LoggingLevel.DEBUG, Message.DOCUMENT_NOT_PARSED + url);
                }
            }
            if (i % 25 == 0) {
                SimpleLogger.log(LoggingLevel.INFO, String.format(Message.COLLECTED_TEXTS.toString(), i, cases.size(), cc.getName()));
            }
            i += 1;
        }
        cases.addAll(newCases);
        return cases;
    }

    @Override
    public String parseText(Document decision) {
        if (decision.getElementsByAttributeValue("id","tabs-3").size() == 0) return null;
        Element table = decision.getElementsByAttributeValue("id", "tabs-3").get(0);
        StringBuilder stringBuilder = new StringBuilder();
        for (Element e : table.getElementsByTag("tr")) {
            if (e.getElementsByTag("th").size() != 0) continue;
            Element textElement = e.getElementsByTag("td").get(2);
            if (textElement.getElementsByTag("a").attr("href").equals("")) continue;
            String url = cc.getSearchString() + textElement.getElementsByTag("a").attr("href");

            if (url.equalsIgnoreCase("https://www.mos-gorsud.ru#")) {
                if (stringBuilder.length() == 0) stringBuilder.append("MALFORMED");
                continue;
            }
            String text = converter.getTxtFromFile(downloader.download(url));
            if (text == null) continue;
            text = cleanUp(text);
            if (stringBuilder.length() > 0) {
                if (stringBuilder.toString().equals("MALFORMED")) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(text);
                } else {
                    stringBuilder.append("$DELIMITER").append(text);
                }
            } else {
                stringBuilder.append(text);
            }
        }
        if (stringBuilder.length() == 0) return null;
        return stringBuilder.toString();
    }


}
