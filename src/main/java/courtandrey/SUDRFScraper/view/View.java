package courtandrey.SUDRFScraper.view;

import courtandrey.SUDRFScraper.Controller;

import java.awt.image.BufferedImage;

public interface View {
    String showCaptcha(BufferedImage image) throws InterruptedException;
    void finish();
    void showFrame(Frame frame);
    void showFrameWithInfo(Frame frame, String message);
    void setController(Controller controller);
}
