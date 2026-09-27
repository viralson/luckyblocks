package pl.lucky;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.function.Consumer;

import static org.bukkit.Material.*;
import static pl.lucky.Ctx.*;
import static pl.lucky.Effect.Kind.*;

/** 100 efektów lucky blocków. */
public final class Effects {

    private final List<Effect> all = new ArrayList<>();

    public List<Effect> all() { return all; }

    public Effect byId(String id) {
        for (Effect e : all) if (e.id().equalsIgnoreCase(id)) return e;
        return null;
    }

    private void add(String id, String name, Effect.Kind k, String tiers, int weight, Consumer<Ctx> a) {
        all.add(new Effect(id, name, k, tiers, weight, false, a));
    }

    private void danger(String id, String name, Effect.Kind k, String tiers, int weight, Consumer<Ctx> a) {
        all.add(new Effect(id, name, k, tiers, weight, true, a));
    }

    private static final List<Enchantment> GOOD_ENCH = List.of(Enchantment.SHARPNESS, Enchantment.PROTECTION, Enchantment.EFFICIENCY,
            Enchantment.UNBREAKING, Enchantment.FORTUNE, Enchantment.LOOTING, Enchantment.POWER, Enchantment.FEATHER_FALLING,
            Enchantment.SILK_TOUCH, Enchantment.MENDING, Enchantment.FIRE_ASPECT, Enchantment.DEPTH_STRIDER, Enchantment.RESPIRATION);

    private static ItemStack book(Enchantment e, int lvl) {
        ItemStack b = new ItemStack(ENCHANTED_BOOK);
        EnchantmentStorageMeta m = (EnchantmentStorageMeta) b.getItemMeta();
        m.addStoredEnchant(e, Math.min(lvl, e.getMaxLevel()), true);
        b.setItemMeta(m);
        return b;
    }

    private static ItemStack randomBook(int tierMult) {
        Enchantment e = pick(GOOD_ENCH);
        return book(e, Math.max(1, Math.min(e.getMaxLevel(), r(tierMult, e.getMaxLevel()))));
    }

    private static final List<Material> DYES_WOOL = List.of(RED_WOOL, ORANGE_WOOL, YELLOW_WOOL, LIME_WOOL, LIGHT_BLUE_WOOL,
            BLUE_WOOL, PURPLE_WOOL, MAGENTA_WOOL, PINK_WOOL, CYAN_WOOL);
    private static final List<Material> DISCS = List.of(MUSIC_DISC_CAT, MUSIC_DISC_BLOCKS, MUSIC_DISC_CHIRP, MUSIC_DISC_FAR,
            MUSIC_DISC_MALL, MUSIC_DISC_MELLOHI, MUSIC_DISC_STAL, MUSIC_DISC_STRAD, MUSIC_DISC_WARD, MUSIC_DISC_WAIT, MUSIC_DISC_PIGSTEP);

    private static void armor(LivingEntity e, Material h, Material c, Material l, Material b, float drop) {
        EntityEquipment eq = e.getEquipment();
        if (eq == null) return;
        eq.setHelmet(new ItemStack(h));
        eq.setChestplate(new ItemStack(c));
        eq.setLeggings(new ItemStack(l));
        eq.setBoots(new ItemStack(b));
        eq.setHelmetDropChance(drop);
        eq.setChestplateDropChance(drop);
        eq.setLeggingsDropChance(drop);
        eq.setBootsDropChance(drop);
    }

    private static void target(Entity e, Player p) { if (e instanceof Mob m) m.setTarget(p); }

