package common.model;

import java.io.Serializable;
import java.util.Objects;

//Класс координат

public class Coordinates implements Serializable {

    private double x;
    private Long y; //Значение поля должно быть больше -228, Поле не может быть null

    public Coordinates() {
    }

    public Coordinates(double x, Long y) {
        if (y == null || y <= -228) {
            throw new IllegalArgumentException("Координата Y не может быть null и должна быть больше -228");
        }
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }
    public Long getY() {
        return y;
    }

    public void setX(double x) { this.x = x; }
    public void setY(Long y) {
        if (y == null || y <= -228) {
            throw new IllegalArgumentException("Координата Y не может быть null и должна быть больше -228");
        }
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Double.compare(that.x, x) == 0 && Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}