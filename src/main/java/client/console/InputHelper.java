package client.console;

import common.model.*;

import java.time.ZonedDateTime;
import java.util.Scanner;

//Вспомогательный класс для ввода данных с консоли с валидацией

public class InputHelper {

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    //Считывает данные для создания объекта HumanBeing
    public HumanBeing readHumanBeing(Long existingId, User owner) {
        String name = readString("Введите имя: ", false);

        System.out.println("Введите координаты:");
        double x = readDouble("  X (double): ");
        Long y = readLong("  Y (> -228): ", -228L);
        Coordinates coordinates = new Coordinates(x, y);

        boolean realHero = readBoolean("Реальный герой? (true/false): ");
        boolean hasToothpick = readBoolean("Есть зубочистка? (true/false): ");
        long impactSpeed = readLong("impactSpeed (> -428): ", -428L);

        WeaponType weaponType = readEnum("weaponType", WeaponType.class);
        Mood mood = readEnum("mood", Mood.class);

        Car car = readCar();

        // Создание объекта
        if (existingId != null) {
            //Для update используем существующий ID
            return new HumanBeing(
                    existingId, name, coordinates, ZonedDateTime.now(),
                    realHero, hasToothpick, impactSpeed,
                    weaponType, mood, car, owner
            );
        } else {
            //Для add (ID сгенерирует БД)
            return new HumanBeing(
                    name, coordinates, realHero, hasToothpick, impactSpeed,
                    weaponType, mood, car, owner
            );
        }
    }

    public Car readCar() {
        boolean inputCompleted = false;
        Car resultCar = null;

        while (!inputCompleted) {
            System.out.print("Ввести данные автомобиля? (y/n): ");
            String carChoice = readString("", false).trim().toLowerCase();

            if (carChoice.equals("y") || carChoice.equals("yes")) {
                String carName = readString("  Имя автомобиля (может быть пустым): ", true);
                if (carName.isEmpty()) {
                    resultCar = null;
                } else {
                    resultCar = new Car(carName);
                }
                inputCompleted = true;
            } else if (carChoice.equals("n") || carChoice.equals("no")) {
                resultCar = null;
                inputCompleted = true;

            } else {
                System.out.println("   Введите y (да) или n (нет)");
            }
        }

        return resultCar;
    }

    public String readString(String prompt, boolean allowEmpty) {
        boolean validInput = false;
        String result = "";

        while (!validInput) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty() || allowEmpty) {
                result = line;
                validInput = true;
            } else {
                System.out.println("  Поле не может быть пустым");
            }
        }
        return result;
    }

    public double readDouble(String prompt) {
        boolean validInput = false;
        double result = 0.0;

        while (!validInput) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                result = Double.parseDouble(line);
                validInput = true;
            } catch (NumberFormatException e) {
                System.out.println("  Введите корректное число");
            }
        }
        return result;
    }

    public long readLong(String prompt, Long min) {
        boolean validInput = false;
        long result = 0;

        while (!validInput) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                long value = Long.parseLong(line);
                if (min != null && value <= min) {
                    System.out.println("  Значение должно быть больше " + min);
                } else {
                    result = value;
                    validInput = true;
                }
            } catch (NumberFormatException e) {
                System.out.println("  Введите корректное целое число");
            }
        }
        return result;
    }

    public boolean readBoolean(String prompt) {
        boolean validInput = false;
        boolean result = false;

        while (!validInput) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("true")) {
                result = true;
                validInput = true;
            } else if (line.equals("false")) {
                result = false;
                validInput = true;
            } else {
                System.out.println("  Введите true или false");
            }
        }
        return result;
    }

    public <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass) {
        boolean validInput = false;
        T result = null;

        while (!validInput) {
            System.out.print(prompt);
            System.out.print("(");
            T[] constants = enumClass.getEnumConstants();
            for (int i = 0; i < constants.length; i++) {
                System.out.print(constants[i].name());
                if (i < constants.length - 1) System.out.print(", ");
            }
            System.out.print("): ");

            String input = scanner.nextLine().trim().toUpperCase();
            try {
                result = Enum.valueOf(enumClass, input);
                validInput = true;
            } catch (IllegalArgumentException e) {
                System.out.println("  Недопустимое значение");
            }
        }
        return result;
    }
}