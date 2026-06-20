package xyz.iamthedefender.cosmetics.versionsupport;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import xyz.iamthedefender.cosmetics.api.particle.ParticleWrapper;
import xyz.iamthedefender.cosmetics.api.util.Utility;
import xyz.iamthedefender.cosmetics.api.versionsupport.IVersionSupport;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class VersionSupport_1_20 implements IVersionSupport {

    @Override
    public @NotNull String getVersion() {
        return "v1.20 handler";
    }

    @Override
    public ItemStack getSkull(String base64) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();

        if (head == null) throw new RuntimeException("Failed to get skull (v1.20)");

        ItemMeta itemMeta = head.getItemMeta();

        if (itemMeta == null) return head;

        itemMeta = XSkull.of(itemMeta).profile(Profileable.detect(base64)).lenient().apply();

        head.setItemMeta(itemMeta);

        return head;
    }

    @Override
    public ItemStack getSkull(Player player) {
        String skin = null;
        
        // 1. ลองดึงสกินโดยตรงจาก Profile ของผู้เล่นผ่าน Reflection (รองรับทั้งไอดีแท้และไอดีเถื่อนที่ผ่าน Bungee/Velocity)
        if (player != null) {
            try {
                Object profile = player.getClass().getMethod("getPlayerProfile").invoke(player);
                java.util.Collection<?> properties = (java.util.Collection<?>) profile.getClass().getMethod("getProperties").invoke(profile);
                for (Object prop : properties) {
                    String propName = (String) prop.getClass().getMethod("getName").invoke(prop);
                    if (propName.equalsIgnoreCase("textures")) {
                        skin = (String) prop.getClass().getMethod("getValue").invoke(prop);
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. ถ้าได้สกินมา ให้ใช้ XSkull สร้างหัว
        if (skin != null) {
            return getSkull(skin);
        }

        // 3. Fallback: ถ้าไม่ได้สกิน (เช่น ผู้เล่น offline หรือดึงไม่ได้) ให้ใช้ชื่อผู้เล่นแทน
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        if (head == null) return null;
        
        ItemMeta meta = head.getItemMeta();
        if (meta instanceof org.bukkit.inventory.meta.SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
            head.setItemMeta(skullMeta);
        }
        return head;
    }

    @Override
    public @NotNull ItemStack applyRenderer(MapRenderer mapRenderer, MapView mapView) {
        ItemStack map = XMaterial.FILLED_MAP.parseItem();
        mapView.getRenderers().forEach(mapView::removeRenderer);
        mapView.addRenderer(mapRenderer);
        MapMeta mapMeta = (MapMeta) map.getItemMeta();
        if (mapMeta == null) {
            Utility.getApi().getPlugin().getLogger().severe("Failed to apply renderer to map, map meta is null!");
            return map;
        }
        mapMeta.setMapView(mapView);
        map.setItemMeta(mapMeta);
        return map;
    }

    @Override
    public boolean isValidParticle(String name) {

        try {
            Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    private PlayerProfile getProfile(String url) {
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID()); // Get a new player profile
        PlayerTextures textures = profile.getTextures();
        URL urlObject;
        try {
            urlObject = new URL(url);
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Invalid URL", exception);
        }
        textures.setSkin(urlObject);
        profile.setTextures(textures);
        return profile;
    }

    private URL getUrlFromBase64(String base64) throws MalformedURLException {
        String decoded = new String(Base64.getDecoder().decode(base64));
        // We simply remove the "beginning" and "ending" part of the JSON, so we're left with only the URL. You could use a proper
        // JSON parser for this, but that's not worth it. The String will always start exactly with this stuff anyway
        return new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length()));
    }

    @Override
    public void displayParticle(Player player, Location location, ParticleWrapper particleWrapper, Color color) {
        Particle dustParticle;
        try {
            dustParticle = Particle.valueOf("DUST");
        } catch (IllegalArgumentException e) {
            dustParticle = Particle.valueOf("REDSTONE");
        }
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
        if (player != null) {
            player.spawnParticle(dustParticle, location, 1, 0, 0, 0, 0, dustOptions);
            return;
        }
        location.getWorld().spawnParticle(dustParticle, location, 1, 0, 0, 0, 0, dustOptions);
    }

    private Object getParticleData(Particle particle) {
        try {
            if (particle.getDataType() == org.bukkit.block.data.BlockData.class) {
                return org.bukkit.Bukkit.createBlockData(org.bukkit.Material.WHITE_WOOL);
            }
            if (particle.getDataType() == ItemStack.class) {
                return new ItemStack(org.bukkit.Material.WHITE_WOOL);
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void displayParticle(Player player, Location location, ParticleWrapper particle) {
        org.bukkit.Particle bukkitParticle = particle.getBukkitParticle();
        if (bukkitParticle == null) return;
        Object data = getParticleData(bukkitParticle);

        if (player != null) {
            player.spawnParticle(bukkitParticle, location, 1, data);
            return;
        }
        location.getWorld().spawnParticle(bukkitParticle, location, 1, data);
    }

    @Override
    public void displayParticle(Player player, Location location, ParticleWrapper particle, int count) {
        org.bukkit.Particle bukkitParticle = particle.getBukkitParticle();
        if (bukkitParticle == null) return;
        Object data = getParticleData(bukkitParticle);

        if (player != null) {
            player.spawnParticle(bukkitParticle, location, count, data);
            return;
        }
        location.getWorld().spawnParticle(bukkitParticle, location, count, data);
    }

    @Override
    public void displayParticle(Player player, Location location, ParticleWrapper particle, int count, float speed) {
        org.bukkit.Particle bukkitParticle = particle.getBukkitParticle();
        if (bukkitParticle == null) return;
        Object data = getParticleData(bukkitParticle);

        if (player != null) {
            player.spawnParticle(bukkitParticle, location, count, 0, 0, 0, speed, data);
            return;
        }
        location.getWorld().spawnParticle(bukkitParticle, location, count, 0, 0, 0, speed, data);
    }

    @Override
    public void displayParticle(Player player, Location location, ParticleWrapper particle, int count, float speed, Vector offset) {
        org.bukkit.Particle bukkitParticle = particle.getBukkitParticle();
        if (bukkitParticle == null) return;
        Object data = getParticleData(bukkitParticle);

        if (player != null) {
            player.spawnParticle(bukkitParticle, location, count, offset.getX(), offset.getY(), offset.getZ(), speed, data);
            return;
        }
        location.getWorld().spawnParticle(bukkitParticle, location, count, offset.getX(), offset.getY(), offset.getZ(), speed, data);
    }

    @Override
    public void sendCameraPacket(Player player, org.bukkit.entity.Entity entity) {
        try {
            // Get NMS Entity
            Object nmsEntity = entity.getClass().getMethod("getHandle").invoke(entity);

            // Create Packet
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetCameraPacket");
            // Constructor: public ClientboundSetCameraPacket(Entity entity)
            Object packet = packetClass.getConstructor(Class.forName("net.minecraft.world.entity.Entity")).newInstance(nmsEntity);

            // Get NMS Player and Connection
            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            
            // In 1.21.11, ServerPlayer.connection is field 'c' (ServerGamePacketListenerImpl)
            // But let's find it by type to be safe.
            java.lang.reflect.Field connectionField = null;
            for (java.lang.reflect.Field field : nmsPlayer.getClass().getFields()) {
                if (field.getType().getSimpleName().equals("ServerGamePacketListenerImpl")) {
                    connectionField = field;
                    break;
                }
            }
            if (connectionField == null) {
                // Fallback to searching all fields including private
                for (java.lang.reflect.Field field : nmsPlayer.getClass().getDeclaredFields()) {
                    if (field.getType().getSimpleName().equals("ServerGamePacketListenerImpl")) {
                        connectionField = field;
                        connectionField.setAccessible(true);
                        break;
                    }
                }
            }

            if (connectionField == null) throw new RuntimeException("Could not find connection field in ServerPlayer");
            
            Object connection = connectionField.get(nmsPlayer);

            // sendPacket is usually 'a' or 'b' or 'send'
            java.lang.reflect.Method sendMethod = null;
            for (java.lang.reflect.Method method : connection.getClass().getMethods()) {
                if ((method.getName().equals("send") || method.getName().equals("a") || method.getName().equals("b")) 
                    && method.getParameterCount() == 1 
                    && method.getParameterTypes()[0].getSimpleName().equals("Packet")) {
                    sendMethod = method;
                    break;
                }
            }

            if (sendMethod == null) throw new RuntimeException("Could not find sendPacket method in ServerGamePacketListenerImpl");

            sendMethod.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
