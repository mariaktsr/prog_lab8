package common.model;

import java.io.Serializable;
import java.util.Objects;

//Класс автомобиля

public class Car implements Serializable {

    private String name; //Поле может быть null

    public Car() {
    }

    public Car(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(name, car.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        } else {
            return "null";
        }
    }
}