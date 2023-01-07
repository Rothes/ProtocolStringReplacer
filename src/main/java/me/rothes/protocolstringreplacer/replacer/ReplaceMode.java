package me.rothes.protocolstringreplacer.replacer;

public enum ReplaceMode {

    COMMON("Common", "Variables.Replacers-Mode.Enums.Common"),
    JSON("Json", "Variables.Replacers-Mode.Enums.Json"),
    DIRECT("Direct", "Variables.Replacers-Mode.Enums.Direct");

    private String node;
    private String localeKey;

    ReplaceMode(String node, String localeKey) {
        this.node = node;
        this.localeKey = localeKey;
    }

    public String getNode() {
        return node;
    }

    public String getLocaleKey() {
        return localeKey;
    }

}
