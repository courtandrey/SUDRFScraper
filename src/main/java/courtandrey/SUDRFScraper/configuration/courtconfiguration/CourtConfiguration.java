package courtandrey.SUDRFScraper.configuration.courtconfiguration;

import courtandrey.SUDRFScraper.configuration.searchrequest.Field;
import courtandrey.SUDRFScraper.strategy.Connection;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Objects;

@JsonAutoDetect
public class CourtConfiguration {
    private int id;
    private int region;
    private String searchString;
    private StrategyName strategyName;
    private Level level;
    private Issue issue;
    private HashMap<Field,String> workingUrl;
    private SearchPattern searchPattern;
    private String vnkod;
    private String name;
    private Connection connection;


    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVnkod() {
        return vnkod;
    }

    public void setVnkod(String vnkod) {
        this.vnkod = vnkod;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRegion() {
        return region;
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public StrategyName getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(StrategyName strategyName) {
        this.strategyName = strategyName;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public HashMap<Field, String> getWorkingUrl() {
        return workingUrl;
    }

    public void setWorkingUrl(HashMap<Field, String> workingUrl) {
        this.workingUrl = workingUrl;
    }
    @JsonIgnore
    public void putWorkingUrl(Field field, String value) {
        workingUrl.put(field,value);
    }

    public SearchPattern getSearchPattern() {
        return searchPattern;
    }

    public void setSearchPattern(SearchPattern searchPattern) {
        this.searchPattern = searchPattern;
    }

    public CourtConfiguration() {}

    @JsonIgnore
    public boolean isVnkodNeeded() {
        return searchPattern == SearchPattern.VNKOD_PATTERN;
    }

    @JsonIgnore
    public boolean isSingleStrategy() {
        return strategyName == StrategyName.CAPTCHA_STRATEGY || connection == Connection.SELENIUM;
    }

    @Override
    public String toString() {
        return "Суд региона " + region +" доступен по ссылке: "+searchString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourtConfiguration that = (CourtConfiguration) o;
        return Objects.equals(searchString, that.searchString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchString);
    }

}
