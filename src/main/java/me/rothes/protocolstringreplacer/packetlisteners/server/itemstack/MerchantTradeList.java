package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.List;

public class MerchantTradeList extends AbstractServerItemPacketListener {

    public MerchantTradeList() {
        super(PacketType.Play.Server.OPEN_WINDOW_MERCHANT);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        StructureModifier<List<MerchantRecipe>> merchantRecipeLists = packetEvent.getPacket().getMerchantRecipeLists();

        List<MerchantRecipe> read = merchantRecipeLists.read(0);
        for (MerchantRecipe recipe : read) {
            for (ItemStack ingredient : recipe.getIngredients()) {
                replaceItemStack(packetEvent, user, listenType, ingredient, filter);
            }

            replaceItemStack(packetEvent, user, listenType, recipe.getResult(), filter);
        }
    }
}
