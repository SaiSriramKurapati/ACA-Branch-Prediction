
public class TageEntry {
    private int tag;
    private Counter counter;

    public TageEntry(int tag, int counterMax) {
        this.tag = tag;
        this.counter = new Counter(counterMax);
    }

    public int getTag() {
        return tag;
    }

    public void setTag( int tag) {
        this.tag = tag;
    }

    public Counter getCounter() {
        return counter;
    }
}