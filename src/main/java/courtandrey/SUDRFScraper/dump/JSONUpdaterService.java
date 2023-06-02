package courtandrey.SUDRFScraper.dump;

import courtandrey.SUDRFScraper.controller.ErrorHandler;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.service.ThreadHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static courtandrey.SUDRFScraper.service.Constant.PATH_TO_RESULT_JSON;
import static courtandrey.SUDRFScraper.service.Constant.PATH_TO_RESULT_META;

public class JSONUpdaterService extends UpdaterService {

    private FileWriter fileWriter;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String fileName;
    private final String meta;

    public JSONUpdaterService(String dumpName, ErrorHandler handler) throws IOException {
        super(dumpName, handler);
        fileName = String.format(PATH_TO_RESULT_JSON.toString(), dumpName, dumpName);
        meta = String.format(PATH_TO_RESULT_META.toString(),dumpName,dumpName);
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
    @SuppressWarnings(value = "unchecked")
    private int getCaseId() throws IOException {
        if (Files.exists(Path.of(meta))) {
            HashMap<String,String> met = mapper.readValue(new File(meta),HashMap.class);
            return Integer.parseInt(met.get("string_count")) + 1;
        }
        Stack<Integer> stack = new Stack<>();
        stack.peek();
        BufferedReader reader = Files.newBufferedReader(Path.of(fileName));
        String stringCase;
        Case _case = null;
        while (reader.ready()) {
            stringCase = reader.readLine();
            try {
                mapper.readValue(stringCase, Case.class);
                _case = mapper.readValue(stringCase, Case.class);
            } catch (Exception ignored) {
            }
        }
        reader.close();
        if (_case != null) {
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
                if (_case == null) break;
                update(_case);
            }
        }
        catch (IOException e) {
            handler.errorOccurred(e, this);
        }
        finally {
            try {
                afterExecute();
            } catch (IOException e) {
                handler.errorOccurred(e, this);
            }
        }
    }

    @Override
    public void createMeta() throws IOException {
        HashMap<String,String> properties = new HashMap<>();
        properties.put("string_count",String.valueOf(Case.idInteger.get()-1));
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
