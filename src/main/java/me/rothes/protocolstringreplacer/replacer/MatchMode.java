package me.rothes.protocolstringreplacer.replacer;

public enum MatchMode {

    CONTAIN("Contain"),
    EQUAL("Equal"),
    REGEX("Regex");

    private String name;

    MatchMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
