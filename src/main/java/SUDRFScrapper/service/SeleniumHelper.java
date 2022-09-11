package SUDRFScrapper.service;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.List;

public final class SeleniumHelper {

    private static WebDriver wd;
    private static SeleniumHelper sh;

    public static synchronized boolean isActive() {
        return wd != null;
    }

    private SeleniumHelper() {}

    public synchronized void refresh() {
        if (wd == null) reset();
        wd.navigate().refresh();
    }

    public synchronized WebElement findElement(By by) {
        if (wd == null) reset();
        return wd.findElement(by);
    }

    public synchronized List<WebElement> findElements(By by) {
        if (wd == null) reset();
        return wd.findElements(by);
    }

    public static synchronized SeleniumHelper getInstance() {
        if (sh == null) {
            System.setProperty("webdriver.gecko.driver", "./src/main/resources/geckodriver");
            String os = System.getProperty("os.name");
            String nul = "nul";
            if (!os.toLowerCase().contains("windows")) nul = "/dev/null";
            System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, nul);
            FirefoxOptions options = new FirefoxOptions();
            options.setHeadless(true);
            sh = new SeleniumHelper();
            wd = new FirefoxDriver(options);
            wd.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(1));
            wd.manage().timeouts().scriptTimeout(Duration.ofMinutes(1));
        }
        return sh;
    }

    private static void reset() {
        wd = new FirefoxDriver();
        wd.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(1));
        wd.manage().timeouts().scriptTimeout(Duration.ofMinutes(1));
    }

    public synchronized String clickAndGetUrl(String sourceUrl, By clickableElement) {
        if (wd == null) reset();
        wd.manage().timeouts().pageLoadTimeout(Duration.ofMinutes(10));
        wd.get(sourceUrl);
        click(clickableElement);
        return wd.getCurrentUrl();
    }

    public synchronized String getCurrentUrl() {
        if (wd == null) reset();
        if (wd.getCurrentUrl() == null) throw new UnsupportedOperationException();
        return wd.getCurrentUrl();
    }

    public synchronized void click(By clickableElement) {
        if (wd == null) reset();
        scrollTo(clickableElement).click();
    }

    public synchronized WebElement scrollTo(By element) {
        if (wd == null) reset();
        if (wd.getCurrentUrl() == null) throw new UnsupportedOperationException();
        JavascriptExecutor ex = (JavascriptExecutor) wd;
        WebElement el = wd.findElement(element);
        ex.executeScript("arguments[0].scrollIntoView();",el);
        return el;
    }

    public synchronized String getPage(String sourceUrl, Integer waitTime) {
        if (wd == null) reset();

        wd.get(sourceUrl);

        if (waitTime!=null) {
            ThreadHelper.sleep(waitTime);
        }

        if (wd.getPageSource() == null) throw new TimeoutException();

        return wd.getPageSource();
    }

    public static synchronized void endSession() {
        if (isActive()) wd.quit();
        sh = null;
    }

}