    public Effects() {

        // =================================================================
        //  DOBRE – SUROWCE (1-8)
        // =================================================================
        add("diamenty", "Garść diamentów", DOBRY, "REL", 12, c -> c.give(it(DIAMOND, r(2, 5) * c.m())));
        add("szmaragdy", "Szmaragdowy skarb", DOBRY, "REL", 10, c -> c.give(it(EMERALD, r(4, 10) * c.m())));
        add("zloto", "Złote sztabki", DOBRY, "RE", 12, c -> c.give(it(GOLD_INGOT, r(8, 16) * c.m())));
        add("zelazo", "Stack żelaza", DOBRY, "RE", 12, c -> c.give(it(IRON_INGOT, r(16, 32) * c.m())));
        add("netherite", "Sztabka netherytu", DOBRY, "EL", 5, c -> c.give(it(NETHERITE_INGOT, c.m() - 1 + r(0, 1))));
        add("deszcz_diamentow", "Deszcz diamentów!", DOBRY, "EL", 5, c -> {
            c.rain(() -> it(DIAMOND, 1), 10 * c.m(), 4);
            c.title("<aqua><b>DESZCZ DIAMENTÓW", "<m>patrz w górę!");
        });
        add("deszcz_zlota", "Złoty deszcz", DOBRY, "RE", 8, c -> c.rain(() -> it(GOLD_NUGGET, r(1, 3)), 20 * c.m(), 4));
        add("bloki_mineralow", "Bloki minerałów", DOBRY, "EL", 5, c -> c.give(it(IRON_BLOCK, 4 * c.m()), it(GOLD_BLOCK, 2 * c.m()),
                it(REDSTONE_BLOCK, 4 * c.m()), it(LAPIS_BLOCK, 3 * c.m())));

        // =================================================================
        //  DOBRE – JEDZENIE I ZWYKŁE (9-16)
        // =================================================================
        add("zlote_jablka", "Złote jabłka", DOBRY, "REL", 10, c -> c.give(it(GOLDEN_APPLE, r(2, 4) * c.m())));
        add("zaklete_jablko", "Zaklęte złote jabłko", DOBRY, "L", 4, c -> c.give(it(ENCHANTED_GOLDEN_APPLE, 1)));
        add("jedzenie", "Uczta", DOBRY, "R", 12, c -> c.give(it(COOKED_BEEF, 16), it(BREAD, 16), it(GOLDEN_CARROT, 8)));
        add("tort", "Tort urodzinowy", DOBRY, "R", 8, c -> {
            c.give(it(CAKE, 1), it(COOKIE, 32));
            c.particles(Particle.NOTE, 15, 1);
            c.msg("<a>🎂 Sto lat! <m>Ktoś tu zasłużył na tort.");
        });
        add("bloki_budowlane", "Paczka budowlańca", DOBRY, "R", 10, c -> c.give(it(OAK_LOG, 32), it(STONE_BRICKS, 64), it(GLASS, 32), it(TORCH, 32)));
        add("perly", "Perły Endu", DOBRY, "RE", 8, c -> c.give(it(ENDER_PEARL, r(4, 8) * c.m())));
        add("butelki_xp", "Butelki doświadczenia", DOBRY, "RE", 9, c -> c.give(it(EXPERIENCE_BOTTLE, 12 * c.m())));
        add("poziomy_xp", "Nagły przypływ wiedzy", DOBRY, "REL", 9, c -> {
            int lv = r(5, 10) * c.m();
            c.p.giveExpLevels(lv);
            c.sound(Sound.ENTITY_PLAYER_LEVELUP, 1f);
            c.msg("<a>+" + lv + " poziomów <m>doświadczenia!");
        });

        // =================================================================
        //  DOBRE – EKWIPUNEK (17-32)
        // =================================================================
        add("zbroja_diament", "Diamentowa zbroja", DOBRY, "EL", 6, c -> c.give(
                named(DIAMOND_HELMET, null, Enchantment.PROTECTION, 2 * c.m()), named(DIAMOND_CHESTPLATE, null, Enchantment.PROTECTION, 2 * c.m()),
                named(DIAMOND_LEGGINGS, null, Enchantment.PROTECTION, 2 * c.m()), named(DIAMOND_BOOTS, null, Enchantment.PROTECTION, 2 * c.m())));
        add("zbroja_netherite", "Zbroja Władcy", DOBRY, "L", 3, c -> c.give(
                named(NETHERITE_HELMET, "<gradient:#f7971e:#ffd200>Hełm Władcy", Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                named(NETHERITE_CHESTPLATE, "<gradient:#f7971e:#ffd200>Napierśnik Władcy", Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                named(NETHERITE_LEGGINGS, "<gradient:#f7971e:#ffd200>Nagolenniki Władcy", Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1),
                named(NETHERITE_BOOTS, "<gradient:#f7971e:#ffd200>Buty Władcy", Enchantment.PROTECTION, 4, Enchantment.FEATHER_FALLING, 4, Enchantment.MENDING, 1)));
        add("miecz", "Miecz Szczęściarza", DOBRY, "EL", 6, c -> c.give(named(c.m() >= 3 ? NETHERITE_SWORD : DIAMOND_SWORD,
                "<gradient:#ffe259:#ffa751>Miecz Szczęściarza", Enchantment.SHARPNESS, 2 + c.m(), Enchantment.LOOTING, c.m(), Enchantment.UNBREAKING, 3)));
        add("kilof", "Kilof Fortuny", DOBRY, "EL", 6, c -> c.give(named(c.m() >= 3 ? NETHERITE_PICKAXE : DIAMOND_PICKAXE,
                "<gradient:#7fdbda:#59c3ff>Kilof Fortuny", Enchantment.EFFICIENCY, 3 + c.m(), Enchantment.FORTUNE, c.m() + 1, Enchantment.UNBREAKING, 3)));
        add("kilof_jedwab", "Jedwabny kilof", DOBRY, "RE", 5, c -> c.give(named(DIAMOND_PICKAXE, "<aqua>Jedwabny kilof", Enchantment.SILK_TOUCH, 1, Enchantment.EFFICIENCY, 4)));
        add("luk", "Łuk Burzy", DOBRY, "E", 5, c -> c.give(named(BOW, "<gradient:#89f7fe:#66a6ff>Łuk Burzy",
                Enchantment.POWER, 5, Enchantment.FLAME, 1, Enchantment.INFINITY, 1, Enchantment.PUNCH, 2), it(ARROW, 1)));
        add("trojzab", "Trójząb Posejdona", DOBRY, "EL", 3, c -> c.give(named(TRIDENT, "<gradient:#43cea2:#185a9d>Trójząb Posejdona",
                Enchantment.LOYALTY, 3, Enchantment.CHANNELING, 1, Enchantment.IMPALING, 5)));
        add("elytra", "Skrzydła!", DOBRY, "L", 3, c -> c.give(named(ELYTRA, "<gradient:#e0c3fc:#8ec5fc>Skrzydła Szczęścia", Enchantment.UNBREAKING, 3), it(FIREWORK_ROCKET, 64)));
        add("totem", "Totem nieśmiertelności", DOBRY, "EL", 5, c -> c.give(it(TOTEM_OF_UNDYING, c.m() - 1)));
        add("narzedzia_netherite", "Netherytowy komplet", DOBRY, "L", 3, c -> c.give(
                named(NETHERITE_PICKAXE, null, Enchantment.EFFICIENCY, 5, Enchantment.UNBREAKING, 3),
                named(NETHERITE_AXE, null, Enchantment.EFFICIENCY, 5, Enchantment.UNBREAKING, 3),
                named(NETHERITE_SHOVEL, null, Enchantment.EFFICIENCY, 5, Enchantment.UNBREAKING, 3)));
        add("tarcza", "Tarcza obrońcy", DOBRY, "R", 7, c -> c.give(named(SHIELD, "<gray>Tarcza obrońcy", Enchantment.UNBREAKING, 3)));
        add("rakietowe_buty", "Rakietowe buty", DOBRY, "E", 4, c -> c.give(named(DIAMOND_BOOTS, "<gold>Rakietowe buty",
                Enchantment.FEATHER_FALLING, 4, Enchantment.DEPTH_STRIDER, 3), it(FIREWORK_ROCKET, 32)));
        add("zestaw_rybaka", "Zestaw rybaka", DOBRY, "R", 6, c -> c.give(named(FISHING_ROD, "<aqua>Wędka szczęścia",
                Enchantment.LURE, 3, Enchantment.LUCK_OF_THE_SEA, 3), it(COOKED_COD, 16)));
        add("zestaw_farmera", "Zestaw farmera", DOBRY, "R", 6, c -> c.give(named(DIAMOND_HOE, "<green>Motyka farmera", Enchantment.FORTUNE, 3),
                it(WHEAT_SEEDS, 32), it(CARROT, 16), it(POTATO, 16), it(BONE_MEAL, 32)));
        add("ksiega", "Magiczna księga", DOBRY, "RE", 9, c -> c.give(randomBook(c.m())));
        add("biblioteka", "Tajna biblioteka", DOBRY, "L", 4, c -> c.give(randomBook(3), randomBook(3), randomBook(3), book(Enchantment.MENDING, 1)));

        // =================================================================
        //  DOBRE – RZADKOŚCI (33-40)
        // =================================================================
        add("mending", "Księga Naprawy", DOBRY, "EL", 5, c -> c.give(book(Enchantment.MENDING, 1)));
        add("beacon", "Latarnia", DOBRY, "L", 2, c -> c.give(it(BEACON, 1)));
        add("gwiazda", "Gwiazda Netheru", DOBRY, "L", 3, c -> c.give(it(NETHER_STAR, 1)));
        add("smocze_jajo", "SMOCZE JAJO", DOBRY, "L", 1, c -> {
            c.give(it(DRAGON_EGG, 1));
            Msg.broadcast("<a>🐉 " + c.p.getName() + " <m>znalazł(a)</m> <gradient:#8e2de2:#4a00e0><b>SMOCZE JAJO</b></gradient><m>!");
        });
        add("serce_morza", "Serce morza", DOBRY, "EL", 3, c -> c.give(it(HEART_OF_THE_SEA, 1), it(NAUTILUS_SHELL, 8), it(PRISMARINE_SHARD, 16)));
        add("shulker", "Shulker pełen skarbów", DOBRY, "EL", 4, c -> {
            ItemStack box = new ItemStack(PURPLE_SHULKER_BOX);
            BlockStateMeta meta = (BlockStateMeta) box.getItemMeta();
            ShulkerBox sb = (ShulkerBox) meta.getBlockState();
            List<Material> loot = List.of(DIAMOND, EMERALD, GOLD_INGOT, IRON_INGOT, ENDER_PEARL, GOLDEN_APPLE, EXPERIENCE_BOTTLE, LAPIS_LAZULI);
            for (int i = 0; i < 6 + 4 * c.m(); i++) sb.getInventory().addItem(it(pick(loot), r(2, 8)));
            meta.setBlockState(sb);
            meta.displayName(Msg.item("<light_purple>Shulker szczęścia"));
            box.setItemMeta(meta);
            c.give(box);
        });
        add("skrzynia", "Skrzynia skarbów", DOBRY, "REL", 8, c -> {
            Block b = c.loc.getBlock();
            if (!b.getType().isAir()) { c.give(it(DIAMOND, 2 * c.m())); return; }
            b.setType(CHEST);
            if (b.getState(false) instanceof org.bukkit.block.Chest chest) {
                List<Material> loot = List.of(DIAMOND, IRON_INGOT, GOLD_INGOT, BREAD, ARROW, EMERALD, GOLDEN_CARROT, OBSIDIAN, TNT, SADDLE, NAME_TAG);
                for (int i = 0; i < 5 + 3 * c.m(); i++) chest.getInventory().setItem(r(0, 26), it(pick(loot), r(1, 6)));
            }
            c.particles(Particle.HAPPY_VILLAGER, 20, 0.6);
            c.msg("<a>Skrzynia skarbów <m>pojawiła się w miejscu bloku!");
        });
        add("wiecej_lucky", "Więcej szczęścia!", DOBRY, "REL", 6, c -> c.give(c.pl.items().item(c.tier, r(2, 3))));
        add("ulepszenie", "Awans bloku", DOBRY, "RE", 4, c -> {
            c.give(c.pl.items().item(c.tier.higher(), 1));
            c.msg("<a>Blok awansował! <m>Masz " + c.tier.higher().shortName + " Lucky Block<m>.");
        });

        // =================================================================
        //  DOBRE – EFEKTY I MOCE (41-48)
        // =================================================================
        add("supermoc", "Supermoc", DOBRY, "REL", 8, c -> {
            int s = 45 * c.m();
            c.potion(PotionEffectType.SPEED, s, 1);
            c.potion(PotionEffectType.HASTE, s, 1);
            c.potion(PotionEffectType.STRENGTH, s, 1);
            c.potion(PotionEffectType.REGENERATION, s / 3, 0);
            c.title("<a><b>SUPERMOC", "<m>na " + s + " sekund");
        });
        add("latanie", "Dar latania", DOBRY, "L", 4, c -> {
            Player p = c.p;
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;
            p.setAllowFlight(true);
            c.title("<aqua><b>LATASZ!", "<m>przez 60 sekund – skacz podwójnie");
            c.later(20 * 60, () -> {
                if (!p.isOnline() || p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;
                p.setFlying(false);
                p.setAllowFlight(false);
                c.potion(PotionEffectType.SLOW_FALLING, 15, 0);
                Msg.send(p, "<m>Moc latania wygasła – spadasz powoli.");
            });
        });
        add("niesmiertelnosc", "Chwilowa nieśmiertelność", DOBRY, "EL", 5, c -> {
            c.potion(PotionEffectType.RESISTANCE, 15 * c.m(), 4);
            c.potion(PotionEffectType.FIRE_RESISTANCE, 30 * c.m(), 0);
            c.msg("<a>Jesteś nie do ruszenia <m>przez " + 15 * c.m() + " s.");
        });
        add("zlote_serca", "Złote serca", DOBRY, "RE", 8, c -> c.potion(PotionEffectType.ABSORPTION, 90, 1 + c.m()));
        add("widzenie", "Oczy sowy i skrzela", DOBRY, "R", 7, c -> {
            c.potion(PotionEffectType.NIGHT_VISION, 300 * c.m(), 0);
            c.potion(PotionEffectType.WATER_BREATHING, 300 * c.m(), 0);
        });
        add("najedzenie", "Pełny brzuszek", DOBRY, "R", 7, c -> {
            c.p.setFoodLevel(20);
            c.p.setSaturation(20);
            c.p.setHealth(Math.min(c.p.getHealth() + 10, c.p.getAttribute(Attribute.MAX_HEALTH).getValue()));
            c.sound(Sound.ENTITY_PLAYER_BURP, 1f);
        });
        add("szczescie", "Czterolistna koniczyna", DOBRY, "RE", 6, c -> {
            c.potion(PotionEffectType.LUCK, 600, c.m());
            c.msg("<a>🍀 Szczęście <m>– kolejne lucky blocki będą łaskawsze!");
        });
        add("fontanna_xp", "Fontanna doświadczenia", DOBRY, "EL", 5, c -> {
            for (int i = 0; i < 25 * c.m(); i++) {
                c.later(i * 2L, () -> c.w.spawn(c.loc.clone().add(0, 0.5, 0), ExperienceOrb.class, o -> {
                    o.setExperience(r(3, 8));
                    o.setVelocity(new Vector((rd() - 0.5) * 0.4, 0.5 + rd() * 0.3, (rd() - 0.5) * 0.4));
                }));
            }
        });

        // =================================================================
        //  DOBRE – ZWIERZAKI I POMOCNICY (49-57)
        // =================================================================
        add("pies", "Wierny piesek", DOBRY, "RE", 7, c -> c.spawn(Wolf.class, c.m(), w -> {
            w.setTamed(true);
            w.setOwner(c.p);
            w.customName(Component.text("Szczęściarz"));
        }));
        add("wataha", "Wilcza wataha", DOBRY, "EL", 4, c -> c.spawn(Wolf.class, 3 + c.m(), w -> { w.setTamed(true); w.setOwner(c.p); }));
        add("kot", "Kotek na szczęście", DOBRY, "R", 6, c -> c.spawn(Cat.class, 1, k -> { k.setTamed(true); k.setOwner(c.p); }));
        add("papuga", "Gadająca papuga", DOBRY, "R", 5, c -> c.spawn(Parrot.class, 1, k -> { k.setTamed(true); k.setOwner(c.p); }));
        add("allay", "Pomocny duszek", DOBRY, "E", 4, c -> c.spawn(Allay.class, 2, null));
        add("kon", "Rumak z siodłem", DOBRY, "RE", 6, c -> c.spawn(Horse.class, 1, h -> {
            h.setTamed(true);
            h.setOwner(c.p);
            h.getInventory().setSaddle(new ItemStack(SADDLE));
            if (c.m() >= 2) h.getInventory().setArmor(new ItemStack(DIAMOND_HORSE_ARMOR));
            h.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.3);
            h.customName(Component.text("Błyskawica"));
        }));
        add("golem", "Żelazny obrońca", DOBRY, "EL", 5, c -> c.spawn(IronGolem.class, c.m() - 1, g -> {
            g.setPlayerCreated(true);
            g.customName(Component.text("Obrońca " + c.p.getName()));
        }));
        add("handlarz", "Handlarz szczęścia", DOBRY, "EL", 4, c -> c.spawn(Villager.class, 1, v -> {
            v.customName(Msg.mm("<gradient:#ffe259:#ffa751>Handlarz szczęścia"));
            v.setCustomNameVisible(true);
            v.setProfession(Villager.Profession.CLERIC);
            v.setVillagerLevel(5);
            List<MerchantRecipe> list = new ArrayList<>();
            MerchantRecipe a = new MerchantRecipe(it(DIAMOND, 2), 10);
            a.addIngredient(it(EMERALD, 1));
            MerchantRecipe b = new MerchantRecipe(it(GOLDEN_APPLE, 2), 5);
            b.addIngredient(it(EMERALD, 2));
            MerchantRecipe d = new MerchantRecipe(book(Enchantment.MENDING, 1), 2);
            d.addIngredient(it(EMERALD, 8));
            list.add(a);
            list.add(b);
            list.add(d);
            v.setRecipes(list);
        }));
        add("kopalnia", "Ukryta kopalnia", DOBRY, "EL", 4, c -> {
            int placed = 0;
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++) {
                    Block b = c.loc.getBlock().getRelative(x, -1, z);
                    Material t = b.getType();
                    if (t == STONE || t == DEEPSLATE || t == DIRT || t == GRASS_BLOCK || t == ANDESITE || t == GRANITE || t == DIORITE) {
                        b.setType(pick(List.of(DIAMOND_ORE, EMERALD_ORE, GOLD_ORE, DIAMOND_ORE, LAPIS_ORE)));
                        placed++;
                    }
                }
            if (placed == 0) c.give(it(DIAMOND_ORE, 3), it(EMERALD_ORE, 2));
            c.msg("<a>Pod Tobą błyszczy kopalnia! <m>Kop w dół.");
        });

        // =================================================================
        //  DOBRE – BUDOWLE (58-60)
        // =================================================================
        add("drzewo_pieniedzy", "Drzewo pieniędzy", DOBRY, "L", 3, c -> {
            Block base = c.loc.getBlock();
            for (int y = 0; y < 5; y++) c.place(base.getRelative(0, y, 0), OAK_LOG);
            for (int x = -2; x <= 2; x++)
                for (int y = 3; y <= 6; y++)
                    for (int z = -2; z <= 2; z++) {
                        if (Math.abs(x) + Math.abs(z) + Math.abs(y - 5) > 3) continue;
                        if (x == 0 && z == 0 && y < 5) continue;
                        Material leaf = rd() < 0.18 ? GOLD_BLOCK : rd() < 0.06 ? DIAMOND_BLOCK : rd() < 0.1 ? EMERALD_BLOCK : OAK_LEAVES;
                        c.place(base.getRelative(x, y, z), leaf);
                    }
            c.title("<gold><b>DRZEWO PIENIĘDZY", "<m>pieniądze jednak rosną na drzewach!");
        });
        add("pomnik", "Twój pomnik", DOBRY, "E", 3, c -> {
            Block b = c.loc.getBlock();
            c.place(b, GOLD_BLOCK);
            c.place(b.getRelative(BlockFace.UP), GOLD_BLOCK);
            Block top = b.getRelative(0, 2, 0);
            if (c.place(top, PLAYER_HEAD) && top.getState() instanceof org.bukkit.block.Skull s) {
                s.setProfile(io.papermc.paper.datacomponent.item.ResolvableProfile.resolvableProfile(c.p.getPlayerProfile()));
                s.update();
            }
            c.msg("<a>Wzniesiono Twój złoty pomnik! <m>(2 bloki złota do wzięcia)");
        });

        // =================================================================
        //  NEUTRALNE / ZABAWNE (61-75)
        // =================================================================
        add("fajerwerki", "Pokaz fajerwerków", NEUTRALNY, "REL", 8, c -> {
            for (int i = 0; i < 6 + 3 * c.m(); i++) {
                c.later(i * 5L, () -> c.firework(c.loc.clone().add(r(-4, 4), 1, r(-4, 4)),
                        Color.fromRGB(r(0, 255), r(0, 255), r(0, 255)), Color.WHITE));
            }
        });
        add("teczowe_owce", "Tęczowe owieczki", NEUTRALNY, "RE", 6, c -> c.spawn(Sheep.class, 5, s -> s.customName(Component.text("jeb_"))));
        add("kurczaki", "Armia kurczaków", NEUTRALNY, "R", 6, c -> {
            c.spawn(Chicken.class, 20, ch -> { if (rd() < 0.5) ch.setBaby(); });
            c.sound(Sound.ENTITY_CHICKEN_AMBIENT, 0.8f);
        });
        add("muzyka", "Szafa grająca", NEUTRALNY, "RE", 5, c -> {
            float[] notes = {0.5f, 0.63f, 0.75f, 1f, 0.75f, 1f, 1.26f, 1.5f};
            for (int i = 0; i < notes.length; i++) {
                float n = notes[i];
                c.later(i * 4L, () -> {
                    c.w.playSound(c.loc, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, n);
                    c.w.spawnParticle(Particle.NOTE, c.loc.clone().add(0, 1, 0), 3, 0.5, 0.3, 0.5, 1);
                });
            }
            c.give(it(pick(DISCS), 1), it(JUKEBOX, 1));
        });
        add("glowa", "Twoja głowa!", NEUTRALNY, "R", 5, c -> {
            ItemStack head = new ItemStack(PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            sm.setOwningPlayer(c.p);
            head.setItemMeta(sm);
            c.give(head);
            c.msg("<m>Trzymaj głowę wysoko!");
        });
        add("konfetti", "Konfetti", NEUTRALNY, "R", 6, c -> {
            for (int i = 0; i < 10; i++) c.later(i * 2L, () -> c.dust(Color.fromRGB(r(0, 255), r(0, 255), r(0, 255)), 30, 1.5));
            c.sound(Sound.ENTITY_PLAYER_LEVELUP, 1.8f);
            c.msg("<a>🎉 Gratulacje! <m>Wygrałeś… konfetti.");
        });
        add("wystrzal", "Wystrzał w niebo", NEUTRALNY, "RE", 6, c -> {
            c.p.setVelocity(new Vector(0, 2.4, 0));
            c.sound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f);
            c.later(15, () -> c.potion(PotionEffectType.SLOW_FALLING, 12, 0));
            c.msg("<a>Wziuuuu! <m>Spadochron otworzy się sam.");
        });
        add("deszcz_ryb", "Deszcz ryb", NEUTRALNY, "R", 5, c -> c.rain(() -> it(pick(List.of(COD, SALMON, TROPICAL_FISH, PUFFERFISH)), 1), 25, 4));
        add("pogoda", "Pogodynka", NEUTRALNY, "R", 4, c -> {
            c.w.setStorm(!c.w.hasStorm());
            c.msg(c.w.hasStorm() ? "<info>Nadciąga deszcz…" : "<a>Wychodzi słońce!");
        });
        add("czas", "Pan czasu", NEUTRALNY, "R", 4, c -> {
            c.w.setTime(c.w.getTime() % 24000 < 12000 ? 13000 : 1000);
            c.msg("<m>Czas się zmienił…");
        });
        add("falszywy_diament", "Diament?", NEUTRALNY, "R", 5, c -> {
            c.give(named(COAL, "<aqua>Diament", Enchantment.UNBREAKING, 1));
            c.later(40, () -> c.msg("<m>…to tylko węgiel. <err>Oszukany!"));
        });
        add("nic", "Nic", NEUTRALNY, "R", 5, c -> {
            c.title("<m>…", "<m>nic się nie stało");
            c.sound(Sound.ENTITY_VILLAGER_NO, 1f);
        });
        add("teleport", "Losowy teleport", NEUTRALNY, "RE", 5, c -> {
            Location to = c.safeSpot(30 * c.m());
            c.particles(Particle.PORTAL, 60, 0.5);
            c.p.teleport(to);
            c.p.getWorld().playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            c.msg("<info>Gdzie ja jestem?!");
        });
        add("dyskoteka", "Dyskoteka", NEUTRALNY, "RE", 5, c -> {
            for (int i = 0; i < 25; i++) {
                int t = i;
                c.later(i * 4L, () -> {
                    c.dust(Color.fromRGB(r(0, 255), r(0, 255), r(0, 255)), 25, 2.5);
                    c.w.playSound(c.loc, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.8f, 1f);
                    if (t % 2 == 0) c.w.playSound(c.loc, Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.5f);
                });
            }
            c.give(it(pick(DYES_WOOL), 16), it(GLOWSTONE, 8));
        });
        add("tecza", "Tęczowy deszcz", NEUTRALNY, "R", 5, c -> c.rain(() -> it(pick(DYES_WOOL), 2), 30, 5));

        // =================================================================
        //  ZŁE – WYBUCHY I KATAKLIZMY (76-79)
        // =================================================================
        add("tnt", "TNT!", ZLY, "RE", 9, c -> {
            c.spawn(TNTPrimed.class, c.m(), t -> t.setFuseTicks(50));
            c.title("<err><b>UCIEKAJ!", "<m>tik tak…");
        });
        add("deszcz_tnt", "Deszcz TNT", ZLY, "EL", 5, c -> {
            for (int i = 0; i < 4 * c.m(); i++) {
                c.later(i * 6L, () -> c.w.spawn(c.p.getLocation().add(r(-4, 4), 12, r(-4, 4)), TNTPrimed.class, t -> {
                    t.setFuseTicks(60);
                    t.getPersistentDataContainer().set(c.pl.mobKey(), PersistentDataType.BYTE, (byte) 1);
                }));
            }
            c.title("<err><b>DESZCZ TNT", "<m>w nogi!");
        });
        add("piorun", "Gniew bogów", ZLY, "RE", 7, c -> {
            c.w.strikeLightning(c.p.getLocation());
            if (c.m() >= 2) c.later(15, () -> c.w.strikeLightning(c.p.getLocation()));
        });
        add("kowadla", "Kowadła z nieba", ZLY, "RE", 6, c -> {
            for (int i = 0; i < 2 + c.m(); i++) {
                c.later(i * 8L, () -> c.w.spawnFallingBlock(c.p.getLocation().add(rd() - 0.5, 12, rd() - 0.5), ANVIL.createBlockData()));
            }
            c.msg("<err>Uwaga na głowę!");
        });

        // =================================================================
        //  ZŁE – POTWORY (80-94)
        // =================================================================
        add("creepery", "Syczące towarzystwo", ZLY, "RE", 7, c -> c.spawn(Creeper.class, 2 * c.m(), x -> target(x, c.p)));
        add("naladowany", "Naładowany creeper", ZLY, "EL", 4, c -> c.spawn(Creeper.class, 1, x -> { x.setPowered(true); target(x, c.p); }));
        add("zombie", "Horda zombie", ZLY, "REL", 8, c -> c.spawn(Zombie.class, 3 * c.m(), z -> {
            armor(z, IRON_HELMET, IRON_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS, 0.05f);
            target(z, c.p);
        }));
        add("szkielety", "Snajperzy", ZLY, "RE", 7, c -> c.spawn(Skeleton.class, 2 * c.m(), s -> {
            s.getEquipment().setItemInMainHand(named(BOW, null, Enchantment.POWER, c.m()));
            target(s, c.p);
        }));
        add("pajaki", "Pajęcze gniazdo", ZLY, "R", 6, c -> c.spawn(CaveSpider.class, 4, s -> target(s, c.p)));
        add("rybiki", "Plaga rybików", ZLY, "R", 5, c -> c.spawn(Silverfish.class, 8, s -> target(s, c.p)));
        add("czarownice", "Sabat czarownic", ZLY, "E", 5, c -> c.spawn(Witch.class, 3, s -> target(s, c.p)));
        add("ravager", "Niszczyciel", ZLY, "EL", 3, c -> c.spawn(Ravager.class, 1, s -> target(s, c.p)));
        danger("warden", "Strażnik głębin", ZLY, "L", 1, c -> {
            c.spawn(Warden.class, 1, null);
            c.title("<dark_aqua><b>WARDEN", "<m>bądź cicho… bardzo cicho");
        });
        danger("wither", "Wither!", ZLY, "L", 1, c -> {
            c.spawn(Wither.class, 1, null);
            Msg.broadcast("<err>☠ " + c.p.getName() + " <m>wypuścił(a) z lucky blocka</m> <err><b>WITHERA</b><m>!");
        });
        add("vexy", "Duchy", ZLY, "EL", 4, c -> c.spawn(Vex.class, 2 + c.m(), v -> target(v, c.p)));
        add("slime", "Gigantyczny szlam", ZLY, "E", 4, c -> c.spawn(Slime.class, 1, s -> { s.setSize(6); target(s, c.p); }));
        add("blazy", "Piekielny ogień", ZLY, "EL", 4, c -> c.spawn(Blaze.class, 2 + c.m(), b -> target(b, c.p)));
        add("endermany", "Rozzłoszczone endermany", ZLY, "E", 4, c -> c.spawn(Enderman.class, 2, e -> target(e, c.p)));
        add("krol_zombie", "Król Zombie", NEUTRALNY, "EL", 4, c -> {
            c.spawn(Zombie.class, 1, z -> {
                z.customName(Msg.mm("<gradient:#56ab2f:#a8e063><b>Król Zombie</b></gradient>"));
                z.setCustomNameVisible(true);
                var scale = z.getAttribute(Attribute.SCALE);
                if (scale != null) scale.setBaseValue(1.8);
                z.getAttribute(Attribute.MAX_HEALTH).setBaseValue(60 * c.m());
                z.setHealth(60 * c.m());
                armor(z, GOLDEN_HELMET, DIAMOND_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS, 0f);
                z.getEquipment().setItemInMainHand(new ItemStack(DIAMOND_SWORD));
                z.getEquipment().setItemInMainHandDropChance(0f);
                z.getPersistentDataContainer().set(c.pl.bossKey(), PersistentDataType.STRING, c.tier.key);
                target(z, c.p);
            });
            c.title("<green><b>KRÓL ZOMBIE", "<m>pokonaj go, by zdobyć skarb!");
        });

        // =================================================================
        //  ZŁE – PUŁAPKI I KLĄTWY (95-100)
        // =================================================================
        add("klatka", "Szklana klatka", ZLY, "RE", 6, c -> {
            Block f = c.p.getLocation().getBlock();
            for (int x = -1; x <= 1; x++)
                for (int y = 0; y <= 2; y++)
                    for (int z = -1; z <= 1; z++) {
                        boolean wall = Math.abs(x) == 1 || Math.abs(z) == 1;
                        if (wall) c.temp(f.getRelative(x, y, z), GLASS, 160);
                    }
            c.temp(f.getRelative(0, 3, 0), GLASS, 160);
            c.p.teleport(f.getLocation().add(0.5, 0, 0.5).setDirection(c.p.getLocation().getDirection()));
            c.msg("<err>Uwięziony! <m>Klatka zniknie za 8 sekund.");
        });
        add("pajeczyny", "Pajęcza sieć", ZLY, "R", 6, c -> {
            Block f = c.p.getLocation().getBlock();
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) for (int y = 0; y <= 1; y++) c.temp(f.getRelative(x, y, z), COBWEB, 200);
        });
        add("debuffy", "Klątwa", ZLY, "RE", 7, c -> {
            c.potion(PotionEffectType.POISON, 6 * c.m(), 0);
            c.potion(PotionEffectType.NAUSEA, 10, 0);
            c.potion(PotionEffectType.BLINDNESS, 5, 0);
            c.potion(PotionEffectType.WEAKNESS, 20 * c.m(), 0);
        });
        add("glod", "Wielki głód", ZLY, "R", 5, c -> {
            c.p.setFoodLevel(2);
            c.p.setSaturation(0);
            c.potion(PotionEffectType.HUNGER, 30, 2);
        });
        add("zamrozenie", "Lodowy dotyk", ZLY, "RE", 5, c -> {
            c.p.setFreezeTicks(300);
            c.potion(PotionEffectType.SLOWNESS, 10, 3);
            c.potion(PotionEffectType.MINING_FATIGUE, 15, 2);
            c.particles(Particle.SNOWFLAKE, 60, 1);
        });
        add("lewitacja", "Lewitacja", ZLY, "RE", 5, c -> {
            c.potion(PotionEffectType.LEVITATION, 3 + 2 * c.m(), 1);
            c.later(20L * (3 + 2 * c.m()), () -> c.potion(PotionEffectType.SLOW_FALLING, 10, 0));
        });
    }
}
