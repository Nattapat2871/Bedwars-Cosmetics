package xyz.iamthedefender.cosmetics.category.finalkilleffects.preview;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.PlayerFilter;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;
import xyz.iamthedefender.cosmetics.api.cosmetics.CosmeticPreview;
import xyz.iamthedefender.cosmetics.api.cosmetics.Cosmetics;
import xyz.iamthedefender.cosmetics.api.cosmetics.CosmeticsType;
import xyz.iamthedefender.cosmetics.api.cosmetics.category.FinalKillEffect;
import xyz.iamthedefender.cosmetics.api.util.ColorUtil;
import xyz.iamthedefender.cosmetics.api.util.Run;
import xyz.iamthedefender.cosmetics.api.util.Utility;

import org.bukkit.util.Vector;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FinalKillEffectPreview extends CosmeticPreview {

    public FinalKillEffectPreview() {
        super(CosmeticsType.FinalKillEffects);
    }
@Override
public void showPreview(Player player, Cosmetics selected, Location previewLocation, Location playerLocation) throws IllegalArgumentException {
    handleLocation(player, playerLocation);

    ArmorStand as = (ArmorStand) player.getWorld().spawnEntity(playerLocation, EntityType.ARMOR_STAND);
    as.setVisible(false);

    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
            100, 2));

    Runnable onEnd = sendKillEffect(player, previewLocation, playerLocation, (FinalKillEffect) selected);

    CosmeticsPlugin.getInstance().getVersionSupport().sendCameraPacket(player, as);
        setOnEnd(player, () -> {
            if (!as.isDead()) as.remove();

            CosmeticsPlugin.getInstance().getVersionSupport().sendCameraPacket(player, player);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);

            if (onEnd != null) onEnd.run();
        });
    }

    public Runnable sendKillEffect(Player player, Location previewLocation, Location playerLocation, FinalKillEffect killEffect) {
        NPCRegistry registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());

        // Calculate relative direction from player to preview point
        Vector viewDir = previewLocation.toVector().subtract(playerLocation.toVector()).setY(0);
        if (viewDir.lengthSquared() == 0) viewDir = new Vector(1, 0, 0);
        viewDir.normalize();

        // Calculate "left" vector (rotated 90 degrees CCW)
        Vector leftDir = new Vector(-viewDir.getZ(), 0, viewDir.getX()).normalize();

        // Victim (Derperino) at center
        Location victimLoc = previewLocation.clone();
        victimLoc.setX(victimLoc.getBlockX() + 0.5);
        victimLoc.setZ(victimLoc.getBlockZ() + 0.5);
        
        // Killer (Player NPC) to the left (5 blocks away initially to walk in)
        Location killerSpawnLoc = victimLoc.clone().add(leftDir.clone().multiply(5));
        
        // Final position for killer (2 blocks away from victim)
        Location killerTargetLoc = victimLoc.clone().add(leftDir.clone().multiply(2));

        // Make them face each other
        killerSpawnLoc.setDirection(victimLoc.toVector().subtract(killerSpawnLoc.toVector()));
        victimLoc.setDirection(killerSpawnLoc.toVector().subtract(victimLoc.toVector()));

        NPC attackerNPC = registry.createNPC(EntityType.PLAYER, player.getDisplayName());
        NPC victimNPC = registry.createNPC(EntityType.PLAYER, ColorUtil.translate("&7Derperino"));

        String[] victimNPCValues = Utility.getFromName(player.getName());

        String derperinoNPCValue = "ewogICJ0aW1lc3RhbXAiIDogMTY4NDU1ODYwMzIyNywKICAicHJvZmlsZUlkIiA6ICI1NjY3NWIyMjMyZjA0ZWUwODkxNzllOWM5MjA2Y2ZlOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVJbmRyYSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81MzhiZjY4MTQ5MWE2ZmM1NmZlZDdhNjlmNDQ5MmYyN2ExZDE4YTdhMDM5ZTNjOWEzZWMyYjkzZmFkOWZlZDY2IgogICAgfQogIH0KfQ";
        String derperinoNPCSign = "BHb9Cye246WSOcArShXCY8Qv+yXWgANNwnKgCcIh6EEZMuWF6pQFgvYuqPn8l+SikiO4qimBcsjKLAigRCO7nnWCjE/GqtTEAmk5ermP5p+56tbS+AEvnSSOG5+0MtI8hcOEDnZTEI3GMcx/cQSmnRylMNlgMYAt7GL7uAkAd8bjnxG01lrrX5KqIzFnvc9quvruKeDV9fvAwgpc8zzZJwcVzOTZhrLxm1rj+iaVmLrP7PpRMOLF9bx3Q4URLedALLbX5PzkRQvZQBgGUsdCx+UDGjicp8gq7RIYnx+RnCEYHrkf9rrs6b8SW0qyAkhxLqlNZeCIPU8GECD4OpOMxathuQ4anI0j9bntXV/Yegdd42vQVjJVTAnQEGahqI5yTyaxL9r5GbegUHi2YQRTnZMuWNAETUxgaaC0v4kvV/DIDowmBgIAP6Anp2JSDAYXkW/mr/WBjyhk0oG31IHwySU1AuV5mch0v1AV5jrBi3Hjrxp6S+j7vMSpXYpzHmx0O92OfaiSg4J8tyJ/3cRxbGUas2Uc2ZuYa3Ke1FGyKmWVWqcX6APmLXagN5Zuug/aCHSPaoogNY29+YK7JQtRlJisUrG30sh7JUmKeIYMtOuQIgzVdHGhvC6xDqcK9hYnLV8XHNVqXZvq8ArrYD/Nw03MMWomFLM0NUaHaNYmLHo=";

        if (victimNPCValues != null) {
            String victimNPCValue = victimNPCValues[0];
            String victimNPCSign = victimNPCValues[1];

            attackerNPC.getOrAddTrait(SkinTrait.class).setSkinPersistent(UUID.randomUUID().toString(), victimNPCSign, victimNPCValue);
        }

        victimNPC.getOrAddTrait(SkinTrait.class).setSkinPersistent(UUID.randomUUID().toString(), derperinoNPCSign, derperinoNPCValue);

        attackerNPC.getOrAddTrait(PlayerFilter.class).setAllowlist();
        attackerNPC.getOrAddTrait(PlayerFilter.class).addPlayer(player.getUniqueId());

        attackerNPC.addTrait(Equipment.class);
        attackerNPC.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, new ItemStack(Material.IRON_SWORD));

        victimNPC.getOrAddTrait(PlayerFilter.class).setAllowlist();
        victimNPC.getOrAddTrait(PlayerFilter.class).addPlayer(player.getUniqueId());

        attackerNPC.spawn(killerSpawnLoc);
        victimNPC.spawn(victimLoc);

        if (attackerNPC.getEntity() != null) {
            attackerNPC.getEntity().setMetadata("NPC2", new FixedMetadataValue(CosmeticsPlugin.getInstance(), ""));
        }

        if (victimNPC.getEntity() != null) {
            victimNPC.getEntity().setMetadata("NPC1", new FixedMetadataValue(CosmeticsPlugin.getInstance(), ""));
        }

        attackerNPC.getNavigator().setTarget(killerTargetLoc);


        Run.delayed(() -> {
            victimNPC.despawn();
            killEffect.execute(player, player, victimLoc, false);
        }, 22L);

        return () -> {
            attackerNPC.destroy();
            victimNPC.destroy();
            registry.deregisterAll();
        };
    }
}