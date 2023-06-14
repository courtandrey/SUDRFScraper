package courtandrey.SUDRFScraper.strategy;

import courtandrey.SUDRFScraper.exception.CaptchaException;
import courtandrey.SUDRFScraper.service.CaptchaPropertiesConfigurator;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue;
import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;
import org.openqa.selenium.TimeoutException;

import static courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue.*;

public class CaptchaStrategy extends ConnectionSUDRFStrategy {
    private boolean didWellItWorkedOnceUsed = false;
    public CaptchaStrategy(CourtConfiguration cc) {
        super(cc);
    }

    int captchaInLoop = 0;

    int prevNum = 1;
    int prevSrvNum = 1;

    @Override
    public void run() {
        try {
            createUrls();
            for (indexUrl = 0; indexUrl < urls.length; indexUrl++) {
                super.run();
                if (issue == Issue.CAPTCHA && finalIssue != NOT_FOUND_CASE) {
                    if (captchaInLoop == 5 && page_num == prevNum && srv_num == prevSrvNum) {
                        if (indexUrl + 1 == urls.length) {
                            finalIssue = Issue.compareAndSetIssue(LOOPED_CAPTCHA,finalIssue);
                            break;
                        }
                        else {
                            captchaInLoop = 0;
                            continue;
                        }
                    } else if (captchaInLoop == 5){
                        captchaInLoop = 0;
                    }

                    prevNum = page_num;
                    prevSrvNum = srv_num;
                    captchaInLoop += 1;
                    issue = null;
                    timeToStopRotatingSrv = false;

                    CaptchaPropertiesConfigurator.configureCaptcha(cc, didWellItWorkedOnceUsed,cc.getConnection());

                    didWellItWorkedOnceUsed = true;
                    createUrls();
                    indexUrl = indexUrl - 1;
                    refreshUrls();
                }
                else if (finalIssue == SUCCESS) {
                    indexUrl += 1;
                    break;
                }
                else {
                    captchaInLoop = 0;
                    clear();
                }
            }
            indexUrl -= 1;
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
