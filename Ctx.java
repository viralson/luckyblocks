package pl.lucky;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Kontekst otwarcia lucky blocka – pomocnicze metody dla efektów. */
public final class Ctx {

    public final LuckyPlugin pl;
    public final Player p;
    public final Location loc;   // środek zniszczonego bloku
    public final Tier tier;
    public final World w;

    public Ctx(LuckyPlugin pl, Player p, Location loc, Tier tier) {
        this.pl = pl;
        this.p = p;
        this.loc = loc;
        this.tier = tier;
        this.w = loc.getWorld();
    }

    public int m() { return tier.mult; }

    public static int r(int min, int max) { return max <= min ? min : ThreadLocalRandom.current().nextInt(min, max + 1); }

    public static double rd() { return ThreadLocalRandom.current().nextDouble(); }

    public static <T> T pick(List<T> l) { return l.get(ThreadLocalRandom.current().nextInt(l.size())); }

    // ------------------------------------------------------------------ przedmioty

    /** Wyrzuca przedmioty z bloku (z efektem „wystrzału”). */
    public void give(ItemStack... items) {
        for (ItemStack it : items) {
            if (it == null || it.getType().isAir()) continue;
            int left = it.getAmount();
            while (left > 0) {
                ItemStack part = it.clone();
                int n = Math.min(left, it.getMaxStackSize());
                part.setAmount(n);
                left -= n;
                Item drop = w.dropItem(loc.clone().add(0, 0.3, 0), part);
                drop.setVelocity(new Vector((rd() - 0.5) * 0.25, 0.35 + rd() * 0.15, (rd() - 0.5) * 0.25));
            }
        }
    }

    public static ItemStack it(Material m, int amount) { return new ItemStack(m, Math.max(1, amount)); }

    /** Przedmiot z nazwą i enchantami (pary: Enchantment, poziom). */
    public static ItemStack named(Material m, String name, Object... enchants) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        if (name != null) meta.displayName(Msg.item(name));
        for (int i = 0; i + 1 < enchants.length; i += 2) {
            meta.addEnchant((Enchantment) enchants[i], (Integer) enchants[i + 1], true);
        }
        it.setItemMeta(meta);
        return it;
    }

    /** Deszcz przedmiotów z nieba wokół gracza. */
    public void rain(Supplier<ItemStack> item, int count, int radius) {
        for (int i = 0; i < count; i++) {
            int delay = i * 2;
            later(delay, () -> {
                Location l = loc.clone().add(r(-radius, radius), 12 + rd() * 4, r(-radius, radius));
                w.dropItem(l, item.get());
                w.spawnParticle(Particle.FIREWORK, l, 3, 0.1, 0.1, 0.1, 0.02);
            });
        }
    }

    // ------------------------------------------------------------------ istoty

    public <T extends Entity> List<T> spawn(Class<T> type, int count, Consumer<T> setup) {
        List<T> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Location l = loc.clone().add((rd() - 0.5) * 4, 0.2, (rd() - 0.5) * 4);
            if (l.getBlock().isSolid()) l = loc.clone().add(0, 0.2, 0);
            T e = w.spawn(l, type, x -> {
                if (setup != null) setup.accept(x);
                x.getPersistentDataContainer().set(pl.mobKey(), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            });
            if (e instanceof LivingEntity le) le.setRemoveWhenFarAway(true);
            out.add(e);
        }
        w.spawnParticle(Particle.LARGE_SMOKE, loc, 20, 1, 0.5, 1, 0.02);
        return out;
    }

    // ------------------------------------------------------------------ efekty

    public void potion(PotionEffectType type, int seconds, int amp) {
        p.addPotionEffect(new PotionEffect(type, seconds * 20, amp, false, true, true));
    }

    public void sound(Sound s, float pitch) { w.playSound(loc, s, 1f, pitch); }

    public void particles(Particle pt, int count, double spread) { w.spawnParticle(pt, loc, count, spread, spread, spread, 0.05); }

    public void dust(Color c, int count, double spread) {
        w.spawnParticle(Particle.DUST, loc, count, spread, spread, spread, 0, new Particle.DustOptions(c, 1.4f));
    }

    public void later(long ticks, Runnable r) {
        Bukkit.getScheduler().runTaskLater(pl, () -> { try { r.run(); } catch (Exception ignored) { } }, Math.max(1, ticks));
    }

    public void msg(String s) { Msg.send(p, s); }

    public void title(String t, String sub) {
        p.showTitle(net.kyori.adventure.title.Title.title(Msg.mm(t), Msg.mm(sub)));
    }

    public void firework(Location at, Color... colors) {
        at.getWorld().spawn(at, org.bukkit.entity.Firework.class, fw -> {
            var meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.values()[r(0, 4)])
                    .withColor(colors.length > 0 ? colors : new Color[]{Color.YELLOW, Color.ORANGE})
                    .trail(true).flicker(rd() < 0.5).build());
            meta.setPower(r(0, 2));
            fw.setFireworkMeta(meta);
            fw.getPersistentDataContainer().set(pl.mobKey(), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
        });
    }

    // ------------------------------------------------------------------ bloki

    /** Stawia blok tymczasowo (tylko w miejscu powietrza/roślin) i przywraca po czasie. */
    public void temp(Block b, Material m, long ticks) {
        if (!b.getType().isAir() && !b.isReplaceable()) return;
        BlockData old = b.getBlockData();
        b.setType(m, false);
        pl.restorer().add(b, old, ticks);
    }

    /** Stawia blok na stałe (tylko w powietrzu). */
    public boolean place(Block b, Material m) {
        if (!b.getType().isAir() && !b.isReplaceable()) return false;
        b.setType(m);
        return true;
    }

    /** Bezpieczne losowe miejsce na powierzchni w promieniu. */
    public Location safeSpot(int radius) {
        for (int i = 0; i < 20; i++) {
            int x = loc.getBlockX() + r(-radius, radius), z = loc.getBlockZ() + r(-radius, radius);
            int y = w.getHighestBlockYAt(x, z);
            Block ground = w.getBlockAt(x, y, z);
            if (ground.isLiquid() || ground.getType() == Material.LAVA || y <= w.getMinHeight()) continue;
            return new Location(w, x + 0.5, y + 1, z + 0.5, p.getLocation().getYaw(), p.getLocation().getPitch());
        }
        return p.getLocation();
    }
}
