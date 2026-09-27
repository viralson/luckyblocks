package pl.lucky;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/** Menu główne i kolekcja efektów. */
public final class LuckyGui {

    private final LuckyPlugin pl;

    public LuckyGui(LuckyPlugin pl) { this.pl = pl; }

    private static String title(String main, String sub) {
        return "<#2b2b2b>" + main + (sub == null ? "" : " <#8a8a8a>· " + sub);
    }

    public void open(Player p) {
        Stats.P s = pl.stats().get(p.getUniqueId());
        Menu m = new Menu(4, title("🍀 Lucky Blocki", null));
        int slot = 11;
        for (Tier t : Tier.values()) {
            ItemStack it = pl.items().item(t, 1);
            var meta = it.getItemMeta();
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>(meta.lore());
            long count = pl.effects().all().stream().filter(e -> pl.opener().allowed(e, t)).count();
            lore.add(Msg.item(""));
            lore.add(Msg.item("<m>Możliwych efektów: <t>" + count));
            lore.add(Msg.item("<m>Otworzyłeś: <t>" + s.opened[t.ordinal()]));
            lore.add(Msg.item("<m>Receptura: <t>" + switch (t) {
                case RZADKI -> "8× złoto + dozownik";
                case EPICKI -> "4× rzadki + 4× diament + ametyst";
                case LEGENDARNY -> "4× epicki + 4× blok diamentów + gwiazda";
            }));
            if (p.hasPermission("lucky.admin")) {
                lore.add(Msg.item(""));
                lore.add(Msg.item("<err>Admin: <a>klik</a> <m>weź 1, <a>shift</a> <m>weź 16"));
            }
            meta.lore(lore);
            it.setItemMeta(meta);
            m.set(slot, it, (pl2, t2) -> {
                if (!pl2.hasPermission("lucky.admin")) return;
                pl2.getInventory().addItem(pl.items().item(t, t2.isShiftClick() ? 16 : 1));
                Sfx.pickup(pl2);
            });
            slot += 2;
        }
        m.set(29, Items.of(Material.BOOK, "<a>Kolekcja efektów",
                "<m>Odkryte: <t>" + s.found.size() + "<m>/" + pl.effects().all().size(),
                "", "<a>Klik</a> <m>otwórz"), (pl2, t) -> { Sfx.enter(pl2); openCollection(pl2, 0); });
        m.set(31, Items.head(p, "<a>" + p.getName(),
                "<m>Otwarte razem: <t>" + s.total(),
                "<m>Rzadkie: <t>" + s.opened[0] + " <m>· Epickie: <t>" + s.opened[1] + " <m>· Legendarne: <t>" + s.opened[2]));
        m.set(33, Items.of(Material.GOLD_BLOCK, "<a>Ranking szczęściarzy", "<a>Klik</a> <m>pokaż"), (pl2, t) -> { pl2.closeInventory(); top(pl2); });
        m.fill(Material.GRAY_STAINED_GLASS_PANE);
        m.open(p);
        Sfx.open(p);
    }

    public void openCollection(Player p, int page) {
        Stats.P s = pl.stats().get(p.getUniqueId());
        List<Effect> list = pl.effects().all();
        int per = 45, pages = (list.size() + per - 1) / per;
        int pg = Math.max(0, Math.min(page, pages - 1));
        Menu m = new Menu(6, title("Kolekcja", s.found.size() + "/" + list.size()));
        boolean admin = p.hasPermission("lucky.admin");
        for (int i = 0; i < per && pg * per + i < list.size(); i++) {
            Effect e = list.get(pg * per + i);
            boolean known = s.found.contains(e.id()) || admin;
            ItemStack it;
            if (known) {
                Material icon = switch (e.kind()) {
                    case DOBRY -> Material.LIME_DYE;
                    case NEUTRALNY -> Material.LIGHT_BLUE_DYE;
                    case ZLY -> Material.RED_DYE;
                };
                StringBuilder tiers = new StringBuilder();
                for (Tier t : Tier.values()) if (e.in(t)) tiers.append(t.shortName).append(" ");
                List<String> lore = new ArrayList<>(List.of("<m>Rodzaj: " + e.kind().display, "<m>Występuje: " + tiers.toString().trim()));
                if (e.dangerous()) lore.add("<err>⚠ niebezpieczny");
                if (!s.found.contains(e.id())) lore.add("<dark>(nieodkryty – widzisz jako admin)");
                if (admin) lore.add("<dark>Shift+klik – przetestuj");
                it = Items.of(icon, 1, (s.found.contains(e.id()) ? "<t>" : "<m>") + e.name(), lore);
            } else {
                it = Items.of(Material.GRAY_DYE, "<dark>???", "<m>Otwieraj lucky blocki, by odkryć.");
            }
            m.set(i, it, admin ? (pl2, t) -> {
                if (t != ClickType.SHIFT_LEFT && t != ClickType.SHIFT_RIGHT) return;
                pl2.closeInventory();
                Tier tier = e.in(Tier.LEGENDARNY) ? Tier.LEGENDARNY : e.in(Tier.EPICKI) ? Tier.EPICKI : Tier.RZADKI;
                pl.opener().open(pl2, pl2.getLocation().getBlock().getRelative(pl2.getFacing(), 2).getLocation(), tier, e);
            } : null);
        }
        for (int i = 45; i < 54; i++) m.set(i, Items.pane(Material.BLACK_STAINED_GLASS_PANE));
        m.set(45, Items.of(Material.ARROW, "<a>← Menu"), (pl2, t) -> { Sfx.back(pl2); open(pl2); });
        if (pg > 0) m.set(48, Items.of(Material.SPECTRAL_ARROW, "<a>← Poprzednia"), (pl2, t) -> { Sfx.page(pl2); openCollection(pl2, pg - 1); });
        m.set(49, Items.of(Material.BOOK, "<m>Strona <t>" + (pg + 1) + "<m>/" + pages));
        if (pg < pages - 1) m.set(50, Items.of(Material.SPECTRAL_ARROW, "<a>Następna →"), (pl2, t) -> { Sfx.page(pl2); openCollection(pl2, pg + 1); });
        m.open(p);
    }

    public void top(org.bukkit.command.CommandSender s) {
        List<Stats.P> l = new ArrayList<>(pl.stats().all());
        l.sort((a, b) -> b.total() - a.total());
        s.sendMessage(Msg.mm("<line>"));
        s.sendMessage(Msg.mm(" " + Msg.BRAND + " <m>· najwięcej otwartych</m>"));
        int i = 1;
        for (Stats.P p : l) {
            if (i > 10 || p.total() == 0) break;
            String medal = i == 1 ? "<#ffd700>①" : i == 2 ? "<#c0c0c0>②" : i == 3 ? "<#cd7f32>③" : "<m>" + i + ".";
            s.sendMessage(Msg.mm("  " + medal + " <t>" + Msg.esc(p.name) + "  <a>" + p.total() + " <m>· kolekcja " + p.found.size()
                    + "/" + pl.effects().all().size()));
            i++;
        }
        if (i == 1) s.sendMessage(Msg.mm("  <m>Nikt jeszcze nie otworzył lucky blocka."));
        s.sendMessage(Msg.mm("<line>"));
    }
}
