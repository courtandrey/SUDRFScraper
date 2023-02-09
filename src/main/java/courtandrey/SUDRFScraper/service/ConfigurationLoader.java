package courtandrey.SUDRFScraper.service;

import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import courtandrey.SUDRFScraper.exception.InitializationException;
import lombok.experimental.UtilityClass;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static courtandrey.SUDRFScraper.service.Constant.*;

@UtilityClass
public class ConfigurationLoader {
    public ArrayList<CourtConfiguration> getCourtConfigurations() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(Path.of(PATH_TO_CONFIG.toString()).toFile(),
                mapper.getTypeFactory().constructCollectionType(ArrayList.class, CourtConfiguration.class));
    }

    public ArrayList<CourtConfiguration> getCourtConfigurations(boolean needToUseBaseConfig) throws IOException {
        if (needToUseBaseConfig) return getCourtConfigurations();
        else {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(Path.of(PATH_TO_CONFIG_BASE.toString()).toFile(),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, CourtConfiguration.class));
        }
    }

    public void doBackUp() {
        try {
            Path configBackUp = Path.of(PATH_TO_CONFIG_BACKUP.toString());
            Files.deleteIfExists(configBackUp);
            Files.copy(Path.of(PATH_TO_CONFIG.toString()), configBackUp);
        } catch (IOException e) {
            throw new InitializationException(e);
        }
    }

    public ArrayList<CourtConfiguration> getCourtConfigurationsFromBase() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(Path.of(PATH_TO_CONFIG_BASE.toString()).toFile(),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, CourtConfiguration.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void refresh(List<CourtConfiguration> ccs) {
        try {
            FileWriter writer = new FileWriter(PATH_TO_CONFIG.toString());
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer,ccs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
