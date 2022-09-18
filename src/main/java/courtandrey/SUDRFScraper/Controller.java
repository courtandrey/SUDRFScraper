package courtandrey.SUDRFScraper;

import courtandrey.SUDRFScraper.configuration.ConfigurationHolder;
import courtandrey.SUDRFScraper.configuration.dumpconfiguration.ServerConnectionInfo;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.searchrequest.Field;
import courtandrey.SUDRFScraper.configuration.searchrequest.SearchRequest;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.StrategyName;
import courtandrey.SUDRFScraper.dump.DBUpdater;
import courtandrey.SUDRFScraper.dump.JSONUpdater;
import courtandrey.SUDRFScraper.dump.Updater;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.dump.model.Dump;
import courtandrey.SUDRFScraper.exception.SearchRequestUnsetException;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue;
import courtandrey.SUDRFScraper.service.ConfigurationHelper;
import courtandrey.SUDRFScraper.service.ConfigurationLoader;
import courtandrey.SUDRFScraper.service.SeleniumHelper;
import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;
import courtandrey.SUDRFScraper.strategy.*;
import courtandrey.SUDRFScraper.view.Frame;
import courtandrey.SUDRFScraper.view.View;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class Controller {
    private Thread mainThread;
    private SearchRequest searchConfiguration;
    private static ConfigurationHolder configHolder = null;
    private final LocalDateTime startDate = LocalDateTime.now();
    private static Updater updater;
    private CountDownLatch countDownLatch;
    private Dump dump;
    private int courts;
    private int cases = 0;
    private final View view;
    private int[] selectedRegions = null;

    /**
     * Select regions which courts you want to scrap. Ignore for scrapping all regions.
     * @param regions regions to scrap.
     */
    @SuppressWarnings("unused")
    public void selectRegions(int... regions) {
        selectedRegions = regions;
    }

    public Controller(View view) {
        this.view = view;
        this.view.setController(this);
        view.showFrame(Frame.SET_DUMP);
    }

    public void prepareScrapper(String dumpName, Dump dump) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (!t.getName().contains("pool")) view.showFrameWithInfo(Frame.INFO, String.format(Message.EXCEPTION_OCCURRED.toString(),e));
        });

        SimpleLogger.initLogger(dumpName);

        this.dump = dump;

        setSearchConfiguration(SearchRequest.getInstance());

        try {
            configHolder = ConfigurationHolder.getInstance();
            prepareModel();

            if (dump == Dump.MySQL) {
                view.showFrame(Frame.SET_CONNECTION_INFO);
                updater = new DBUpdater(dumpName, this);
            }
            else if (dump == Dump.JSON) {
                updater = new JSONUpdater(dumpName, this);
                view.showFrame(Frame.SET_REQUEST);
            }
            else {
                view.showFrameWithInfo(Frame.ERROR,Message.UNKNOWN_DUMP.toString());
            }
        } catch (IOException e) {
            view.showFrameWithInfo(Frame.ERROR, String.format(Message.IOEXCEPTION_OCCURRED.toString(), e));
        }
    }

    public void errorOccurred(Throwable e, Thread t) {
        if (e instanceof IOException) {
            view.showFrameWithInfo(Frame.ERROR, String.format(Message.IOEXCEPTION_OCCURRED.toString(), e));
        }
        else if (e instanceof ClassNotFoundException) {
            view.showFrameWithInfo(Frame.ERROR, Message.DRIVER_NOT_FOUND.toString());
        }
        else if (e instanceof SQLException) {
            view.showFrameWithInfo(Frame.ERROR, String.format(Message.SQL_EXCEPTION_OCCURRED.toString(), e));
        }
        else {
            view.showFrameWithInfo(Frame.ERROR, String.format(Message.EXCEPTION_OCCURRED.toString(),e));
        }

        if (t != null && !t.getName().contains("pool")) {
            view.finish();
            if (mainThread != null) {
                mainThread.interrupt();
            }
        }
    }

    public void setServerConnectionInfo(String DB_URL, String user, String password) {
        if (dump != Dump.MySQL) throw new UnsupportedOperationException(Message.WRONG_DUMP.toString());
        ServerConnectionInfo.setDbUrl(DB_URL);
        ServerConnectionInfo.setUser(user);
        ServerConnectionInfo.setPassword(password);
    }

    private void reset(SearchRequest request) {
        java.lang.reflect.Field[] fields = request.getClass().getDeclaredFields();
        for (java.lang.reflect.Field f:fields) {
            try {
                f.setAccessible(true);
                if (!f.getName().equals("instance") && f.get(request) != null
                        && !f.getName().equals("field"))  {
                    f.set(request, null);
                } else if (f.getName().equals("field")) {
                    f.set(request, Field.CRIMINAL);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void setSearchConfiguration(SearchRequest searchConfiguration) {
        reset(searchConfiguration);
        this.searchConfiguration = searchConfiguration;
    }

    static class StrategyThreadPoolExecutor extends ThreadPoolExecutor {
        private final Controller controller;

        public StrategyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                          TimeUnit unit, BlockingQueue<Runnable> workQueue, Controller controller) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
            this.controller = controller;
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {

            SUDRFStrategy strategy = (SUDRFStrategy) r;

            controller.update(strategy.getResultCases());

            if (t == null && r instanceof Future<?> future) {
                try {
                    if (future.isDone())
                        future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    (new StrategyUEH()).handle(r, e.getCause());
                }
            } else if (t != null) {
                (new StrategyUEH()).handle(r, t);
            }
        }
    }

    private void prepareModel() throws IOException {
        if (!ConfigurationHelper.checkVnkods(configHolder.getCCs())) {
            configHolder.changeCCs();
        }
    }

    /**
     * @return SearchRequest object to manage.
     */
    public SearchRequest manageSearchRequest() {
        return searchConfiguration;
    }

    /**
     * Executes scrapping.
     * @param needToContinue false for the first execution, true for resuming a forcibly-ended execution or
     *                       scrapping courts that were inactive or had connection issues.
     *                       Note: if program has been ended forcibly it will resume from the place it stopped
     *                       scrapping not the place it stopped dumping info. You should consider certain data-loss in
     *                       that case.
     * @throws SearchRequestUnsetException if none of search request parameters was set.
     */
    public void executeScrapping(boolean needToContinue) throws SearchRequestUnsetException {
        view.showFrameWithInfo(Frame.INFO, Message.BEGINNING_OF_EXECUTION.toString());
        mainThread = Thread.currentThread();
        try {
            checkSearchConfiguration();
            if (!needToContinue) {
                ConfigurationHelper.reset(configHolder.getCCs());
                ConfigurationLoader.refresh(configHolder.getCCs());
            } else {
                ConfigurationHelper.analyzeIssues(configHolder.getCCs());
            }

            updater.start();

            scrap();

            updater.join();
        } catch (InterruptedException e) {
            errorOccurred(e, null);
        } finally {
            end();
        }
    }

    private void checkSearchConfiguration() throws SearchRequestUnsetException {
        if (!manageSearchRequest().checkFields()) {
            throw new SearchRequestUnsetException(Message.SEARCH_REQUEST_NOT_SET.toString());
        }
    }

    private void sumItUp() {
        LocalDateTime endDate = LocalDateTime.now();
        long executionTime = ChronoUnit.MINUTES.between(startDate,endDate);
        SimpleLogger.log(LoggingLevel.INFO,String.format(Message.EXECUTION_TIME.toString(),executionTime));

        updater.writeSummery(ConfigurationHelper.wrapIssues(configHolder.getCCs()));
    }

    private void end() {
        sumItUp();

        view.finish();

        SeleniumHelper.endSession();

        SimpleLogger.close();
    }

    private void continueScrapping() {
        ConfigurationHelper.analyzeIssues(configHolder.getCCs());
        execute(true);
    }

    private void scrap() {
        execute(false);
        continueScrapping();
        updater.registerEnding();
        view.showFrameWithInfo(Frame.INFO, Message.DUMP.toString());
    }

    private void execute(Boolean ignoreInactive) {
        List<CourtConfiguration> mainCCS = new ArrayList<>(configHolder.getCCs().stream().filter(x -> !x.isSingleStrategy() &&
                x.getStrategyName() != StrategyName.END_STRATEGY).toList());

        List<CourtConfiguration> singleCCS = new ArrayList<>(configHolder.getCCs().stream().filter(x -> x.getStrategyName()
                == StrategyName.CAPTCHA_STRATEGY).toList());
        singleCCS.addAll(configHolder.getCCs().stream()
                .filter(x -> x.isSingleStrategy() && x.getStrategyName() != StrategyName.CAPTCHA_STRATEGY).toList());

        if (ignoreInactive) {
            mainCCS = mainCCS.stream().filter(x -> x.getIssue() != Issue.INACTIVE_COURT
                    || x.getIssue() != Issue.INACTIVE_MODULE).toList();
            singleCCS = singleCCS.stream().filter(x -> x.getIssue() != Issue.INACTIVE_COURT
                    || x.getIssue() != Issue.INACTIVE_MODULE).toList();
        }

        if (selectedRegions != null) {
            mainCCS = mainCCS.stream().filter(x -> Arrays.stream(selectedRegions).anyMatch(r -> r == x.getRegion())).toList();
            singleCCS = singleCCS.stream().filter(x -> Arrays.stream(selectedRegions).anyMatch(r -> r == x.getRegion())).toList();
        }

        mainCCS = checkIfNothingToExecute(mainCCS);
        singleCCS = checkIfNothingToExecute(singleCCS);


        countDownLatch = new CountDownLatch(mainCCS.size() + singleCCS.size());

        ThreadPoolExecutor mainExecutor = new StrategyThreadPoolExecutor(4, 10,
                10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(mainCCS.size()), this);
        ThreadPoolExecutor seleniumExecutor = new StrategyThreadPoolExecutor(1, 1,
                10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(singleCCS.size()), this);

        courts = mainCCS.size() + singleCCS.size();

        for (CourtConfiguration cc : mainCCS) {
            mainExecutor.execute(this.selectStrategy(cc));
        }
        for (CourtConfiguration cc : singleCCS) {
            seleniumExecutor.execute(this.selectStrategy(cc));
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mainExecutor.shutdown();
        seleniumExecutor.shutdown();
    }

    protected synchronized void update(Collection<Case> cases) {
        countDownLatch.countDown();

        this.cases += cases.size();

        view.showFrameWithInfo(Frame.INFO, String.format(Message.RESULT.toString(),
                courts - countDownLatch.getCount(), courts, this.cases));

        refresh();

        if (cases.size() != 0) {
            updater.update(cases);
        }
    }

    private synchronized void refresh() {
        ConfigurationLoader.refresh(configHolder.getCCs());
    }


    private List<CourtConfiguration> checkIfNothingToExecute(List<CourtConfiguration> ccs) {
        if (ccs.size() == 0) {
            CourtConfiguration emptyCC = new CourtConfiguration();
            emptyCC.setStrategyName(StrategyName.END_STRATEGY);
            ccs = new ArrayList<>();
            ccs.add(emptyCC);
            return ccs;
        }
        return ccs;
    }

    private SUDRFStrategy selectStrategy(CourtConfiguration cc) {
        switch (cc.getStrategyName()) {

            case PRIMARY_STRATEGY -> {
                return new PrimaryStrategy(cc);
            }

            case CAPTCHA_STRATEGY -> {
                return new CaptchaStrategy(cc);
            }

            case END_STRATEGY -> {
                return new EndStrategy(cc);
            }

            default -> throw new IllegalArgumentException(Message.STRATEGY_NOT_CHOSEN.toString());
        }
    }

}
