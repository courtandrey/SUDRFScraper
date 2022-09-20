package courtandrey.SUDRFScraper.dump;

import com.fasterxml.jackson.databind.ObjectMapper;
import courtandrey.SUDRFScraper.configuration.searchrequest.SearchRequest;
import courtandrey.SUDRFScraper.controller.ErrorHandler;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Queue;

import courtandrey.SUDRFScraper.dump.model.Case;
import static courtandrey.SUDRFScraper.service.Constant.*;

public abstract class UpdaterService extends Thread implements Updater{
    protected boolean isScrappingOver;
    protected String dumpName;
    protected Queue<Case> cases = new ArrayDeque<>();
    private final String SUMMERY;
    protected ErrorHandler handler;
    protected boolean isMetaNeeded = false;

    @Override
    public void startService() {
        this.start();
    }

    public UpdaterService(String dumpName, ErrorHandler handler) throws IOException{
        this.dumpName = dumpName;
        this.handler = handler;
        SUMMERY = String.format(PATH_TO_SUMMERY.toString(), dumpName, dumpName);
        Path dumpDirectory = Path.of(BASIC_RESULT_PATH.toString());
        if (Files.notExists(dumpDirectory)) {
            Files.createDirectory(dumpDirectory);
        }
        Path dump = Path.of(String.format(PATH_TO_RESULT_DIRECTORY.toString(), dumpName));
        if (Files.notExists(dump)) {
            Files.createDirectory(dump);
        }
    }

    @Override
    public void registerEnding() {
        isScrappingOver = true;
    }

    @Override
    public void addMeta() {
        isMetaNeeded = true;
    }

    protected abstract void createMeta() throws IOException;

    @Override
    public void joinService() throws InterruptedException {
        registerEnding();
        this.join();
    }

    protected HashMap<String, String> getBasicProperties() {
        HashMap<String, String> properties = new HashMap<>();
        SearchRequest sc = SearchRequest.getInstance();
        if (sc.getArticle() != null) {
            properties.put("article",sc.getArticle().toString());
        } else {
            properties.put("field",sc.getField().toString());
        }
        if (sc.getResultDateFrom() != null) {
            properties.put("result_date_from",sc.getResultDateFrom());
        }
        if (sc.getResultDateTill() != null) {
            properties.put("result_date_till",sc.getResultDateTill());
        }
        if (sc.getText() != null) {
            properties.put("text",sc.getText());
        }
        return properties;
    }

    protected void writeMeta(HashMap<String,String> meta) throws IOException {
        FileWriter writer = new FileWriter(String.format(PATH_TO_RESULT_META.toString(), dumpName, dumpName), false);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, meta);
    }
    protected void afterExecute() throws IOException {
        close();

        if (isMetaNeeded) {
            createMeta();
        }
    }
    protected abstract void close();

    @Override
    public synchronized void update(Collection<Case> casesList) {
        cases.addAll(casesList);
    }

    @Override
    public void writeSummery(String text){
        try (FileWriter w = new FileWriter(SUMMERY)) {
            w.write(text);
            w.write(getSummeryInfo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getSummeryInfo() throws IOException {
        StringBuilder returnString = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(PATH_TO_SUMMERY_INFO.toString()))) {
            while (reader.ready()) {
                returnString.append(reader.readLine());
                returnString.append("\n");
            }
        }
        return returnString.toString();
    }

}
