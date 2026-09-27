package pl.lucky;

import java.util.function.Consumer;

/**
 * Jeden efekt lucky blocka.
 * tiers – w których blokach może wypaść: "R" rzadki, "E" epicki, "L" legendarny (np. "REL").
 */
public record Effect(String id, String name, Kind kind, String tiers, int weight, boolean dangerous, Consumer<Ctx> action) {

    public enum Kind {
        DOBRY("<ok>szczęście"), NEUTRALNY("<info>niespodzianka"), ZLY("<err>pech");
        public final String display;
        Kind(String d) { display = d; }
    }

    public boolean in(Tier t) { return tiers.indexOf(t.code()) >= 0; }
}
