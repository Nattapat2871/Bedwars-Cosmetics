package xyz.iamthedefender.cosmetics.category.victorydance;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;
import xyz.iamthedefender.cosmetics.api.cosmetics.CosmeticsType;
import xyz.iamthedefender.cosmetics.api.cosmetics.FieldsType;
import xyz.iamthedefender.cosmetics.api.cosmetics.RarityType;
import xyz.iamthedefender.cosmetics.api.cosmetics.category.VictoryDance;
import xyz.iamthedefender.cosmetics.api.event.VictoryDancesExecuteEvent;
import xyz.iamthedefender.cosmetics.util.DebugUtil;
import xyz.iamthedefender.cosmetics.util.StartupUtils;
import xyz.iamthedefender.cosmetics.api.util.Utility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VictoryDanceHandler2023 implements Listener {

    public static Map<UUID, VictoryDance> victoryDanceMap = new HashMap<>();

    @EventHandler
    public void onGameEnd2023(GameEndEvent e) {

        boolean isVictoryDancesEnabled = CosmeticsPlugin.getInstance().getConfig().getBoolean("victory-dances.enabled");
        if (!isVictoryDancesEnabled) return;

        IArena arena = e.getArena();

        for (UUID uuid : e.getWinners()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;

            // Safety check: is the player actually in this arena?
            if (!arena.isPlayer(p)) continue;
            String selected = CosmeticsPlugin.getInstance().getApi().getSelectedCosmetic(p, CosmeticsType.VictoryDances);
            VictoryDancesExecuteEvent event = new VictoryDancesExecuteEvent(p);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled())
                continue;

            DebugUtil.addMessage("Executing " + selected + " Victory Dance for " + p.getDisplayName());
            for(VictoryDance victoryDance : StartupUtils.victoryDancesList){
                if (selected.equals(victoryDance.getIdentifier())){
                    if (victoryDance.getField(FieldsType.RARITY, p) == RarityType.NONE) continue;
                    victoryDance.execute(p);

                    victoryDanceMap.put(uuid, victoryDance);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeaveArena(PlayerLeaveArenaEvent event) {
        Player player = event.getPlayer();

        VictoryDance dance = victoryDanceMap.remove(player.getUniqueId());
        if (dance != null) {
            dance.stopExecution(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        VictoryDance dance = victoryDanceMap.remove(player.getUniqueId());
        if (dance != null) {
            dance.stopExecution(player);
        }
    }
}
