import mpi.MPI;

public class Listener implements Runnable{
    private final DSM dsm;

    public Listener(final DSM dsm) {
        this.dsm = dsm;
    }

    @Override
    public void run() {
        final int me = MPI.COMM_WORLD.Rank();

        while (true) {
            final Object[] messageBuffer = new Object[1];

            MPI.COMM_WORLD.Recv(messageBuffer, 0, 1, MPI.OBJECT, MPI.ANY_SOURCE, MPI.ANY_TAG);

            final Message message = (Message) messageBuffer[0];
            final Message.Type messageType = message.getType();
            String timestamp = DSM.getStringTimestamp(message.getTimestamp());

            switch (messageType) {
                case SUBSCRIBE:
                    dsm.syncSubscription(message.getVariable(), message.getRank(), message.getTimestamp());
                    break;
                case UPDATE:
                    dsm.setVariable(message.getVariable(), message.getValue(), message.getRank(), message.getTimestamp());
                    break;
                case QUIT:
                    dsm.finalize(message.getRank(), message.getTimestamp());
                    System.out.printf("Worker nr. %s - Final DSM state: %s.%n", me, dsm);
                    break;
                default:
                    System.out.println("Received a weird message...");
            }
        }
    }
}
