package courtandrey.SUDRFScraper.configuration.courtconfiguration;

import courtandrey.SUDRFScraper.configuration.searchrequest.Field;
import courtandrey.SUDRFScraper.service.Constants;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public enum SearchPattern {
    DEPRECATED_PRIMARY_PATTERN,
    PRIMARY_PATTERN,
    VNKOD_PATTERN,
    DEPRECATED_SECONDARY_PATTERN,
    SECONDARY_PATTERN,
    PATTERN_CAPTCHA,
    BRAND_NEW_PATTERN,
    ;

    private Properties getProps(String path) {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public String[] getPattern(Field field) {
        switch (field) {
            case ADMIN -> {
                return getAdminPattern();
            }
            case CRIMINAL -> {
                return getCriminalPattern();
            }
        }
        return new String[]{};
    }

    public String[] getCriminalPattern() {
        return String.valueOf(getProps(Constants.PATH_TO_CRIMINAL_PROPERTIES).getProperty(this.toString())).split("\\$DELIMITER");
    }

    public String[] getAdminPattern() {
        return String.valueOf(getProps(Constants.PATH_TO_ADMIN_PROPERTIES).getProperty(this.toString())).split("\\$DELIMITER");
    }
}















