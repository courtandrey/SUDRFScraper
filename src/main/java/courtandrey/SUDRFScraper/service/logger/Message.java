package courtandrey.SUDRFScraper.service.logger;

import courtandrey.SUDRFScraper.service.ConstantsGetter;

public enum Message {
    FILL_IN_CAPTCHA,
    RESULT,
    STRATEGY_NOT_CHOSEN,
    DUMP,
    SEARCH_REQUEST_NOT_SET,
    EXECUTION_TIME,
    WRONG_DUMP,
    NO_TEXT_FOUND,
    VNKOD_NOT_FOUND,
    EXECUTION_EXCEPTION_OCCURRED,
    EXCEPTION_OCCURRED_WHILE_PARSING,
    DOCUMENT_NOT_PARSED,
    VNKOD_MISSING,
    MANY_VNKODS,
    GO_TO_ANOTHER_PAGE,
    BEGINNING_OF_EXECUTION,
    CASES_WITH_TEXTS,
    NO_DECISION_TEXT,
    CASES_PER_REGION,
    UNKNOWN_DUMP,
    UNKNOWN_ARTICLE,
    INVALID_OUTPUT,
    CONNECTION_INFO_NOT_SET,
    IOEXCEPTION_OCCURRED,
    DRIVER_NOT_FOUND,
    SQL_EXCEPTION_OCCURRED,
    EXCEPTION_OCCURRED,
    WRONG_DATE_FORMAT,
    WRONG_ARTICLE_FORMAT,
    SUSPICIOUS_NUMBER_OF_CASES,
    SQL_CONNECTION_ERROR
    ;

    @Override
    public String toString() {
        return ConstantsGetter.getMessage(this);
    }

}
