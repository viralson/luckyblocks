package pl.lucky;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/** Przedmioty lucky blocków i receptury. */
public final class LuckyItems {

    private final LuckyPlugin pl;
    private final NamespacedKey key;
    private final List<NamespacedKey> recipes = new ArrayList<>();

    public LuckyItems(LuckyPlugin pl) {
        this.pl = pl;
        this.key = new NamespacedKey(pl, "tier");
    }

    public ItemStack item(Tier t, int amount) {
        ItemStack it = new ItemStack(t.block, Math.max(1, Math.min(64, amount)));
        ItemMeta m = it.getItemMeta();
        m.displayName(Msg.item(t.name));
        m.lore(List.of(
                Msg.item("<m>Postaw i zniszcz, aby poznać swój los."),
                Msg.item(""),
                Msg.item("<ok>" + t.good + "% <m>szczęścia  <dark>│</dark>  <err>" + t.bad + "% <m>pecha"),
                Msg.item("<m>Siła nagród: <a>" + "★".repeat(t.mult) + "<dark>" + "★".repeat(3 - t.mult))));
        m.setEnchantmentGlintOverride(true);
        m.getPersistentDataContainer().set(key, PersistentDataType.STRING, t.key);
        it.setItemMeta(m);
        return it;
    }

    public Tier tier(ItemStack it) {
        if (it == null || !it.hasItemMeta()) return null;
        return Tier.byKey(it.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING));
    }

    @SuppressWarnings({"deprecation", "removal"}) // stary konstruktor działa na każdym buildzie 26.x
    public void registerRecipes() {
        if (!pl.getConfig().getBoolean("receptury", true)) return;
        // Rzadki: 8 sztabek złota wokół dozownika
        ShapedRecipe r = new ShapedRecipe(new NamespacedKey(pl, "lucky_rzadki"), item(Tier.RZADKI, 1));
        r.shape("GGG", "GDG", "GGG");
        r.setIngredient('G', Material.GOLD_INGOT);
        r.setIngredient('D', Material.DROPPER);
        add(r);
        // Epicki: 4 rzadkie + 4 diamenty + blok ametystu
        ShapedRecipe e = new ShapedRecipe(new NamespacedKey(pl, "lucky_epicki"), item(Tier.EPICKI, 1));
        e.shape("RDR", "DAD", "RDR");
        e.setIngredient('R', new RecipeChoice.ExactChoice(item(Tier.RZADKI, 1)));
        e.setIngredient('D', Material.DIAMOND);
        e.setIngredient('A', Material.AMETHYST_BLOCK);
        add(e);
        // Legendarny: 4 epickie + 4 bloki diamentów + gwiazda Netheru
        ShapedRecipe l = new ShapedRecipe(new NamespacedKey(pl, "lucky_legendarny"), item(Tier.LEGENDARNY, 1));
        l.shape("EDE", "DND", "EDE");
        l.setIngredient('E', new RecipeChoice.ExactChoice(item(Tier.EPICKI, 1)));
        l.setIngredient('D', Material.DIAMOND_BLOCK);
        l.setIngredient('N', Material.NETHER_STAR);
        add(l);
    }

    private void add(ShapedRecipe r) {
        Bukkit.removeRecipe(r.getKey());
        Bukkit.addRecipe(r);
        recipes.add(r.getKey());
    }

    public void unregister() {
        for (NamespacedKey k : recipes) Bukkit.removeRecipe(k);
        recipes.clear();
    }
}
