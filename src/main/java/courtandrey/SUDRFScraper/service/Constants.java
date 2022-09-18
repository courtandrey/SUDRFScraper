package courtandrey.SUDRFScraper.service;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
    public final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
    public final String PATH_TO_CONFIG = "./src/main/resources/config/config_sudrf.json";
    public final String PATH_TO_CONFIG_BACKUP = "./src/main/resources/config/config_sudrf_backup.json";
    public final String PATH_TO_CONFIG_BASE = "./src/main/resources/config/config_sudrf_base.json";
    public final String PATH_TO_CAPTCHA = "./src/main/resources/captcha/captcha%d.properties";
    public final String PATH_TO_LOGS = "./results/%s/%s_logs.log";
    public final String PATH_TO_COURT_HISTORY = "./src/main/resources/courts/%d.txt";
    public final String PATH_TO_SUMMERY_INFO = "./src/main/resources/info/summery_info.txt";
    public final String DB_Driver = "com.mysql.cj.jdbc.Driver";
    public final String PATH_TO_PRECONFIG = "./src/main/resources/config/primary_config_sudrf.txt";
    public final String PRECONFIG_URL_ENDING="/modules.php?name=sud#";
    public final String PATH_TO_CRIMINAL_PROPERTIES = "./src/main/resources/searchpatterns/criminal.properties";
    public final String PATH_TO_ADMIN_PROPERTIES = "./src/main/resources/searchpatterns/admin.properties";
    public final String PATH_TO_CAS_PROPERTIES = "./src/main/resources/searchpatterns/CAS.properties";

}
