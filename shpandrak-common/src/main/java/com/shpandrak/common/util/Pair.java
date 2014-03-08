package com.shpandrak.common.util;

/**
 * Created with IntelliJ IDEA.
 * User: liebea
 * Date: 3/27/13
 * Time: 2:47 PM
 */
public class Pair<A, B> {
    private A aObject;
    private B bObject;

    public static <A,B> Pair<A,B> of(A aObject, B bObject){
        return new Pair<A, B>(aObject, bObject);
    }

    public Pair(A aObject, B bObject) {
        this.aObject = aObject;
        this.bObject = bObject;
    }

    public A getA() {
        return aObject;
    }

    public B getB() {
        return bObject;
    }

    public void setA(A aObject) {
        this.aObject = aObject;
    }

    public void setB(B bObject) {
        this.bObject = bObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (aObject != null ? !aObject.equals(pair.aObject) : pair.aObject != null) return false;
        if (bObject != null ? !bObject.equals(pair.bObject) : pair.bObject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = aObject != null ? aObject.hashCode() : 0;
        result = 31 * result + (bObject != null ? bObject.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "aObject=" + aObject +
                ", bObject=" + bObject +
                '}';
    }
}
