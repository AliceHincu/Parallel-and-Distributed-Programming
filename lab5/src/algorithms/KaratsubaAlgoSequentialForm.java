package algorithms;

import domain.BigNumber;
import domain.Polynomial;

import java.math.BigInteger;

public class KaratsubaAlgoSequentialForm {
    /**
     * <a href="https://medium.com/analytics-vidhya/what-is-karatsuba-approach-for-efficient-polynomial-multiplication-e033032f2309">...</a>
     * Algorithm Steps:
     *
     * 1. Divide each polynomial in 2 halves: upper and lower.
     * 2. For first polynomial we denote upper and lower halves with highP1 and lowP1 respectively.
     * For second polynomial we denote the same by highP2 and lowP2.
     * 3. Upper half of each polynomial is in the form: a1* x ^ (n/2 -1) + a2 * x ^ (n/2-2) + ……., where a1, a2…. are coefficients of each term, and are actually like a ((n-1) in subscript), a((n-2) in subscript) and goes till a(n/2 in subscript).
     * 4. Lower half of each polynomial is in the form: a1* x ^ (n/2 -1) + a2 * x ^ (n/2–2) + ……., where a1, a2…. are coefficients of each term, and are actually like a ((n/2 -1) in subscript), a((n/2-2) in subscript) and goes till a(0 in subscript).
     * 5. Now, to obtain the final multiplied polynomial, we have to do,
     * z1 = (highP2 * highP1)
     * z2 = (lowP1 * lowP2)
     * z3 = (highP2 + lowP2) * (lowP1 + highP1)
     * r1 = (highP2 * highP1) * (x^n)
     * r2 = (z3 - z1 - z2) * (x ^ n/2)
     * ( r1 + r2 + z2) < = it represents the final multiplied polynomial.
     *
     * @param p1 - first polynomial
     * @param p2 - second polynomial
     * @return - the multiplication of the 2 polynomials
     */
    public static Polynomial multiply(Polynomial p1, Polynomial p2) {
        if (p1.getDegree() < 2 || p2.getDegree() < 2) {
            return RegularAlgoSequentialForm.multiply(p1, p2);
        }

        int mid = Math.max(p1.getDegree(), p2.getDegree()) / 2;

        Polynomial lowP1 = new Polynomial(p1.getCoefficients().subList(0, mid)); // right P1
        Polynomial highP1 = new Polynomial(p1.getCoefficients().subList(mid, p1.getDegree() + 1)); // left P1
        Polynomial lowP2 = new Polynomial(p2.getCoefficients().subList(0, mid)); // right P2
        Polynomial highP2 = new Polynomial(p2.getCoefficients().subList(mid, p2.getDegree() + 1));// left P2

        Polynomial z1 = KaratsubaAlgoSequentialForm.multiply(highP1, highP2); // a * c
        Polynomial z2 = KaratsubaAlgoSequentialForm.multiply(lowP1, lowP2); // b * d
        Polynomial z3 = KaratsubaAlgoSequentialForm.multiply(Polynomial.add(lowP1, highP1), Polynomial.add(lowP2, highP2)); // (a + b) * (c + d)

        //calculate the final result
        Polynomial r1 = Polynomial.shift(z1, 2 * mid); // z1 * x^2n
        Polynomial r2 = Polynomial.shift(Polynomial.subtract(Polynomial.subtract(z3, z1), z2), mid); // (z2 - z3 - z1) * x^n
        return Polynomial.add(Polynomial.add(r1, r2), z2);
    }

    /**
     * Algorithm Steps: (big numbers)
     * 1. Compute starting set  (a*c)
     * 2. Compute set after starting set may it be ending set (b*d)
     * 3. Compute starting set with ending sets
     * 4. Subtract values of step 3 from step2 from step1
     * 5. Pad up (Add) n zeros to the number obtained from Step1, step2 value unchanged, and pad up n/2 to value
     * obtained from step4.
     *
     * @param x - first nr
     * @param y - second nr
     * @return - the multiplication result
     */
    public static BigInteger multiply(BigInteger x, BigInteger y) {
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

        BigInteger z1 = multiply(high1, high2);  // a*c
        BigInteger z2 = multiply(low1, low2);  // b*d
        BigInteger z3 = multiply(low1.add(high1), low2.add(high2)); // (a+b) * (c+d)

        BigInteger r1 = BigNumber.addZeros(z1, 2 * len);
        BigInteger r2 = BigNumber.addZeros(z3.subtract(z1).subtract(z2), len);

        return r1.add(r2).add(z2);
    }
}
