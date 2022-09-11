package SUDRFScrapper.service;

import SUDRFScrapper.configuration.courtconfiguration.CourtConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static SUDRFScrapper.service.Constants.*;

public final class ConfigurationLoader {
    private ConfigurationLoader() {
    }

    public static ArrayList<CourtConfiguration> getCourtConfigurations() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(Path.of(PATH_TO_CONFIG).toFile(),
                mapper.getTypeFactory().constructCollectionType(ArrayList.class, CourtConfiguration.class));
    }

    public static ArrayList<CourtConfiguration> getCourtConfigurations(boolean needToUseBaseConfig) throws IOException {
        if (needToUseBaseConfig) return getCourtConfigurations();
        else {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(Path.of(PATH_TO_CONFIG_BASE).toFile(),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, CourtConfiguration.class));
        }
    }

    public static void doBackUp() {
        try {
            Files.deleteIfExists(Path.of(PATH_TO_CONFIG_BACKUP));
            Files.copy(Path.of(PATH_TO_CONFIG),Path.of(PATH_TO_CONFIG_BACKUP));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<CourtConfiguration> getCourtConfigurationsFromBackup() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(Path.of(PATH_TO_CONFIG_BACKUP).toFile(),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, CourtConfiguration.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<CourtConfiguration> getCourtConfigurationsFromBase() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(Path.of(PATH_TO_CONFIG_BASE).toFile(),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, CourtConfiguration.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized static void refresh(List<CourtConfiguration> ccs) {
        try {
            FileWriter writer = new FileWriter(PATH_TO_CONFIG);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer,ccs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
