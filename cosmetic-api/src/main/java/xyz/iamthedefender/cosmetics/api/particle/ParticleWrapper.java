package xyz.iamthedefender.cosmetics.api.particle;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedParticle;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.iamthedefender.cosmetics.api.cosmetics.Cosmetics;
import xyz.iamthedefender.cosmetics.api.util.Utility;
import xyz.iamthedefender.cosmetics.api.versionsupport.IVersionSupport;

import java.util.Objects;
import java.util.Optional;


@Getter
public class ParticleWrapper {

    private final @Nullable EnumWrappers.Particle wrapperParticle;
    private final @Nullable WrappedParticle<?> newWrapperParticle;
    private final @Nullable org.bukkit.Particle bukkitParticle;

    public ParticleWrapper(@Nullable EnumWrappers.Particle wrapperParticle, @Nullable WrappedParticle<?> newWrapperParticle, @Nullable org.bukkit.Particle bukkitParticle) {
        this.wrapperParticle = wrapperParticle;
        this.newWrapperParticle = newWrapperParticle;
        this.bukkitParticle = bukkitParticle;

        if (wrapperParticle == null && newWrapperParticle == null && bukkitParticle == null) {
            throw new IllegalArgumentException("All arguments cannot be null!");
        }
    }

    public ParticleWrapper(@Nullable EnumWrappers.Particle wrapperParticle) {
        this(wrapperParticle, null, null);
    }

    public ParticleWrapper(@Nullable WrappedParticle<?> newWrapperParticle) {
        this(null, newWrapperParticle, null);
    }

    public ParticleWrapper(@Nullable org.bukkit.Particle bukkitParticle) {
        this(null, null, bukkitParticle);
    }

    public org.bukkit.Particle getBukkitParticle() {
        if (bukkitParticle != null) {
            return bukkitParticle;
        }
        if (newWrapperParticle != null) {
            return newWrapperParticle.getParticle();
        }
        if (wrapperParticle != null) {
            try {
                return org.bukkit.Particle.valueOf(wrapperParticle.name());
            } catch (Exception ignored) {}
        }
        return null;
    }

    public @NotNull IVersionSupport support() {
        return Utility.getApi().getVersionSupport();
    }

    public static @NotNull Optional<ParticleWrapper> getParticle(@NotNull String name) {
        Objects.requireNonNull(name, "The particle name cannot be null!");

        name = name.toUpperCase();

        // Handle renames for 1.21+
        String originalName = name;
        if (name.equals("REDSTONE")) name = "DUST";
        if (name.equals("SMOKE_LARGE")) name = "LARGE_SMOKE";
        if (name.equals("BLOCK_DUST")) name = "BLOCK";
        if (name.equals("ITEM_CRACK")) name = "ITEM";
        if (name.equals("SPELL_WITCH")) name = "WITCH";
        if (name.equals("FIREWORKS_SPARK")) name = "FIREWORK";
        if (name.equals("VILLAGER_HAPPY")) name = "HAPPY_VILLAGER";
        if (name.equals("VILLAGER_ANGRY")) name = "ANGRY_VILLAGER";

        ParticleWrapper particleWrapper = null;

        try {
            Class.forName("org.bukkit.Particle");

            org.bukkit.Particle bParticle = null;
            try {
                bParticle = org.bukkit.Particle.valueOf(name);
            } catch (IllegalArgumentException e) {
                // Try original name if renamed failed
                bParticle = org.bukkit.Particle.valueOf(originalName);
            }
            particleWrapper = new ParticleWrapper(null, WrappedParticle.create(bParticle, null), bParticle);
        } catch (Exception exception) {
            try {
                particleWrapper = new ParticleWrapper(EnumWrappers.Particle.valueOf(originalName), null, null);
                Objects.requireNonNull(particleWrapper.getWrapperParticle());

                if (!Utility.getApi().getVersionSupport().isValidParticle(particleWrapper.getWrapperParticle().name())) {
                    throw new RuntimeException("Invalid particle: " + particleWrapper.getWrapperParticle().getName());
                }

            }catch (Exception exception1) {
                particleWrapper = null;
            }
        }


        return Optional.ofNullable(particleWrapper);
    }
}
