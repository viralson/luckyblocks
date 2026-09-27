package pl.lucky;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

/**
 * Wiadomości i paleta kolorów (MiniMessage).
 * Własne tagi: <t> tekst, <m> przygaszony, <a> akcent (złoty), <money> kwoty,
 * <ok> sukces, <err> błąd, <info> informacja, <myst> niespodzianka, <line> separator.
 */
public final class Msg {

    public static final int C_TEXT = 0xD4D4D4, C_MUTED = 0x8A8A8A, C_ACCENT = 0xFFD84D, C_MONEY = 0x7EE787,
            C_OK = 0x7EE787, C_ERR = 0xFF7B72, C_INFO = 0x79C0FF, C_MYST = 0xD2A8FF, C_DARK = 0x3C3C3C;

    private static final MiniMessage MM = MiniMessage.builder()
            .tags(TagResolver.resolver(
                    TagResolver.standard(),
                    Placeholder.styling("t", TextColor.color(C_TEXT)),
                    Placeholder.styling("m", TextColor.color(C_MUTED)),
                    Placeholder.styling("a", TextColor.color(C_ACCENT)),
                    Placeholder.styling("money", TextColor.color(C_MONEY)),
                    Placeholder.styling("ok", TextColor.color(C_OK)),
                    Placeholder.styling("err", TextColor.color(C_ERR)),
                    Placeholder.styling("info", TextColor.color(C_INFO)),
                    Placeholder.styling("myst", TextColor.color(C_MYST)),
                    Placeholder.styling("dark", TextColor.color(C_DARK)),
                    Placeholder.parsed("line", "<#3c3c3c><st>                              </st></#3c3c3c>")))
            .build();

    public static final String BRAND = "<gradient:#ffe259:#ffa751><b>LuckyBlock</b></gradient>";
    public static final String PREFIX = BRAND + " <dark>│</dark> <t>";

    private Msg() {}

    public static Component mm(String s) { return MM.deserialize(s); }

    /** Tekst przedmiotu (bez kursywy). */
    public static Component item(String s) {
        return MM.deserialize(s).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static void send(CommandSender to, String s) { to.sendMessage(MM.deserialize(PREFIX + s)); }

    public static void ok(CommandSender to, String s) { send(to, "<ok>✔</ok> " + s); }

    public static void err(CommandSender to, String s) { send(to, "<err>✘</err> " + s); }

    public static void broadcast(String s) { Bukkit.getServer().sendMessage(MM.deserialize(PREFIX + s)); }

    /** Bezpieczny tekst od gracza. */
    public static String esc(String s) { return s == null ? "?" : MM.escapeTags(s); }

    /** Nazwa przedmiotu jako zwykły tekst (z ilością). */
    public static String name(ItemStack it) {
        String n = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(it.effectiveName());
        return esc(n) + (it.getAmount() > 1 ? " <m>×" + it.getAmount() + "</m>" : "");
    }

    /** Czysta nazwa bez formatowania (do wyszukiwania). */
    public static String plain(ItemStack it) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(it.effectiveName());
    }

    public static String fmt(long n) { return String.format(Locale.ROOT, "%,d", n).replace(',', ' '); }

    /** Czas w formacie 1d 2h / 3h 5m / 45s. */
    public static String left(long millis) {
        long s = Math.max(0, millis / 1000);
        if (s >= 86400) return (s / 86400) + "d " + ((s / 3600) % 24) + "h";
        if (s >= 3600) return (s / 3600) + "h " + ((s / 60) % 60) + "m";
        if (s >= 60) return (s / 60) + "m " + (s % 60) + "s";
        return s + "s";
    }

    /** Przycisk w czacie. */
    public static String button(String label, String command, String hover) {
        return "<click:run_command:'" + command + "'><hover:show_text:'<t>" + hover + "'><a>[" + label + "]</a></hover></click>";
    }
}
