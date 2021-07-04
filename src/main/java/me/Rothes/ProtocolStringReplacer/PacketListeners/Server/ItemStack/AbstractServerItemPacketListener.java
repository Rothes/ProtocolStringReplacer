package me.Rothes.ProtocolStringReplacer.PacketListeners.Server.ItemStack;

import com.comphenix.protocol.PacketType;
import me.Rothes.ProtocolStringReplacer.API.Configuration.DotYamlConfiguration;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.AbstractServerPacketListener;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.Replacer.ListenType;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerConfig;
import me.Rothes.ProtocolStringReplacer.User.User;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public abstract class AbstractServerItemPacketListener extends AbstractServerPacketListener {

    protected final BiPredicate<ReplacerConfig, User> itemFilter;
    protected NamespacedKey userCacheKey;

    protected AbstractServerItemPacketListener(PacketType packetType) {
        super(packetType, ListenType.ITEMSTACK);
        itemFilter = (replacerFile, user) -> {
            DotYamlConfiguration configuration = replacerFile.getConfiguration();
            List<String> windowTitles = configuration.getStringList("Filter鰠itemstack鰠Window-Title");
            if (containType(replacerFile)) {
                return windowTitles.isEmpty() || windowTitles.contains(user.getCurrentWindowTitle());
            }
            return false;
        };
    }

    protected void saveUserMetaCacche(User user, ItemStack originalItem, ItemStack replacedItem) {
        if (user.hasPermission("protocolstringreplacer.feature.usermetacache")) {
            if (originalItem.hasItemMeta()) {
                ItemMeta originalMeta = originalItem.getItemMeta();
                ItemMeta replacedMeta = replacedItem.getItemMeta();
                if (!originalMeta.equals(replacedMeta)) {
                    Short uniqueCacheKey = user.nextUniqueCacheKey();
                    if (ProtocolStringReplacer.getInstance().getServerMajorVersion() >= 13) {
                        CustomItemTagContainer tagContainer = replacedMeta.getCustomTagContainer();
                        tagContainer.setCustomTag(getUserCacheKey(), ItemTagType.SHORT, uniqueCacheKey);
                    } else {
                        List<String> lore = replacedMeta.getLore();
                        if (lore == null) {
                            lore = new ArrayList<>();
                        }
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("§p§s§r§-§x");
                        for (char Char : uniqueCacheKey.toString().toCharArray()) {
                            stringBuilder.append('§').append(Char);
                        }
                        lore.add(stringBuilder.toString());
                        replacedMeta.setLore(lore);
                    }
                    replacedItem.setItemMeta(replacedMeta);
                    user.getMetaCache().put(uniqueCacheKey, originalMeta);
                }
            }
        }
    }

    protected NamespacedKey getUserCacheKey() {
        if (userCacheKey == null) {
            userCacheKey = ProtocolStringReplacer.getInstance().getPacketListenerManager().getUserCacheKey();
        }
        return userCacheKey;
    }

}
