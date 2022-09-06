package me.rothes.protocolstringreplacer.replacer;

public enum ListenType {

    CHAT("Chat", true),
    CHAT_PREVIEW("Chat-Preview", true),
    TAB_COMPLETE("Tab-Complete", true),
    SIGN("Sign", true),
    TITLE("Title", true),
    ENTITY("Entity", true),
    BOSS_BAR("Boss-Bar", true),
    ITEMSTACK("ItemStack", true),
    WINDOW_TITLE("Window-Title", true),
    SCOREBOARD("ScoreBoard", true),
    CONSOLE("Console", false),
    KICK_DISCONNECT("Kick-Disconnect", true), // Is it important capture-able now? lol
    COMBAT_KILL("Combat-Kill", true);

    private String name;
    private boolean capturable;

    ListenType(String name, boolean capturable) {
        this.name = name;
        this.capturable = capturable;
    }

    public static ListenType getType(String typeName) {
        for (ListenType type : ListenType.values()) {
            if (type.getName().equalsIgnoreCase(typeName)) {
                return type;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public boolean isCapturable() {
        return capturable;
    }

}
