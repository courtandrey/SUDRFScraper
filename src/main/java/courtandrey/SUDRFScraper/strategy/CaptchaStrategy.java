package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.exception.CaptchaException;
import courtandrey.SUDRFScraper.service.CaptchaPropertiesConfigurator;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue;
import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;
import org.openqa.selenium.TimeoutException;

public class CaptchaStrategy extends ConnectionSUDRFStrategy {
    private boolean didWellItWorkedOnceUsed = false;
    public CaptchaStrategy(CourtConfiguration cc) {
        super(cc);
    }

    @Override
    public void run() {
        try {
            createUrls();
            for (; indexUrl < urls.length; indexUrl++) {
                super.run();
                if (issue == Issue.CAPTCHA) {
                    issue = null;
                    timeToStopRotatingSrv = false;

                    CaptchaPropertiesConfigurator.configureCaptcha(cc, didWellItWorkedOnceUsed);

                    didWellItWorkedOnceUsed = true;
                    createUrls();
                    --indexUrl;
                    refreshUrls();
                } else if (issue == Issue.SUCCESS) {
                    indexUrl++;
                    break;
                } else {
                    clear();
                }
            }
            --indexUrl;
        }
        catch (InterruptedException e) {
            SimpleLogger.log(LoggingLevel.ERROR, String.format(Message.EXCEPTION_OCCURRED.toString(),e));
            finalIssue = Issue.ERROR;
        }
        catch (TimeoutException e) {
            finalIssue = Issue.CONNECTION_ERROR;
        }
        catch (CaptchaException e) {
            finalIssue = Issue.CAPTCHA_NOT_CONFIGURABLE;
        }
        finish();

    }

    private void refreshUrls() {
        for (int i = 0; i< urls.length; i++) {
            urls[i] = urls[i].replace("page="+1,"page="+page_num);
            urls[i] = urls[i].replace("srv_num="+1,"srv_num="+srv_num);
            urls[i] = urls[i].replace("num_build="+1,"num_build="+(build));
        }
    }

}
