package SUDRFScrapper.strategy;

import SUDRFScrapper.configuration.courtconfiguration.Issue;
import SUDRFScrapper.exception.CaptchaException;
import SUDRFScrapper.exception.VnkodNotFoundException;
import SUDRFScrapper.service.logger.Message;
import SUDRFScrapper.service.logger.LoggingLevel;
import SUDRFScrapper.service.logger.SimpleLogger;

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
