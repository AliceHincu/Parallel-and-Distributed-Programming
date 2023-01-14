package Problem3;

public class Notification implements Runnable {
    private final Node primaryVariable;
    private final Integer newValue;

    public Notification(Node primaryVariable, Integer newValue) {
        this.primaryVariable = primaryVariable;
        this.newValue = newValue;
    }

    @Override
    public void run() {
        this.primaryVariable.setData(newValue);
    }

    @Override
    public String toString() {
        return "The primary variable " + primaryVariable + " will have the new value: " + newValue;
    }
}
