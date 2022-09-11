package courtandrey.SUDRFScraper.service;

import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;

public final class ThreadHelper {
    private ThreadHelper() {}

    public static void sleep(double seconds) {
        try {
            Thread.sleep((long)(1000 * seconds));
        } catch (InterruptedException e) {
            SimpleLogger.log(LoggingLevel.ERROR, String.format(Message.EXCEPTION_OCCURRED.toString(), e));
        }
    }
}
