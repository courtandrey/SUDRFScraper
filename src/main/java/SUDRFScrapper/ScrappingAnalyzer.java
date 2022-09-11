package SUDRFScrapper;

import SUDRFScrapper.configuration.courtconfiguration.CourtConfiguration;
import SUDRFScrapper.configuration.dumpconfiguration.ServerConnectionInfo;
import SUDRFScrapper.dump.model.Case;
import SUDRFScrapper.dump.model.Dump;
import SUDRFScrapper.service.ConfigurationLoader;
import SUDRFScrapper.service.logger.Message;
import SUDRFScrapper.service.logger.SimpleLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ScrappingAnalyzer {
    protected String name;
    private ScrappingAnalyzer analyzer;

    private ScrappingAnalyzer(String name) {
        this.name=name;
    }

    public ScrappingAnalyzer(String name, Dump dump){
        this.analyzer = getAnalyzer(dump,name);
    }

    public void showCasesPerRegion() throws IOException {
        analyzer.showCasesPerRegion();
    }

    public void showRandomText() throws IOException {
        analyzer.showRandomText();
    }

    public void showTextNumber() throws IOException {
        analyzer.showTextNumber();
    }

    private ScrappingAnalyzer getAnalyzer(Dump dump, String name) {
        switch (dump) {
            case JSON -> {
                return new JSONScrappingAnalyzer(name);
            }
            case MySQL -> {
                return new MySQLScrappingAnalyzer(name);
            }
        }
        throw new UnsupportedOperationException(Message.UNKNOWN_DUMP.toString());
    }

    static class JSONScrappingAnalyzer extends ScrappingAnalyzer {
        private final String PATH_TO_DUMP;

        public JSONScrappingAnalyzer(String name) {
            super(name);
            PATH_TO_DUMP = "./results/" + name + "/" + name + ".json";
        }
        @Override
        public void showCasesPerRegion() throws IOException {
            BufferedReader reader = Files.newBufferedReader(Path.of(PATH_TO_DUMP));
            List<CourtConfiguration> ccs = ConfigurationLoader.getCourtConfigurations();
            HashMap<Integer,Integer> checkMap = new HashMap<>();

            for (CourtConfiguration cc:ccs) {
                checkMap.put(cc.getRegion(),0);
            }

            ObjectMapper mapper = new ObjectMapper();
            while (reader.ready()) {
                String str = reader.readLine();
                if (str.equals("")) continue;
                Case _case = mapper.readValue(str,Case.class);
                checkMap.merge(_case.getRegion(),1,Integer::sum);
            }

            checkMap.keySet()
                    .stream()
                    .sorted(Comparator.comparing(checkMap::get).reversed()).toList()
                    .forEach(x -> SimpleLogger.println(String.format(Message.CASES_PER_REGION.toString(), x, checkMap.get(x))));

            reader.close();
        }

        @Override
        public void showRandomText() throws IOException {
            int stringCount = 0;

            if (Files.notExists(Path.of(PATH_TO_DUMP))) {
                BufferedReader reader = Files.newBufferedReader(Path.of(PATH_TO_DUMP));
                while (reader.ready()) {
                    reader.readLine();
                    stringCount = stringCount + 1;
                }
                reader.close();
            }

            else {
                HashMap<String, String> meta = getMeta();
                stringCount = Integer.parseInt(meta.get("string_count"));
            }

            int caseNumber = (int) (Math.random() * stringCount);
            BufferedReader reader = Files.newBufferedReader(Path.of(PATH_TO_DUMP));
            ObjectMapper mapper = new ObjectMapper();

            for (int i = 0; i < stringCount; i++) {
                Case _case = mapper.readValue(reader.readLine(),Case.class);
                if (_case.getText() != null) break;
                if (i == stringCount - 1) {
                    SimpleLogger.println(Message.NO_DECISION_TEXT);
                    return;
                }
            }

            for (int i = 0; i < caseNumber; i++) {
                reader.readLine();
            }

            Case _case = mapper.readValue(reader.readLine(),Case.class);

            while (_case.getText() == null) {
                if (!reader.ready()) {
                    reader.close();
                    reader = Files.newBufferedReader(Path.of(PATH_TO_DUMP));
                }
                _case = mapper.readValue(reader.readLine(), Case.class);
            }

            SimpleLogger.println(_case.getText());
        }

        @Override
        public void showTextNumber() throws IOException {
            BufferedReader reader = Files.newBufferedReader(Path.of(PATH_TO_DUMP));
            ObjectMapper mapper = new ObjectMapper();
            Case _case;
            int textNum = 0;
            int stringNum = 0;
            while (reader.ready()) {
                String str = reader.readLine();
                if (str.equals("")) continue;
                _case = mapper.readValue(str, Case.class);
                stringNum = stringNum + 1;
                if (_case.getText() != null) textNum = textNum + 1;
            }
            reader.close();
            SimpleLogger.println(String.format(Message.CASES_WITH_TEXTS.toString(), stringNum, textNum));
        }

        private HashMap<String,String> getMeta() throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            return (HashMap<String, String>) mapper.readValue(new FileReader("./results/"+name+"_meta.json"),HashMap.class);
        }
    }

    static class MySQLScrappingAnalyzer extends ScrappingAnalyzer {
        public MySQLScrappingAnalyzer(String name) {
            super(name);
        }

        @Override
        public void showCasesPerRegion() {

        }

        @Override
        public void showRandomText() {

        }

        @Override
        public void showTextNumber() {

        }
    }

    public void setServerConnectionInfo(String DB_URL, String user, String password) {
        if (!(analyzer instanceof MySQLScrappingAnalyzer)) throw new UnsupportedOperationException(Message.WRONG_DUMP.toString());
        ServerConnectionInfo.setDbUrl(DB_URL);
        ServerConnectionInfo.setUser(user);
        ServerConnectionInfo.setPassword(password);
    }
}
