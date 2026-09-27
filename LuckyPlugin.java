package pl.lucky;

import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class LuckyPlugin extends JavaPlugin {

    private LuckyItems items;
    private Tracker tracker;
    private Effects effects;
    private Opener opener;
    private Stats stats;
    private LuckyGui gui;
    private Restorer restorer;
    private LuckyCommand command;
    private NamespacedKey mobKey, bossKey, fallKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        mobKey = new NamespacedKey(this, "lucky_mob");
        bossKey = new NamespacedKey(this, "lucky_boss");
        fallKey = new NamespacedKey(this, "lucky_fall");

        items = new LuckyItems(this);
        tracker = new Tracker(this);
        effects = new Effects();
        opener = new Opener(this);
        stats = new Stats(this);
        stats.load();
        gui = new LuckyGui(this);
        restorer = new Restorer();

        items.registerRecipes();
        tracker.loadAll();

        command = new LuckyCommand(this);
        PluginCommand pc = getCommand("lucky");
        if (pc != null) {
            pc.setExecutor(command);
            pc.setTabCompleter(command);
        }
        getServer().getPluginManager().registerEvents(new LuckyListener(this), this);

        getServer().getScheduler().runTaskTimer(this, restorer::tick, 1L, 1L);
        getServer().getScheduler().runTaskTimer(this, () -> {
            tracker.ambient();
            stats.saveIfDirty();
        }, 40L, 40L);

        // automatyczny deszcz lucky blocków
        long every = getConfig().getLong("deszcz.co-ile-minut", 0);
        if (every > 0) {
            getServer().getScheduler().runTaskTimer(this, () -> {
                if (getServer().getOnlinePlayers().isEmpty()) return;
                Tier t = Tier.byKey(getConfig().getString("deszcz.rodzaj", "rzadki"));
                command.rain(t == null ? Tier.RZADKI : t, getConfig().getInt("deszcz.ilosc", 2));
            }, every * 1200L, every * 1200L);
        }

        getLogger().info("LuckyBlock włączony – " + effects.all().size() + " efektów, " + tracker.count() + " bloków w załadowanych chunkach.");
    }

    @Override
    public void onDisable() {
        if (restorer != null) restorer.restoreAll();
        if (stats != null) stats.save();
        if (items != null) items.unregister();
    }

    public LuckyItems items() { return items; }
    public Tracker tracker() { return tracker; }
    public Effects effects() { return effects; }
    public Opener opener() { return opener; }
    public Stats stats() { return stats; }
    public LuckyGui gui() { return gui; }
    public Restorer restorer() { return restorer; }
    public NamespacedKey mobKey() { return mobKey; }
    public NamespacedKey bossKey() { return bossKey; }
    public NamespacedKey fallKey() { return fallKey; }
}
