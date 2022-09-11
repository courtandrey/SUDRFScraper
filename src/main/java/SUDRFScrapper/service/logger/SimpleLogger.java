package SUDRFScrapper.service.logger;

import SUDRFScrapper.configuration.courtconfiguration.CourtConfiguration;
import SUDRFScrapper.configuration.courtconfiguration.Issue;
import SUDRFScrapper.configuration.searchrequest.SearchRequest;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static SUDRFScrapper.service.Constants.PATH_TO_COURT_HISTORY;
import static SUDRFScrapper.service.Constants.PATH_TO_LOGS;

public final class SimpleLogger {
    private static FileWriter logWriter;
    private static final DateTimeFormatter  dt = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss");
    private static String name = "default";

    private SimpleLogger() {}

    public static void initLogger(String name) {
        SimpleLogger.name = name;
    }

    public synchronized static void log(LoggingLevel level, Object message) {
        try {
            LocalDateTime ldt = LocalDateTime.now();
            getLogWriter().write(level.toString()+" "+ldt.format(dt)+" "+message+"\n");
        } catch (IOException e) {
            reopen();
            log(level, message);
        }
    }

    public synchronized static void addToCourtHistory(CourtConfiguration cc) throws IOException {
        Path courtHistory = Path.of("./src/main/resources/courts/");
        if (Files.notExists(courtHistory)) Files.createDirectory(courtHistory);
        try (FileWriter writer = new FileWriter(String.format(PATH_TO_COURT_HISTORY, cc.getId()), true)) {
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

    private static FileWriter getLogWriter()  {
        if (logWriter == null) {
            try {
                logWriter = new FileWriter(String.format(PATH_TO_LOGS, name, name),true);
                logWriter.write(Message.BEGINNING_OF_EXECUTION + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logWriter;
    }

    public synchronized static void println(Object message) {
        System.out.println(message);
    }

    public static void close() {
        if (logWriter != null) {
            try {
                logWriter.flush();
                logWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
