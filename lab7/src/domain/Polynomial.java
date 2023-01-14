package domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Serializable = convert its state to a byte stream so that the byte stream can be reverted into a copy of the object.
 */
public class Polynomial implements Serializable {
    private final List<Integer> coefficients;   // coefficients p(x) = sum { coef[i] * x^i }
    private final int MAX_COEFFICIENT_VALUE = 5;
    private int degree;

    public Polynomial(List<Integer> coefficients) {
        this.coefficients = coefficients;
        this.degree = coefficients.size() - 1;
    }

    /**
     * Generate a polynomial with the given degree
     * @param degree - degree of wanted polynomial
     */
    public Polynomial(int degree) {
        this.degree = degree;
        this.coefficients = new ArrayList<>(degree + 1);

        Random randomGenerator = new Random();
        for (int i = 0; i < degree; i++) {
//            this.coefficients.add(randomGenerator.nextInt(MAX_COEFFICIENT_VALUE));
            this.coefficients.add(1);
        }
//        this.coefficients.add(randomGenerator.nextInt(1, MAX_COEFFICIENT_VALUE));
        this.coefficients.add(1);
    }

    /**
     * Returns the sum of two polynomials.
     *
     * @param  p1 the first polynomial
     * @param  p2 the second polynomial
     * @return the polynomial whose value is {@code (p1 + p2)}
     */
    public static Polynomial add(Polynomial p1, Polynomial p2) {
        int maxDegree = Math.max(p1.getDegree(), p2.getDegree());
        List<Integer> newCoefficients = new ArrayList<>(Collections.nCopies(maxDegree + 1, 0));
        for (int i = 0; i <= p1.getDegree(); i++) newCoefficients.set(i, p1.getCoefficient(i));
        for (int i = 0; i <= p2.getDegree(); i++) newCoefficients.set(i, newCoefficients.get(i)+p2.getCoefficient(i));
        Polynomial poly = new Polynomial(newCoefficients);
        return poly;
    }
    /**
     * Returns the result of subtracting p2 from p1
     *
     * @param  p1 the first polynomial
     * @param  p2 the second polynomial
     * @return the polynomial whose value is {@code (p1 - p2)}
     */
    public static Polynomial subtract(Polynomial p1, Polynomial p2) {
        int maxDegree = Math.max(p1.getDegree(), p2.getDegree());
        List<Integer> newCoefficients = new ArrayList<>(Collections.nCopies(maxDegree + 1, 0));
        for (int i = 0; i <= p1.getDegree(); i++) newCoefficients.set(i, p1.getCoefficient(i));
        for (int i = 0; i <= p2.getDegree(); i++) newCoefficients.set(i, newCoefficients.get(i)-p2.getCoefficient(i));
        Polynomial poly = new Polynomial(newCoefficients);
        poly.reduce();
        return poly;
    }
//    public static Polynomial subtract(Polynomial p1, Polynomial p2) {
//        int minDegree = Math.min(p1.getDegree(), p2.getDegree());
//        int maxDegree = Math.max(p1.getDegree(), p2.getDegree());
//        List<Integer> coefficients = new ArrayList<>(maxDegree + 1);
//
//        // Subtract the 2 polynomials
//        for (int i = 0; i <= minDegree; i++) {
//            coefficients.add(p1.getCoefficients().get(i) - p2.getCoefficients().get(i));
//        }
//
//        addRemainingCoefficients(p1, p2, minDegree, maxDegree, coefficients);
//
//        // Remove coefficients starting from biggest power if coefficient is 0
//
//        int i = coefficients.size() - 1;
//        while (coefficients.get(i) == 0 && i > 0) {
//            coefficients.remove(i);
//            i--;
//        }
//
//        return new Polynomial(coefficients);
//    }

    /**
     * pre-compute the degree of the polynomial, in case of leading zero coefficients
     * (that is, the length of the array need not relate to the degree of the polynomial)
     */
    private void reduce() {
        degree = -1;
        for (int i = this.coefficients.size() - 1; i >= 0; i--) {
            if (this.coefficients.get(i) != 0) {
                degree = i;
                return;
            }
        }
    }

    public static Polynomial shift(Polynomial p, int offset) {
        List<Integer> coefficients = new ArrayList<>();
        for (int i = 0; i < offset; i++) coefficients.add(0);
        for (int i = 0; i <= p.getDegree(); i++) coefficients.add(p.getCoefficients().get(i));
        return new Polynomial(coefficients);
    }

    public List<Integer> getCoefficients() {
        return coefficients;
    }

    public Integer getCoefficient(int index){
        return coefficients.get(index);
    }

    public void setCoefficient(int index, Integer value){
        coefficients.set(index, value);
    }

    public int getDegree() {
        return degree;
    }

    /**
     * Return a string representation of this polynomial.
     * @return a string representation of this polynomial in the format
     *         4x^5 - 3x^2 + 11x + 5
     */
    @Override
    public String toString() {
        if      (degree == -1) return "0";
        else if (degree ==  0) return "" + coefficients.get(0);
        else if (degree ==  1) return coefficients.get(1) + "x + " + coefficients.get(0);

        StringBuilder s = new StringBuilder(coefficients.get(degree) + "x^" + degree);
        for (int i = degree - 1; i >= 0; i--) {
            if      (coefficients.get(i) == 0) continue;
            else if (coefficients.get(i)  > 0) s.append(" + ").append(coefficients.get(i));
            else if (coefficients.get(i)  < 0) s.append(" - ").append(-coefficients.get(i));
            if      (i == 1) s.append("x");
            else if (i >  1) s.append("x^").append(i);
        }
        return s.toString();
    }
}
