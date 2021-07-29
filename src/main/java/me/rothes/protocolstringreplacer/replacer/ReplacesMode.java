package me.rothes.protocolstringreplacer.replacer;

public enum ReplacesMode {

    COMMON("Common", "常规"),
    JSON("Json", "Json");

    private String node;
    private String name;

    ReplacesMode(String node, String name) {
        this.node = node;
        this.name = name;
    }

    public String getNode() {
        return node;
    }

    public String getName() {
        return name;
    }

}
