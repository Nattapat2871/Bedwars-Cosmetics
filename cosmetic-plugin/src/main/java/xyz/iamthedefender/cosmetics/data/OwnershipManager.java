package xyz.iamthedefender.cosmetics.data;

import org.bukkit.Bukkit;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class OwnershipManager {

    public static boolean hasOwnership(UUID uuid, String cosmeticId) {
        try (Connection connection = CosmeticsPlugin.getInstance().getRemoteDatabase().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM cosmetics_ownership WHERE uuid = ? AND cosmetic_id = ?")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, cosmeticId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to check cosmetic ownership: " + e.getMessage());
            return false;
        }
    }

    public static void addOwnership(UUID uuid, String cosmeticId) {
        try (Connection connection = CosmeticsPlugin.getInstance().getRemoteDatabase().getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT OR IGNORE INTO cosmetics_ownership (uuid, cosmetic_id) VALUES (?, ?)")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, cosmeticId);
            statement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to add cosmetic ownership: " + e.getMessage());
        }
    }
}
