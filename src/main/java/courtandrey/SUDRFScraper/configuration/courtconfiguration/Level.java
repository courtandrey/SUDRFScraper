package courtandrey.SUDRFScraper.configuration.courtconfiguration;

import courtandrey.SUDRFScraper.exception.LevelParsingException;


public enum Level {
    DISTRICT,
    REGION,
    GARRISON,
    MOSGORSUD;

    public static Level parseLevel(String s) {
        if (s.equalsIgnoreCase(DISTRICT.name())) return DISTRICT;
        if (s.equalsIgnoreCase(REGION.name())) return REGION;
        if (s.equalsIgnoreCase(GARRISON.name())) return GARRISON;
        if (s.equalsIgnoreCase(MOSGORSUD.name())) return MOSGORSUD;
        throw new LevelParsingException("Unable to parse Level");
    }
}
