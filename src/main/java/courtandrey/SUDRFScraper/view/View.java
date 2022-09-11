package courtandrey.SUDRFScraper.view;

import courtandrey.SUDRFScraper.SUDRFScraper;

public interface View {
    void finish();
    void showFrame(Frame frame);
    void showFrameWithInfo(Frame frame, String message);
    void setController(SUDRFScraper scrapper);
}
