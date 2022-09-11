package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue;
import courtandrey.SUDRFScraper.exception.CaptchaException;
import courtandrey.SUDRFScraper.exception.VnkodNotFoundException;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;

public class StrategyUEH {
    public void handle(Thread t, Throwable e) {
        SUDRFStrategy strategy = (SUDRFStrategy) t;
        if (e instanceof VnkodNotFoundException) {
            strategy.finalIssue = Issue.compareAndSetIssue(Issue.CONFIGURATION_ERROR, strategy.finalIssue);
        }
        else if (e instanceof CaptchaException) {
            strategy.finalIssue = Issue.compareAndSetIssue(Issue.INACTIVE_COURT, strategy.finalIssue);
        }
        else {
            SimpleLogger.log(LoggingLevel.ERROR,String.format(Message.EXECUTION_EXCEPTION_OCCURRED.toString(), e.toString())
                    + strategy.cc.getSearchString());
            strategy.finalIssue = Issue.compareAndSetIssue(Issue.ERROR, strategy.finalIssue);
        }
        strategy.finish();
    }

}
