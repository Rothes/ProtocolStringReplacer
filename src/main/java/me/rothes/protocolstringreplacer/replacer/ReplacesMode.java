package me.rothes.protocolstringreplacer.replacer;

public enum ReplacesMode {

    COMMON("Common", "Enum.Replacers-Mode.Common"),
    JSON("Json", "Enum.Replacers-Mode.Json");

    private String node;
    private String localeKey;

    ReplacesMode(String node, String localeKey) {
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
