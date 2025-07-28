package vn.edu.fpt.pokemongame;
import android.graphics.Point;

public class PointPair {
    public Point p1;
    public Point p2;

    public PointPair(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    // The Set will compare the hashCode of the object FIRST. If the hashCodes are equal, it will check the equals method.
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PointPair))
            return false;
        PointPair other = (PointPair) o;
        return (p1.equals(other.p1) && p2.equals(other.p2)) || (p1.equals(other.p2) && p2.equals(other.p1));
    }

    @Override
    public int hashCode() {
        return p1.hashCode() ^ p2.hashCode(); //we can return p1.hashCode() + p2.hashCode() but it will overflow due to size of int
    }
}