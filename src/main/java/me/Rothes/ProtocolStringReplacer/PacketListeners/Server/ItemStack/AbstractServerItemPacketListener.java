package me.Rothes.ProtocolStringReplacer.PacketListeners.Server.ItemStack;

import com.comphenix.protocol.PacketType;
import me.Rothes.ProtocolStringReplacer.API.Configuration.DotYamlConfiguration;
import me.Rothes.ProtocolStringReplacer.PacketListeners.Server.AbstractServerPacketListener;
import me.Rothes.ProtocolStringReplacer.ProtocolStringReplacer;
import me.Rothes.ProtocolStringReplacer.User.User;
import me.Rothes.ProtocolStringReplacer.Replacer.ReplacerFile;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.List;
import java.util.function.BiPredicate;

public class AbstractServerItemPacketListener extends AbstractServerPacketListener {

    protected final BiPredicate<ReplacerFile, User> itemFilter;
    protected final NamespacedKey userCacheKey = ProtocolStringReplacer.getPacketListenerManager().getUserCacheKey();

    protected AbstractServerItemPacketListener(PacketType packetType) {
        super(packetType);
        itemFilter = (replacerFile, user) -> {
            DotYamlConfiguration configuration = replacerFile.getConfiguration();
            List<String> windowTitles = configuration.getStringList("Filter鰠" + packetType.name() + "鰠Window-Title");
            if (containPacket(replacerFile)) {
                if (windowTitles.isEmpty() || windowTitles.contains(user.getCurrentlyWindowTitle())) {
                    return true;
                }
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
                    CustomItemTagContainer tagContainer = replacedMeta.getCustomTagContainer();
                    tagContainer.setCustomTag(userCacheKey, ItemTagType.SHORT, uniqueCacheKey);
                    replacedItem.setItemMeta(replacedMeta);
                    user.getMetaCache().put(uniqueCacheKey, originalMeta);
                }
            }
        }
    }

}
