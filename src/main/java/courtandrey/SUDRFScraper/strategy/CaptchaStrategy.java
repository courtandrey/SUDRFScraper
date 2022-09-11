package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.service.CaptchaPropertiesConfigurator;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue;
import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;

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

                    CaptchaPropertiesConfigurator.configurateCaptcha(cc, didWellItWorkedOnceUsed);

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
        }
        finish();

    }

    private void refreshUrls() {
        for (int i = 0; i< urls.length; i++) {
            urls[i] = urls[i].replace("page="+1,"page="+page_num);
            urls[i] = urls[i].replace("srv_num="+1,"srv_num="+srv_num);
        }
    }

}
