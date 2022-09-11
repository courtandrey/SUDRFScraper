package SUDRFScrapper.dump.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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

    public int getRegion() {
        return region;
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }


    public String getJudge() {
        return judge;
    }

    public void setJudge(String judge) {
        this.judge = judge;
    }

    public String getResultDate() {
        return resultDate;
    }

    public void setResultDate(String resultDate) {
        this.resultDate = resultDate;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
