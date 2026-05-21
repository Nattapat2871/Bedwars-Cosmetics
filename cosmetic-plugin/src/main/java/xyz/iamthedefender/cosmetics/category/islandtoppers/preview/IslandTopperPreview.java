package xyz.iamthedefender.cosmetics.category.islandtoppers.preview;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;
import xyz.iamthedefender.cosmetics.api.configuration.ConfigManager;
import xyz.iamthedefender.cosmetics.api.cosmetics.CosmeticPreview;
import xyz.iamthedefender.cosmetics.api.cosmetics.Cosmetics;
import xyz.iamthedefender.cosmetics.api.cosmetics.CosmeticsType;
import xyz.iamthedefender.cosmetics.api.cosmetics.FieldsType;
import xyz.iamthedefender.cosmetics.api.cosmetics.RarityType;
import xyz.iamthedefender.cosmetics.api.cosmetics.category.IslandTopper;
import xyz.iamthedefender.cosmetics.api.menu.SystemGui;
import xyz.iamthedefender.cosmetics.api.util.BlockData;
import xyz.iamthedefender.cosmetics.api.util.ColorUtil;
import xyz.iamthedefender.cosmetics.api.util.Run;
import xyz.iamthedefender.cosmetics.api.util.config.ConfigUtils;
import xyz.iamthedefender.cosmetics.util.StartupUtils;

import java.io.File;
import java.util.*;

import static xyz.iamthedefender.cosmetics.util.StartupUtils.getCosmeticLocation;
import static xyz.iamthedefender.cosmetics.util.StartupUtils.getPlayerLocation;

public class IslandTopperPreview extends CosmeticPreview {

    public IslandTopperPreview() {
        super(CosmeticsType.IslandToppers);
    }

    @Override
    public void showPreview(Player player, Cosmetics selected, Location previewLocation, Location playerLocation) throws IllegalArgumentException {
        if (!(selected instanceof IslandTopper)) return;
        
        handleLocation(player, playerLocation);

        Location eyeLocation = playerLocation.clone().add(0, 1.6, 0);

        ArmorStand as = (ArmorStand) player.getWorld().spawnEntity(eyeLocation, EntityType.ARMOR_STAND);
        as.setVisible(false);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                100, 2));

        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1.equals(player)) continue;
            player1.hidePlayer(player);
        }

        CosmeticsPlugin.getInstance().getVersionSupport().sendCameraPacket(player, as);

        sendIslandTopper(player, previewLocation, selected.getIdentifier());

        setOnEnd(player, () -> {
            if (!as.isDead()) as.remove();
            CosmeticsPlugin.getInstance().getVersionSupport().sendCameraPacket(player, player);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            
            // Clean up blocks
            ConfigManager config = ConfigUtils.getIslandToppers();
            String topperFileName = config.getString(CosmeticsType.IslandToppers.getSectionKey() + "." + selected.getIdentifier() + ".file");
            if (topperFileName != null) {
                File file = new File(CosmeticsPlugin.getInstance().getHandler().getAddonPath() + "/IslandToppers/" + topperFileName);
                if (file.exists()) {
                    BlockFace direction = BlockFace.SELF;
                    try {
                        direction = BlockFace.valueOf(rpGetPlayerDirection(player));
                    } catch (Exception ignored) {}
                    Map<Location, BlockData> blockLocations = CosmeticsPlugin.getInstance().getWorldEditHandler().extractBlockData(file, previewLocation, player.getWorld(), direction);
                    for (Location loc : blockLocations.keySet()) {
                        player.sendBlockChange(loc, Material.AIR, (byte) 0);
                    }
                }
            }
        });
    }

    private void sendIslandTopper(Player player, Location location, String selected) {
        BlockFace direction = BlockFace.SELF;

        try {
            direction = BlockFace.valueOf(rpGetPlayerDirection(player));
        } catch (IllegalArgumentException ignored) {
        }

        ConfigManager config = ConfigUtils.getIslandToppers();
        String topperFileName = config.getString(CosmeticsType.IslandToppers.getSectionKey() + "." + selected + ".file");

        if (topperFileName == null) {
            Bukkit.getLogger().severe("Can't find file for " + selected + " island topper!");
            return;
        }

        File file = new File(CosmeticsPlugin.getInstance().getHandler().getAddonPath() + "/IslandToppers/" + topperFileName);
        if (!file.exists()) {
            Bukkit.getLogger().severe("The file " + file.getName() + " does not exist!");
            return;
        }

        // Extract block data from clipboard
        Map<Location, BlockData> blockLocations = CosmeticsPlugin.getInstance().getWorldEditHandler().extractBlockData(file, location, player.getWorld(), direction);
        if (blockLocations.isEmpty()) return;

        // Start animation
        boolean useOrder = CosmeticsPlugin.getInstance().getConfig().getBoolean("island-toppers.order");
        startBlockAnimation(player, blockLocations, useOrder);
    }


    private void startBlockAnimation(Player player, Map<Location, BlockData> blockLocations, boolean useOrder) {
        List<Location> locations = new ArrayList<>(blockLocations.keySet());

        new BukkitRunnable() {
            private int index = 0;

            @Override
            public void run() {
                if (blockLocations.isEmpty() || index >= locations.size()) {
                    cancel();
                    return;
                }

                Location loc = locations.get(index);
                BlockData blockData = blockLocations.get(loc);
                player.sendBlockChange(loc, blockData.getMaterial(), blockData.getData());

                index++;
            }
        }.runTaskTimerAsynchronously(CosmeticsPlugin.getInstance(), 0L, 0L);
    }

    private String rpGetPlayerDirection(Player playerSelf) {
        String dir;
        float y = playerSelf.getLocation().getYaw();
        if (y < 0) {
            y += 360;
        }
        y %= 360;
        int i = (int) ((y + 8) / 22.5);
        if (i == 0) {
            dir = "WEST";
        } else if (i == 1) {
            dir = "WEST_NORTHWEST";
        } else if (i == 2) {
            dir = "NORTHWEST";
        } else if (i == 3) {
            dir = "NORTH_NORTHWEST";
        } else if (i == 4) {
            dir = "NORTH";
        } else if (i == 5) {
            dir = "NORTH_NORTHEAST";
        } else if (i == 6) {
            dir = "NORTHEAST";
        } else if (i == 7) {
            dir = "EAST_NORTHEAST";
        } else if (i == 8) {
            dir = "EAST";
        } else if (i == 9) {
            dir = "EAST_SOUTHEAST";
        } else if (i == 10) {
            dir = "SOUTHEAST";
        } else if (i == 11) {
            dir = "SOUTH_SOUTHEAST";
        } else if (i == 12) {
            dir = "SOUTH";
        } else if (i == 13) {
            dir = "SOUTH_SOUTHWEST";
        } else if (i == 14) {
            dir = "SOUTHWEST";
        } else if (i == 15) {
            dir = "WEST_SOUTHWEST";
        } else {
            dir = "WEST";
        }
        return dir;
    }
}
