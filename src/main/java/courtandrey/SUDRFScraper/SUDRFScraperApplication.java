package courtandrey.SUDRFScraper;

import courtandrey.SUDRFScraper.configuration.ApplicationConfiguration;
import courtandrey.SUDRFScraper.controller.Controller;
import courtandrey.SUDRFScraper.view.SimpleSwingView;

import java.util.ArrayList;
import java.util.stream.Stream;

public class SUDRFScraperApplication {
    public static void main(String[] args) {
        ApplicationConfiguration.getInstance();
        Controller controller = new Controller(new SimpleSwingView());
        controller.initExecution();
    }
}