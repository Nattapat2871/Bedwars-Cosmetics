package xyz.iamthedefender.cosmetics.category.shopkeeperskins.utils;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.PlayerFilter;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;
import xyz.iamthedefender.cosmetics.api.configuration.ConfigManager;
import xyz.iamthedefender.cosmetics.api.cosmetics.CosmeticsType;
import xyz.iamthedefender.cosmetics.api.util.Utility;
import xyz.iamthedefender.cosmetics.api.util.config.ConfigUtils;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ShopKeeperSkinsUtils {
    

    /**
     * This creates an entity NPC.
     * */
    private static void createEntityNPC(final EntityType ent, final Location loc) {
        NPCRegistry registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        NPC npc = registry.createNPC(ent, "");
        npc.setBukkitEntityType(ent);
        npc.getOrAddTrait(LookClose.class).lookClose(getLookClose());
        npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false);
        npc.spawn(loc);

    }

    /**
     * This creates an entity NPC but with a timer.
     * */
    private static Runnable createEntityNPC(final Player p,final EntityType ent, final Location loc, int ticks) {
        NPCRegistry registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        NPC npc = registry.createNPC(ent, "");
        npc.setBukkitEntityType(ent);
        npc.getOrAddTrait(PlayerFilter.class).setAllowlist();
        npc.getOrAddTrait(PlayerFilter.class).addPlayer(p.getUniqueId());

        boolean spawned = npc.spawn(loc);
        if (!spawned) {
            CosmeticsPlugin.getInstance().getLogger().warning("Citizens failed to spawn NPC of type " + ent + " at " + loc);
        }

        npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false);
        npc.data().setPersistent(NPC.Metadata.DEATH_SOUND, "");
        npc.data().setPersistent(NPC.Metadata.AMBIENT_SOUND, "");
        npc.data().setPersistent(NPC.Metadata.HURT_SOUND, "");
        npc.data().setPersistent(NPC.Metadata.SILENT, true);

        return () -> {
            npc.destroy();
            registry.deregisterAll();
        };
    }

    /**
     * This method should only be used
     * When playing in game.
     * */
    private static void createShopKeeperNPC(Player p, Location loc, String value, String sign, Boolean mirror) {
        NPCRegistry registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());

        // Shop NPC
        NPC npc = registry.createNPC(EntityType.PLAYER, "");
        npc.setName("&r");

        if (mirror) {
            String[] skin = Utility.getFromPlayer(p);
            if (skin != null) {
                npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(UUID.randomUUID().toString(), skin[1], skin[0]);
            } else {
                npc.getOrAddTrait(SkinTrait.class).setSkinName(p.getName(), true);
            }
        } else {
            npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(UUID.randomUUID().toString(), sign, value);
        }

        npc.getTrait(LookClose.class).lookClose(getLookClose());
        npc.getOrAddTrait(HologramTrait.class).clear();
        boolean spawned = npc.spawn(loc);
        if (!spawned) {
            CosmeticsPlugin.getInstance().getLogger().warning("Citizens failed to spawn shopkeeper NPC at " + loc);
        }

        if (npc.getEntity() != null) {
            npc.getEntity().setMetadata("NPC2", new FixedMetadataValue(CosmeticsPlugin.getInstance(), ""));
            npc.getEntity().setMetadata("shop_entity_cosmetics", new FixedMetadataValue(CosmeticsPlugin.getInstance(), ""));
        }
        npc.data().setPersistent(NPC.Metadata.DEATH_SOUND, "");
        npc.data().setPersistent(NPC.Metadata.AMBIENT_SOUND, "");
        npc.data().setPersistent(NPC.Metadata.HURT_SOUND, "");
        npc.data().setPersistent(NPC.Metadata.SILENT, true);
    }

    /**
     * This method should only be used
     * When sending a preview.
     * */
    private static Runnable createShopKeeperNPC(Player p, Location loc, String value, String sign, Boolean mirror, int ticks) {
        NPCRegistry registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        // Shop NPC
        NPC npc = registry.createNPC(EntityType.PLAYER, "");
        npc.setName("&r");
        
        npc.getOrAddTrait(HologramTrait.class).clear();

        npc.getOrAddTrait(PlayerFilter.class).setAllowlist();
        npc.getOrAddTrait(PlayerFilter.class).addPlayer(p.getUniqueId());

        boolean spawned = npc.spawn(loc);
        if (!spawned) {
            CosmeticsPlugin.getInstance().getLogger().warning("Citizens failed to spawn shopkeeper NPC at " + loc);
        }

        if (mirror) {
            String[] skin = Utility.getFromPlayer(p);
            if (skin != null) {
                npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(UUID.randomUUID().toString(), skin[1], skin[0]);
            } else {
                npc.getOrAddTrait(SkinTrait.class).setSkinName(p.getName(), true);
            }
        } else {
            npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(UUID.randomUUID().toString(), sign, value);
        }

        if (npc.getEntity() != null) {
            npc.getEntity().setMetadata("NPC2", new FixedMetadataValue(CosmeticsPlugin.getInstance(), ""));
            npc.getEntity().setMetadata("shop_entity_cosmetics", new FixedMetadataValue(CosmeticsPlugin.getInstance(), ""));
        }

        return () -> {
            npc.destroy();
            registry.deregisterAll();
        };
    }


    /**

     Spawns a NPC in the form of a shopkeeper at the provided location using the selected skin from the player's
     cosmetic selection. If the selected skin has the option to be mirrored, it will use the player's current skin.
     Also spawns another NPC at the provided location1.
     @param p The player whose selected skin will be used for the NPC.
     @param loc The location where the first NPC will be spawned.
     */
    public static void spawnShopKeeperNPC(Player p, Location loc) {
        CosmeticsPlugin plugin = CosmeticsPlugin.getInstance();
        String skin = plugin.getApi().getSelectedCosmetic(p, CosmeticsType.ShopKeeperSkins);
        ConfigManager config = ConfigUtils.getShopKeeperSkins();
        String key = CosmeticsType.ShopKeeperSkins.getSectionKey();
        String skinvalue = config.getString(key + "." + skin + ".skin-value");
        String skinsign = config.getString(key + "." + skin + ".skin-sign");
        String etype = config.getString(key + "." + skin + ".entity-type");
        boolean mirror = config.getBoolean(key + "." + skin + ".mirror");

        if (mirror){
            createShopKeeperNPC(p, loc, skinvalue, skinsign, true);
            return;
        }
        if (etype != null) {
            createEntityNPC(EntityType.valueOf(etype), loc);
        }else if (skinvalue != null && skinsign != null) {
            createShopKeeperNPC(p, loc, skinvalue, skinsign, false);
        }
    }

    public static void spawnShopKeeperNPC(Player p, Location loc, String skin) {
        ConfigManager config = ConfigUtils.getShopKeeperSkins();
        String key = CosmeticsType.ShopKeeperSkins.getSectionKey();
        String skinvalue = config.getString(key + "." + skin + ".skin-value");
        String skinsign = config.getString(key + "." + skin + ".skin-sign");
        String etype = config.getString(key + "." + skin + ".entity-type");
        boolean mirror = config.getBoolean(key + "." + skin + ".mirror");

        if (mirror){
            createShopKeeperNPC(p, loc, skinvalue, skinsign, true);
            return;
        }
        if (etype != null) {
            createEntityNPC(EntityType.valueOf(etype), loc);
        }else if (skinvalue != null && skinsign != null) {
            createShopKeeperNPC(p, loc, skinvalue, skinsign, false);
        }
    }

    public static Runnable spawnShopKeeperNPCForPreview(Player p, Location loc, String skin) {
        ConfigManager config = ConfigUtils.getShopKeeperSkins();
        String key = CosmeticsType.ShopKeeperSkins.getSectionKey();
        String skinvalue = config.getString(key + "." + skin + ".skin-value");
        String skinsign = config.getString(key + "." + skin + ".skin-sign");
        String etype = config.getString(key + "." + skin + ".entity-type");
        boolean mirror = config.getBoolean(key + "." + skin + ".mirror");

        if (mirror){
            return createShopKeeperNPC(p, loc, skinvalue, skinsign, true, 5);
        }
        if (etype != null) {
            return createEntityNPC(p, EntityType.valueOf(etype), loc, 5);
        }else if (skinvalue != null && skinsign != null) {
            return createShopKeeperNPC(p, loc, skinvalue, skinsign, false, 5);
        }
        return () -> {};
    }

    private static boolean getLookClose() {
        return ConfigUtils.getMainConfig().getBoolean("settings.shopkeeper_skins.look_close");
    }
}
