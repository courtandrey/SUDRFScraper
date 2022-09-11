package SUDRFScrapper.service;

import java.awt.*;

public final class SystemHelper {
    private SystemHelper(){}
    public static void doBeeps() {
        Toolkit.getDefaultToolkit().beep();
        ThreadHelper.sleep(0.75);
        Toolkit.getDefaultToolkit().beep();
        ThreadHelper.sleep(0.75);
        Toolkit.getDefaultToolkit().beep();
    }
}
