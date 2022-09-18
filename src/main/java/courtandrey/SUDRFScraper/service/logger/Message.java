package courtandrey.SUDRFScraper.service.logger;

public enum Message {
    FILL_IN_CAPTCHA("YOU SHOULD FILL IN THE CAPTCHA FORM"),
    RESULT("%d/%d courts done. %d cases matching request found"),
    STRATEGY_NOT_CHOSEN("Strategy could not be chosen"),
    DUMP("Dumping cases..."),
    SEARCH_REQUEST_NOT_SET("You should set at least one parameter of search request"),
    EXECUTION_TIME("Execution time: %d minutes"),
    WRONG_DUMP("Wrong dump type"),
    NO_TEXT_FOUND("No text was found in court: "),
    VNKOD_NOT_FOUND("Couldn't set vnkod for court: "),
    EXECUTION_EXCEPTION_OCCURRED("%s occurred during execution of "),
    EXCEPTION_OCCURRED_WHILE_PARSING("%s occurred during parsing of "),
    DOCUMENT_NOT_PARSED("Document wasn't parsed: "),
    VNKOD_MISSING("Vnkod is missing "),
    MANY_VNKODS("More than one vnkod found: "),
    GO_TO_ANOTHER_PAGE("CHANGE PAGE"),
    BEGINNING_OF_EXECUTION("BEGINNING OF EXECUTION"),
    CASES_WITH_TEXTS("All cases: %d \nCases with decision text: %d"),
    NO_DECISION_TEXT("There is no case with decision text"),
    CASES_PER_REGION("Region %d: %d cases"),
    UNKNOWN_DUMP("Unknown dump type"),
    UNKNOWN_ARTICLE("Unknown article type"),
    OUTPUT_NOT_SET("You must choose output parameters"),
    CONNECTION_INFO_NOT_SET("You must type info to make a SQL connection"),
    IOEXCEPTION_OCCURRED("IOException occurred: %s"),
    DRIVER_NOT_FOUND("SQL Driver not found"),
    SQL_EXCEPTION_OCCURRED("SQLException occurred: %s"),
    EXCEPTION_OCCURRED("Exception occurred: %s"),
    WRONG_DATE_FORMAT("Format of date is wrong.\nExample of correct format: 2022 2 24"),
    WRONG_ARTICLE_FORMAT("Format of article is wrong"),
    SUSPICIOUS_NUMBER_OF_CASES("Suspicious number of cases: ")
    ;

    Message(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

    final String message;
}
