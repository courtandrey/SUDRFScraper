package courtandrey.SUDRFScraper.service.logger;

import courtandrey.SUDRFScraper.configuration.ApplicationConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue;
import courtandrey.SUDRFScraper.configuration.searchrequest.SearchRequest;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static courtandrey.SUDRFScraper.service.Constant.PATH_TO_COURT_HISTORY;
import static courtandrey.SUDRFScraper.service.Constant.PATH_TO_LOGS;

public final class SimpleLogger {
    private static FileWriter logWriter;
    private static final DateTimeFormatter  dt = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss");
    private static String name = "default";
    private static Boolean useCourtHistory;
    private static boolean isInited = false;

    private SimpleLogger() {}

    public static void initLogger(String name) {
        SimpleLogger.name = name;
        useCourtHistory = Boolean.parseBoolean(ApplicationConfiguration.getInstance().getProperty("log.court.history"));
    }

    private static void initLogger() {
        if (!isInited) {
            initLogger(name);
            isInited = true;
        }
    }

    public synchronized static void log(LoggingLevel level, Object message) {
        try {
            LocalDateTime ldt = LocalDateTime.now();
            String string = level.toString() + " " + ldt.format(dt) + " " + message + "\n";
            getLogWriter().write(string);
            getLogWriter().flush();
            System.out.println(string);
        } catch (IOException e) {
            reopen();
            log(level, message);
        }
    }

    public synchronized static void addToCourtHistory(CourtConfiguration cc) throws IOException {
        if (!useCourtHistory) return;
        initLogger();
        Path courtHistory = Path.of("./src/main/resources/courts/");
        if (Files.notExists(courtHistory)) Files.createDirectory(courtHistory);
        try (FileWriter writer = new FileWriter(String.format(PATH_TO_COURT_HISTORY.toString(), cc.getId()), true)) {
            if (cc.getIssue() == null)  {
                cc.setIssue(Issue.ERROR);
            }
            writer.write(cc.getIssue().toString() + " " + LocalDate.now() + " " + SearchRequest.getInstance().toString());
            writer.write("\n");
        } catch (IOException e) {
            log(LoggingLevel.ERROR, String.format(Message.IOEXCEPTION_OCCURRED.toString(),e));
        }
    }

    private synchronized static void reopen() {
        logWriter = null;
    }

    private static FileWriter getLogWriter() throws IOException {
        if (logWriter == null) {
            logWriter = new FileWriter(String.format(PATH_TO_LOGS.toString(), name, name),true);
            log(LoggingLevel.INFO, Message.BEGINNING_OF_EXECUTION + "\n");
        }
        return logWriter;
    }

    public synchronized static void println(Object message) {
        System.out.println(message);
    }

    public static void close() throws IOException{
        if (logWriter != null) {
            logWriter.flush();
            logWriter.close();
        }
    }
}
