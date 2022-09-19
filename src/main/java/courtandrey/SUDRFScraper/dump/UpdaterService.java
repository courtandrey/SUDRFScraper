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
import courtandrey.SUDRFScraper.service.Constants;

public abstract class UpdaterService extends Thread implements Updater{
    protected boolean isScrappingOver;
    protected String dumpName;
    protected Queue<Case> cases = new ArrayDeque<>();
    private final String PATH_TO_SUMMERY;
    protected ErrorHandler handler;

    @Override
    public void startService() {
        this.start();
    }

    public UpdaterService(String dumpName, ErrorHandler handler) {
        this.dumpName = dumpName;
        this.handler = handler;
        PATH_TO_SUMMERY = String.format(Constants.PATH_TO_SUMMERY, dumpName, dumpName);
        try {
            Path results = Path.of(Constants.PATH_TO_RESULTS_DIRECTORY);
            if (Files.notExists(results)) Files.createDirectory(results);
            Path dump = Path.of(String.format(Constants.PATH_TO_RESULT_DIRECTORY, dumpName));
            if (Files.notExists(dump)) {
                Files.createDirectory(dump);
            }
        } catch (IOException e) {
            handler.errorOccurred(e, null);
        }
    }

    @Override
    public void registerEnding() {
        isScrappingOver = true;
    }

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
        FileWriter writer = new FileWriter(String.format(Constants.PATH_TO_RESULT_META, dumpName, dumpName), false);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, meta);
    }
    protected void afterExecute() {
        close();
    }
    protected abstract void close();

    @Override
    public synchronized void update(Collection<Case> casesList) {
        cases.addAll(casesList);
    }

    @Override
    public void writeSummery(String text){
        try (FileWriter w = new FileWriter(PATH_TO_SUMMERY)) {
            w.write(text);
            w.write(getSummeryInfo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getSummeryInfo() throws IOException {
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
