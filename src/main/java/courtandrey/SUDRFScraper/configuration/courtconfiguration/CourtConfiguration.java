package courtandrey.SUDRFScraper.configuration.courtconfiguration;

import courtandrey.SUDRFScraper.configuration.searchrequest.Field;
import courtandrey.SUDRFScraper.strategy.Connection;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.HashMap;
import java.util.Objects;

@JsonAutoDetect
@Data
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
    @JsonIgnore
    public void putWorkingUrl(Field field, String value) {
        workingUrl.put(field,value);
    }

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
