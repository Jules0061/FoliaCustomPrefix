package com.foliaCustomPrefix.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class LuckPermsHook {

    private static final int PREFIX_PRIORITY = 1000;
    private static final String PREFIX_SUFFIX = "§r§7 ";

    private final LuckPerms luckPerms;

    public LuckPermsHook() {
        this.luckPerms = LuckPermsProvider.get();
    }

    public String getPrefix(Player player) {
        CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        return metaData.getPrefix();
    }

    public String getPrefix(UUID uuid) {
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) {
            return null;
        }
        return user.getCachedData().getMetaData().getPrefix();
    }

    public CompletableFuture<Void> setUserPrefix(UUID uuid, String legacyPrefix) {
        return luckPerms.getUserManager().modifyUser(uuid, user -> {
            removeOwnedPrefixNodes(user);
            user.data().add(PrefixNode.builder(legacyPrefix + PREFIX_SUFFIX, PREFIX_PRIORITY).build());
        });
    }

    public CompletableFuture<UUID> lookupUniqueId(String name) {
        return luckPerms.getUserManager().lookupUniqueId(name);
    }

    public CompletableFuture<Void> clearUserPrefix(UUID uuid) {
        return luckPerms.getUserManager().modifyUser(uuid, this::removeOwnedPrefixNodes);
    }

    private void removeOwnedPrefixNodes(User user) {
        user.data().clear(NodeType.PREFIX.predicate(node -> node.getPriority() == PREFIX_PRIORITY));
    }
}
