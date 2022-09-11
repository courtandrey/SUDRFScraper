package SUDRFScrapper.configuration;

import SUDRFScrapper.configuration.courtconfiguration.CourtConfiguration;
import SUDRFScrapper.service.ConfigurationLoader;

import java.io.IOException;
import java.util.List;

public class ConfigurationHolder {
    private List<CourtConfiguration> ccs;
    private static ConfigurationHolder configurationHolder = null;
    private ConfigurationHolder() throws IOException {
        ccs = ConfigurationLoader.getCourtConfigurations();
        ConfigurationLoader.doBackUp();
    }

    public void changeCCs() throws IOException {
        ccs = ConfigurationLoader.getCourtConfigurations(true);
    }

    public static ConfigurationHolder getInstance() throws IOException {
        if (configurationHolder == null) {
            configurationHolder = new ConfigurationHolder();
        }
        return configurationHolder;
    }

    public List<CourtConfiguration> getCCs() {
        return ccs;
    }
}
