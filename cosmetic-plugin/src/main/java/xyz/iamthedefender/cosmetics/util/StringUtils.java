package xyz.iamthedefender.cosmetics.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StringUtils {


    public static List<String> formatLore(List<String> lores, String name, int price, String status, String rarity, String ownedCount){
        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));

        for(int i = 0; i < lores.size(); i++){
            String line = lores.get(i);
            if (line.contains("{status}")){
                line = line.replace("{status}", status);
            }
            if (line.contains("{name}")){
                line = line.replace("{name}", name.replace("-", " "));
            }
            if (line.contains("{cost}")){
                line = line.replace("{cost}", decimalFormat.format(price));
            }
            if (line.contains("{rarity}")){
                line = line.replace("{rarity}", rarity);
            }
            
            // Handle all variations of owned placeholders
            String[] ownedTags = {"{owned}", "{ownedSpray}", "{ownedspray}", "{ownedownerspary}", "{ownedsprays}", "{ownedpt}", "{ownedprojectiletrails}", "{ownedvd}", "{ownedvictorydances}", "{ownedfke}", "{ownedfinalkilleffects}", "{ownedit}", "{ownedislandtoppers}", "{ownedkm}", "{ownedkillmessages}", "{ownedbd}", "{ownedbedbreakeffects}", "{ownedws}", "{ownedwoodskin}", "{ownedwoodskins}", "{ownedgly}", "{ownedglyph}", "{ownedglyphs}", "{ownedsk}", "{ownedshopkeeper}", "{ownedshopkeeperskin}", "{ownedshopkeeperskins}", "{owneddc}", "{owneddeathcry}", "{owneddeathcries}", "{ownerspary}", "{ownedfinalkill}", "{ownedbbe}"};
            for (String tag : ownedTags) {
                if (line.contains(tag)) {
                    line = line.replace(tag, ownedCount);
                }
            }
            lores.set(i, line);
        }
        return new ArrayList<>(lores);
    }

    public static List<String> formatLore(List<String> lores, String name, int price, String status, String rarity){
        return formatLore(lores, name, price, status, rarity, "");
    }
    public static String replaceHyphensAndCaptalizeFirstLetter(String str) {
        if (str == null){
            return "&cDISABLED";
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        if (str == null){
            return str;
        }
        for (char c : str.toCharArray()) {
            if (c == '-') {
                result.append(' ');
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }
}
