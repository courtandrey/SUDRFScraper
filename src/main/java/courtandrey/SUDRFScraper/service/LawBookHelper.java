package courtandrey.SUDRFScraper.service;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static courtandrey.SUDRFScraper.service.Constant.PATH_TO_CAS_MOSGORSUD_LAWBOOK;

public final class LawBookHelper {
    private static final Properties propsCas = getProps();

    private static Properties getProps() {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(String.format(PATH_TO_CAS_MOSGORSUD_LAWBOOK.toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
    public static String getMosGorSudCodeCas(String number) {
        return propsCas.get(number).toString();
    }
    private LawBookHelper(){}
}
