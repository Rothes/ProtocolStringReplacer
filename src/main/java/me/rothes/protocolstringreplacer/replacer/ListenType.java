package me.rothes.protocolstringreplacer.replacer;

public enum ListenType {

    CHAT("Chat"),
    SIGN("Sign"),
    TITLE("Title"),
    ENTITY("Entity"),
    BOSS_BAR("Boss-Bar"),
    ITEMSTACK("ItemStack"),
    WINDOW_TITLE("Window-Title"),
    SCOREBOARD("ScoreBoard");

    private String name;

    ListenType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
