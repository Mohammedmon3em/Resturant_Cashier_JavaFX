package com.seafoodpos.model;

public enum JuiceSize {
    SMALL("Small", "\u0635\u063a\u064a\u0631"),
    LARGE("Large", "\u0643\u0628\u064a\u0631");

    private final String englishLabel;
    private final String arabicLabel;

    JuiceSize(String en, String ar) {
        this.englishLabel = en;
        this.arabicLabel  = ar;
    }

    public String getEnglishLabel()   { return englishLabel; }
    public String getArabicLabel()    { return arabicLabel; }
    public String getBilingualLabel() { return englishLabel + " / " + arabicLabel; }

    public static JuiceSize fromString(String s) {
        if (s == null) return null;
        return switch (s.toUpperCase()) {
            case "SMALL" -> SMALL;
            case "LARGE" -> LARGE;
            default -> null;
        };
    }
}
