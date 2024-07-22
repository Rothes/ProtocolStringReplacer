package io.github.rothes.protocolstringreplacer.nms.packetreader;

public enum ChatType {

    PLAYER_CHAT("chat"),
    SYSTEM_CHAT("system"),
    GAME_INFO("game_info"),
    SAY("say_command"),
    MSG_INCOMING(
            "msg_command_incoming",
            "msg_command" // 1.19
    ),
    MSG_OUTGOING("msg_command_outgoing"),
    TEAM_MSG_INCOMING(
            "team_msg_command_incoming",
            "team_msg_command" // 1.19
    ),
    TEAM_MSG_OUTGOING("team_msg_command_outgoing"),
    EMOTE("emote_command"),
    TELLRAW(
            "raw",
            "tellraw_command" // 1.19
    );

    private final String[] keys;

    ChatType(String... keys) {
        this.keys = keys;
    }

    public String[] getKeys() {
        return keys;
    }
}
