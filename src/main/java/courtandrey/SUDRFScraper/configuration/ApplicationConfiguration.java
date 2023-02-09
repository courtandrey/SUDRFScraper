package courtandrey.SUDRFScraper.configuration;

import courtandrey.SUDRFScraper.exception.InitializationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static courtandrey.SUDRFScraper.service.Constant.PATH_TO_APP_PROPERTIES;

public class ApplicationConfiguration {
    public static final Properties props = new Properties();

    private ApplicationConfiguration() {
        try {
            props.load(new FileReader(PATH_TO_APP_PROPERTIES.toString()));
        } catch (IOException e) {
            throw new InitializationException(e);
        }
    }

    private static ApplicationConfiguration configuration;

    public static ApplicationConfiguration getInstance() {
        if (configuration == null) {
            configuration = new ApplicationConfiguration();
        }
        return configuration;
    }

    public synchronized String getProperty(String key) {
        return props.getProperty(key);
    }
    public synchronized void setProperty(String key, String value) {
        props.setProperty(key, value);
    }
}
