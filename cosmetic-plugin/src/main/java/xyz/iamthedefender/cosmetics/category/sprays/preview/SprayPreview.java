package xyz.iamthedefender.cosmetics.category.sprays.preview;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.cryptomorin.xseries.XSound;
import xyz.iamthedefender.cosmetics.api.cosmetics.*;
import xyz.iamthedefender.cosmetics.api.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;
import xyz.iamthedefender.cosmetics.api.cosmetics.category.Spray;
import xyz.iamthedefender.cosmetics.api.menu.SystemGui;
import xyz.iamthedefender.cosmetics.api.util.Run;
import xyz.iamthedefender.cosmetics.category.sprays.util.SpraysUtil;
import xyz.iamthedefender.cosmetics.util.StartupUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static xyz.iamthedefender.cosmetics.util.StartupUtils.getCosmeticLocation;
import static xyz.iamthedefender.cosmetics.util.StartupUtils.getPlayerLocation;

public class SprayPreview extends CosmeticPreview {

    private final Map<UUID, Map<Integer, org.bukkit.inventory.ItemStack>> inventories = new HashMap<>();

    public SprayPreview() {
        super(CosmeticsType.Sprays);
    }

    @Override
    public void showPreview(Player player, xyz.iamthedefender.cosmetics.api.cosmetics.Cosmetics selected, Location previewLocation, Location playerLocation) throws IllegalArgumentException{
        if (!(selected instanceof Spray)) return;

        Spray spray = (Spray) selected;

        if (spray.getField(FieldsType.RARITY, player) == RarityType.NONE) {
            XSound.ENTITY_VILLAGER_NO.play(player, 1.0f, 1.0f);
            return;
        }

        if (previewLocation == null || playerLocation == null) {
            throw new IllegalArgumentException("Preview location or Player location is not set!");
        }

        handleLocation(player, playerLocation);

        final ArmorStand as = (ArmorStand) player.getWorld().spawnEntity(playerLocation, EntityType.ARMOR_STAND);
        as.setVisible(false);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                100, 2));

        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (player1.equals(player)) continue;
            player1.hidePlayer(player);
        }

        Location eyeLocation = playerLocation.clone().add(0, 1.6, 0);
        
        // กำหนดตำแหน่งสำหรับวาง Item Frame
        Location frameLoc = eyeLocation.clone().add(playerLocation.getDirection().multiply(2));
        
        BlockFace playerFacing = getCardinalDirection(playerLocation);
        BlockFace frameFacing = playerFacing.getOppositeFace(); 
        
        // ถอย Barrier ไปอยู่ด้านหลังของ Item Frame 1 บล็อก โดยอิงจากทิศที่ผู้เล่นมอง
        Location barrierLoc = frameLoc.getBlock().getRelative(playerFacing).getLocation();
        
        barrierLoc.getBlock().setType(Material.BARRIER);
        barrierLoc.getChunk().load(true);
        
        // เสก ItemFrame ที่ตำแหน่ง frameLoc ซึ่งจะเกาะกับ Barrier ที่อยู่ข้างหลังพอดี
        final ItemFrame frame = (ItemFrame) player.getWorld().spawnEntity(frameLoc, EntityType.ITEM_FRAME);

        final PacketAdapter adapter = new PacketAdapter(CosmeticsPlugin.getInstance(), PacketType.Play.Server.SPAWN_ENTITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacket().getIntegers().read(0) == frame.getEntityId() &&
                !event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        };

        CosmeticsPlugin.getInstance().getProtocolManager().addPacketListener(adapter);
        
        frame.setFacingDirection(frameFacing, true);
        
        SpraysUtil.spawnSprays(player, frame, true, (Spray) selected);

        XSound.ENTITY_SILVERFISH_HURT.play(player, 10f, 10f);

        CosmeticsPlugin.getInstance().getVersionSupport().sendCameraPacket(player, as);

        setOnEnd(player, () -> {
            if (!as.isDead()) as.remove();

            if (frame.isValid()) {
                frame.setItem(new ItemStack(Material.AIR));
                frame.remove();
            }
            // ลบ Barrier ออกจากตำแหน่งที่ถอยไปแล้ว
            barrierLoc.getBlock().setType(Material.AIR);
            CosmeticsPlugin.getInstance().getProtocolManager().removePacketListener(adapter);
            CosmeticsPlugin.getInstance().getVersionSupport().sendCameraPacket(player, player);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        });
    }

    public static BlockFace getCardinalDirection(Location location) {
        float yaw = location.getYaw();
        if (yaw < 0) yaw += 360;
        yaw %= 360;
        if (yaw <= 45 || yaw >= 315) return BlockFace.SOUTH;
        if (yaw > 45 && yaw < 135) return BlockFace.WEST;
        if (yaw >= 135 && yaw <= 225) return BlockFace.NORTH;
        if (yaw > 225 && yaw < 315) return BlockFace.EAST;
        return BlockFace.SOUTH;
    }
}