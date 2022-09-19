package courtandrey.SUDRFScraper.controller;

public interface ErrorHandler {
    void errorOccurred(Throwable e, Thread t);
}
