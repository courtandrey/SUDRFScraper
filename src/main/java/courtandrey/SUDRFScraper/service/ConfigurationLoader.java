package courtandrey.SUDRFScraper.service;

import courtandrey.SUDRFScraper.configuration.ApplicationConfiguration;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import courtandrey.SUDRFScraper.exception.InitializationException;
import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;
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
    private String dumpName;
    private String path = PATH_TO_CONFIG.toString();

    public static void setDumpName(String dumpName) {
        ConfigurationLoader.dumpName = dumpName;
    }

    public ArrayList<CourtConfiguration> getCourtConfigurations() throws IOException {
        if (ApplicationConfiguration.getInstance().getProperty("basic.continue") != null &&
            ApplicationConfiguration.getInstance().getProperty("basic.continue").equals("true")) {
            String altPath = String.format(PATH_TO_RESULT_DIRECTORY.toString(), dumpName) + dumpName + "_result_config.json";
            if (Files.exists(Path.of(altPath))) {
                path = altPath;
            }
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(Path.of(path).toFile(),
                    mapper.getTypeFactory().constructCollectionType(ArrayList.class, CourtConfiguration.class));
        }catch (IOException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.MALFORMED_CONFIG.toString());
            return getCourtConfigurationsFromBackUp();
        }
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

    public ArrayList<CourtConfiguration> getCourtConfigurationsFromBackUp() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<CourtConfiguration> ls = mapper.readValue(Path.of(PATH_TO_CONFIG_BACKUP.toString()).toFile(),
                mapper.getTypeFactory().constructCollectionType(ArrayList.class, CourtConfiguration.class));
        refresh(ls);
        return ls;
    }

    public synchronized void refresh(List<CourtConfiguration> ccs) {
        try {
            FileWriter writer = new FileWriter(path);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer,ccs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void storeConfiguration(List<CourtConfiguration> ccs) {
        try {
            FileWriter writer = new FileWriter(String.format(PATH_TO_RESULT_DIRECTORY.toString(),dumpName) + dumpName + "_result_config.json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer,ccs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
