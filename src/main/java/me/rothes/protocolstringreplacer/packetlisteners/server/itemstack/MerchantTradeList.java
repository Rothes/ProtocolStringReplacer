package me.rothes.protocolstringreplacer.packetlisteners.server.itemstack;

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
import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MerchantTradeList extends AbstractServerItemPacketListener {

    private static final Converter CONVERTER = new Converter();

    public MerchantTradeList() {
        super(PacketType.Play.Server.OPEN_WINDOW_MERCHANT);
    }

    protected void process(@NotNull PacketEvent packetEvent) {
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
        List<ReplacerConfig> nbt = replacerManager.getAcceptedReplacers(user, itemNbtFilter);
        List<ReplacerConfig> display = replacerManager.getAcceptedReplacers(user, itemDisplayFilter);
        List<ReplacerConfig> entries = replacerManager.getAcceptedReplacers(user, itemEntriesFilter);

        List<MerchantRecipe> replaced = new ArrayList<>();

        MerchantRecipe toAdd;
        List<MerchantRecipe> read = merchantRecipeLists.read(0);
        for (MerchantRecipe recipe : read) {
            List<ItemStack> ingredients = recipe.getIngredients();
            for (ItemStack ingredient : ingredients) {
                replaceItemStack(packetEvent, user, listenType, ingredient, nbt, display, entries, false);
            }
            replaceItemStack(packetEvent, user, listenType, recipe.getResult(), nbt, display, entries, true);

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
                    .collect(() -> (List<Object>) merchantRecipeListConstructor.invoke(), List::add, List::addAll);
        }

        @Override
        public List<MerchantRecipe> getSpecific(Object generic) {
            return ((List<Object>) generic).stream().map(o -> (MerchantRecipe)nmsMerchantRecipeToBukkit.invoke(o)).collect(Collectors.toList());
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
