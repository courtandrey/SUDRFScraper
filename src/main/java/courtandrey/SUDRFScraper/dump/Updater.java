package courtandrey.SUDRFScraper.dump;

import courtandrey.SUDRFScraper.SUDRFScraper;
import courtandrey.SUDRFScraper.dump.model.Case;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static courtandrey.SUDRFScraper.service.Constants.PATH_TO_SUMMERY_INFO;

public abstract class Updater extends Thread{
    protected boolean isScrappingOver;
    protected String dumpName;
    private final String PATH_TO_SUMMERY;
    protected SUDRFScraper controller;

    public Updater(String dumpName, SUDRFScraper controller) {
        this.dumpName = dumpName;
        this.controller = controller;
        PATH_TO_SUMMERY = "./results/" + dumpName + "/" + dumpName + "_summery.txt";
        try {
            Path results = Path.of("./results/");
            if (Files.notExists(results)) Files.createDirectory(results);
            Path dump = Path.of("./results/" + dumpName + "/");
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
        try (BufferedReader reader = Files.newBufferedReader(Path.of(PATH_TO_SUMMERY_INFO))) {
            while (reader.ready()) {
                returnString.append(reader.readLine());
                returnString.append("\n");
            }
        }
        return returnString.toString();
    }

}
