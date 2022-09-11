package SUDRFScrapper.service;

public final class Constants {
    public static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36";
    public static final String PATH_TO_CONFIG = "./src/main/resources/config/config_sudrf.json";
    public static final String PATH_TO_CONFIG_BACKUP = "./src/main/resources/config/config_sudrf_backup.json";
    public static final String PATH_TO_CONFIG_BASE = "./src/main/resources/config/config_sudrf_base.json";
    public final static String PATH_TO_CAPTCHA = "./src/main/resources/captcha/captcha%d.properties";
    public static final String PATH_TO_LOGS = "./results/%s/%s_logs.txt";
    public static final String PATH_TO_COURT_HISTORY = "./src/main/resources/courts/%d.txt";
    public static final String PATH_TO_SUMMERY_INFO = "./src/main/resources/info/summery_info.txt";
    public static final String DB_Driver = "com.mysql.cj.jdbc.Driver";
    public static final String PATH_TO_PRECONFIG = "./src/main/resources/config/primary_config_sudrf.txt";
    public static final String PRECONFIG_URL_ENDING="/modules.php?name=sud#";
    public static final String PATH_TO_CRIMINAL_PROPERTIES = "./src/main/resources/searchpatterns/criminal.properties";
    public static final String PATH_TO_ADMIN_PROPERTIES = "./src/main/resources/searchpatterns/admin.properties";

}
