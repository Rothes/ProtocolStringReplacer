package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
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
        PacketContainer packet = packetEvent.getPacket().deepClone();
        StructureModifier<List<MerchantRecipe>> merchantRecipeLists = packet.getMerchantRecipeLists();

        ReplacerManager replacerManager = ProtocolStringReplacer.getInstance().getReplacerManager();
        List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(user, itemFilter);

        List<MerchantRecipe> replaced = new ArrayList<>();

        MerchantRecipe toAdd;
        List<MerchantRecipe> read = merchantRecipeLists.read(0);
        for (MerchantRecipe recipe : read) {
            List<ItemStack> ingredients = recipe.getIngredients();
            for (ItemStack ingredient : ingredients) {
                replaceItemStack(packetEvent, user, listenType, ingredient, replacers, false);
            }
            replaceItemStack(packetEvent, user, listenType, recipe.getResult(), replacers, false);

            toAdd = new MerchantRecipe(recipe.getResult(), recipe.getUses(), recipe.getMaxUses(),
                    recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(),
                    recipe.getDemand(), recipe.getSpecialPrice());
            toAdd.setIngredients(ingredients);
            replaced.add(toAdd);
        }
        merchantRecipeLists.write(0, replaced);
        packetEvent.setPacket(packet);
    }
}
