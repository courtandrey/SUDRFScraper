package SUDRFScrapper.service;

import SUDRFScrapper.service.logger.LoggingLevel;
import SUDRFScrapper.service.logger.Message;
import SUDRFScrapper.service.logger.SimpleLogger;

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
