package io.github.rothes.protocolstringreplacer.bukkit.packetlisteners.server.itemstack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import io.github.rothes.protocolstringreplacer.bukkit.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.bukkit.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.bukkit.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.bukkit.replacer.ReplacerManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MerchantTradeList extends AbstractServerItemPacketListener {

    private static final Converter CONVERTER = new Converter();

    public MerchantTradeList() {
        super(PacketType.Play.Server.OPEN_WINDOW_MERCHANT);
    }

    protected void process(PacketEvent packetEvent) {
        PsrUser user = getEventUser(packetEvent);
        if (user == null) {
            return;
        }
        user.setInMerchant(true);
        if (ProtocolStringReplacer.getInstance().getConfigManager().removeCacheWhenMerchantTrade) {
            user.getPlayer().updateInventory();
        }
        PacketContainer packet = packetEvent.getPacket().deepClone();
        StructureModifier<List<MerchantRecipe>> merchantRecipeLists = packet.getModifier().withType(
                MinecraftReflection.getMerchantRecipeList(), CONVERTER);

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
            replaceItemStack(packetEvent, user, listenType, recipe.getResult(), replacers, true);

            toAdd = new MerchantRecipe(recipe.getResult(), recipe.getUses(), recipe.getMaxUses(),
                    recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(),
                    recipe.getDemand(), recipe.getSpecialPrice());
            toAdd.setIngredients(ingredients);
            replaced.add(toAdd);
        }
        merchantRecipeLists.write(0, replaced);
        packetEvent.setPacket(packet);
    }

    private static class Converter implements EquivalentConverter<List<MerchantRecipe>> {
        private final ConstructorAccessor merchantRecipeListConstructor;
        private final MethodAccessor bukkitMerchantRecipeToCraft;
        private final MethodAccessor craftMerchantRecipeToNMS;
        private final MethodAccessor nmsMerchantRecipeToBukkit;

        Converter() {
            Class<?> merchantRecipeListClass = MinecraftReflection.getMerchantRecipeList();
            merchantRecipeListConstructor = Accessors.getConstructorAccessor(merchantRecipeListClass);
            Class<?> craftMerchantRecipeClass = MinecraftReflection.getCraftBukkitClass("inventory.CraftMerchantRecipe");
            FuzzyReflection reflection = FuzzyReflection.fromClass(craftMerchantRecipeClass, false);
            bukkitMerchantRecipeToCraft = Accessors.getMethodAccessor(reflection.getMethodByName("fromBukkit"));
            craftMerchantRecipeToNMS = Accessors.getMethodAccessor(reflection.getMethodByName("toMinecraft"));

            Class<?> merchantRecipeClass = MinecraftReflection.getMinecraftClass(
                    "world.item.trading.MerchantRecipe", "world.item.trading.MerchantOffer","MerchantRecipe"
            );
            reflection = FuzzyReflection.fromClass(merchantRecipeClass, false);
            nmsMerchantRecipeToBukkit = Accessors.getMethodAccessor(reflection.getMethodByName("asBukkit"));
        }

        @Override
        public Object getGeneric(List<MerchantRecipe> specific) {
            return specific.stream().map(recipe -> craftMerchantRecipeToNMS.invoke(bukkitToCraft(recipe)))
                    .collect(() -> (List<Object>)merchantRecipeListConstructor.invoke(), List::add, List::addAll);
        }

        @Override
        public List<MerchantRecipe> getSpecific(Object generic) {
            return ((List<Object>)generic).stream().map(o -> (MerchantRecipe)nmsMerchantRecipeToBukkit.invoke(o)).collect(Collectors.toList());
        }

        public Object bukkitToCraft(MerchantRecipe bukkit) {
            MerchantRecipe craft = (MerchantRecipe) bukkitMerchantRecipeToCraft.invoke(null, bukkit);
            // Thanks, CraftBukkit
            craft.setDemand(bukkit.getDemand());
            craft.setSpecialPrice(bukkit.getSpecialPrice());
            return craft;
        }

        @Override
        public Class<List<MerchantRecipe>> getSpecificType() {
            Class<?> dummy = List.class;
            return (Class<List<MerchantRecipe>>) dummy;
        }

    }
}
