package io.github.rothes.protocolstringreplacer.packetlistener.server.itemstack

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.reflect.EquivalentConverter
import com.comphenix.protocol.reflect.FuzzyReflection
import com.comphenix.protocol.reflect.accessors.Accessors
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor
import com.comphenix.protocol.reflect.accessors.MethodAccessor
import com.comphenix.protocol.utility.MinecraftReflection
import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer
import io.github.rothes.protocolstringreplacer.get
import io.github.rothes.protocolstringreplacer.set
import org.bukkit.inventory.MerchantRecipe

class MerchantTradeList : AbstractServerItemPacketListener(PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {

    override fun process(packetEvent: PacketEvent) {
        val user = getEventUser(packetEvent) ?: return
        user.isInMerchant = true
        if (ProtocolStringReplacer.getInstance().configManager.removeCacheWhenMerchantTrade) {
            user.player.updateInventory()
        }
        val packet = packetEvent.packet.deepClone()
        val merchantRecipeLists = packet.modifier.withType(
            MinecraftReflection.getMerchantRecipeList(), Converter
        )

        val replacerManager = ProtocolStringReplacer.getInstance().replacerManager
        val nbt = replacerManager.getAcceptedReplacers(user, itemNbtFilter)
        val display = replacerManager.getAcceptedReplacers(user, itemDisplayFilter)
        val entries = replacerManager.getAcceptedReplacers(user, itemEntriesFilter)

        merchantRecipeLists[0] = merchantRecipeLists[0].map { recipe ->
            MerchantRecipe(
                replaceItemStack(packetEvent, user, listenType, recipe.result, nbt, display, entries, true) ?: return,
                recipe.uses,
                recipe.maxUses,
                recipe.hasExperienceReward(),
                recipe.villagerExperience,
                recipe.priceMultiplier,
                recipe.demand,
                recipe.specialPrice
            ).also {
                it.ingredients = recipe.ingredients.map { item ->
                    replaceItemStack(packetEvent, user, listenType, item!!, nbt, display, entries, false) ?: return
                }
            }
        }
        packetEvent.packet = packet
    }

    private object Converter : EquivalentConverter<List<MerchantRecipe>> {

        private val merchantRecipeListConstructor: ConstructorAccessor
        private val bukkitMerchantRecipeToCraft: MethodAccessor
        private val craftMerchantRecipeToNMS: MethodAccessor
        private val nmsMerchantRecipeToBukkit: MethodAccessor

        init {
            val merchantRecipeListClass = MinecraftReflection.getMerchantRecipeList()
            merchantRecipeListConstructor = Accessors.getConstructorAccessor(merchantRecipeListClass)
            val craftMerchantRecipeClass = MinecraftReflection.getCraftBukkitClass("inventory.CraftMerchantRecipe")
            var reflection = FuzzyReflection.fromClass(craftMerchantRecipeClass, false)
            bukkitMerchantRecipeToCraft = Accessors.getMethodAccessor(reflection.getMethodByName("fromBukkit"))
            craftMerchantRecipeToNMS = Accessors.getMethodAccessor(reflection.getMethodByName("toMinecraft"))

            val merchantRecipeClass = MinecraftReflection.getMinecraftClass(
                "world.item.trading.MerchantRecipe", "world.item.trading.MerchantOffer", "MerchantRecipe"
            )
            reflection = FuzzyReflection.fromClass(merchantRecipeClass, false)
            nmsMerchantRecipeToBukkit = Accessors.getMethodAccessor(reflection.getMethodByName("asBukkit"))
        }

        override fun getGeneric(specific: List<MerchantRecipe>): Any {
            @Suppress("UNCHECKED_CAST")
            return (merchantRecipeListConstructor.invoke() as MutableList<Any>).apply {
                specific.forEach { recipe ->
                    add(craftMerchantRecipeToNMS.invoke(bukkitToCraft(recipe)))
                }
            }
        }

        override fun getSpecific(generic: Any): List<MerchantRecipe> {
            return (generic as List<*>).map { o -> nmsMerchantRecipeToBukkit.invoke(o) as MerchantRecipe }
        }

        fun bukkitToCraft(bukkit: MerchantRecipe): Any {
            val craft = bukkitMerchantRecipeToCraft.invoke(null, bukkit) as MerchantRecipe
            // Thanks, CraftBukkit. Doesn't set these field for 1.18-1.19.1
            if (ProtocolStringReplacer.getInstance().serverMajorVersion.toInt() == 18) {
                craft.demand = bukkit.demand
                craft.specialPrice = bukkit.specialPrice
            }
            return craft
        }

        override fun getSpecificType(): Class<List<MerchantRecipe>> {
            @Suppress("UNCHECKED_CAST")
            return List::class.java as Class<List<MerchantRecipe>>
        }
    }
}
