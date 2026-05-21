package xyz.iamthedefender.cosmetics.api.bedwars;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.iamthedefender.cosmetics.api.handler.ITeamHandler;

import java.util.List;

public class RunningTeam implements ITeamHandler {
    private final String name;
    private final String color;
    private final List<Player> members;

    public RunningTeam(String name, String color, List<Player> members) {
        this.name = name;
        this.color = color;
        this.members = members;
    }

    @Override
    public Location getBed() {
        return null;
    }

    @Override
    public List<Player> getPlayers() {
        return members;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Location> getStoreLocations() {
        return null;
    }

    @Override
    public Location getSpawn() {
        return null;
    }

    @Override
    public int getSize() {
        return members.size();
    }

    public String getColor() {
        return color;
    }

    public List<Player> getMembers() {
        return members;
    }
}
