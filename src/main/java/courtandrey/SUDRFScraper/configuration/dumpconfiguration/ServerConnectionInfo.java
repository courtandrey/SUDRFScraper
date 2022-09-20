package courtandrey.SUDRFScraper.configuration.dumpconfiguration;

import courtandrey.SUDRFScraper.dump.DBUpdaterService;

import java.sql.SQLException;

public final class ServerConnectionInfo {
    private static String DB_URL = "";
    private static String user = "";
    private static String password = "";

    public static String getDbUrl() {
        return DB_URL;
    }

    public static void setDbUrl(String dbUrl) {
        DB_URL = dbUrl;
    }

    public static String getUser() {
        return user;
    }

    public static void setUser(String user) {
        ServerConnectionInfo.user = user;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        ServerConnectionInfo.password = password;
    }

    public static void testConnection() throws SQLException {
        if (!DB_URL.equals("") && !user.equals("") && !password.equals("")) {
            DBUpdaterService.CasesDB.getConnection();
        }
        else {
            throw new SQLException();
        }
    }
}
