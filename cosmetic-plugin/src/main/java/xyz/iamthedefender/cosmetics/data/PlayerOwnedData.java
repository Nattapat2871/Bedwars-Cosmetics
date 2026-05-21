package xyz.iamthedefender.cosmetics.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;
import xyz.iamthedefender.cosmetics.api.cosmetics.Cosmetics;
import xyz.iamthedefender.cosmetics.api.cosmetics.CosmeticsType;
import xyz.iamthedefender.cosmetics.api.cosmetics.category.*;
import xyz.iamthedefender.cosmetics.util.StartupUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
public class PlayerOwnedData{
    private final UUID uuid;
    @Setter
    private int bedDestroy, deathCry, finalKillEffect, glyph, islandTopper, killMessage, projectileTrail, shopkeeperSkin, spray, victoryDance, woodSkin;

    public PlayerOwnedData (UUID uuid) {
        this.uuid = uuid;
        load();
    }

    public void load() {
        try {
            Connection connection = CosmeticsPlugin.getInstance().getRemoteDatabase().getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM player_owned_data WHERE uuid = ?");
            statement.setString(1, uuid.toString());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                bedDestroy = result.getInt("bed_destroy");
                deathCry = result.getInt("death_cry");
                finalKillEffect = result.getInt("final_kill_effect");
                glyph = result.getInt("glyph");
                islandTopper = result.getInt("island_topper");
                killMessage = result.getInt("kill_message");
                projectileTrail = result.getInt("projectile_trail");
                shopkeeperSkin = result.getInt("shopkeeper_skin");
                spray = result.getInt("spray");
                victoryDance = result.getInt("victory_dance");
                woodSkin = result.getInt("wood_skin");
                statement.close();
                connection.close();
            }else{
                demo();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void demo(){
        String sql = "INSERT INTO player_owned_data (uuid, bed_destroy, death_cry, final_kill_effect, glyph, island_topper, kill_message, projectile_trail, shopkeeper_skin, spray, victory_dance, wood_skin) " +
        "VALUES (?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);";

        try{
            Connection connection = CosmeticsPlugin.getInstance().getRemoteDatabase().getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
            statement.close();
            connection.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            Connection connection = CosmeticsPlugin.getInstance().getRemoteDatabase().getConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE player_owned_data SET bed_destroy = ?, death_cry = ?, final_kill_effect = ?, glyph = ?, island_topper = ?, kill_message = ?, projectile_trail = ?, shopkeeper_skin = ?, spray = ?, victory_dance = ?, wood_skin = ? WHERE uuid = ?;");
            statement.setInt(1, bedDestroy);
            statement.setInt(2, deathCry);
            statement.setInt(3, finalKillEffect);
            statement.setInt(4, glyph);
            statement.setInt(5, islandTopper);
            statement.setInt(6, killMessage);
            statement.setInt(7, projectileTrail);
            statement.setInt(8, shopkeeperSkin);
            statement.setInt(9, spray);
            statement.setInt(10, victoryDance);
            statement.setInt(11, woodSkin);
            statement.setString(12, uuid.toString());
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int calculateOwned(CosmeticsType type, List<? extends Cosmetics> list) {
        Player p = Bukkit.getPlayer(uuid);
        int count = 0;
        for (Cosmetics cosmetic : list) {
            if (OwnershipManager.hasOwnership(uuid, cosmetic.getIdentifier())) {
                count++;
                continue;
            }
            if (p != null) {
                if (p.hasPermission(type.getPermissionFormat() + ".*") || p.hasPermission(type.getPermissionFormat() + "." + cosmetic.getIdentifier())) {
                    count++;
                }
            }
        }
        return count;
    }

    public void updateOwned(){
        setBedDestroy(calculateOwned(CosmeticsType.BedBreakEffects, StartupUtils.bedDestroyList));
        setDeathCry(calculateOwned(CosmeticsType.DeathCries, StartupUtils.deathCryList));
        setFinalKillEffect(calculateOwned(CosmeticsType.FinalKillEffects, StartupUtils.finalKillList));
        setGlyph(calculateOwned(CosmeticsType.Glyphs, StartupUtils.glyphsList));
        setIslandTopper(calculateOwned(CosmeticsType.IslandToppers, StartupUtils.islandTopperList));
        setKillMessage(calculateOwned(CosmeticsType.KillMessages, StartupUtils.killMessageList));
        setProjectileTrail(calculateOwned(CosmeticsType.ProjectileTrails, StartupUtils.projectileTrailList));
        setShopkeeperSkin(calculateOwned(CosmeticsType.ShopKeeperSkins, StartupUtils.shopKeeperSkinList));
        setSpray(calculateOwned(CosmeticsType.Sprays, StartupUtils.sprayList));
        setVictoryDance(calculateOwned(CosmeticsType.VictoryDances, StartupUtils.victoryDancesList));
        setWoodSkin(calculateOwned(CosmeticsType.WoodSkins, StartupUtils.woodSkinsList));
        
        save();
    }
}
