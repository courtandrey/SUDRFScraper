package courtandrey.SUDRFScraper.controller;

import courtandrey.SUDRFScraper.configuration.ApplicationConfiguration;
import courtandrey.SUDRFScraper.configuration.ConfigurationHolder;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Level;
import courtandrey.SUDRFScraper.configuration.dumpconfiguration.ServerConnectionInfo;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.CourtConfiguration;
import courtandrey.SUDRFScraper.configuration.searchrequest.Field;
import courtandrey.SUDRFScraper.configuration.searchrequest.SearchRequest;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.StrategyName;
import courtandrey.SUDRFScraper.dump.DBUpdaterService;
import courtandrey.SUDRFScraper.dump.JSONUpdaterService;
import courtandrey.SUDRFScraper.dump.Updater;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.dump.model.Dump;
import courtandrey.SUDRFScraper.exception.LevelParsingException;
import courtandrey.SUDRFScraper.exception.SearchRequestUnsetException;
import courtandrey.SUDRFScraper.configuration.courtconfiguration.Issue;
import courtandrey.SUDRFScraper.service.*;
import courtandrey.SUDRFScraper.service.logger.LoggingLevel;
import courtandrey.SUDRFScraper.service.logger.Message;
import courtandrey.SUDRFScraper.service.logger.SimpleLogger;
import courtandrey.SUDRFScraper.strategy.*;
import courtandrey.SUDRFScraper.view.ViewFrame;
import courtandrey.SUDRFScraper.view.View;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

public class Controller {
    private Thread mainThread;
    private SearchRequest searchConfiguration;
    private static ConfigurationHolder configHolder = null;
    private final LocalDateTime startDate = LocalDateTime.now();
    private static Updater updaterService;
    private CountDownLatch countDownLatch;
    private Dump dump;
    private int courts;
    private int cases = 0;
    private final View view;
    private final SUDRFErrorHandler handler;
    private int[] selectedRegions = null;
    private Level[] levels = null;

    private StrategyName[] strategyNames = null;
    /**
     * Select regions which courts you want to scrap. Ignore for scrapping all regions.
     * @param regions regions to scrap.
     */
    @SuppressWarnings("unused")
    public void selectRegions(int... regions) {
        selectedRegions = regions;
    }

    class SUDRFErrorHandler implements ErrorHandler {
        public synchronized void errorOccurred(Throwable e, Thread t) {
            if (e instanceof IOException) {
                view.showFrameWithInfo(ViewFrame.ERROR, String.format(Message.IOEXCEPTION_OCCURRED.toString(), e));
            }
            else if (e instanceof ClassNotFoundException) {
                view.showFrameWithInfo(ViewFrame.ERROR, Message.DRIVER_NOT_FOUND.toString());
            }
            else if (e instanceof SQLException) {
                view.showFrameWithInfo(ViewFrame.ERROR, String.format(Message.SQL_EXCEPTION_OCCURRED.toString(), e));
            }
            else if (e instanceof SearchRequestUnsetException && e.getMessage().equals(Message.UNKNOWN_DUMP.toString())) {
                view.showFrameWithInfo(ViewFrame.ERROR, String.format(e.getMessage()));
            }
            else {
                view.showFrameWithInfo(ViewFrame.ERROR, String.format(Message.EXCEPTION_OCCURRED.toString(),e));
            }

            if (t == null || !t.getName().contains("pool")) {
                view.finish();
                if (mainThread != null) {
                    mainThread.interrupt();
                }
            }
        }
    }

    public Controller(View view) {
        this.view = view;
        this.view.setController(this);
        CaptchaPropertiesConfigurator.setView(view);
        handler = new SUDRFErrorHandler();
    }

    public void initExecution() {
        view.showFrame(ViewFrame.SET_DUMP);
    }

