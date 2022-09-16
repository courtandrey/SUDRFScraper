package courtandrey.SUDRFScraper.dump.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@NoArgsConstructor
public class Case {
    public static volatile AtomicInteger idInteger = new AtomicInteger(0);
    private int id;
    private int region;
    private String name;
    private String caseNumber;
    private String entryDate;
    private String names;
    private String judge;
    private String resultDate;
    private String decision;
    private String endDate;
    private String text;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Case aCase = (Case) o;
        return region == aCase.region && Objects.equals(name, aCase.name) && Objects.equals(caseNumber, aCase.caseNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, name, caseNumber);
    }
}
