package courtandrey.SUDRFScraper.configuration.courtconfiguration;

import courtandrey.SUDRFScraper.exception.LevelParsingException;

public enum StrategyName {
    PRIMARY_STRATEGY,
    CAPTCHA_STRATEGY,
    END_STRATEGY,
    MOSGORSUD_STRATEGY;

    public static StrategyName parseStrategy(String s) {
        if (s.equalsIgnoreCase(PRIMARY_STRATEGY.name())) return PRIMARY_STRATEGY;
        if (s.equalsIgnoreCase(CAPTCHA_STRATEGY.name())) return CAPTCHA_STRATEGY;
        if (s.equalsIgnoreCase(END_STRATEGY.name())) return END_STRATEGY;
        throw new LevelParsingException("Unable to parse Level");
    }
}
