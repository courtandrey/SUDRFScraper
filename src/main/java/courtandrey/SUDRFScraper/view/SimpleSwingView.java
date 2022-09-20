package courtandrey.SUDRFScraper.view;

import courtandrey.SUDRFScraper.configuration.dumpconfiguration.ServerConnectionInfo;
import courtandrey.SUDRFScraper.controller.Controller;
import courtandrey.SUDRFScraper.configuration.searchrequest.Field;
import courtandrey.SUDRFScraper.configuration.searchrequest.article.AdminArticle;
import courtandrey.SUDRFScraper.configuration.searchrequest.article.CASArticle;
import courtandrey.SUDRFScraper.configuration.searchrequest.article.CriminalArticle;
import courtandrey.SUDRFScraper.dump.model.Dump;
import courtandrey.SUDRFScraper.service.SystemHelper;
import courtandrey.SUDRFScraper.service.ThreadHelper;
import courtandrey.SUDRFScraper.service.logger.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleSwingView implements View {
    private Controller controller;
    private static boolean alive = true;
    private JFrame info;
    private final static ArrayList<JFrame> frames = new ArrayList<>();
    public SimpleSwingView() {}

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void showFrame(ViewFrame viewFrame) {
        switch (viewFrame) {
            case SET_DUMP -> showDumpFrame();
            case SET_REQUEST -> showRequestFrame();
            case SET_CONNECTION_INFO -> showSetConnectionInfo();
            default -> throw new UnsupportedOperationException("Unknown frame type");
        }
    }

    @Override
    public synchronized void showFrameWithInfo(ViewFrame viewFrame, String message) {
        switch (viewFrame) {
            case INFO -> update(message);
            case ERROR -> showError(null, message);
            default -> throw new UnsupportedOperationException("Unknown frame type");
        }
    }

    private void update(String message) {
        if (info == null) {
            showInfo(message);
        } else {
            updateInfo(message);
        }
    }

    private void updateInfo(String message) {
        info.getContentPane().removeAll();
        info.getContentPane().add(new Label(message));
        info.setLocationRelativeTo(null);
        info.pack();
        info.revalidate();
        info.repaint();
    }

    private void showRequestFrame() {
        if (!alive) return;
        JFrame mainFrame = new JFrame("Select request");
        frames.add(mainFrame);
        JPanel frame = new JPanel();
        frame.setLayout(new BoxLayout(frame, BoxLayout.Y_AXIS));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800,800);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("NOTE: You should select at least one request option", JLabel.LEFT);
        infoPanel.add(label);
        frame.add(infoPanel);

        JLabel label1 = new JLabel("Result date starting from: ");

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(label1);
        panel.add(new JLabel("Year: "));
        JTextField yearResultDateFrom = new JTextField(4);
        panel.add(yearResultDateFrom);
        panel.add(new JLabel(" Month: "));
        JTextField monthResultDateFrom = new JTextField(2);
        panel.add(monthResultDateFrom);
        panel.add((new JLabel(" Day: ")));
        JTextField dayResultDateFrom = new JTextField(2);
        panel.add(dayResultDateFrom);

        frame.add(panel);

        JLabel label2 = new JLabel("Result date ending till: ");

        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel2.add(label2);
        panel2.add(new JLabel("Year: "));
        JTextField yearResultDateTill = new JTextField(4);
        panel2.add(yearResultDateTill);
        panel2.add(new JLabel(" Month: "));
        JTextField monthResultDateTill = new JTextField(2);
        panel2.add(monthResultDateTill);
        panel2.add((new JLabel(" Day: ")));
        JTextField dayResultDateTill = new JTextField(2);
        panel2.add(dayResultDateTill);

        frame.add(panel2);

        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label3 = new JLabel("Text to search within acts: ");
        textPanel.add(label3);
        JTextField text = new JTextField(20);
        textPanel.add(text);
        frame.add(textPanel);

        JLabel label4 = new JLabel("Article: ");

        JPanel articlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        articlePanel.add(label4);
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Select type of article");

        JMenuItem i1 = new JMenuItem("Administrative article");
        JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel3.add(new JLabel("Part of article: "));
        JTextField adminArticle = new JTextField(20);
        panel3.add(adminArticle);

        JMenuItem i2 = new JMenuItem("Administrative offense article");
        JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel4.add(new JLabel("Chapter: "));
        JTextField adminOffenseChapter = new JTextField(2);
        panel4.add(adminOffenseChapter);
        panel4.add(new JLabel(" Article: "));
        JTextField adminOffenseArticle = new JTextField(2);
        panel4.add(adminOffenseArticle);
        panel4.add((new JLabel(" SubArticle: ")));
        JTextField adminOffenseSubArticle = new JTextField(2);
        panel4.add(adminOffenseSubArticle);
        panel4.add((new JLabel(" Part: ")));
        JTextField adminOffensePart = new JTextField(2);
        panel4.add(adminOffensePart);
        panel4.add((new JLabel(" SubPart: ")));
        JTextField adminOffenseSubPart = new JTextField(2);
        panel4.add(adminOffenseSubPart);

        JMenuItem i3 = new JMenuItem("Criminal article");
        JPanel panel5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel5.add(new JLabel(" Article: "));
        JTextField criminalArticle = new JTextField(3);
        panel5.add(criminalArticle);
        panel5.add((new JLabel(" SubArticle: ")));
        JTextField criminalSubArticle = new JTextField(2);
        panel5.add(criminalSubArticle);
        panel5.add((new JLabel(" Part: ")));
        JTextField criminalPart = new JTextField(2);
        panel5.add(criminalPart);
        panel5.add((new JLabel(" Letter: ")));
        JTextField criminalLetter = new JTextField(1);
        panel5.add(criminalLetter);

        JButton button = new JButton("Confirm");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        i1.addActionListener(e -> {
            menu.setText(i1.getText());
            Component[] c = frame.getComponents();
            if (Arrays.stream(c).anyMatch(x -> x == panel4)) {
                frame.remove(panel4);
                clear(panel4);
            }
            if (Arrays.stream(c).anyMatch(x -> x == panel5)) {
                frame.remove(panel5);
                clear(panel5);
            }
            frame.remove(button);
            frame.add(panel3);
            frame.add(button);
            frame.revalidate();
            frame.repaint();
            mainFrame.pack();
        });

        i2.addActionListener(e -> {
            menu.setText(i2.getText());
            Component[] c = frame.getComponents();
            if (Arrays.stream(c).anyMatch(x -> x == panel3)) {
                frame.remove(panel3);
                clear(panel3);
            }
            if (Arrays.stream(c).anyMatch(x -> x == panel5)) {
                frame.remove(panel5);
                clear(panel5);
            }
            frame.remove(button);
            frame.add(panel4);
            frame.add(button);
            frame.revalidate();
            frame.repaint();
            mainFrame.pack();
        });

        i3.addActionListener(e -> {
            menu.setText(i3.getText());
            Component[] c = frame.getComponents();
            if (Arrays.stream(c).anyMatch(x -> x == panel3)) {
                frame.remove(panel3);
                clear(panel3);
            }
            if (Arrays.stream(c).anyMatch(x -> x == panel4)) {
                frame.remove(panel4);
                clear(panel4);
            }
            frame.remove(button);
            frame.add(panel5);
            frame.add(button);
            frame.revalidate();
            frame.repaint();
            mainFrame.pack();
        });

        menu.add(i1);
        menu.add(i2);
        menu.add(i3);
        bar.add(menu);
        articlePanel.add(bar);
        frame.add(articlePanel);
        button.addActionListener(e -> {
            int requestPart = 0;

            if (yearResultDateFrom.getText().strip().length() > 0 && monthResultDateFrom.getText().strip().length() > 0
                        && dayResultDateFrom.getText().strip().length() > 0) {
                try {
                    int year = Integer.parseInt(yearResultDateFrom.getText().strip());
                    int month = Integer.parseInt(monthResultDateFrom.getText().strip());
                    int day = Integer.parseInt(dayResultDateFrom.getText().strip());
                    if (year <= 0) throw new IllegalArgumentException();
                    if (month <= 0 || month > 12) throw new IllegalArgumentException();
                    if (day <= 0 || day > 31) throw new IllegalArgumentException();
                    controller.manageSearchRequest().setResultDateFrom(LocalDate.of(year, month, day));
                    requestPart++;
                } catch (Exception ex) {
                    showError(mainFrame, Message.WRONG_DATE_FORMAT.toString());
                    return;
                }
            }

            if (yearResultDateTill.getText().strip().length() > 0 && monthResultDateTill.getText().strip().length() > 0
                    && dayResultDateTill.getText().strip().length() > 0) {
                try {
                    int year = Integer.parseInt(yearResultDateTill.getText().strip());
                    int month = Integer.parseInt(monthResultDateTill.getText().strip());
                    int day = Integer.parseInt(dayResultDateTill.getText().strip());
                    if (year <= 0) throw new IllegalArgumentException();
                    if (month <= 0 || month > 12) throw new IllegalArgumentException();
                    if (day <= 0 || day > 31) throw new IllegalArgumentException();
                    controller.manageSearchRequest().setResultDateTill(LocalDate.of(year, month, day));
                    requestPart++;
                } catch (Exception ex) {
                    showError(mainFrame, Message.WRONG_DATE_FORMAT.toString());
                    return;
                }
            }
            try {
                if (adminArticle.getText().strip().length() > 0) {
                    controller.manageSearchRequest().setArticle(new CASArticle(adminArticle.getText().strip()));
                    requestPart++;
                } else if (adminOffenseChapter.getText().strip().length() > 0
                        && adminOffenseArticle.getText().strip().length() > 0) {
                    int chapter = Integer.parseInt(adminOffenseChapter.getText().strip());
                    int article = Integer.parseInt(adminOffenseArticle.getText().strip());
                    int subArticle = 0;
                    int part = 0;
                    int subPart = 0;
                    if (adminOffenseSubArticle.getText().strip().length() > 0) {
                        subArticle = Integer.parseInt(adminOffenseSubArticle.getText().strip());
                    }
                    if (adminOffensePart.getText().strip().length() > 0) {
                        part = Integer.parseInt(adminOffenseChapter.getText().strip());
                    }
                    if (adminOffenseSubPart.getText().strip().length() > 0 ) {
                        subPart = Integer.parseInt(adminOffenseSubPart.getText().strip());
                    }

                    if (subArticle > 0 && part > 0 && subPart > 0) {
                        controller.manageSearchRequest().setArticle(new AdminArticle(chapter, article, subArticle, part, subPart));
                    }
                    else if (subArticle > 0 && part > 0) {
                        controller.manageSearchRequest().setArticle(new AdminArticle(chapter, article, subArticle, part, true));
                    }
                    else if (subArticle > 0) {
                        controller.manageSearchRequest().setArticle(new AdminArticle(chapter, article, subArticle, true));
                    }
                    else if (part > 0 && subPart > 0) {
                        controller.manageSearchRequest().setArticle(new AdminArticle(chapter, article, part, subPart, false));
                    }
                    else if (part > 0) {
                        controller.manageSearchRequest().setArticle(new AdminArticle(chapter, article, part, false));
                    }
                    else if (subPart == 0) {
                        controller.manageSearchRequest().setArticle(new AdminArticle(chapter, article));
                    }
                    else {
                        throw new IllegalArgumentException();
                    }
                    ++requestPart;
                } else if (criminalArticle.getText().strip().length() > 0) {
                    int article = Integer.parseInt(criminalArticle.getText().strip());
                    int subArticle = 0;
                    int part = 0;
                    char letter = 0;
                    if (criminalSubArticle.getText().strip().length() > 0) {
                        subArticle = Integer.parseInt(criminalSubArticle.getText().strip());
                    }
                    if (criminalPart.getText().strip().length() > 0) {
                        part = Integer.parseInt(criminalPart.getText().strip());
                    }
                    if (criminalLetter.getText().strip().length() > 0) {
                        if (criminalLetter.getText().strip().length() > 1) throw new IllegalArgumentException();
                        letter = criminalLetter.getText().strip().charAt(0);
                    }
                    if (subArticle > 0 && part > 0 && letter > 0) {
                        controller.manageSearchRequest().setArticle(new CriminalArticle(article, subArticle, part, letter));
                    }
                    else if (subArticle > 0 && part > 0) {
                        controller.manageSearchRequest().setArticle(new CriminalArticle(article, subArticle, part));
                    }
                    else if (subArticle > 0) {
                        controller.manageSearchRequest().setArticle(new CriminalArticle(article, subArticle, true));
                    }
                    else if (part > 0 && letter > 0) {
                        controller.manageSearchRequest().setArticle(new CriminalArticle(article, part, letter));
                    }
                    else if (part > 0) {
                        controller.manageSearchRequest().setArticle(new CriminalArticle(article,part));
                    }
                    else if (letter == 0) {
                        controller.manageSearchRequest().setArticle(new CriminalArticle(article));
                    }
                    else {
                        throw new IllegalArgumentException();
                    }
                    ++requestPart;
                }
            } catch (Exception ex) {
                showError(mainFrame, Message.WRONG_ARTICLE_FORMAT.toString());
                return;
            }

            if (Arrays.stream(frame.getComponents()).anyMatch(x -> x == panel3 || x == panel4 || x == panel5)) {
                if (Arrays.stream(frame.getComponents()).anyMatch(x -> x == panel3)) {
                    controller.manageSearchRequest().setField(Field.CAS);
                }
                else if (Arrays.stream(frame.getComponents()).anyMatch(x -> x == panel4)) {
                    controller.manageSearchRequest().setField(Field.ADMIN);
                }
                else if (Arrays.stream(frame.getComponents()).anyMatch(x -> x == panel5)) {
                    controller.manageSearchRequest().setField(Field.CRIMINAL);
                }
            }

            if (text.getText().strip().length() > 0) {
                controller.manageSearchRequest().setText(text.getText().strip());
                ++requestPart;
            }

            if (requestPart == 0) {
                showError(mainFrame, Message.SEARCH_REQUEST_NOT_SET.toString());
            } else {
                mainFrame.dispose();
                (new Thread(() -> controller.executeScrapping(false))).start();
            }
        });

        frame.add(button);
        mainFrame.add(frame);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private void clear(Container c) {
        for (Component comp:c.getComponents()) {
            if (comp instanceof JTextField) {
                ((JTextField) comp).setText("");
            }
        }
    }

    private void showDumpFrame() {
        if (!alive) return;
        AtomicReference<Dump> dump = new AtomicReference<>();
        JFrame frame = new JFrame("Select dump");
        frames.add(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,400);

        JPanel panel = new JPanel(new GridBagLayout());
        frame.add(panel,BorderLayout.WEST);
        JLabel label = new JLabel("Type output name: ");
        panel.add(label);
        JTextField textPane = new JTextField(10);
        panel.add(textPane);

        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Choose dump type");
        JMenuItem i1 = new JMenuItem("MySQL");
        JMenuItem i2 = new JMenuItem("JSON");

        i1.addActionListener(e -> {
            menu.setText(i1.getText());
            dump.set(Dump.MySQL);
        });
        i2.addActionListener(e -> {
            menu.setText(i2.getText());
            dump.set(Dump.JSON);
        });

        menu.add(i1);
        menu.add(i2);
        bar.add(menu);
        frame.getContentPane().add(bar, BorderLayout.EAST);

        JButton button = new JButton("Confirm");
        button.addActionListener(e -> {
            if (dump.get() != null && textPane.getText().length() > 0) {
                frame.dispose();
                frames.remove(frame);
                controller.prepareScrapper(textPane.getText(), dump.get());
            } else {
                showError(frame, Message.OUTPUT_NOT_SET.toString());
            }
        });
        frame.getContentPane().add(button, BorderLayout.PAGE_END);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void finish() {
        alive = false;
        for (JFrame frame:frames) {
            frame.dispose();
        }
    }

    @Override
    public String showCaptcha(BufferedImage image) throws InterruptedException {
        if (!alive) return null;

        JFrame captchaFrame = new JFrame("CAPTCHA");
        frames.add(captchaFrame);
        captchaFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        captchaFrame.setSize(250,150);

        JPanel main = new JPanel();
        main.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel label = new JLabel(Message.FILL_IN_CAPTCHA.toString(), JLabel.CENTER);

        JLabel picLabel = new JLabel(new ImageIcon(image), JLabel.CENTER);
        picLabel.setBounds(new Rectangle(100,30));
        main.add(label);
        main.add(picLabel);

        JTextField textField = new JTextField(5);

        CountDownLatch latch = new CountDownLatch(1);

        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (textField.getText().strip().length() == 5) {
                    latch.countDown();
                }
            }
        });

        main.add(textField);
        captchaFrame.getContentPane().add(main);
        captchaFrame.setLocationRelativeTo(null);
        captchaFrame.pack();
        captchaFrame.setVisible(true);

        SystemHelper.doBeeps();

        latch.await();

        ThreadHelper.sleep(4);

        frames.remove(captchaFrame);

        captchaFrame.dispose();

        return textField.getText().strip();

    }

    private void showInfo(String message) {
        info = new JFrame("INFO");
        frames.add(info);
        info.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        info.setSize(250,150);
        JLabel label = new JLabel(message);
        info.getContentPane().add(label,BorderLayout.CENTER);
        info.pack();
        info.setLocationRelativeTo(null);
        info.setVisible(true);
    }

    private void showError(JFrame frame, String message) {
        JFrame errorFrame = new JFrame("ERROR");
        errorFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        errorFrame.setSize(250,150);
        JLabel label = new JLabel(message);
        errorFrame.getContentPane().add(label,BorderLayout.CENTER);
        JButton button = new JButton("OK");
        button.addActionListener(e -> {
            errorFrame.dispose();
            if (frame == null) {
                System.exit(0);
            }
        });
        errorFrame.getContentPane().add(button, BorderLayout.PAGE_END);
        errorFrame.pack();
        errorFrame.setLocationRelativeTo(frame);
        errorFrame.setVisible(true);
    }

    private void showSetConnectionInfo() {
        if (!alive) return;
        JFrame frame = new JFrame("Set Connection Info");
        frames.add(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,300);

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1,BoxLayout.Y_AXIS));
        frame.add(panel1,BorderLayout.WEST);
        JTextField field1 = new JTextField(15);
        field1.setText(ServerConnectionInfo.getDbUrl());
        JLabel label1 = new JLabel("Type DB URL: ");
        panel1.add(label1);
        panel1.add(field1);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        frame.add(panel2,BorderLayout.CENTER);
        JTextField field2 = new JTextField(15);
        field2.setText(ServerConnectionInfo.getUser());
        JLabel label2 = new JLabel("Type username: ");
        panel2.add(label2);
        panel2.add(field2);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        frame.add(panel3,BorderLayout.EAST);
        JTextField field3 = new JTextField(15);
        field3.setText(ServerConnectionInfo.getPassword());
        JLabel label3 = new JLabel("Type password: ");
        panel3.add(label3);
        panel3.add(field3);

        JButton button = new JButton("Confirm");

        button.addActionListener(e -> {
            try {
                if (field1.getText().length() > 0 && field2.getText().length() > 0 && field3.getText().length() > 0) {
                    controller.setServerConnectionInfo(field1.getText(), field2.getText(), field3.getText());
                    frames.remove(frame);
                    frame.dispose();
                    showFrame(ViewFrame.SET_REQUEST);
                } else {
                    showError(frame, Message.CONNECTION_INFO_NOT_SET.toString());
                }
            }
            catch (SQLException ex) {
                showError(frame, Message.SQL_CONNECTION_ERROR.toString());
            }
        });

        frame.getContentPane().add(button, BorderLayout.PAGE_END);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}