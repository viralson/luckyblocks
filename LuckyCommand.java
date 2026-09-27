package pl.lucky;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public final class LuckyCommand implements TabExecutor {

    private final LuckyPlugin pl;

    public LuckyCommand(LuckyPlugin pl) { this.pl = pl; }

    private static int num(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        String sub = a.length == 0 ? "" : a[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "" -> { if (s instanceof Player p) pl.gui().open(p); else help(s); }
            case "kolekcja", "collection" -> { if (s instanceof Player p) { Sfx.enter(p); pl.gui().openCollection(p, 0); } }
            case "top" -> pl.gui().top(s);
            case "daj", "give" -> {
                if (!admin(s)) return true;
                Player t = a.length > 1 ? Bukkit.getPlayerExact(a[1]) : null;
                Tier tier = a.length > 2 ? Tier.byKey(a[2]) : null;
                if (t == null || tier == null) { Msg.err(s, "Użycie: <a>" + Msg.esc("/lucky daj <gracz> <rzadki|epicki|legendarny> [ilość]")); return true; }
                int n = a.length > 3 ? Math.max(1, num(a[3], 1)) : 1;
                t.getInventory().addItem(pl.items().item(tier, n)).values().forEach(x -> t.getWorld().dropItemNaturally(t.getLocation(), x));
                Msg.ok(s, "Dano <a>" + n + "×</a> " + tier.shortName + " <m>graczowi</m> <a>" + t.getName());
                if (s != t) Msg.send(t, "<a>Otrzymujesz " + n + "×</a> " + tier.shortName + " Lucky Block<m>!");
            }
            case "test" -> {
                if (!admin(s) || !(s instanceof Player p)) return true;
                Effect e = a.length > 1 ? pl.effects().byId(a[1]) : null;
                if (e == null) { Msg.err(p, "Nieznany efekt. <m>Lista: /lucky efekty"); return true; }
                Tier t = a.length > 2 && Tier.byKey(a[2]) != null ? Tier.byKey(a[2])
                        : e.in(Tier.LEGENDARNY) ? Tier.LEGENDARNY : e.in(Tier.EPICKI) ? Tier.EPICKI : Tier.RZADKI;
                Location at = p.getLocation().getBlock().getRelative(p.getFacing(), 2).getLocation();
                pl.opener().open(p, at, t, e);
            }
            case "efekty" -> {
                if (!admin(s)) return true;
                StringBuilder sb = new StringBuilder();
                for (Effect e : pl.effects().all()) {
                    String col = e.kind() == Effect.Kind.DOBRY ? "<ok>" : e.kind() == Effect.Kind.ZLY ? "<err>" : "<info>";
                    sb.append("<click:suggest_command:'/lucky test ").append(e.id()).append("'>").append(col).append(e.id()).append("</click><dark>, ");
                }
                Msg.send(s, "<m>Efekty (" + pl.effects().all().size() + "): " + sb);
            }
            case "deszcz", "rain" -> {
                if (!admin(s)) return true;
                Tier t = a.length > 1 && Tier.byKey(a[1]) != null ? Tier.byKey(a[1]) : Tier.RZADKI;
                int n = a.length > 2 ? Math.max(1, num(a[2], 3)) : 3;
                rain(t, n);
            }
            case "reload" -> {
                if (!admin(s)) return true;
                pl.reloadConfig();
                Msg.ok(s, "Przeładowano config.");
            }
            default -> help(s);
        }
        return true;
    }

    private boolean admin(CommandSender s) {
        if (s.hasPermission("lucky.admin")) return true;
        Msg.err(s, "Brak uprawnień.");
        return false;
    }

    /** Event: lucky blocki spadają z nieba obok każdego gracza. */
    public void rain(Tier t, int perPlayer) {
        Msg.broadcast("<a>☔ Deszcz Lucky Blocków!</a> " + t.shortName + " <m>– spójrz w niebo!");
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < perPlayer; i++) {
                int d = i * 10;
                Bukkit.getScheduler().runTaskLater(pl, () -> {
                    if (!p.isOnline()) return;
                    Location l = p.getLocation().add(Ctx.r(-8, 8), 25, Ctx.r(-8, 8));
                    if (l.getY() > l.getWorld().getMaxHeight() - 2) l.setY(l.getWorld().getMaxHeight() - 2);
                    FallingBlock fb = l.getWorld().spawnFallingBlock(l, t.block.createBlockData());
                    fb.setDropItem(false);
                    fb.setHurtEntities(false);
                    fb.getPersistentDataContainer().set(pl.fallKey(), PersistentDataType.STRING, t.key);
                }, d);
            }
        }
    }

    private void help(CommandSender s) {
        s.sendMessage(Msg.mm("<line>"));
        s.sendMessage(Msg.mm(" " + Msg.BRAND + " <m>· komendy</m>"));
        s.sendMessage(Msg.mm("  <a>/lucky <dark>·</dark> <m>menu i szanse"));
        s.sendMessage(Msg.mm("  <a>/lucky kolekcja <dark>·</dark> <m>odkryte efekty"));
        s.sendMessage(Msg.mm("  <a>/lucky top <dark>·</dark> <m>ranking"));
        if (s.hasPermission("lucky.admin")) {
            s.sendMessage(Msg.mm("  <err>admin</err> <m>" + Msg.esc("daj <gracz> <rodzaj> [ilość] · test <efekt> · efekty · deszcz [rodzaj] [ilość] · reload")));
        }
        s.sendMessage(Msg.mm("<line>"));
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] a) {
        List<String> out = new ArrayList<>();
        boolean admin = s.hasPermission("lucky.admin");
        List<String> tiers = List.of("rzadki", "epicki", "legendarny");
        if (a.length == 1) {
            out.addAll(List.of("kolekcja", "top"));
            if (admin) out.addAll(List.of("daj", "test", "efekty", "deszcz", "reload"));
        } else if (admin && a.length == 2) {
            switch (a[0].toLowerCase(Locale.ROOT)) {
                case "daj" -> Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
                case "test" -> pl.effects().all().forEach(e -> out.add(e.id()));
                case "deszcz" -> out.addAll(tiers);
                default -> { }
            }
        } else if (admin && a.length == 3) {
            switch (a[0].toLowerCase(Locale.ROOT)) {
                case "daj", "test" -> out.addAll(tiers);
                case "deszcz" -> out.addAll(List.of("1", "3", "5"));
                default -> { }
            }
        } else if (admin && a.length == 4 && a[0].equalsIgnoreCase("daj")) {
            out.addAll(List.of("1", "16", "64"));
        }
        String last = a[a.length - 1].toLowerCase(Locale.ROOT);
        return out.stream().filter(x -> x.toLowerCase(Locale.ROOT).startsWith(last)).toList();
    }
}
