package SUDRFScrapper.dump;

import SUDRFScrapper.SUDRFScraper;
import SUDRFScrapper.configuration.dumpconfiguration.ServerConnectionInfo;
import SUDRFScrapper.dump.model.Case;
import SUDRFScrapper.dump.repository.Cases;
import SUDRFScrapper.service.ThreadHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;

import static SUDRFScrapper.service.Constants.DB_Driver;

public class DBUpdater extends Updater {
    private CasesDB casesDB;
    private final Queue<Case> cases = new ArrayDeque<>();
    protected boolean isScrappingOver = false;

    public static class CasesDB{
        private Cases cases;

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(ServerConnectionInfo.getDbUrl(),ServerConnectionInfo.getUser(),
                    ServerConnectionInfo.getPassword());
        }

        public CasesDB(String name) throws ClassNotFoundException, SQLException {
            Class.forName(DB_Driver);
            cases = new Cases(name);
        }

        protected void update(Case _case) throws SQLException {
            if (_case!=null) cases.addCase(_case);
        }
    }

    public DBUpdater(String name, SUDRFScraper controller) {
        super(name, controller);
        try {
            casesDB = new CasesDB(name);
        } catch (ClassNotFoundException | SQLException e) {
            controller.errorOccurred(e, null);
        }
    }

    @Override
    public void run() {
        try {
            casesDB.cases.createTable();
            while (!isScrappingOver) {
                if (cases.isEmpty()) {
                    ThreadHelper.sleep(10);
                } else {
                    Case _case;
                    synchronized (this) {
                        _case = cases.poll();
                    }
                    casesDB.update(_case);
                }
            }
            while (!cases.isEmpty()) {
                Case _case = cases.poll();
                casesDB.update(_case);
            }
        }
        catch (SQLException e) {
            controller.errorOccurred(e, this);
        }
    }

    @Override
    public synchronized void update(Collection<Case> casesList) {
        cases.addAll(casesList);
    }

    public void close() {
        casesDB.cases.close();
    }
}
