import java.io.Serializable;

public class Message implements Serializable {
    private String variable;
    private Integer value;
    private Integer rank;
    private Type type;
    private int[] timestamp;

    public Message(String variable, Integer value, Integer rank, Type type) {
        this.variable = variable;
        this.value = value;
        this.rank = rank;
        this.type = type;
    }

    public Message(String variable, Integer value, Integer rank, Type type, int[] timestamp) {
        this.variable = variable;
        this.value = value;
        this.rank = rank;
        this.type = type;
        this.timestamp = timestamp;
    }

    public static Message subscribeMessage(String variable, Integer rank, int[] timestamp) {
        return new Message(variable, 0, rank, Type.SUBSCRIBE, timestamp);
    }

    public static Message updateMessage(String variable, Integer value, Integer rank, int[] timestamp) {
        return new Message(variable, value, rank, Type.UPDATE, timestamp);
    }

    public static Message quitMessage(Integer rank, int[] timestamp) {
        return new Message("", 0, rank, Type.QUIT, timestamp);
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "variable='" + variable + '\'' +
                ", value=" + value +
                ", rank=" + rank +
                ", type=" + type +
                '}';
    }

    public int[] getTimestamp() {
        return timestamp;
    }

    public enum Type {
        SUBSCRIBE, UPDATE, QUIT
    }
}
