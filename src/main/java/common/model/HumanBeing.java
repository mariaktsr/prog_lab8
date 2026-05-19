package common.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

//Класс человека

public class HumanBeing implements Comparable<HumanBeing>, Serializable {

    private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Boolean realHero; //Поле не может быть null
    private boolean hasToothpick;
    private Long impactSpeed; //Значение поля должно быть больше -428, Поле не может быть null
    private WeaponType weaponType; //Поле не может быть null
    private Mood mood; //Поле не может быть null
    private Car car; //Поле может быть null
    private User owner; //Поле не может быть null, пользователь, создавший объект

    public HumanBeing() {
    }

    //Конструктор для нового элемента (создаётся на клиенте)
    public HumanBeing(String name, Coordinates coordinates,
                      Boolean realHero, boolean hasToothpick, Long impactSpeed,
                      WeaponType weaponType, Mood mood, Car car, User owner) {
        this.id = null;
        this.creationDate = null;
        setName(name);
        setCoordinates(coordinates);
        setRealHero(realHero);
        setHasToothpick(hasToothpick);
        setImpactSpeed(impactSpeed);
        setWeaponType(weaponType);
        setMood(mood);
        setCar(car);
        setOwner(owner);
    }

    //Конструктор для существующего элемента
    public HumanBeing(Long id, String name, Coordinates coordinates, ZonedDateTime creationDate,
                      Boolean realHero, boolean hasToothpick, Long impactSpeed,
                      WeaponType weaponType, Mood mood, Car car, User owner) {
        setId(id);
        setName(name);
        setCoordinates(coordinates);
        setCreationDate(creationDate);
        setRealHero(realHero);
        setHasToothpick(hasToothpick);
        setImpactSpeed(impactSpeed);
        setWeaponType(weaponType);
        setMood(mood);
        setCar(car);
        setOwner(owner);
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Coordinates getCoordinates() {
        return coordinates;
    }
    public ZonedDateTime getCreationDate() {
        return creationDate;
    }
    public Boolean getRealHero() {
        return realHero;
    }
    public boolean isHasToothpick() {
        return hasToothpick;
    }
    public Long getImpactSpeed() {
        return impactSpeed;
    }
    public WeaponType getWeaponType() {
        return weaponType;
    }
    public Mood getMood() {
        return mood;
    }
    public Car getCar() {
        return car;
    }
    public User getOwner() {
        return owner;
    }

    public void setId(Long id) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("ID не может быть null и должен быть больше 0");
        }
        this.id = id;
    }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть null или пустой строкой");
        }
        this.name = name;
    }
    public void setCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Координаты не могут быть null");
        }
        this.coordinates = coordinates;
    }
    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }
    public void setRealHero(Boolean realHero) {
        if (realHero == null) {
            throw new IllegalArgumentException("Поле realHero не может быть null");
        }
        this.realHero = realHero;
    }
    public void setHasToothpick(boolean hasToothpick) {
        this.hasToothpick = hasToothpick;
    }
    public void setImpactSpeed(Long impactSpeed) {
        if (impactSpeed == null || impactSpeed <= -428) {
            throw new IllegalArgumentException("Скорость удара не может быть null и должна быть больше -428");
        }
        this.impactSpeed = impactSpeed;
    }
    public void setWeaponType(WeaponType weaponType) {
        if (weaponType == null) {
            throw new IllegalArgumentException("Тип оружия не может быть null");
        }
        this.weaponType = weaponType;
    }
    public void setMood(Mood mood) {
        if (mood == null) {
            throw new IllegalArgumentException("Настроение не может быть null");
        }
        this.mood = mood;
    }
    public void setCar(Car car) {
        this.car = car;
    }
    public void setOwner(User owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Владелец объекта не может быть null");
        }
        this.owner = owner;
    }

    @Override
    public int compareTo(HumanBeing other) {
        if (this.id == null && other.id == null) return 0;
        if (this.id == null) return -1;
        if (other.id == null) return 1;
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HumanBeing that = (HumanBeing) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "HumanBeing{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", realHero=" + realHero +
                ", hasToothpick=" + hasToothpick +
                ", impactSpeed=" + impactSpeed +
                ", weaponType=" + weaponType +
                ", mood=" + mood +
                ", car=" + car +
                ", owner=" + owner +
                '}';
    }
}