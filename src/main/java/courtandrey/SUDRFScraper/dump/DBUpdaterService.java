package courtandrey.SUDRFScraper.dump;

import courtandrey.SUDRFScraper.configuration.dumpconfiguration.ServerConnectionInfo;
import courtandrey.SUDRFScraper.controller.ErrorHandler;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.dump.repository.Cases;
import courtandrey.SUDRFScraper.service.ThreadHelper;
import courtandrey.SUDRFScraper.service.Constants;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Queue;

public class DBUpdaterService extends UpdaterService {
    private final CasesDB casesDB;
    private final Queue<Case> cases = new ArrayDeque<>();
    protected boolean isScrappingOver = false;

    public static class CasesDB{
        private final Cases cases;

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(ServerConnectionInfo.getDbUrl(),ServerConnectionInfo.getUser(),
                    ServerConnectionInfo.getPassword());
        }

        public CasesDB(String name) throws ClassNotFoundException, SQLException {
            Class.forName(Constants.DB_Driver);
            cases = new Cases(name);
        }

        protected void update(Case _case) throws SQLException {
            if (_case != null) cases.addCase(_case);
        }
    }

    public DBUpdaterService(String name, ErrorHandler handler) throws IOException, ClassNotFoundException, SQLException {
        super(name, handler);
        casesDB = new CasesDB(name);
    }

    @Override
    public void addMeta() throws IOException {
        writeMeta(getBasicProperties());
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
            handler.errorOccurred(e, this);
        }
        finally {
            afterExecute();
        }
    }

    public void close() {
        casesDB.cases.close();
    }
}
