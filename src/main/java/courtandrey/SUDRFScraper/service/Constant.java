package courtandrey.SUDRFScraper.service;

public enum Constant {
    PATH_TO_APP_PROPERTIES,
    BASIC_RESULT_PATH ,
    PATH_TO_RESULT_DIRECTORY,
    PATH_TO_RESULT_JSON,
    PATH_TO_RESULT_META,
    PATH_TO_SUMMERY,
    UA,
    PATH_TO_CONFIG,
    PATH_TO_CONFIG_BACKUP,
    PATH_TO_CONFIG_BASE,
    PATH_TO_CAPTCHA,
    PATH_TO_LOGS,
    PATH_TO_COURT_HISTORY,
    PATH_TO_SUMMERY_INFO,
    DB_Driver,
    PATH_TO_PRECONFIG,
    PRECONFIG_URL_ENDING,
    PATH_TO_CRIMINAL_PROPERTIES,
    PATH_TO_ADMIN_PROPERTIES,
    PATH_TO_CAS_PROPERTIES;
    @Override
    public String toString() {
        return ConstantsGetter.getStringConstant(this);
    }
}
