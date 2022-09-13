package courtandrey.SUDRFScraper.view;

import courtandrey.SUDRFScraper.Controller;

public interface View {
    void finish();
    void showFrame(Frame frame);
    void showFrameWithInfo(Frame frame, String message);
    void setController(Controller controller);
}
