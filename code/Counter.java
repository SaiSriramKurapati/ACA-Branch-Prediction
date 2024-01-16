
public class Counter {
    private int value;
    private int max;

    public Counter(int max) {
        this.max = max;
    }

    public void increment() {
        if (value < max) {
            value++;
        }
    }

    public void decrement() {
        if (value > 0) {
            value--;
        }
    }

    public int getValue() {
        return value;
    }
}