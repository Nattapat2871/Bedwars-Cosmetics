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
import xyz.iamthedefender.cosmetics.util.MainMenuUtils;

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
                List<String> lore = Utility.getListLang(player, langLoc + "." + name + ".lore");
                String itemName = Utility.getMSGLang(player, langLoc + "." + name + ".name");
                int slot = config.getInt(loc + "." + name + ".slot");
                List<String> lores = MainMenuUtils.formatLore(player, lore, name);
                boolean disabled = config.getBoolean(loc + "." + name + ".disabled");

                // Translate for XItemStack
                ConfigurationSection configurationSection = new MemoryConfiguration();
                configurationSection.set("lore", lores);
                configurationSection.set("name", itemName);

                if (itemStack != null && !disabled) {
                    Bukkit.getLogger().info("[Cosmetics Debug] Setting menu item: " + name + " in slot: " + slot);
                    super.setItem(slot, XItemStack.edit(itemStack, configurationSection, s -> s, null), (e) -> {
                        Bukkit.getLogger().info("[Cosmetics Debug] Clicked menu item: " + name);
                        XSound.UI_BUTTON_CLICK.play((Player) e.getWhoClicked(), 0.3f, 1.0f);
                        MainMenuUtils.openMenus((Player) e.getWhoClicked(), name);
                    });
                } else if (itemStack == null) {
                    Bukkit.getLogger().warning("[Cosmetics Debug] ItemStack is null for item: " + name);
                }
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
