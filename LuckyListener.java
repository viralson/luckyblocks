package pl.lucky;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class LuckyListener implements Listener {

    private final LuckyPlugin pl;

    public LuckyListener(LuckyPlugin pl) { this.pl = pl; }

    // ------------------------------------------------------------------ stawianie / niszczenie

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Tier t = pl.items().tier(e.getItemInHand());
        if (t != null) pl.tracker().mark(e.getBlockPlaced(), t);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        Tier t = pl.tracker().get(b);
        if (t != null) {
            pl.tracker().unmark(b);
            if (b.getType() != t.block) return;       // blok podmieniony – zwykłe zniszczenie
            e.setDropItems(false);
            e.setExpToDrop(0);
            Player p = e.getPlayer();
            Location loc = b.getLocation();
            Bukkit.getScheduler().runTask(pl, () -> pl.opener().open(p, loc, t, null));
            return;
        }
        // szansa na lucky block z rud
        if (e.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        String n = b.getType().name();
        if (!n.endsWith("_ORE")) return;
        double chance = pl.getConfig().getDouble("z-rud.szansa", 0.01);
        if (Math.random() >= chance) return;
        double roll = Math.random();
        Tier drop = roll < pl.getConfig().getDouble("z-rud.legendarny", 0.05) ? Tier.LEGENDARNY
                : roll < pl.getConfig().getDouble("z-rud.epicki", 0.25) ? Tier.EPICKI : Tier.RZADKI;
        b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), pl.items().item(drop, 1));
        Msg.send(e.getPlayer(), "<a>✦</a> Z rudy wypadł " + drop.shortName + " <t>Lucky Block<m>!");
        Sfx.star(e.getPlayer(), true);
    }

    // ------------------------------------------------------------------ ochrona

    private void protect(List<Block> blocks) { blocks.removeIf(b -> pl.tracker().get(b) != null); }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.getEntity() instanceof TNTPrimed tnt && tnt.getPersistentDataContainer().has(pl.mobKey(), PersistentDataType.BYTE)
                && !pl.getConfig().getBoolean("wybuchy-niszcza-bloki", false)) {
            e.blockList().clear();
            return;
        }
        protect(e.blockList());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) { protect(e.blockList()); }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks()) if (pl.tracker().get(b) != null) { e.setCancelled(true); return; }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks()) if (pl.tracker().get(b) != null) { e.setCancelled(true); return; }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireworkDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Firework fw && fw.getPersistentDataContainer().has(pl.mobKey(), PersistentDataType.BYTE)) {
            e.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------ deszcz lucky blocków

    @EventHandler(ignoreCancelled = true)
    public void onLand(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock fb)) return;
        String tk = fb.getPersistentDataContainer().get(pl.fallKey(), PersistentDataType.STRING);
        Tier t = Tier.byKey(tk);
        if (t == null) return;
        Block b = e.getBlock();
        Bukkit.getScheduler().runTask(pl, () -> {
            if (b.getType() == t.block) {
                pl.tracker().mark(b, t);
                b.getWorld().spawnParticle(org.bukkit.Particle.DUST, b.getLocation().add(0.5, 1, 0.5), 20, 0.4, 0.3, 0.4, 0,
                        new org.bukkit.Particle.DustOptions(t.color, 1.5f));
            }
        });
    }

    // ------------------------------------------------------------------ boss

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        Entity en = e.getEntity();
        String tk = en.getPersistentDataContainer().get(pl.bossKey(), PersistentDataType.STRING);
        Tier t = Tier.byKey(tk);
        if (t == null) return;
        e.getDrops().clear();
        e.getDrops().add(new ItemStack(org.bukkit.Material.DIAMOND, 4 * t.mult));
        e.getDrops().add(new ItemStack(org.bukkit.Material.GOLDEN_APPLE, 2 * t.mult));
        e.getDrops().add(pl.items().item(t, 1));
        e.setDroppedExp(100 * t.mult);
        Player killer = e.getEntity().getKiller();
        if (killer != null) Msg.broadcast("<t>" + killer.getName() + " <m>pokonał(a)</m> <green><b>Króla Zombie</b></green><m>!");
    }

    // ------------------------------------------------------------------ chunki

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) { pl.tracker().loadChunk(e.getChunk()); }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) { pl.tracker().unloadChunk(e.getChunk()); }

    // ------------------------------------------------------------------ menu

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder(false) instanceof Menu menu)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= menu.size()) return;
        Menu.Action a = menu.action(slot);
        if (a == null) return;
        var type = e.getClick();
        Bukkit.getScheduler().runTask(pl, () -> { if (menu.isViewing(p)) a.click(p, type); });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder(false) instanceof Menu) e.setCancelled(true);
    }
}
