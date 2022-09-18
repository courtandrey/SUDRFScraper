package courtandrey.SUDRFScraper.dump;

import courtandrey.SUDRFScraper.Controller;
import courtandrey.SUDRFScraper.dump.model.Case;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import courtandrey.SUDRFScraper.service.Constants;

public abstract class Updater extends Thread{
    protected boolean isScrappingOver;
    protected String dumpName;
    private final String PATH_TO_SUMMERY;
    protected Controller controller;

    public Updater(String dumpName, Controller controller) {
        this.dumpName = dumpName;
        this.controller = controller;
        PATH_TO_SUMMERY = String.format(Constants.PATH_TO_SUMMERY, dumpName, dumpName);
        try {
            Path results = Path.of(Constants.PATH_TO_RESULTS_DIRECTORY);
            if (Files.notExists(results)) Files.createDirectory(results);
            Path dump = Path.of(String.format(Constants.PATH_TO_RESULT_DIRECTORY, dumpName));
            if (Files.notExists(dump)) {
                Files.createDirectory(dump);
            }
        } catch (IOException e) {
            controller.errorOccurred(e, null);
        }
    }

    public void registerEnding() {
        isScrappingOver = true;
    }

    public abstract void update(Collection<Case> cases);
    public abstract void close();

    public void writeSummery(String text){
        try (FileWriter w = new FileWriter(PATH_TO_SUMMERY)) {
            w.write(text);
            w.write(getSummeryInfo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSummeryInfo() throws IOException {
        StringBuilder returnString = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(Constants.PATH_TO_SUMMERY_INFO))) {
            while (reader.ready()) {
                returnString.append(reader.readLine());
                returnString.append("\n");
            }
        }
        return returnString.toString();
    }

}
