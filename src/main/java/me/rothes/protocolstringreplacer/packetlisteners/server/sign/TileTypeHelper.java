package me.rothes.protocolstringreplacer.packetlisteners.server.sign;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;

public class TileTypeHelper {

    private static final Object signType;
    private static final Object hangingSignType;

    static {
        Object tempSign;
        Object tempHSign;
        try {
            tempSign = Class.forName("net.minecraft.world.level.block.entity.TileEntityTypes").getField("h").get(null);
            if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 20) {
                tempHSign = Class.forName("net.minecraft.world.level.block.entity.TileEntityTypes").getField("i").get(null);
            } else {
                tempHSign = null;
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            ProtocolStringReplacer.error("§4Error when getting sign type instances.");
            tempSign = null;
            tempHSign = null;
        }
        signType = tempSign;
        hangingSignType = tempHSign;
    }

    private TileTypeHelper() {
    }

    public static boolean isSignType(Object type) {
        return type != null && (type == signType || type == hangingSignType);
    }

}
