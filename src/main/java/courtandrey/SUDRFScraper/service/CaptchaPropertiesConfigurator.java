package courtandrey.SUDRFScraper.service;

import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Level;
import courtandrey.SUDRFScraper.exception.CaptchaException;
import courtandrey.SUDRFScraper.view.SimpleSwingView;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Properties;

public class CaptchaPropertiesConfigurator {
    private static SeleniumHelper sh;
    private final CourtConfiguration cc;
    private final Properties props;
    protected static String wellItWorkedOnce;

    public CaptchaPropertiesConfigurator(CourtConfiguration cc) {
        this.cc = cc;
        props = getProps();
    }

    public String getCaptcha() {
        return String.valueOf(props.get(String.valueOf(cc.getId())));
    }

    private Properties getProps() {
        Properties properties = new Properties();
        try {
            if (Files.notExists(Path.of(String.format(Constants.PATH_TO_CAPTCHA,cc.getRegion())))) {
                Files.createFile(Path.of(String.format(Constants.PATH_TO_CAPTCHA,cc.getRegion())));
            }
            properties.load(new FileReader(String.format(Constants.PATH_TO_CAPTCHA,cc.getRegion())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static void configurateCaptcha(CourtConfiguration cc, boolean didWellItWorkedOnceUsed) throws InterruptedException {
        CaptchaPropertiesConfigurator cpc = new CaptchaPropertiesConfigurator(cc);

        if (cpc.checkProperties(didWellItWorkedOnceUsed)) return;

        if (sh == null) {
            sh = SeleniumHelper.getInstance();
        }

        String urlFprCaptcha = (new URLCreator(cc)).createUrlForCaptcha();

        if (sh.getCurrentUrl().equals(urlFprCaptcha)) {
            sh.refresh();
            ThreadHelper.sleep(10);
        } else {
            sh.getPage(urlFprCaptcha, 10);
        }

        String captcha = null;

        try {
            for (WebElement e:sh.findElements(By.tagName("tr"))) {
                try {
                    e.findElement(By.name("captchaid"));
                    String dataUrl = e.findElement(By.tagName("img")).getAttribute("src");
                    byte[] dataBytes = Base64.getDecoder().decode(dataUrl.replaceFirst("data:.+,",""));
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(dataBytes));
                    captcha = SimpleSwingView.showCaptcha(image);
                    break;
                } catch (NoSuchElementException ignored) {}
            }
            if (captcha == null) throw new CaptchaException();
        } catch (IOException e) {
            throw new CaptchaException();
        }


        String captchaId = sh.findElement(By.name("captchaid")).getAttribute("value");

        String value = captcha + "&" + captchaId;

        if (cc.getLevel()!= Level.REGION) {
            wellItWorkedOnce = value;
        }

        cpc.setProperty(value);
    }

    private void setProperty(String value) {
        props.setProperty(String.valueOf(cc.getId()),value);
        refresh();
    }

    private void refresh() {
        try {
            props.store(new FileWriter(String.format(Constants.PATH_TO_CAPTCHA,cc.getRegion())),"");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkProperties(boolean didWellItWorkedOceUsed) {
        if (props.isEmpty()) return false;
        else {
            if (wellItWorkedOnce == null) return false;
            if (didWellItWorkedOceUsed) return false;
            setProperty(wellItWorkedOnce);
        }
        return true;
    }
}