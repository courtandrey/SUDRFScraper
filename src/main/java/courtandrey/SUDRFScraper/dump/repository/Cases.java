package courtandrey.SUDRFScraper.dump.repository;

import courtandrey.SUDRFScraper.dump.DBUpdaterService;
import courtandrey.SUDRFScraper.dump.model.Case;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Cases {
    Connection connection;
    private final String name;

    public Cases(String name) throws SQLException {
        this.name = name;
    }

    public void addCase(Case _case) throws SQLException {
        String query = "INSERT INTO " +name+"(region,court_name,case_number," +
                "entry_date,names_articles,judge,result_date,decision,end_date,decision_text) VALUES(";
        query+= _case.getRegion()+",";
        query+= "\""+_case.getName()+"\""+", ";
        query+= _case.getCaseNumber()==null ? "null, " : "\""+_case.getCaseNumber()+"\""+", ";
        query+=_case.getEntryDate()==null ? "null, " : "\""+_case.getEntryDate()+"\""+", ";
        query+=_case.getNames()==null ? "null, " : "\""+_case.getNames().replace("\"","\\\"")+"\""+", ";
        query+=_case.getJudge()==null ? "null, " : "\""+_case.getJudge()+"\""+", ";
        query+=_case.getResultDate()==null ? "null, " : "\""+_case.getResultDate()+"\""+", ";
        query+=_case.getDecision()==null ? "null, " : "\""+_case.getDecision()+"\""+", ";
        query+=_case.getEndDate()==null ? "null, " : "\""+_case.getEndDate()+"\""+", ";
        query+=_case.getText()==null ? "null" : "\""+_case.getText().replace("\"","\\\"")+"\"";
        query+=")";
        executeSqlStatement(query);
    }

    public void createTable() throws SQLException {
        executeSqlStatement("CREATE TABLE IF NOT EXISTS " +name+"("+
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "region INT,"+
                "court_name VARCHAR(255),"+
                "case_number VARCHAR(255) UNIQUE," +
                "entry_date VARCHAR(255)," +
                "names_articles VARCHAR(4095)," +
                "judge VARCHAR(255)," +
                "result_date VARCHAR(255)," +
                "decision VARCHAR(255)," +
                "end_date VARCHAR(255)," +
                "decision_text MEDIUMTEXT)");
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeSqlStatement(String query) throws SQLException {
        reopenConnection();
        Statement statement = connection.createStatement();
        statement.execute(query);
        statement.close();
    }

    private void reopenConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DBUpdaterService.CasesDB.getConnection();
        }
    }
}
