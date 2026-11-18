package com.sds.bookrecommendation.quiz;

/**
 * The six reader types the personality quiz can produce.
 * Each one has a display name, a short description for the result screen, and a site theme.
 */
public enum Archetype {

    ADVENTURER("Adventurer",
            "You love fast-paced stories full of action, journeys and new worlds.", "fantasy"),
    DREAMER("Dreamer",
            "You get lost in imagination, feelings and beautiful language.", "romance"),
    DETECTIVE("Detective",
            "You enjoy puzzles, clues and twists you did not see coming.", "mystery"),
    THINKER("Thinker",
            "You like big ideas, hard questions and stories that make you think.", "mystery"),
    SCHOLAR("Scholar",
            "You read to learn, from history and science to real lives and facts.", "fantasy"),
    COMFORT_READER("Comfort Reader",
            "You want warm, familiar, feel-good stories you can relax into.", "romance");

    private final String displayName;
    private final String description;
    private final String theme;

    Archetype(String displayName, String description, String theme) {
        this.displayName = displayName;
        this.description = description;
        this.theme = theme;
    }

    /** Name shown to the user, for example "Comfort Reader". */
    public String getDisplayName() {
        return displayName;
    }

    /** Short description shown on the quiz result screen. */
    public String getDescription() {
        return description;
    }

    /** Site theme that matches this reader type (fantasy, mystery or romance). */
    public String getTheme() {
        return theme;
    }

    /**
     * Finds an archetype from text such as "Adventurer", "comfort reader" or "COMFORT_READER".
     * Returns null when the text is empty or unknown.
     */
    public static Archetype fromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String key = text.trim().toUpperCase().replace(' ', '_').replace('-', '_');
        for (Archetype archetype : values()) {
            if (archetype.name().equals(key)) {
                return archetype;
            }
        }
        return null;
    }
}