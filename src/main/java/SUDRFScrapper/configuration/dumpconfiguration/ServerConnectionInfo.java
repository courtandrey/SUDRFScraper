package SUDRFScrapper.configuration.dumpconfiguration;

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
}
