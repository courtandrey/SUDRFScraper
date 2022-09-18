package courtandrey.SUDRFScraper.configuration;

import courtandrey.SUDRFScraper.service.Constants;
import lombok.experimental.UtilityClass;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@UtilityClass
public class ApplicationConfiguration {
    public final Properties props = new Properties();
    static {
        try {
            props.load(new FileReader(Constants.PATH_TO_APP_PROPERTIES));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public synchronized Object getProperty(String key) {
        return props.getProperty(key);
    }
}
