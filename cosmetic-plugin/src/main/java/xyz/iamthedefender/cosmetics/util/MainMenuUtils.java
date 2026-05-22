package xyz.iamthedefender.cosmetics.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;
import xyz.iamthedefender.cosmetics.api.configuration.ConfigManager;
import xyz.iamthedefender.cosmetics.api.cosmetics.CosmeticsType;
import xyz.iamthedefender.cosmetics.api.util.ColorUtil;
import xyz.iamthedefender.cosmetics.api.util.Constants;
import xyz.iamthedefender.cosmetics.api.util.Messages;
import xyz.iamthedefender.cosmetics.api.util.Utility;
import xyz.iamthedefender.cosmetics.api.util.config.ConfigUtils;
import xyz.iamthedefender.cosmetics.menu.CategoryMenu;
import xyz.iamthedefender.cosmetics.BwcAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainMenuUtils {

    public static List<String> getGenericLore() {
        return Arrays.asList("&7Unlocked: &a{owned}", "&7Currently Selected: &a{selected}", "", "&eClick to view.");
    }

    public static void saveLores() {
        List<String> genericLore = getGenericLore();
        
        saveVariation("Sprays", "&aSprays", genericLore);
        saveVariation("ProjectileTrails", "&aProjectile Trails", genericLore);
        saveVariation("FinalKillEffects", "&aFinal Kill Effects", genericLore);
        saveVariation("KillMessages", "&aKill Messages", genericLore);
        saveVariation("Glyphs", "&aGlyphs", genericLore);
        saveVariation("BedBreakEffects", "&aBed Destroys", genericLore);
        saveVariation("WoodSkins", "&aWood Skins", genericLore);
        saveVariation("VictoryDances", "&aVictory Dances", genericLore);
        saveVariation("IslandToppers", "&aIsland Toppers", genericLore);
        saveVariation("ShopKeeperSkins", "&aShopKeeper Skins", genericLore);
        saveVariation("DeathCries", "&aDeath Cries", genericLore);

        Utility.saveIfNotExistsLang("cosmetics.main-menu.Back.lore", Collections.singletonList("&cClick to close."));
        Utility.saveIfNotExistsLang("cosmetics.main-menu.Back.name", "&cBack");

        Utility.saveIfNotExistsLang("cosmetics.main-menu.Balance.lore", Arrays.asList("&7Your balance: &a{balance}", "", "&eClick to view your balance."));
        Utility.saveIfNotExistsLang("cosmetics.main-menu.Balance.name", "&aBalance");
    }
    
    private static void saveVariation(String key, String name, List<String> lore) {
        Utility.saveIfNotExistsLang("cosmetics.main-menu." + key + ".name", name);
        Utility.saveIfNotExistsLang("cosmetics.main-menu." + key + ".lore", lore);
        // Also save with hyphens just in case
        String hyphenated = key.replaceAll("([a-z])([A-Z])", "$1-$2");
        if (!hyphenated.equals(key)) {
            Utility.saveIfNotExistsLang("cosmetics.main-menu." + hyphenated + ".name", name);
            Utility.saveIfNotExistsLang("cosmetics.main-menu." + hyphenated + ".lore", lore);
        }
    }

    public static String getOwnedTotal(Player p, CosmeticsType type) {
        xyz.iamthedefender.cosmetics.data.PlayerOwnedData data = CosmeticsPlugin.getInstance().getPlayerManager().getPlayerOwnedData(p.getUniqueId());
        int owned = 0;
        int total = 0;
        
        switch (type) {
            case Sprays: owned = data.getSpray(); total = StartupUtils.sprayList.size(); break;
            case ShopKeeperSkins: owned = data.getShopkeeperSkin(); total = StartupUtils.shopKeeperSkinList.size(); break;
            case ProjectileTrails: owned = data.getProjectileTrail(); total = StartupUtils.projectileTrailList.size(); break;
            case Glyphs: owned = data.getGlyph(); total = StartupUtils.glyphsList.size(); break;
            case FinalKillEffects: owned = data.getFinalKillEffect(); total = StartupUtils.finalKillList.size(); break;
            case BedBreakEffects: owned = data.getBedDestroy(); total = StartupUtils.bedDestroyList.size(); break;
            case WoodSkins: owned = data.getWoodSkin(); total = StartupUtils.woodSkinsList.size(); break;
            case VictoryDances: owned = data.getVictoryDance(); total = StartupUtils.victoryDancesList.size(); break;
            case KillMessages: owned = data.getKillMessage(); total = StartupUtils.killMessageList.size(); break;
            case IslandToppers: owned = data.getIslandTopper(); total = StartupUtils.islandTopperList.size(); break;
            case DeathCries: owned = data.getDeathCry(); total = StartupUtils.deathCryList.size(); break;
        }
        
        return "&a" + owned + "&7/&a" + total;
    }

    public static List<String> formatLore(Player p, List<String> lore, String name) {
        if (lore == null) return new ArrayList<>();
        BwcAPI api = (BwcAPI) CosmeticsPlugin.getInstance().getApi();

        CosmeticsType type = CosmeticsType.fromName(name);
        String selectedStr = "";
        String ownedProgress = "";
        if (type != null) {
            selectedStr = "&a" + StringUtils.replaceHyphensAndCaptalizeFirstLetter(api.getSelectedCosmetic(p, type));
            ownedProgress = getOwnedTotal(p, type);
        }

        List<String> formatted = new ArrayList<>();
        boolean unlockedFound = false;

        for (String line : lore) {
            String s = line;

            if (type != null) {
                // Unlocked count replacements
                String[] ownedTags = {"{owned}", "{ownedSpray}", "{ownedspray}", "{ownedownerspary}", "{ownedsprays}", "{ownedpt}", "{ownedprojectiletrails}", "{ownedvd}", "{ownedvictorydances}", "{ownedfke}", "{ownedfinalkilleffects}", "{ownedit}", "{ownedislandtoppers}", "{ownedkm}", "{ownedkillmessages}", "{ownedbd}", "{ownedbedbreakeffects}", "{ownedws}", "{ownedwoodskin}", "{ownedwoodskins}", "{ownedgly}", "{ownedglyph}", "{ownedglyphs}", "{ownedsk}", "{ownedshopkeeper}", "{ownedshopkeeperskin}", "{ownedshopkeeperskins}", "{owneddc}", "{owneddeathcry}", "{owneddeathcries}", "{ownerspary}", "{ownedfinalkill}", "{ownedbbe}"};
                for (String tag : ownedTags) {
                    s = s.replace(tag, ownedProgress);
                }

                // Selected name replacements
                String[] selectedTags = {"{selected}", "{spraysselected}", "{spary}", "{spray}", "{sprays}", "{projectileselected}", "{projectiletrail}", "{projectile}", "{victorydanceselected}", "{victorydance}", "{victorydances}", "{finalkillselected}", "{finalkilleffect}", "{finalkill}", "{islandtopperselected}", "{islandtopper}", "{islandtoppers}", "{killmessageselected}", "{killmessage}", "{killmessages}", "{bedbreakselected}", "{bedbreak}", "{bedbreakeffect}", "{woodskin}", "{woodskins}", "{glyphsselected}", "{glyph}", "{glyphs}", "{shopkeeperselected}", "{shopkeeper}", "{shopkeeperskin}", "{shopkeeperskins}", "{deathcryselected}", "{deathcry}", "{deathcries}", "{victory}", "{killmsg}"};
                for (String tag : selectedTags) {
                    s = s.replace(tag, selectedStr);
                }
            }

            if (s.toLowerCase().contains("unlocked") || s.toLowerCase().contains("owned") || (!ownedProgress.isEmpty() && s.contains(ownedProgress))) {
                unlockedFound = true;
            }

            // Balance
            if (s.contains("{balance}")) {
                String balance = "%vault_eco_balance_commas%";
                if (CosmeticsPlugin.isPlaceholderAPI()) {
                    balance = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, balance);
                } else {
                    balance = String.valueOf(CosmeticsPlugin.getInstance().getEconomy().getBalance(p));
                }
                s = s.replace("{balance}", balance);
            }

            // PAPI
            if (CosmeticsPlugin.isPlaceholderAPI()) {
                s = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, s);
            }

            formatted.add(ColorUtil.translate(s));
        }

        // Auto-inject Unlocked if missing
        if (!unlockedFound && type != null) {
            int selectedIndex = -1;
            for (int i = 0; i < formatted.size(); i++) {
                String strip = ChatColor.stripColor(formatted.get(i)).toLowerCase();
                if (strip.contains("selected")) {
                    selectedIndex = i;
                    break;
                }
            }
            if (selectedIndex != -1) {
                formatted.add(selectedIndex, ColorUtil.translate("&7Unlocked: " + ownedProgress));
            } else {
                formatted.add(0, ColorUtil.translate("&7Unlocked: " + ownedProgress));
            }
        }

        return formatted;
    }

    public static void openMenus(Player p, String name) {
        CosmeticsType type = CosmeticsType.fromName(name);

        if (name.equals("Back")) {
            String command = CosmeticsPlugin.getInstance().getMenuData().getYml().getString("Main-Menu.Back.custom-command");
            if (command == null) {
                p.getOpenInventory().close();
            } else {
                Bukkit.dispatchCommand(p, command);
            }
            return;
        }

        if (type == null) return;

        String title = Messages.of(type.name().toLowerCase() + Constants.Key.COSMETICS_MENU_TITLES_SUFFIX, type.getFormatedName()).value(p);

        new CategoryMenu(type, title).open(p);
    }
}
