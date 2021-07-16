package me.rothes.protocolstringreplacer.replacer;

public enum ListenType {

    CHAT("chat"),
    SIGN("sign"),
    TITLE("title"),
    ENTITY("entity"),
    BOSS_BAR("boss-bar"),
    ITEMSTACK("itemstack"),
    WINDOW_TITLE("window-title");

    private String name;

    ListenType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
