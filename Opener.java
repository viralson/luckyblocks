package pl.lucky;

import org.bukkit.*;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Otwieranie lucky blocka: animacja, losowanie efektu, ogłoszenia. */
public final class Opener {

    private final LuckyPlugin pl;

    public Opener(LuckyPlugin pl) { this.pl = pl; }

    /** Losuje efekt dla danego bloku (z uwzględnieniem mikstury Szczęścia). */
    public Effect roll(Player p, Tier t) {
        int good = t.good, neutral = t.neutral, bad = t.bad;
        var luck = p.getPotionEffect(org.bukkit.potion.PotionEffectType.LUCK);
        if (luck != null) {
            int shift = Math.min(bad, 8 * (luck.getAmplifier() + 1));
            good += shift;
            bad -= shift;
        }
        int roll = ThreadLocalRandom.current().nextInt(good + neutral + bad);
        Effect.Kind kind = roll < good ? Effect.Kind.DOBRY : roll < good + neutral ? Effect.Kind.NEUTRALNY : Effect.Kind.ZLY;

        List<Effect> pool = new ArrayList<>();
        for (Effect e : pl.effects().all()) if (allowed(e, t) && e.kind() == kind) pool.add(e);
        if (pool.isEmpty()) for (Effect e : pl.effects().all()) if (allowed(e, t)) pool.add(e);
        int total = pool.stream().mapToInt(Effect::weight).sum();
        int r = ThreadLocalRandom.current().nextInt(Math.max(1, total));
        for (Effect e : pool) {
            r -= e.weight();
            if (r < 0) return e;
        }
        return pool.getFirst();
    }

    public boolean allowed(Effect e, Tier t) {
        if (!e.in(t)) return false;
        if (e.dangerous() && !pl.getConfig().getBoolean("niebezpieczne-efekty", true)) return false;
        return !pl.getConfig().getStringList("wylaczone-efekty").contains(e.id());
    }

    /** Animacja i otwarcie. forced != null – wymuszony efekt (test admina). */
    public void open(Player p, Location blockLoc, Tier t, Effect forced) {
        Location center = blockLoc.clone().add(0.5, 0.5, 0.5);
        World w = center.getWorld();
        int duration = pl.getConfig().getInt("animacja-tickow", 24);

        ItemDisplay disp = w.spawn(center.clone(), ItemDisplay.class, d -> {
            d.setItemStack(pl.items().item(t, 1));
            d.setPersistent(false);
            d.setInterpolationDuration(2);
            d.setTeleportDuration(2);
            d.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(0.8f), new AxisAngle4f()));
        });

        new org.bukkit.scheduler.BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                i++;
                float prog = (float) i / duration;
                if (disp.isValid()) {
                    float angle = i * 0.45f + prog * prog * 6f;
                    float scale = 0.8f + prog * 0.35f;
                    disp.setInterpolationDelay(0);
                    disp.setTransformation(new Transformation(new Vector3f(0, prog * 0.9f, 0), new AxisAngle4f(angle, 0, 1, 0),
                            new Vector3f(scale), new AxisAngle4f()));
                }
                Location at = center.clone().add(0, prog * 0.9, 0);
                w.spawnParticle(Particle.DUST, at, 6, 0.35, 0.35, 0.35, 0, new Particle.DustOptions(t.color, 1.2f));
                if (i % 3 == 0) w.playSound(center, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 0.6f + prog * 1.4f);
                if (t == Tier.LEGENDARNY) w.spawnParticle(Particle.END_ROD, at, 2, 0.3, 0.3, 0.3, 0.02);
                if (i < duration) return;

                cancel();
                disp.remove();
                w.spawnParticle(Particle.FIREWORK, at, 40, 0.3, 0.3, 0.3, 0.15);
                w.spawnParticle(Particle.DUST, at, 40, 0.6, 0.6, 0.6, 0, new Particle.DustOptions(t.color, 1.8f));
                w.playSound(center, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.6f);
                if (!p.isOnline()) return;
                apply(p, center, t, forced != null ? forced : roll(p, t));
            }
        }.runTaskTimer(pl, 1L, 1L);
    }

    private void apply(Player p, Location center, Tier t, Effect e) {
        String kindCol = switch (e.kind()) {
            case DOBRY -> "<ok>";
            case NEUTRALNY -> "<info>";
            case ZLY -> "<err>";
        };
        Msg.send(p, t.shortName + " <dark>│</dark> " + kindCol + e.name());
        p.sendActionBar(Msg.mm(kindCol + "✦ " + e.name() + " ✦"));
        try {
            e.action().accept(new Ctx(pl, p, center, t));
        } catch (Exception ex) {
            pl.getLogger().warning("Błąd efektu " + e.id() + ": " + ex);
        }

        Stats.P s = pl.stats().get(p.getUniqueId());
        s.name = p.getName();
        s.opened[t.ordinal()]++;
        boolean isNew = s.found.add(e.id());
        pl.stats().dirty();
        if (isNew) {
            Msg.send(p, "<a>✚ Nowy efekt w kolekcji!</a> <m>(" + s.found.size() + "/" + pl.effects().all().size() + ") · /lucky kolekcja");
        }

        boolean announce = t == Tier.LEGENDARNY && pl.getConfig().getBoolean("ogloszenia.legendarny", true)
                || t == Tier.EPICKI && e.kind() == Effect.Kind.DOBRY && pl.getConfig().getBoolean("ogloszenia.epicki-dobry", false);
        if (announce) {
            Msg.broadcast("<t>" + p.getName() + " <m>otworzył(a)</m> " + t.shortName + " <m>Lucky Block →</m> " + kindCol + e.name());
        }
    }
}
