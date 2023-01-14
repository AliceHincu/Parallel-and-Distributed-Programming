import mpi.MPI;

import java.lang.constant.Constable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DSM {
    // The DSM mechanism shall provide a predefined number of integer variables residing on each of the processes.
    private final Map<String, Object> variables;
    private final Map<String, Set<Integer>> subscribers;
    private final int me = MPI.COMM_WORLD.Rank();
    private final int[] counter = new int[3]; // main, worker1, worker2

    public DSM() {
        this.variables = new ConcurrentHashMap<>();
        variables.put("a", 0);
        variables.put("b", 0);
        variables.put("c", 0);

        subscribers = new ConcurrentHashMap<>();
        subscribers.put("a", new HashSet<>());
        subscribers.put("b", new HashSet<>());
        subscribers.put("c", new HashSet<>());

        counter[me] = 1;

        System.out.printf("%s Started %s.%n", DSM.getStringTimestamp(counter), me);
    }

    public void subscribe(final String variable) {
        subscribers.get(variable).add(me);
        counter[me] += 1;
        System.out.printf("%s Process nr. %s - Sending SUBSCRIBE message for variable '%s'.%n", DSM.getStringTimestamp(counter), me, variable);
        final Message message = Message.subscribeMessage(variable, me, counter);
        sendToAll(message);
    }

    public void syncSubscription(final String variable, final int rank, final int[] rankCounter) {
        subscribers.get(variable).add(rank);
        counter[me] += 1;
        updateCounter(rankCounter);
        System.out.printf("%s Process nr. %s - Received SUBSCRIBE message from %s for variable '%s'.%n", DSM.getStringTimestamp(counter), me, rank, variable);
    }

    private void updateCounter(final int[] otherTimeStamp) {
        for(int i=0; i<MPI.COMM_WORLD.Size(); i++)
            if(me != i) counter[i] = Math.max(counter[i], otherTimeStamp[i]);
    }

    private void sendToSubscribers(final String variable, final Message message) {
        for (int i = 0; i < MPI.COMM_WORLD.Size(); i++) {
            // Only nodes that subscribe to a variable will receive notifications about changes of that variable
            if (me == i || !subscribers.get(variable).contains(i))
                continue;

            MPI.COMM_WORLD.Send(new Object[]{message}, 0, 1, MPI.OBJECT, i, 0);
        }
    }

    private void sendToAll(final Message message) {
        for (int i = 0; i < MPI.COMM_WORLD.Size(); i++) {
            if (me == i && !message.getType().equals(Message.Type.QUIT)) {
                continue;
            }

            MPI.COMM_WORLD.Send(new Object[]{message}, 0, 1, MPI.OBJECT, i, 0);
        }
    }


    /**
     * Write a value to a variable (local or residing in another process);
     * @param variable
     * @param value
     */
    public synchronized void updateVariable(final String variable, final Integer value) {
        setVariable(variable, value, me, counter);
        counter[me] += 1;
        System.out.printf("%s Process nr. %s - Sending UPDATE message for variable '%s'.%n", DSM.getStringTimestamp(counter), me, variable);
        final Message message = Message.updateMessage(variable, value, me, counter);
        sendToSubscribers(variable, message);
    }

    public synchronized void compareAndExchange(final String variable, final Integer oldValue, final Integer newValue) {
        if (oldValue.equals(variables.get(variable))) {
            updateVariable(variable, newValue);
        }
    }

    public synchronized void setVariable(final String variable, final Integer value, final Integer senderRank, final int[] rankCounter) {
        if (senderRank != me) {
            counter[me] += 1;
            updateCounter(rankCounter);
            System.out.printf("%s Process nr. %s - Received UPDATE message from %s to update variable '%s' = %s).%n", DSM.getStringTimestamp(counter), me, senderRank, variable, value);
        }

        if (variables.containsKey(variable))
            variables.put(variable, value);
    }

    public void close() {
        counter[me] += 1;
        System.out.printf("%s Process nr. %s - Sending CLOSE message.%n", DSM.getStringTimestamp(counter), me);
        sendToAll(Message.quitMessage(me, counter));
    }

    public void finalize(int senderRank, int[] timestamp) {
        if(senderRank != me) {
            counter[me] += 1;
            updateCounter(timestamp);
            System.out.printf("%s Process nr. %s - Received QUIT message.%n", DSM.getStringTimestamp(counter), me);
        }
    }

    public static String getStringTimestamp(int[] timestamp){
        return "(" + timestamp[0] + ", " + timestamp[1] + ", " + timestamp[2] + ")";
    }

    @Override
    public String toString() {
        return "DSM{" +
                "variables=" + variables +
                ", subscribers=" + subscribers +
                '}';
    }
}
