package domain;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Operation {
    public static Polynomial multiplySimple(Object o, Object o1, int begin, int end) {
        Polynomial p1 = (Polynomial) o;
        Polynomial p2 = (Polynomial) o1;

        // Initialize the product polynomial
        int sizeOfResultCoefficientList = p1.getDegree() + p2.getDegree() + 1;
        List<Integer> coefficients = new ArrayList<>(Collections.nCopies(sizeOfResultCoefficientList, 0));

        // Multiply two polynomials term by term. Take [begin, end) terms from the first with every term of the second polynomial
        for (int i = begin; i < end; i++) {
            for (int j = 0; j < p2.getCoefficients().size(); j++) {
                int index = i + j;
                int valueToAdd = p1.getCoefficient(i) * p2.getCoefficient(j);
                coefficients.set(index, coefficients.get(index) + valueToAdd);
            }
        }
        return new Polynomial(coefficients);
    }

    public static Polynomial multiplyKaratsuba(Polynomial p1, Polynomial p2) {
        if (p1.getDegree() < 2 || p2.getDegree() < 2) {
            return multiplySimple(p1, p2, 0, p1.getDegree() + 1);
        }

        int mid = Math.max(p1.getDegree(), p2.getDegree()) / 2;

        Polynomial lowP1 = new Polynomial(p1.getCoefficients().subList(0, mid)); // right P1
        Polynomial highP1 = new Polynomial(p1.getCoefficients().subList(mid, p1.getDegree() + 1)); // left P1
        Polynomial lowP2 = new Polynomial(p2.getCoefficients().subList(0, mid)); // right P2
        Polynomial highP2 = new Polynomial(p2.getCoefficients().subList(mid, p2.getDegree() + 1));// left P2

        Polynomial z1 = multiplyKaratsuba(highP1, highP2); // a * c
        Polynomial z2 = multiplyKaratsuba(lowP1, lowP2); // b * d
        Polynomial z3 = multiplyKaratsuba(Polynomial.add(lowP1, highP1), Polynomial.add(lowP2, highP2)); // (a + b) * (c + d)

        // calculate the final result
        Polynomial r1 = Polynomial.shift(z1, 2 * mid); // z1 * x^2n
        Polynomial r2 = Polynomial.shift(Polynomial.subtract(Polynomial.subtract(z3, z1), z2), mid); // (z2 - z3 - z1) * x^n
        return Polynomial.add(Polynomial.add(r1, r2), z2);
    }

    public static BigInteger multiplyKaratsuba(BigInteger x, BigInteger y) {
        int len = Math.min(x.toString().length(), y.toString().length());
        if (len < 10) /* fall back to traditional multiplication */
            return x.multiply(y);

        len /= 2;

        String xs = x.toString();
        String ys = y.toString();

        BigInteger high1 = new BigInteger(xs.substring(0, xs.length() - len));
        BigInteger low1 = new BigInteger(xs.substring(xs.length() - len));
        BigInteger high2 = new BigInteger(ys.substring(0, ys.length() - len));
        BigInteger low2 = new BigInteger(ys.substring(ys.length() - len));

        BigInteger z1 = multiplyKaratsuba(high1, high2);  // a*c
        BigInteger z2 = multiplyKaratsuba(low1, low2);  // b*d
        BigInteger z3 = multiplyKaratsuba(low1.add(high1), low2.add(high2)); // (a+b) * (c+d)

        BigInteger r1 = BigNumber.addZeros(z1, 2 * len);
        BigInteger r2 = BigNumber.addZeros(z3.subtract(z1).subtract(z2), len);

        return r1.add(r2).add(z2);
    }

    public static Polynomial buildResult(Object[] results) {
        int degree = ((Polynomial) results[0]).getDegree();
        List<Integer> coefficients = new ArrayList<>(Collections.nCopies(degree + 1, 0));

        for (int i = 0; i < degree + 1; i++) {
            for (Object o : results) {
                coefficients.set(i, coefficients.get(i) + ((Polynomial) o).getCoefficients().get(i));
            }
        }
        return new Polynomial(coefficients);
    }

    public static BigInteger buildResultBigNumber(Object[] results) {
        int size = results.length;
        BigInteger result = new BigInteger(String.valueOf(0));

        for (Object o : results) {
            BigInteger number = (BigInteger) o;
            result = result.add(number);
        }
        return result;
    }
}