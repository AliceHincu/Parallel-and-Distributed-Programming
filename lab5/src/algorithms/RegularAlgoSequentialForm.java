package algorithms;

import domain.Polynomial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegularAlgoSequentialForm {
    /**
     * 1) Create a product array prod[] of size m+n-1.
     * 2) Initialize all entries in prod[] as 0.
     * 3) Traverse array A[] and do following for every element A[i]
     * ...(3.a) Traverse array B[] and do following for every element B[j]
     * prod[i+j] = prod[i+j] + A[i] * B[j]
     * 4) Return prod[].
     *
     * @param p1 - first polynomial
     * @param p2 - second polynomial
     * @return - the multiplication of the 2 polynomials
     */
    public static Polynomial multiply(Polynomial p1, Polynomial p2) {
        // Initialize the product polynomial
        int sizeOfResultCoefficientList = p1.getDegree() + p2.getDegree() + 1;
        List<Integer> coefficients = new ArrayList<>(Collections.nCopies(sizeOfResultCoefficientList, 0));

        // Multiply two polynomials term by term. Take ever term of first polynomial
        for (int i = 0; i < p1.getCoefficients().size(); i++) {
            // Multiply the current term of first polynomial with every term of second polynomial.
            for (int j = 0; j < p2.getCoefficients().size(); j++) {
                int index = i + j;
                int valueToAdd = p1.getCoefficient(i) * p2.getCoefficient(j);
                coefficients.set(index, coefficients.get(index) + valueToAdd);
            }
        }
        return new Polynomial(coefficients);
    }
}
