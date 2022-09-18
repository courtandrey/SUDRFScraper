package courtandrey.SUDRFScraper.dump;

import courtandrey.SUDRFScraper.Controller;
import courtandrey.SUDRFScraper.configuration.searchrequest.SearchRequest;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.service.ThreadHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class JSONUpdater extends Updater{
    private final Queue<Case> cases = new ArrayDeque<>();
    private FileWriter fileWriter;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String fileName;

    public JSONUpdater(String dumpName, Controller controller) {
        super(dumpName, controller);
        fileName = "./results/" + dumpName +"/" + dumpName + ".json";
        try {
            renew();
            if (Files.size(Path.of(fileName)) > 0) {
                fileWriter.write("\n");
                Case.idInteger = new AtomicInteger(getCaseId());
            }
        } catch (IOException e) {
            controller.errorOccurred(e, null);
        }
    }

    private int getCaseId() throws IOException {
        BufferedReader reader = Files.newBufferedReader(Path.of(fileName));
        String stringCase = "";
        while (reader.ready()) {
            stringCase = reader.readLine();
        }
        reader.close();
        if (!stringCase.equals("")) {
            Case _case = mapper.readValue(stringCase, Case.class);
            return _case.getId() + 1;
        }
        return 0;

    }

    @Override
    public synchronized void update(Collection<Case> casesList) {
        cases.addAll(casesList);
    }

    private synchronized Case poll() {
        return cases.poll();
    }

    @Override
    public void run() {
        try {
            while (!isScrappingOver) {
                if (cases.isEmpty())  {
                    ThreadHelper.sleep(10);
                }
                else {
                    Case _case = poll();
                    update(_case);
                }
            }
            while (!cases.isEmpty()) {
                Case _case = cases.poll();
                update(_case);
            }
        } finally {
            close();
            try {
                addMeta();
            } catch (IOException e) {
                controller.errorOccurred(e, this);
            }
        }
    }

    private void addMeta() throws IOException {
        HashMap<String,String> properties = new HashMap<>();
        BufferedReader reader = Files.newBufferedReader(Path.of(fileName));
        int stringCount = 0;
        while (reader.ready()) {
            reader.readLine();
            stringCount = stringCount + 1;
        }
        reader.close();
        properties.put("string_count",String.valueOf(stringCount));
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
        FileWriter writer = new FileWriter(fileName.split("\\.json")[0] +"_meta.json", false);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer,properties);
    }

    private void renew() throws IOException {
        fileWriter = new FileWriter(fileName, StandardCharsets.UTF_8,true);
    }

    private void update(Case _case) {
        try {
            _case.setId(Case.idInteger.getAndIncrement());
            mapper.writeValue(fileWriter,_case);
            renew();
            fileWriter.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close() {
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
