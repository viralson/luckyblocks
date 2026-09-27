package pl.lucky;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Przywraca bloki postawione tymczasowo przez efekty (klatki, pajęczyny...). */
public final class Restorer {

    private record Entry(Block b, BlockData old, long at) {}

    private final List<Entry> entries = new ArrayList<>();
    private long tick;

    public void add(Block b, BlockData old, long ticks) { entries.add(new Entry(b, old, tick + ticks)); }

    public void tick() {
        tick++;
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            if (e.at() > tick) continue;
            e.b().setBlockData(e.old(), false);
            it.remove();
        }
    }

    public void restoreAll() {
        for (Entry e : entries) e.b().setBlockData(e.old(), false);
        entries.clear();
    }
}
