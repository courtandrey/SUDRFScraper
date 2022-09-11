package SUDRFScrapper.configuration;

import SUDRFScrapper.configuration.courtconfiguration.CourtConfiguration;
import SUDRFScrapper.configuration.courtconfiguration.Level;
import SUDRFScrapper.configuration.courtconfiguration.SearchPattern;
import SUDRFScrapper.configuration.courtconfiguration.StrategyName;
import SUDRFScrapper.configuration.searchrequest.Field;
import SUDRFScrapper.service.*;
import SUDRFScrapper.service.logger.LoggingLevel;
import SUDRFScrapper.service.logger.Message;
import SUDRFScrapper.service.logger.SimpleLogger;
import SUDRFScrapper.strategy.Connection;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static SUDRFScrapper.service.Constants.*;

public class PrimaryConfigurator {

    private static int id=0;

    private static final List<CourtConfiguration> baseCCS = ConfigurationLoader.getCourtConfigurationsFromBase();

    public static void configurateCourts() throws IOException {
        try (BufferedReader configReader = Files.newBufferedReader(Path.of(PATH_TO_PRECONFIG));
             FileWriter writer=new FileWriter(PATH_TO_CONFIG)){

            List<CourtConfiguration> courtConfigurations = new ArrayList<>();

            while (configReader.ready()) {
                courtConfigurations.addAll(getCourtConfigurations(configReader.readLine()));
            }

            ObjectMapper mapper=new ObjectMapper();
            mapper.writeValue(writer,courtConfigurations);

            SimpleLogger.close();
        }
    }

    private static Set<CourtConfiguration> getCourtConfigurations(String configLine) {
        String[] splits = configLine.split(",");

        Set<CourtConfiguration> courtConfigurations=new HashSet<>();
        String sourceURL=splits[2].replace("$NAME",splits[0]);
        int regionCode=Integer.parseInt(splits[1]);

        courtConfigurations.add(getCourtConfiguration(sourceURL.replace(PRECONFIG_URL_ENDING,""),
                regionCode));

        HashSet<String> urls = getURLs(sourceURL);
        for (String url:urls) {
            courtConfigurations.add(getCourtConfiguration(url,regionCode));
        }

        SimpleLogger.println("Region "+regionCode+" finished");

        return courtConfigurations;
    }

    private static CourtConfiguration getCourtConfiguration(String url, int region) {
        CourtConfiguration cc = new CourtConfiguration();

        cc.setRegion(region);
        
        cc.setStrategyName(StrategyName.PRIMARY_STRATEGY);

        cc.setSearchString(changeURL(url));

        cc.setId(id);
        HashMap<Field,String> hm = new HashMap<>();
        for (Field f:Field.values()) {
            hm.put(f,"null");
        }
        cc.setWorkingUrl(hm);

        switch (region) {
            case 63,73,28,69,36 -> {
                if (!cc.getSearchString().equals("http://oblsud--vrn.sudrf.ru"))
                    cc.setSearchPattern(SearchPattern.VNKOD_PATTERN);
                else {
                    cc.setSearchPattern(SearchPattern.PRIMARY_PATTERN);
                }
            }

            case 16,31 -> {
                cc.setConnection(Connection.SELENIUM);
                cc.setSearchPattern(SearchPattern.SECONDARY_PATTERN);
            }

            default -> cc.setSearchPattern(SearchPattern.DEPRECATED_PRIMARY_PATTERN);
        }


        if (url.contains("//oblsud.")||url.contains("//vs.")||url.contains("//sankt-peterburgsky")||url.contains("//gs.")
            ||url.contains("//os.")||url.contains("//kraevo")||url.contains("//sud.")) {
            cc.setLevel(Level.REGION);
        } else {
            cc.setLevel(Level.DISTRICT);
        }

        cc.setVnkod(getVnkod(cc));

        ConfigurationHelper.configurateExceptions(cc);

        ++id;

        return cc;
    }

    private static String changeURL(String url) {
        url=url.replaceFirst("\\.","--");
        url=url.split("\\.ru")[0]+"ru";
        return url;
    }

    private static HashSet<String> getURLs(String sourceURL) {
        HashSet<String> urls;
        while (true) {
            try {
                Document soup = Jsoup
                        .connect(sourceURL)
                        .userAgent(UA)
                        .referrer(sourceURL.replace(PRECONFIG_URL_ENDING, "/"))
                        .timeout(60 * 1000 * 2)
                        .get();
                urls = findURLs(soup, sourceURL);
                break;
            } catch (IOException e) {
                SimpleLogger.println("Проблемы со связью: "+sourceURL);
            }
        }
        if (urls.isEmpty()) {
            System.out.println(sourceURL + ": не найдены необходимые суды.");
        }
        return urls;
    }

    private static HashSet<String> findURLs(Document soup, String sourceURL) {
        HashSet<String> urls = new HashSet<>();
        Elements upperElements = soup.getElementsByClass("mapSubtreeCont");
        for (Element el:upperElements) {
            if (el.text().toLowerCase().contains("районный суд")) {
                Elements infoElements = el.getElementsByClass("mapSubtreeCont");
                for (Element infoElement : infoElements) {
                    Element infoBlock = infoElement.getElementById("ulSudInfo");
                    if (infoBlock != null) {
                        Elements infoItems = infoBlock.getElementsByTag("li");
                        for (int i = 0; i < infoItems.size(); i++) {
                            if (infoItems.get(i).text().contains("сайт")) {
                                urls.add(infoItems.get(i).getElementsByAttribute("href").get(0).text());
                                break;
                            }
                            if (i == infoItems.size() - 1) {
                                SimpleLogger.println(sourceURL + " link not found " + infoBlock.text());
                            }
                        }
                    } else {
                        SimpleLogger.println(sourceURL + ": webelements not found");
                    }
                }
            }
        }
        return urls;
    }
    private static String getVnkodFromBackupCCS(CourtConfiguration cc) {
        if (baseCCS == null) throw new UnsupportedOperationException();
        for (CourtConfiguration courtConfiguration: baseCCS) {
            if (cc.equals(courtConfiguration)) {
                return courtConfiguration.getVnkod();
            }
        }
        return null;
    }

    private static String getVnkod(CourtConfiguration cc) {
        if (getVnkodFromBackupCCS(cc) != null) {
            return getVnkodFromBackupCCS(cc);
        } else if (!cc.isVnkodNeeded()) return null;

        URLCreator urlCreator = new URLCreator(cc);

        String[] urls = urlCreator.createUrlForVnkodConfiguration();
        String vnkod = null;

        for (String url:urls) {
            try {
                vnkod = SeleniumHelper.getInstance().clickAndGetUrl(url, By.id("searchBtn"))
                        .split("vnkod=")[1].substring(0, 8);
                break;
            }
            catch (ArrayIndexOutOfBoundsException ignored) {}
        }

        if (vnkod == null) {
            SimpleLogger.println(Message.VNKOD_MISSING+cc.getSearchString());
            SimpleLogger.log(LoggingLevel.WARNING,Message.VNKOD_MISSING+cc.getSearchString());
        }

        return vnkod;
    }

}