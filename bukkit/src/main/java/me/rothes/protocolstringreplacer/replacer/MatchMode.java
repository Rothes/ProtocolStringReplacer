package me.rothes.protocolstringreplacer.replacer;

public enum MatchMode {

    CONTAIN("Contain", "Variables.Match-Mode.Enums.Contain"),
    EQUAL("Equal", "Variables.Match-Mode.Enums.Equal"),
    REGEX("Regex", "Variables.Match-Mode.Enums.Regex");

    private String name;
    private String localeKey;

    MatchMode(String name, String localeKey) {
        this.name = name;
        this.localeKey = localeKey;
    }

    public String getName() {
        return name;
    }

    public String getLocaleKey() {
        return localeKey;
    }
}
