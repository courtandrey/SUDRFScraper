package courtandrey.SUDRFScraper.view;

import courtandrey.SUDRFScraper.controller.Controller;

import java.awt.image.BufferedImage;

public interface View {
    String showCaptcha(BufferedImage image) throws InterruptedException;
    void finish();
    void showFrame(ViewFrame viewFrame);
    void showFrameWithInfo(ViewFrame viewFrame, String message);
    void setController(Controller controller);
}
