package servicetests;

import courtandrey.SUDRFScraper.configuration.ApplicationConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue;
import courtandrey.SUDRFScraper.configuration.searchrequest.SearchRequest;
import courtandrey.SUDRFScraper.configuration.searchrequest.article.CriminalArticle;
import courtandrey.SUDRFScraper.service.Constants;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class SimpleLoggerTest {
    static {
        ApplicationConfiguration.setProperty("log.court.history", "true");
    }
    @Test
    public void successAddToCourtHistoryTest() throws IOException {
        CourtConfiguration cc = new CourtConfiguration();
        cc.setId(-1);
        cc.setIssue(Issue.NOT_FOUND_CASE);
        SearchRequest.getInstance().setArticle(new CriminalArticle(228));

        SimpleLogger.addToCourtHistory(cc);

        Path courtPath = Path.of(String.format(Constants.PATH_TO_COURT_HISTORY, cc.getId()));

        assert Files.exists(courtPath) && Files.size(courtPath) > 0;

        BufferedReader reader = Files.newBufferedReader(courtPath);
        String lastLine = "";
        while (reader.ready()) {
            lastLine = reader.readLine();
        }
        reader.close();

        assert lastLine.equals("Cases not found " + LocalDate.now() + " " + "{article = Уголовная Статья 228;}");

        Files.deleteIfExists(courtPath);
    }
}
