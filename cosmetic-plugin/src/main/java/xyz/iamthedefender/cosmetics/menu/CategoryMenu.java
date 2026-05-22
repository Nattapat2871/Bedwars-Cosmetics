package xyz.iamthedefender.cosmetics.menu;

import com.cryptomorin.xseries.XSound;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.iamthedefender.cosmetics.CosmeticsPlugin;
import xyz.iamthedefender.cosmetics.api.CosmeticsAPI;
import xyz.iamthedefender.cosmetics.api.configuration.ConfigManager;
import xyz.iamthedefender.cosmetics.api.cosmetics.Cosmetics;
import xyz.iamthedefender.cosmetics.api.cosmetics.CosmeticsType;
import xyz.iamthedefender.cosmetics.api.cosmetics.RarityType;
import xyz.iamthedefender.cosmetics.api.event.CosmeticPurchaseEvent;
import xyz.iamthedefender.cosmetics.api.menu.ClickableItem;
import xyz.iamthedefender.cosmetics.api.menu.impl.ChestSystemGui;
import xyz.iamthedefender.cosmetics.api.util.ColorUtil;
import xyz.iamthedefender.cosmetics.api.util.ItemBuilder;
import xyz.iamthedefender.cosmetics.api.util.Run;
import xyz.iamthedefender.cosmetics.api.util.Utility;
import xyz.iamthedefender.cosmetics.data.OwnershipManager;
import xyz.iamthedefender.cosmetics.util.DebugUtil;
import xyz.iamthedefender.cosmetics.util.StartupUtils;
import xyz.iamthedefender.cosmetics.util.StringUtils;
import xyz.iamthedefender.cosmetics.util.VaultUtils;
import xyz.iamthedefender.cosmetics.BwcAPI;
import xyz.iamthedefender.cosmetics.util.MainMenuUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CategoryMenu extends ChestSystemGui {

    ConfigManager config;
    CosmeticsType cosmeticsType;
    String originalTitle;
    String title;
    List<Integer> slots;
    int page;

    public CategoryMenu(CosmeticsType type, String title) {
        this(type, title, 1);
    }

    public CategoryMenu(CosmeticsType type, String title, int page) {
        super(title, 6);
        this.config = type.getConfig();
        this.cosmeticsType = type;
        this.originalTitle = title.split(" \\(")[0];
        this.title = title;
        this.slots = CosmeticsPlugin.getInstance().getHandler().getInventorySlots();
        this.page = page;

        Utility.getApi().getSystemGuiManager().setByPlayer(null, this); // Placeholder
    }

    @Override
    public void onOpen(@NotNull Player player) {
        ConfigManager configManager = cosmeticsType.getConfig();
        ConfigurationSection section = config.getYml().getConfigurationSection(cosmeticsType.getSectionKey());

        if (section == null) return;

        clearInventory();
        
        // Calculate owned/total for items and title
        xyz.iamthedefender.cosmetics.data.PlayerOwnedData ownedData = CosmeticsPlugin.getInstance().getPlayerManager().getPlayerOwnedData(player.getUniqueId());
        int ownedCount = 0;
        int totalCount = section.getKeys(false).size();
        
        for (String id : section.getKeys(false)) {
            if (player.hasPermission(cosmeticsType.getPermissionFormat() + "." + id) || OwnershipManager.hasOwnership(player.getUniqueId(), id)) {
                ownedCount++;
            }
        }
        
        String ownedProgress = "&a" + ownedCount + "&7/&a" + totalCount;
        
        // Update the title for the inventory (Ensure no duplication)
        this.title = originalTitle + ColorUtil.translate(" (" + ownedProgress + "&7)");

        Map<ClickableItem, RarityType> rarityMap = new HashMap<>();

        // Set up the items
        for(String id : section.getKeys(false)) {
            // set the variables
            String path = cosmeticsType.getSectionKey() + "." + id + ".";

            ItemStack stack = configManager.getItemStack(path + "item");
            int price = config.getInt(path + "price");
            String rarityStr = config.getString(path + "rarity");
            RarityType rarity = RarityType.COMMON;
            if (rarityStr != null) {
                try {
                    rarity = RarityType.valueOf(rarityStr.toUpperCase());
                } catch (Exception ignored) {}
            }
            // From language file
            String formattedName = Utility.getMSGLang(player, "cosmetics." + path + "name");
            List<String> lore = Utility.getListLang(player ,"cosmetics." + path + "lore");
            
            // Remove preview lore for Island Toppers
            if (cosmeticsType == CosmeticsType.IslandToppers) {
                lore.removeIf(line -> line.toLowerCase().contains("preview") || line.toLowerCase().contains("right-click"));
            }

            // Support variations of owned placeholders in sub-menus
            String finalOwnedProgress = ownedProgress;
            BwcAPI bwcApi = (BwcAPI) CosmeticsPlugin.getInstance().getApi();
            String selectedStr = "&a" + StringUtils.replaceHyphensAndCaptalizeFirstLetter(bwcApi.getSelectedCosmetic(player, cosmeticsType));
            
            lore = lore.stream().map(s -> {
                String result = s;
                // Owned tags
                String[] ownedTags = {"{ownedSpray}", "{ownedspray}", "{ownedgly}", "{ownedglyph}", "{ownedws}", "{ownedwoodskin}", "{ownedsk}", "{ownedshopkeeper}", "{ownedpt}", "{ownedvd}", "{ownedfke}", "{ownedbd}", "{ownedkm}", "{ownedit}", "{owned}", "{ownedfinalkill}", "{ownedbbe}"};
                for (String tag : ownedTags) {
                    result = result.replace(tag, finalOwnedProgress);
                }
                // Selected name tags (added for completeness)
                String[] selectedTags = {"{selected}", "{spraysselected}", "{spary}", "{spray}", "{sprays}", "{projectileselected}", "{projectiletrail}", "{projectile}", "{victorydanceselected}", "{victorydance}", "{victorydances}", "{finalkillselected}", "{finalkilleffect}", "{finalkill}", "{islandtopperselected}", "{islandtopper}", "{islandtoppers}", "{killmessageselected}", "{killmessage}", "{killmessages}", "{bedbreakselected}", "{bedbreak}", "{bedbreakeffect}", "{woodskin}", "{woodskins}", "{glyphsselected}", "{glyph}", "{glyphs}", "{shopkeeperselected}", "{shopkeeper}", "{shopkeeperskin}", "{shopkeeperskins}", "{deathcryselected}", "{deathcry}", "{deathcries}", "{victory}", "{killmsg}"};
                for (String tag : selectedTags) {
                    result = result.replace(tag, selectedStr);
                }
                return result;
            }).collect(Collectors.toList());

            lore = StringUtils.formatLore(lore, formattedName, price, getItemStatus(player, cosmeticsType, id, price), rarity.getChatColor() + rarity.toString(), ownedProgress);
            boolean disabled = config.getBoolean(path + "disabled");
            // Items
            ClickableItem item = null;
            List<String> lore1 = new ArrayList<>(lore);

            if (stack != null && !disabled) {
                String colorCode = "&a";
                int returnValue = onClick(player, cosmeticsType, price, id, true);
                if (returnValue == 2){
                    colorCode = "&c";
                }
                if (returnValue == -2 ){ // <- Selected
                    stack.addUnsafeEnchantment(Enchantment.FORTUNE, 1);
                }

                item = new ClickableItem(new ItemBuilder(stack)
                        .name(colorCode + formattedName)
                        .lore(lore1)
                        .itemFlag(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES)
                        .build(), (e) -> {

                    if (e.getClick() == ClickType.RIGHT) {
                        if (cosmeticsType != CosmeticsType.IslandToppers) {
                            XSound.UI_BUTTON_CLICK.play(player, 0.3f, 1.0f);
                        }
                        previewClick(player, cosmeticsType, id, price);
                        return;
                    }

                    if (e.getClick() == ClickType.LEFT){
                        XSound.UI_BUTTON_CLICK.play(player, 0.3f, 1.0f);
                        onClick(player, cosmeticsType, price, id, false);
                    }
                });
            }

            if (item != null) {
                rarityMap.put(item, rarity);
            }
        }

        if (CosmeticsPlugin.getInstance().getConfig().getBoolean("BackItemInCosmeticsMenu")) {
            setItem(49, new ItemBuilder().material(Material.ARROW).name("&aBack").build(), (e) -> {
                XSound.UI_BUTTON_CLICK.play((Player) e.getWhoClicked(), 0.3f, 1.0f);
                new MainMenu((Player) e.getWhoClicked()).open((Player) e.getWhoClicked());
            });
        }

        createPages(rarityMap);
    }

    @Override
    public void onClose(Player player) {
        // Cleanup if needed
    }


    public int findFirstEmptySlot(Inventory inventory) {
        for (Integer slot : slots) {
            if (inventory.getItem(slot) == null) {
                return slot;
            }
        }
        return -1;
    }

    public void createPages(Map<ClickableItem, RarityType> rarityMap) {
        int itemsPerPage = slots.size();
        List<ClickableItem> allItems = new ArrayList<>(rarityMap.keySet());
        int totalPages = (int) Math.ceil((double) allItems.size() / itemsPerPage);

        int itemStartIndex = (page - 1) * itemsPerPage;
        int itemEndIndex = Math.min(itemStartIndex + itemsPerPage, allItems.size());

        List<ClickableItem> pageItems = allItems.subList(itemStartIndex, itemEndIndex);

        if(page < totalPages) {
            setItem(51, new ItemBuilder().material(Material.ARROW).name("&aNext page").build(), (e) -> {
                XSound.UI_BUTTON_CLICK.play((Player) e.getWhoClicked(), 0.3f, 1.0f);
                new CategoryMenu(cosmeticsType, title, page + 1).open((Player) e.getWhoClicked());
            });
        }

        if(page > 1) {
            setItem(47, new ItemBuilder().material(Material.ARROW).name("&aPrevious page").build(), (e) -> {
                XSound.UI_BUTTON_CLICK.play((Player) e.getWhoClicked(), 0.3f, 1.0f);
                new CategoryMenu(cosmeticsType, title, page - 1).open((Player) e.getWhoClicked());
            });
        }

        Map<ClickableItem, RarityType> rarityMapNew = new HashMap<>();
        for (ClickableItem item : pageItems) {
            rarityMapNew.put(item, rarityMap.get(item));
        }

        addItemsAccordingToRarity(rarityMapNew);
    }


    private void addItemsAccordingToRarity(Map<ClickableItem, RarityType> items) {
        List<ClickableItem> none = new ArrayList<>();
        List<ClickableItem> common = new ArrayList<>();
        List<ClickableItem> rare = new ArrayList<>();
        List<ClickableItem> epic = new ArrayList<>();
        List<ClickableItem> legendary = new ArrayList<>();

        for(ClickableItem item : items.keySet()){
            switch (items.get(item)){
                case NONE:
                    none.add(item);
                    break;
                case COMMON:
                    common.add(item);
                    break;
                case RARE:
                    rare.add(item);
                    break;
                case EPIC:
                    epic.add(item);
                    break;
                case LEGENDARY:
                    legendary.add(item);
                    break;
            }
        }

        for (ClickableItem item : none) {
            int slot = findFirstEmptySlot(getInventory());
            if (slot != -1) setItem(slot, item);
        }
        for (ClickableItem item : legendary) {
            int slot = findFirstEmptySlot(getInventory());
            if (slot != -1) setItem(slot, item);
        }
        for (ClickableItem item : epic) {
            int slot = findFirstEmptySlot(getInventory());
            if (slot != -1) setItem(slot, item);
        }
        for (ClickableItem item : rare) {
            int slot = findFirstEmptySlot(getInventory());
            if (slot != -1) setItem(slot, item);
        }
        for (ClickableItem item : common) {
            int slot = findFirstEmptySlot(getInventory());
            if (slot != -1) setItem(slot, item);
        }
    }

    public String getItemStatus(Player p, CosmeticsType type, String id, int price) {
        String permission = type.getPermissionFormat() + "." + id;
        if (p.hasPermission(permission) || OwnershipManager.hasOwnership(p.getUniqueId(), id)) {
            BwcAPI api = (BwcAPI) CosmeticsPlugin.getInstance().getApi();
            if (api.getSelectedCosmetic(p, type).equals(id)) {
                return "&aSELECTED";
            }
            return "&eClick to select!";
        } else {
            return "&cCost: " + price;
        }
    }

    public int onClick(Player p, CosmeticsType type, int price, String id, boolean isOnlyForCheck) {
        Economy eco = CosmeticsPlugin.getInstance().getEconomy();
        Permission perm = VaultUtils.getPermissions();
        BwcAPI api = (BwcAPI) CosmeticsPlugin.getInstance().getApi();
        String permissionFormat = type.getPermissionFormat();

        if (p.hasPermission(permissionFormat + "." + id) || OwnershipManager.hasOwnership(p.getUniqueId(), id)) {
            if (api.getSelectedCosmetic(p, type).equals(id)) {
                return -2;
            }
            if (isOnlyForCheck) return 1;
            api.setSelectedCosmetic(p, type, id);
            XSound.UI_BUTTON_CLICK.play(p, 0.3f, 1.0f);
            new CategoryMenu(cosmeticsType, title, page).open(p);
            return -2;
        }

        if (price > eco.getBalance(p)) {
            if (isOnlyForCheck) return 2;
            p.sendMessage(ColorUtil.translate("&cYou don't have enough money!"));
            XSound.ENTITY_VILLAGER_NO.play(p, 1.0f, 1.0f);
            return 2;
        }

        if (isOnlyForCheck) return 0;

        CosmeticPurchaseEvent event = new CosmeticPurchaseEvent(p, type);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            if (perm != null) perm.playerAdd(p, permissionFormat + "." + id);
            OwnershipManager.addOwnership(p.getUniqueId(), id);
            
            // Force update owned data for UI
            CosmeticsPlugin.getInstance().getPlayerManager().getPlayerOwnedData(p.getUniqueId()).updateOwned();

            api.setSelectedCosmetic(p, type, id);
            eco.withdrawPlayer(p, price);
            p.playSound(p.getLocation(), XSound.ENTITY_VILLAGER_YES.parseSound(), 1.0f, 1.0f);
            
            String name = Utility.getMSGLang(p, "cosmetics." + type.getSectionKey() + "." + id + ".name");
            p.sendMessage(ColorUtil.translate("&aYou have purchased " + name + " for " + price + " coins!"));
            
            new CategoryMenu(cosmeticsType, title, page).open(p);

            DebugUtil.addMessage("Selected " + id + " for " + type + " and paid " + price + " coins");
            return -2;
        }

        return -1;
    }


    public void previewClick(Player player, CosmeticsType type, String id, int price){
        if (type == CosmeticsType.IslandToppers) {
            onClick(player, type, price, id, false);
            return;
        }

        Cosmetics cosmetics = CosmeticsPlugin.findCosmetic(id, type);

        if (cosmetics == null) return;

        if (cosmetics.getRarity() == RarityType.NONE) {
            onClick(player, type, price, id, false);
            return;
        }

        AtomicBoolean found = new AtomicBoolean(false);

        Location previewLocation = StartupUtils.getCosmeticLocation();
        Location playerLocation = StartupUtils.getPlayerLocation();

        if (previewLocation == null || playerLocation == null) {
            player.sendMessage(ColorUtil.translate("&cPreview location is not set! Contact staff!"));
            return;
        }

        CosmeticsPlugin.getInstance().getPreviewList().stream().filter(cosmeticPreview -> cosmeticPreview.getType() == type).findFirst().ifPresent(cosmeticPreview -> {
            cosmeticPreview.showPreview(player, cosmetics, previewLocation, playerLocation);
            found.set(true);
        });

        if (found.get()) return;

        onClick(player, type, price, id, false);
    }
}
