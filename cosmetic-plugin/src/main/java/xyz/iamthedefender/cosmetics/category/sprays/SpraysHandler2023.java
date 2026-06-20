package xyz.iamthedefender.cosmetics.category.sprays;

import com.cryptomorin.xseries.XMaterial;
import xyz.iamthedefender.cosmetics.api.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.inventory.ItemStack;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;
import xyz.iamthedefender.cosmetics.api.cosmetics.CosmeticsType;
import xyz.iamthedefender.cosmetics.api.cosmetics.category.Spray;
import xyz.iamthedefender.cosmetics.api.util.Messages;
import xyz.iamthedefender.cosmetics.util.StartupUtils;

public class SpraysHandler2023 implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent e) {

        boolean isSpraysEnabled = CosmeticsPlugin.getInstance().getConfig().getBoolean("sprays.enabled");
        if (!isSpraysEnabled) return;

        Player p = e.getPlayer();
        Entity clicked = e.getRightClicked();
        
        ItemFrame itemFrame = null;
        if (clicked instanceof ItemFrame) {
            itemFrame = (ItemFrame) clicked;
        } else if (clicked instanceof ArmorStand && clicked.hasMetadata("HOLO_ITEM_FRAME")) {
            // Find the item frame this armor stand is labeling
            for (Entity nearby : clicked.getNearbyEntities(0.5, 1.5, 0.5)) {
                if (nearby instanceof ItemFrame) {
                    itemFrame = (ItemFrame) nearby;
                    break;
                }
            }
        }

        if (itemFrame != null) {
            String selected = CosmeticsPlugin.getInstance().getApi().getSelectedCosmetic(p, CosmeticsType.Sprays);
            if (selected == null || selected.isEmpty()) {
                p.sendMessage(ColorUtil.translate("&cYou don't have a spray selected!"));
                return;
            }

            ItemStack frameItem = itemFrame.getItem();
            XMaterial material = frameItem == null ? XMaterial.AIR : XMaterial.matchXMaterial(frameItem);
            
            // Allow if empty (AIR) or already a map
            if (material == XMaterial.AIR || material == XMaterial.MAP || material == XMaterial.FILLED_MAP) {
                boolean found = false;
                for(Spray spray : StartupUtils.sprayList){
                    if (spray.getIdentifier().equals(selected)){
                        spray.execute(p, itemFrame);
                        found = true;
                        break;
                    }
                }
                if (found) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onGameStart2023(com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent event) {

        boolean isSpraysEnabled = CosmeticsPlugin.getInstance().getConfig().getBoolean("sprays.enabled");
        if (!isSpraysEnabled) return;

        if (event.getNewState().name().equals("playing")) {
            for (final Entity e : event.getArena().getWorld().getEntities()) {
                if (e.getType() == EntityType.ITEM_FRAME) {
                    ItemFrame itemFrame = (ItemFrame) e;
                    if (itemFrame.getItem() == null || itemFrame.getItem().getType() == Material.AIR) {
                        ArmorStand stand = (ArmorStand) e.getWorld().spawnEntity(e.getLocation().subtract(0.0, 0.9, 0.0), EntityType.ARMOR_STAND);
                        stand.setVisible(false);
                        stand.setGravity(false);
                        stand.setCustomName(Messages.SPRAY_CLICK.value(null));
                        stand.setMetadata("HOLO_ITEM_FRAME", new FixedMetadataValue(CosmeticsPlugin.getInstance(), ""));
                        stand.setCustomNameVisible(true);
                        stand.setMarker(true);
                        stand.setSmall(true);
                    }
                }
            }
        }
    }
}
