package pl.lucky;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Pamięta, które bloki w świecie są lucky blockami.
 * Dane trzymane w PersistentDataContainer chunka – przetrwają restart bez osobnego pliku.
 */
public final class Tracker {

    private final LuckyPlugin pl;
    /** Lucky blocki w załadowanych chunkach (do efektów cząsteczkowych). */
    private final Map<Location, Tier> loaded = new HashMap<>();

    public Tracker(LuckyPlugin pl) { this.pl = pl; }

    private NamespacedKey key(Block b) {
        return new NamespacedKey(pl, "lb_" + b.getX() + "_" + b.getY() + "_" + b.getZ());
    }

    public void mark(Block b, Tier t) {
        b.getChunk().getPersistentDataContainer().set(key(b), PersistentDataType.STRING, t.key);
        loaded.put(b.getLocation(), t);
    }

    public Tier get(Block b) {
        String s = b.getChunk().getPersistentDataContainer().get(key(b), PersistentDataType.STRING);
        return Tier.byKey(s);
    }

    public void unmark(Block b) {
        b.getChunk().getPersistentDataContainer().remove(key(b));
        loaded.remove(b.getLocation());
    }

    public int count() { return loaded.size(); }

    // ------------------------------------------------------------------ chunki

    public void loadChunk(Chunk c) {
        PersistentDataContainer pdc = c.getPersistentDataContainer();
        for (NamespacedKey k : pdc.getKeys()) {
            if (!k.getNamespace().equals(pl.getName().toLowerCase(Locale.ROOT)) || !k.getKey().startsWith("lb_")) continue;
            String[] p = k.getKey().substring(3).split("_");
            if (p.length != 3) continue;
            try {
                Tier t = Tier.byKey(pdc.get(k, PersistentDataType.STRING));
                if (t == null) continue;
                Location l = new Location(c.getWorld(), Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                loaded.put(l, t);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public void unloadChunk(Chunk c) {
        loaded.keySet().removeIf(l -> l.getWorld().equals(c.getWorld()) && (l.getBlockX() >> 4) == c.getX() && (l.getBlockZ() >> 4) == c.getZ());
    }

    public void loadAll() {
        for (World w : Bukkit.getWorlds()) for (Chunk c : w.getLoadedChunks()) loadChunk(c);
    }

    /** Delikatna poświata wokół lucky blocków w pobliżu graczy. */
    public void ambient() {
        if (!pl.getConfig().getBoolean("poswiata", true)) return;
        Iterator<Map.Entry<Location, Tier>> it = loaded.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Location, Tier> e = it.next();
            Location l = e.getKey();
            Block b = l.getBlock();
            if (b.getType() != e.getValue().block) { it.remove(); continue; }
            boolean near = false;
            for (Player p : l.getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(l) < 32 * 32) { near = true; break; }
            }
            if (!near) continue;
            Location c = l.clone().add(0.5, 0.5, 0.5);
            l.getWorld().spawnParticle(Particle.DUST, c, e.getValue() == Tier.LEGENDARNY ? 6 : 3, 0.45, 0.45, 0.45, 0,
                    new Particle.DustOptions(e.getValue().color, 1.1f));
            if (e.getValue() == Tier.LEGENDARNY) l.getWorld().spawnParticle(Particle.END_ROD, c.add(0, 0.6, 0), 1, 0.2, 0.1, 0.2, 0.01);
        }
    }
}
