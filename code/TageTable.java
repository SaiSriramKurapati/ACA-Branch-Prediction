import java.util.ArrayList;
import java.util.List;

public class TageTable {
    private List<TageEntry> entries;

    public TageTable(int size, int counterMax) {
        entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new TageEntry(0, counterMax));
        }
    }

    public TageEntry getEntry(int index) {
        return entries.get(index);
    }
}