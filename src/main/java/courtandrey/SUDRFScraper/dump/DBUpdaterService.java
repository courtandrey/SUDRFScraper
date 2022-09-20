package courtandrey.SUDRFScraper.dump;

import courtandrey.SUDRFScraper.configuration.dumpconfiguration.ServerConnectionInfo;
import courtandrey.SUDRFScraper.controller.ErrorHandler;
import courtandrey.SUDRFScraper.dump.model.Case;
import courtandrey.SUDRFScraper.dump.repository.Cases;
import courtandrey.SUDRFScraper.service.ThreadHelper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Queue;

import static courtandrey.SUDRFScraper.service.Constant.DB_Driver;

public class DBUpdaterService extends UpdaterService {
    private final CasesDB casesDB;
    private final Queue<Case> cases = new ArrayDeque<>();
    protected boolean isScrappingOver = false;

    public static class CasesDB{
        private final Cases cases;

        public static Connection getConnection() throws SQLException {
            ServerConnectionInfo connectionInfo = ServerConnectionInfo.getInstance();
            return DriverManager.getConnection(connectionInfo.getDbUrl(),connectionInfo.getUser(),
                    connectionInfo.getPassword());
        }

        public CasesDB(String name) throws ClassNotFoundException, SQLException {
            Class.forName(DB_Driver.toString());
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
    public void createMeta() throws IOException {
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
            try {
                afterExecute();
            } catch (IOException e) {
                handler.errorOccurred(e, this);
            }
        }
    }

    public void close() {
        casesDB.cases.close();
    }
}
