package domain;

import java.math.BigInteger;
import java.util.Random;

public class BigNumber {
    public static BigInteger generate(int digits) {
        StringBuilder number = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < digits; i++) {
            number.append(r.nextInt(10));
        }
        return new BigInteger(number.toString());
    }

    public static BigInteger addZeros(BigInteger num, int offset) {
        StringBuilder res = new StringBuilder();
        res.append(num.toString());
        res.append("0".repeat(Math.max(0, offset)));
        return new BigInteger(res.toString());
    }
}
