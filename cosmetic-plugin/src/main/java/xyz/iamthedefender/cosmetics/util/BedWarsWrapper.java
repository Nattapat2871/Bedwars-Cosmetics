package xyz.iamthedefender.cosmetics.util;

import com.tomkeuper.bedwars.api.arena.team.ITeam;
import xyz.iamthedefender.cosmetics.api.bedwars.RunningTeam;

public class BedWarsWrapper {

    public static RunningTeam wrap(ITeam team) {
        return new RunningTeam(team.getName(), team.getColor().name(), team.getMembers());
    }
}
