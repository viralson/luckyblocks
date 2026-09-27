package pl.lucky;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/** Statystyki graczy: otwarte bloki i odkryte efekty (kolekcja). */
public final class Stats {

    public static final class P {
        public String name = "?";
        public final int[] opened = new int[3];
        public final Set<String> found = new HashSet<>();
        public int total() { return opened[0] + opened[1] + opened[2]; }
    }

    private final LuckyPlugin pl;
    private final File file;
    private final Map<UUID, P> players = new HashMap<>();
    private boolean dirty;

    public Stats(LuckyPlugin pl) {
        this.pl = pl;
        this.file = new File(pl.getDataFolder(), "statystyki.yml");
    }

    public P get(UUID u) { return players.computeIfAbsent(u, k -> new P()); }

    public Collection<P> all() { return players.values(); }

    public void dirty() { dirty = true; }

    public void saveIfDirty() { if (dirty) save(); }

    public void load() {
        if (!file.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        for (String k : y.getKeys(false)) {
            ConfigurationSection s = y.getConfigurationSection(k);
            if (s == null) continue;
            P p = get(UUID.fromString(k));
            p.name = s.getString("nick", "?");
            List<Integer> o = s.getIntegerList("otwarte");
            for (int i = 0; i < Math.min(3, o.size()); i++) p.opened[i] = o.get(i);
            p.found.addAll(s.getStringList("odkryte"));
        }
    }

    public void save() {
        dirty = false;
        YamlConfiguration y = new YamlConfiguration();
        players.forEach((u, p) -> {
            y.set(u + ".nick", p.name);
            y.set(u + ".otwarte", List.of(p.opened[0], p.opened[1], p.opened[2]));
            y.set(u + ".odkryte", new ArrayList<>(p.found));
        });
        try {
            if (!pl.getDataFolder().exists()) pl.getDataFolder().mkdirs();
            y.save(file);
        } catch (IOException e) {
            pl.getLogger().warning("Nie udało się zapisać statystyki.yml: " + e.getMessage());
        }
    }
}
