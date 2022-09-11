package SUDRFScrapper.configuration.courtconfiguration;

import SUDRFScrapper.configuration.searchrequest.Field;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static SUDRFScrapper.service.Constants.PATH_TO_ADMIN_PROPERTIES;
import static SUDRFScrapper.service.Constants.PATH_TO_CRIMINAL_PROPERTIES;

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
        return String.valueOf(getProps(PATH_TO_CRIMINAL_PROPERTIES).getProperty(this.toString())).split("\\$DELIMITER");
    }

    public String[] getAdminPattern() {
        return String.valueOf(getProps(PATH_TO_ADMIN_PROPERTIES).getProperty(this.toString())).split("\\$DELIMITER");
    }
}















