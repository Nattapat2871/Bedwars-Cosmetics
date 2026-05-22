package xyz.iamthedefender.cosmetics.menu;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;
import xyz.iamthedefender.cosmetics.api.configuration.ConfigManager;
import xyz.iamthedefender.cosmetics.api.menu.impl.ChestSystemGui;
import xyz.iamthedefender.cosmetics.api.util.ItemBuilder;
import xyz.iamthedefender.cosmetics.api.util.Messages;
import xyz.iamthedefender.cosmetics.api.util.Utility;
import xyz.iamthedefender.cosmetics.api.util.ColorUtil;
import xyz.iamthedefender.cosmetics.util.MainMenuUtils;
import xyz.iamthedefender.cosmetics.util.StringUtils;

import java.util.List;

public class MainMenu extends ChestSystemGui {

    public MainMenu(Player player) {
        super(Messages.MAIN_MENU_GUI_TITLE.value(player), 6);
    }

    @Override
    public void onOpen(@NotNull Player player) {
        String loc = "Main-Menu";
        String langLoc = "cosmetics.main-menu";
        FileConfiguration config = CosmeticsPlugin.getInstance().getMenuData().getYml();

        for(String name : config.getConfigurationSection(loc).getKeys(false)) {
            try {
                ItemStack itemStack = ConfigManager.getItemStack(config, loc + "." + name + ".item");
                int slot = config.getInt(loc + "." + name + ".slot");
                boolean disabled = config.getBoolean(loc + "." + name + ".disabled");

                if (itemStack == null || disabled) continue;

                // Robust lookup: try direct, then sanitized, then hyphenated
                String itemName = null;
                List<String> lore = null;

                String[] lookups = {
                        langLoc + "." + name + ".name",
                        langLoc + "." + name.replace("-", "").replace("_", "").replace(" ", "") + ".name",
                        langLoc + "." + name.replaceAll("([a-z])([A-Z])", "$1-$2") + ".name",
                        langLoc + "." + name.toLowerCase() + ".name"
                };

                for (String lookup : lookups) {
                    String temp = Utility.getMSGLang(player, lookup);
                    if (temp != null && !temp.equals(lookup)) {
                        itemName = temp;
                        break;
                    }
                }

                String[] loreLookups = {
                        langLoc + "." + name + ".lore",
                        langLoc + "." + name.replace("-", "").replace("_", "").replace(" ", "") + ".lore",
                        langLoc + "." + name.replaceAll("([a-z])([A-Z])", "$1-$2") + ".lore",
                        langLoc + "." + name.toLowerCase() + ".lore"
                };

                for (String lookup : loreLookups) {
                    List<String> temp = Utility.getListLang(player, lookup);
                    if (temp != null && !temp.isEmpty() && !temp.get(0).equals(lookup)) {
                        lore = temp;
                        break;
                    }
                }

                // Fallback to name if still null
                if (itemName == null) {
                    itemName = ColorUtil.translate("&a" + StringUtils.replaceHyphensAndCaptalizeFirstLetter(name));
                }
                if (lore == null) {
                    lore = MainMenuUtils.getGenericLore();
                }

                List<String> formattedLore = MainMenuUtils.formatLore(player, lore, name);

                Bukkit.getLogger().info("[Cosmetics Debug] Setting menu item: " + name + " in slot: " + slot);
                
                super.setItem(slot, new ItemBuilder(itemStack)
                        .name(itemName)
                        .lore(formattedLore)
                        .build(), (e) -> {
                    Bukkit.getLogger().info("[Cosmetics Debug] Clicked menu item: " + name);
                    XSound.UI_BUTTON_CLICK.play((Player) e.getWhoClicked(), 0.3f, 1.0f);
                    MainMenuUtils.openMenus((Player) e.getWhoClicked(), name);
                });
            }catch (Exception exception){
                Bukkit.getLogger().warning("There was an error with main menu item: " + name);
                throw new RuntimeException(exception);
            }
        }
    }

    @Override
    public void onClose(Player player) {

    }
}
