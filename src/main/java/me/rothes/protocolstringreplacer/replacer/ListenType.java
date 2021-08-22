package me.rothes.protocolstringreplacer.replacer;

public enum ListenType {

    CHAT("Chat", true),
    SIGN("Sign", false),
    TITLE("Title", true),
    ENTITY("Entity", false),
    BOSS_BAR("Boss-Bar", true),
    ITEMSTACK("ItemStack", false),
    WINDOW_TITLE("Window-Title", true),
    SCOREBOARD("ScoreBoard", false);

    public static ListenType getType(String typeName) {
        for (ListenType type : ListenType.values()) {
            if (type.getName().equalsIgnoreCase(typeName)) {
                return type;
            }
        }
        return null;
    }

    private String name;
    private boolean capturable;

    ListenType(String name, boolean capturable) {
        this.name = name;
        this.capturable = capturable;
    }

    public String getName() {
        return name;
    }

    public boolean isCapturable() {
        return capturable;
    }

}
