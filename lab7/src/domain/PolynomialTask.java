package domain;

public class PolynomialTask implements Runnable {
    private final int start;
    private final int end;
    private final Polynomial p1, p2, result;

    public PolynomialTask(int start, int end, Polynomial p1, Polynomial p2, Polynomial result) {
        this.start = start;
        this.end = end;
        this.p1 = p1;
        this.p2 = p2;
        this.result = result;
    }

    @Override
    public void run() {
        // calculate coefficients of result that are in the interval [start, end]
        for (int resultIndex = start; resultIndex < end; resultIndex++) {
            for (int k = 0; k <= resultIndex; k++) {
                if (validateIndexes(k, resultIndex)) {
                    int value = p1.getCoefficient(k) * p2.getCoefficient(resultIndex - k);
                    result.getCoefficients().set(resultIndex, result.getCoefficients().get(resultIndex) + value);
                }
            }
        }
    }

    /**
     * Verify that the indexes are not bigger than the degree of their polynomial
     * @param k => the index of the coefficient from p1
     * @param resultIndex - k => the index of the coefficient from p2
     * @return
     */
    private boolean validateIndexes(int k, int resultIndex){
        return k <= p1.getDegree() && (resultIndex - k) <= p2.getDegree();
    }
}