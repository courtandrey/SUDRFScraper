package enter;

import SUDRFScrapper.*;
import SUDRFScrapper.view.SimpleSwingView;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException {
       new SUDRFScraper(new SimpleSwingView());
    }
}
