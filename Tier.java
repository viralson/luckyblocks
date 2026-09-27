package pl.lucky;

import org.bukkit.Color;
import org.bukkit.Material;

/** Rodzaje lucky blocków. */
public enum Tier {

    RZADKI("rzadki", "<gradient:#ffe259:#ffa751><b>Rzadki Lucky Block</b></gradient>", "<#ffd84d>Rzadki",
            Material.SPONGE, Color.fromRGB(0xFFD84D), 1, 55, 20, 25),
    EPICKI("epicki", "<gradient:#c471f5:#fa71cd><b>Epicki Lucky Block</b></gradient>", "<#d58cff>Epicki",
            Material.AMETHYST_BLOCK, Color.fromRGB(0xC471F5), 2, 60, 15, 25),
    LEGENDARNY("legendarny", "<gradient:#f7971e:#ffd200:#f7971e><b>Legendarny Lucky Block</b></gradient>", "<#ffb300>Legendarny",
            Material.GOLD_BLOCK, Color.fromRGB(0xFFB300), 3, 70, 10, 20);

    public final String key;
    public final String name;
    public final String shortName;
    public final Material block;
    public final Color color;
    public final int mult;
    public final int good, neutral, bad;

    Tier(String key, String name, String shortName, Material block, Color color, int mult, int good, int neutral, int bad) {
        this.key = key;
        this.name = name;
        this.shortName = shortName;
        this.block = block;
        this.color = color;
        this.mult = mult;
        this.good = good;
        this.neutral = neutral;
        this.bad = bad;
    }

    public char code() { return name().charAt(0); } // R / E / L

    public Tier higher() { return this == RZADKI ? EPICKI : LEGENDARNY; }

    public static Tier byKey(String k) {
        if (k == null) return null;
        for (Tier t : values()) if (t.key.equalsIgnoreCase(k) || t.name().equalsIgnoreCase(k) || String.valueOf(t.code()).equalsIgnoreCase(k)) return t;
        return null;
    }
}
