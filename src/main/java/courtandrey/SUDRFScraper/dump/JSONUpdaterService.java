package courtandrey.SUDRFScraper.dump;

import courtandrey.SUDRFScraper.controller.ErrorHandler;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.service.Constants;
import courtandrey.SUDRFScraper.service.ThreadHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class JSONUpdaterService extends UpdaterService {
    private FileWriter fileWriter;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String fileName;

    public JSONUpdaterService(String dumpName, ErrorHandler handler) throws IOException {
        super(dumpName, handler);
        fileName = String.format(Constants.PATH_TO_RESULT_JSON, dumpName, dumpName);
        try {
            renew();
            if (Files.size(Path.of(fileName)) > 0) {
                fileWriter.write("\n");
                Case.idInteger = new AtomicInteger(getCaseId());
            }
        } catch (IOException e) {
            handler.errorOccurred(e, null);
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
        }
        catch (IOException e) {
            handler.errorOccurred(e, this);
        }
        finally {
            afterExecute();
        }
    }

    @Override
    public void addMeta() throws IOException {
        HashMap<String,String> properties = new HashMap<>();
        BufferedReader reader = Files.newBufferedReader(Path.of(fileName));
        int stringCount = 0;
        while (reader.ready()) {
            reader.readLine();
            stringCount = stringCount + 1;
        }
        reader.close();
        properties.put("string_count",String.valueOf(stringCount));
        properties.putAll(getBasicProperties());
        writeMeta(properties);
    }

    private void renew() throws IOException {
        fileWriter = new FileWriter(fileName, StandardCharsets.UTF_8,true);
    }

    private void update(Case _case) throws IOException {
        _case.setId(Case.idInteger.getAndIncrement());
        mapper.writeValue(fileWriter,_case);
        renew();
        fileWriter.write("\n");
    }

    @Override
    protected void close() {
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