    private int[] extractSelectedRegions() {
        String regionString = ApplicationConfiguration.getInstance().getProperty("basic.regions");
        if (regionString.isEmpty()) return  null;
        String[] regionsString = regionString.split(",");
        int[] regions = new int[regionsString.length];
        try {
            for (int i = 0; i < regionsString.length; i++) {
                regions[i] = Integer.parseInt(regionsString[i]);
            }
        } catch (Exception e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.WRONG_REGIONS_FORMAT);
            return null;
        }
        return regions;
    }

    public void prepareScrapper(String dumpName, Dump dump) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (!t.getName().contains("pool")) handler.errorOccurred(e, null);
        });
        ConfigurationLoader.setDumpName(dumpName);

        SimpleLogger.initLogger(dumpName);

        this.dump = dump;

        setSearchConfiguration(SearchRequest.getInstance());

        selectedRegions = extractSelectedRegions();
        levels = extractSelectedLevels();
        strategyNames = extractStrategies();

        try {
            configHolder = ConfigurationHolder.getInstance();
            prepareModel();

            if (dump == Dump.MySQL) {
                view.showFrame(ViewFrame.SET_CONNECTION_INFO);
                updaterService = new DBUpdaterService(dumpName, new SUDRFErrorHandler());
            }
            else if (dump == Dump.JSON) {
                updaterService = new JSONUpdaterService(dumpName, new SUDRFErrorHandler());
                view.showFrame(ViewFrame.SET_REQUEST);
            }
            else {
                handler.errorOccurred(new SearchRequestUnsetException(Message.UNKNOWN_DUMP.toString()), null);
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            handler.errorOccurred(e, null);
        }
    }

    private StrategyName[] extractStrategies() {
        String regionString = ApplicationConfiguration.getInstance().getProperty("dev.strategies");
        if (regionString == null || regionString.isEmpty()) return null;
        String[] regionsString = regionString.split(",");
        StrategyName[] regions = new StrategyName[regionsString.length];
        try {
            for (int i = 0; i < regionsString.length; i++) {
                regions[i] = StrategyName.parseStrategy(regionsString[i]);
            }
        } catch (Exception e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.WRONG_STRATEGY_FORMAT);
            return null;
        }
        return regions;
    }

    private Level[] extractSelectedLevels() {
        String regionString = ApplicationConfiguration.getInstance().getProperty("basic.levels");
        if (regionString.isEmpty()) return  null;
        String[] regionsString = regionString.split(",");
        Level[] regions = new Level[regionsString.length];
        try {
            for (int i = 0; i < regionsString.length; i++) {
                regions[i] = Level.parseLevel(regionsString[i]);
            }
        } catch (LevelParsingException e) {
            SimpleLogger.log(LoggingLevel.WARNING, Message.WRONG_LEVEL_FORMAT);
            return null;
        }
        return regions;
    }

    public void setServerConnectionInfo(String DB_URL, String user, String password) throws SQLException {
        if (dump != Dump.MySQL) throw new UnsupportedOperationException(Message.WRONG_DUMP.toString());
        ServerConnectionInfo info = ServerConnectionInfo.getInstance();
        info.setDbUrl(DB_URL);
        info.setUser(user);
        info.setPassword(password);
        info.testConnection();
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
            if (strategy.getCc().getIssue() != null) {
                SimpleLogger.log(LoggingLevel.INFO, String.format(Message.EXECUTION_STATUS_END.toString(),
                        strategy.getCc().getName(), strategy.getCc().getIssue()));
            }

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
        Runtime.getRuntime().addShutdownHook(new Thread(this::end));
        view.showFrameWithInfo(ViewFrame.INFO, Message.BEGINNING_OF_EXECUTION.toString());
        mainThread = Thread.currentThread();
        try {
            checkSearchConfiguration();
            if (!needToContinue) {
                ConfigurationHelper.reset(configHolder.getCCs());
                ConfigurationLoader.refresh(configHolder.getCCs());
            } else {
                ConfigurationHelper.analyzeIssues(configHolder.getCCs());
            }

            updaterService.startService();

            scrap();
        } catch (InterruptedException e) {
            handler.errorOccurred(e, null);
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

        updaterService.writeSummery(ConfigurationHelper.wrapIssues(configHolder.getCCs()));
    }

    boolean isEnded = false;

    private void end() {
        if (isEnded) return;
        updaterService.addMeta();
        try {
            updaterService.joinService();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ConfigurationLoader.storeConfiguration(configHolder.getCCs());
        sumItUp();

        view.finish();

        ThreadHelper.sleep(5);

        SeleniumHelper.endSession();

        try {
            SimpleLogger.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        isEnded = true;
    }

    private void continueScrapping() throws InterruptedException {
        ConfigurationHelper.analyzeIssues(configHolder.getCCs());
        execute(true);
    }

    private void scrap() throws InterruptedException {
        execute(false);
        continueScrapping();
        view.showFrameWithInfo(ViewFrame.INFO, Message.DUMP.toString());
    }

    private void execute(Boolean ignoreInactive) throws InterruptedException {
            List<CourtConfiguration> mainCCS = new ArrayList<>(configHolder.getCCs().stream().filter(x -> !x.isSingleStrategy() &&
                    x.getStrategyName() != StrategyName.END_STRATEGY).toList());

            List<CourtConfiguration> singleCCS = new ArrayList<>(configHolder.getCCs().stream().filter(x -> x.getStrategyName()
                    == StrategyName.CAPTCHA_STRATEGY).toList());

            List<CourtConfiguration> mosgorsud = new ArrayList<>(configHolder.getCCs().stream().filter(x -> x.getStrategyName()
                    == StrategyName.MOSGORSUD_STRATEGY).toList());

            singleCCS.addAll(configHolder.getCCs().stream()
                    .filter(x -> x.isSingleStrategy() && x.getStrategyName() != StrategyName.CAPTCHA_STRATEGY
                    && x.getStrategyName() != StrategyName.END_STRATEGY && x.getStrategyName() != StrategyName.MOSGORSUD_STRATEGY).toList());

            if (ignoreInactive) {
                mainCCS = mainCCS.stream().filter(x -> x.getIssue() != Issue.INACTIVE_COURT
                        && x.getIssue() != Issue.INACTIVE_MODULE).toList();
                singleCCS = singleCCS.stream().filter(x -> x.getIssue() != Issue.INACTIVE_COURT
                        && x.getIssue() != Issue.INACTIVE_MODULE).toList();
                mosgorsud = mosgorsud.stream().filter(x -> x.getIssue() != Issue.INACTIVE_COURT
                        && x.getIssue() != Issue.INACTIVE_MODULE).toList();
            }

            if (selectedRegions != null) {
                mainCCS = mainCCS.stream().filter(x -> Arrays.stream(selectedRegions).anyMatch(r -> r == x.getRegion())).toList();
                singleCCS = singleCCS.stream().filter(x -> Arrays.stream(selectedRegions).anyMatch(r -> r == x.getRegion())).toList();

                mosgorsud = mosgorsud.stream().filter(x -> Arrays.stream(selectedRegions).anyMatch(r -> r == x.getRegion())).toList();
            }

            if (levels != null) {
                mainCCS = mainCCS.stream().filter(x->Arrays.stream(levels).anyMatch(r->r==x.getLevel())).toList();
                singleCCS = singleCCS.stream().filter(x->Arrays.stream(levels).anyMatch(r->r==x.getLevel())).toList();
                mosgorsud = mosgorsud.stream().filter(x->Arrays.stream(levels).anyMatch(r->r==x.getLevel())).toList();
            }

            if (strategyNames != null) {
                mainCCS = mainCCS.stream().filter(x->Arrays.stream(strategyNames).anyMatch(r->r==x.getStrategyName())).toList();
                singleCCS = singleCCS.stream().filter(x->Arrays.stream(strategyNames).anyMatch(r->r==x.getStrategyName())).toList();
                mosgorsud = mosgorsud.stream().filter(x->Arrays.stream(strategyNames).anyMatch(r->r==x.getStrategyName())).toList();
            }

            mainCCS = checkIfNothingToExecute(mainCCS);
            singleCCS = checkIfNothingToExecute(singleCCS);
            mosgorsud = checkIfNothingToExecute(mosgorsud);


            countDownLatch = new CountDownLatch(mainCCS.size() + singleCCS.size() + mosgorsud.size());

            ThreadPoolExecutor mosgorsudExecutor = new StrategyThreadPoolExecutor(1, 1,
                10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(mosgorsud.size()), this);
            ThreadPoolExecutor mainExecutor = new StrategyThreadPoolExecutor(3, 4,
                    10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(mainCCS.size()), this);
            ThreadPoolExecutor seleniumExecutor = new StrategyThreadPoolExecutor(1, 1,
                    10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(singleCCS.size()), this);

            courts = mainCCS.size() + singleCCS.size() + mosgorsud.size();
            for (CourtConfiguration cc : mosgorsud) {
                seleniumExecutor.execute(this.selectStrategy(cc));
            }
            for (CourtConfiguration cc : singleCCS) {
                seleniumExecutor.execute(this.selectStrategy(cc));
            }
            for (CourtConfiguration cc : mainCCS) {
                mainExecutor.execute(this.selectStrategy(cc));
            }

            countDownLatch.await();

            mainExecutor.shutdown();
            seleniumExecutor.shutdown();
            mosgorsudExecutor.shutdown();
    }

    private synchronized void update(Collection<Case> cases) {
        countDownLatch.countDown();

        this.cases += cases.size();

        view.showFrameWithInfo(ViewFrame.INFO, String.format(Message.RESULT.toString(),
                courts - countDownLatch.getCount(), courts, this.cases));

        refresh();

        if (cases.size() != 0) {
            updaterService.update(cases);
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
            case MOSGORSUD_STRATEGY -> {
                return new MosGorSudStrategy(cc);
            }

            default -> throw new IllegalArgumentException(Message.STRATEGY_NOT_CHOSEN.toString());
        }
    }

}
