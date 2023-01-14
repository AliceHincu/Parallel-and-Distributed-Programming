package Domain;

public class Vector {
    private final double[] vec;
    private final int size;

    public Vector(double[] vec){
        this.vec = vec;
        this.size = vec.length;
    }

    public double[] getVec() {
        return vec;
    }

    public int getSize() {
        return size;
    }
}