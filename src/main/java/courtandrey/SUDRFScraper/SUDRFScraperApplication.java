package courtandrey.SUDRFScraper;

import courtandrey.SUDRFScraper.controller.Controller;
import courtandrey.SUDRFScraper.view.SimpleSwingView;

public class SUDRFScraperApplication {
    public static void main(String[] args) {
        Controller controller = new Controller(new SimpleSwingView());
        controller.initExecution();
    }
}
