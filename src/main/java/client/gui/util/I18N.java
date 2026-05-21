package client.gui.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Менеджер локализации с встроенными ресурсами.
 * Все переводы хранятся в коде — не нужны внешние .properties файлы.
 */
public class I18N {

    // ✅ Поддерживаемые локали
    public static final Locale RU = new Locale("ru");
    public static final Locale NO = new Locale("no", "NO");
    public static final Locale LT = new Locale("lt", "LT");
    public static final Locale EN_GB = Locale.UK;

    public static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(RU, NO, LT, EN_GB);

    // ✅ Хранилище переводов: Locale -> (key -> value)
    static final Map<Locale, Map<String, String>> BUNDLES = new HashMap<>();

    private static Locale currentLocale = RU;
    private static final ObjectProperty<Locale> localeProperty = new SimpleObjectProperty<>(RU);

    // Форматтеры
    private static NumberFormat numberFormat;
    private static DateTimeFormatter dateTimeFormatter;

    static {
        initializeBundles();
        setLocale(currentLocale);
    }

    // ========================================================================
    // 🔹 Инициализация переводов
    // ========================================================================

    private static void initializeBundles() {
        // === English (UK) ===
        Map<String, String> en = new HashMap<>();
        en.put("window.title", "HumanBeing Collection");
        en.put("window.auth.title", "Authorization");
        en.put("window.main.title", "Main Window");

        en.put("auth.username", "Username");
        en.put("auth.password", "Password");
        en.put("auth.login", "Log in");
        en.put("auth.register", "Register");
        en.put("auth.welcome", "Welcome, {0}!");
        en.put("auth.error.empty", "Username and password cannot be empty");
        en.put("auth.error.failed", "Error: {0}");
        en.put("auth.connected", "Connected to server");
        en.put("auth.not.connected", "Not connected to server");

        en.put("settings.language", "Language:");

        en.put("main.user.label", "User:");
        en.put("main.menu.commands", "Commands");
        en.put("main.menu.help", "Help");
        en.put("main.toolbar.refresh", "Refresh");
        en.put("main.toolbar.add", "Add");
        en.put("main.toolbar.delete", "Delete");
        en.put("main.toolbar.logout", "Logout");
        en.put("main.search.placeholder", "Search...");

        en.put("table.id", "ID");
        en.put("table.name", "Name");
        en.put("table.coordinates", "Coordinates");
        en.put("table.creationDate", "Creation date");
        en.put("table.realHero", "Real hero");
        en.put("table.hasToothpick", "Has toothpick");
        en.put("table.impactSpeed", "Impact speed");
        en.put("table.weaponType", "Weapon type");
        en.put("table.mood", "Mood");
        en.put("table.car", "Car");
        en.put("table.owner", "Owner");

        en.put("table.filter", "Filter:");
        en.put("table.sort.asc", "Sort ascending");
        en.put("table.sort.desc", "Sort descending");
        en.put("table.edit", "Edit");
        en.put("table.remove", "Remove");

        en.put("dialog.add.title", "Add Object");
        en.put("dialog.add.header", "Enter new object data");
        en.put("dialog.edit.title", "Edit Object");
        en.put("dialog.edit.header", "Editing: {0}");
        en.put("dialog.name", "Name");
        en.put("dialog.x", "X coordinate");
        en.put("dialog.y", "Y coordinate (>-228)");
        en.put("dialog.realHero", "Real hero?");
        en.put("dialog.hasToothpick", "Has toothpick?");
        en.put("dialog.impactSpeed", "Impact speed (>-428)");
        en.put("dialog.weaponType", "Weapon type");
        en.put("dialog.mood", "Mood");
        en.put("dialog.hasCar", "Has car?");
        en.put("dialog.carName", "Car name");
        en.put("dialog.save", "Save");
        en.put("dialog.cancel", "Cancel");

        en.put("error.validation.title", "Validation Error");
        en.put("error.validation.header", "Please check entered data");
        en.put("error.validation.number", "Coordinates and speed must be valid numbers!");
        en.put("error.validation.name", "Name cannot be empty!");
        en.put("error.validation.y", "Y coordinate must be greater than -228!");
        en.put("error.validation.speed", "Impact speed must be greater than -428!");

        en.put("cmd.show", "Show all");
        en.put("cmd.info", "Collection info");
        en.put("cmd.clear", "Clear collection");
        en.put("cmd.sort", "Sort collection");
        en.put("cmd.sum", "Sum of impact speed");
        en.put("cmd.print.mood", "Print by mood (descending)");
        en.put("cmd.filter.name", "Filter by name");
        en.put("cmd.remove.index", "Remove by index");
        en.put("cmd.remove.id", "Remove by ID");
        en.put("cmd.insert", "Insert at index");
        en.put("cmd.add", "Add object");
        en.put("cmd.update", "Update object");
        en.put("cmd.help", "Help");
        en.put("cmd.execute", "Execute script");

        en.put("msg.confirm.clear", "Clear the ENTIRE collection?");
        en.put("msg.confirm.delete", "Delete object \"{0}\"?");
        en.put("msg.success.saved", "Saved successfully");
        en.put("msg.success.deleted", "Deleted successfully");
        en.put("msg.error.owner", "Only the owner can modify this object!");
        en.put("msg.error.network", "Network error: {0}");

        en.put("canvas.title", "Visualization");
        en.put("canvas.click.info", "Object information");
        en.put("canvas.no.data", "No objects to display");

        BUNDLES.put(EN_GB, Collections.unmodifiableMap(en));

        // === Russian ===
        Map<String, String> ru = new HashMap<>();
        ru.put("window.title", "Коллекция HumanBeing");
        ru.put("window.auth.title", "Авторизация");
        ru.put("window.main.title", "Главное окно");

        ru.put("auth.username", "Логин");
        ru.put("auth.password", "Пароль");
        ru.put("auth.login", "Войти");
        ru.put("auth.register", "Регистрация");
        ru.put("auth.welcome", "Добро пожаловать, {0}!");
        ru.put("auth.error.empty", "Логин и пароль не могут быть пустыми");
        ru.put("auth.error.failed", "Ошибка: {0}");
        ru.put("auth.connected", "Подключено к серверу");
        ru.put("auth.not.connected", "Нет подключения к серверу");

        ru.put("settings.language", "Язык:");

        ru.put("main.user.label", "Пользователь:");
        ru.put("main.menu.commands", "Команды");
        ru.put("main.menu.help", "Помощь");
        ru.put("main.toolbar.refresh", "Обновить");
        ru.put("main.toolbar.add", "Добавить");
        ru.put("main.toolbar.delete", "Удалить");
        ru.put("main.toolbar.logout", "Выйти");
        ru.put("main.search.placeholder", "Поиск...");

        ru.put("table.id", "ID");
        ru.put("table.name", "Имя");
        ru.put("table.coordinates", "Координаты");
        ru.put("table.creationDate", "Дата создания");
        ru.put("table.realHero", "Настоящий герой");
        ru.put("table.hasToothpick", "Есть зубочистка");
        ru.put("table.impactSpeed", "Скорость удара");
        ru.put("table.weaponType", "Тип оружия");
        ru.put("table.mood", "Настроение");
        ru.put("table.car", "Автомобиль");
        ru.put("table.owner", "Владелец");

        ru.put("table.filter", "Фильтр:");
        ru.put("table.sort.asc", "Сортировать по возрастанию");
        ru.put("table.sort.desc", "Сортировать по убыванию");
        ru.put("table.edit", "Редактировать");
        ru.put("table.remove", "Удалить");

        ru.put("dialog.add.title", "Добавление объекта");
        ru.put("dialog.add.header", "Введите данные нового объекта");
        ru.put("dialog.edit.title", "Редактирование объекта");
        ru.put("dialog.edit.header", "Изменение: {0}");
        ru.put("dialog.name", "Имя");
        ru.put("dialog.x", "Координата X");
        ru.put("dialog.y", "Координата Y (> -228)");
        ru.put("dialog.realHero", "Настоящий герой?");
        ru.put("dialog.hasToothpick", "Есть зубочистка?");
        ru.put("dialog.impactSpeed", "Скорость удара (> -428)");
        ru.put("dialog.weaponType", "Тип оружия");
        ru.put("dialog.mood", "Настроение");
        ru.put("dialog.hasCar", "Есть автомобиль?");
        ru.put("dialog.carName", "Марка автомобиля");
        ru.put("dialog.save", "Сохранить");
        ru.put("dialog.cancel", "Отмена");

        ru.put("error.validation.title", "Ошибка ввода");
        ru.put("error.validation.header", "Проверьте введённые данные");
        ru.put("error.validation.number", "Координаты и скорость должны быть числами!");
        ru.put("error.validation.name", "Имя не может быть пустым!");
        ru.put("error.validation.y", "Координата Y должна быть больше -228!");
        ru.put("error.validation.speed", "Скорость удара должна быть больше -428!");

        ru.put("cmd.show", "Показать все");
        ru.put("cmd.info", "Информация о коллекции");
        ru.put("cmd.clear", "Очистить коллекцию");
        ru.put("cmd.sort", "Сортировать коллекцию");
        ru.put("cmd.sum", "Сумма скорости удара");
        ru.put("cmd.print.mood", "Вывести по настроению (по убыванию)");
        ru.put("cmd.filter.name", "Фильтр по имени");
        ru.put("cmd.remove.index", "Удалить по индексу");
        ru.put("cmd.remove.id", "Удалить по ID");
        ru.put("cmd.insert", "Вставить по индексу");
        ru.put("cmd.add", "Добавить объект");
        ru.put("cmd.update", "Обновить объект");
        ru.put("cmd.help", "Справка");
        ru.put("cmd.execute", "Выполнить скрипт");

        ru.put("msg.confirm.clear", "Очистить ВСЮ коллекцию?");
        ru.put("msg.confirm.delete", "Удалить объект \"{0}\"?");
        ru.put("msg.success.saved", "Успешно сохранено");
        ru.put("msg.success.deleted", "Успешно удалено");
        ru.put("msg.error.owner", "Только владелец может изменять этот объект!");
        ru.put("msg.error.network", "Ошибка сети: {0}");

        ru.put("canvas.title", "Визуализация");
        ru.put("canvas.click.info", "Информация об объекте");
        ru.put("canvas.no.data", "Нет объектов для отображения");

        BUNDLES.put(RU, Collections.unmodifiableMap(ru));

        // === Norwegian ===
        Map<String, String> no = new HashMap<>();
        no.put("window.title", "HumanBeing-samling");
        no.put("window.auth.title", "Autorisasjon");
        no.put("window.main.title", "Hovedvindu");

        no.put("auth.username", "Brukernavn");
        no.put("auth.password", "Passord");
        no.put("auth.login", "Logg inn");
        no.put("auth.register", "Registrer");
        no.put("auth.welcome", "Velkommen, {0}!");
        no.put("auth.error.empty", "Brukernavn og passord kan ikke være tomme");
        no.put("auth.error.failed", "Feil: {0}");
        no.put("auth.connected", "Tilkoblet server");
        no.put("auth.not.connected", "Ikke tilkoblet server");

        no.put("settings.language", "Språk:");

        no.put("main.user.label", "Bruker:");
        no.put("main.menu.commands", "Kommandoer");
        no.put("main.menu.help", "Hjelp");
        no.put("main.toolbar.refresh", "Oppdater");
        no.put("main.toolbar.add", "Legg til");
        no.put("main.toolbar.delete", "Slett");
        no.put("main.toolbar.logout", "Logg ut");
        no.put("main.search.placeholder", "Søk...");

        no.put("table.id", "ID");
        no.put("table.name", "Navn");
        no.put("table.coordinates", "Koordinater");
        no.put("table.creationDate", "Opprettelsesdato");
        no.put("table.realHero", "Ekte helt");
        no.put("table.hasToothpick", "Har tannpirker");
        no.put("table.impactSpeed", "Slaghastighet");
        no.put("table.weaponType", "Våpentype");
        no.put("table.mood", "Humør");
        no.put("table.car", "Bil");
        no.put("table.owner", "Eier");

        no.put("table.filter", "Filter:");
        no.put("table.sort.asc", "Sorter stigende");
        no.put("table.sort.desc", "Sorter synkende");
        no.put("table.edit", "Rediger");
        no.put("table.remove", "Slett");

        no.put("dialog.add.title", "Legg til objekt");
        no.put("dialog.add.header", "Skriv inn data for nytt objekt");
        no.put("dialog.edit.title", "Rediger objekt");
        no.put("dialog.edit.header", "Redigerer: {0}");
        no.put("dialog.name", "Navn");
        no.put("dialog.x", "X-koordinat");
        no.put("dialog.y", "Y-koordinat (> -228)");
        no.put("dialog.realHero", "Ekte helt?");
        no.put("dialog.hasToothpick", "Har tannpirker?");
        no.put("dialog.impactSpeed", "Slaghastighet (> -428)");
        no.put("dialog.weaponType", "Våpentype");
        no.put("dialog.mood", "Humør");
        no.put("dialog.hasCar", "Har bil?");
        no.put("dialog.carName", "Bilmerke");
        no.put("dialog.save", "Lagre");
        no.put("dialog.cancel", "Avbryt");

        no.put("error.validation.title", "Valideringsfeil");
        no.put("error.validation.header", "Vennligst sjekk inntastet data");
        no.put("error.validation.number", "Koordinater og hastighet må være gyldige tall!");
        no.put("error.validation.name", "Navn kan ikke være tomt!");
        no.put("error.validation.y", "Y-koordinat må være større enn -228!");
        no.put("error.validation.speed", "Slaghastighet må være større enn -428!");

        no.put("cmd.show", "Vis alle");
        no.put("cmd.info", "Informasjon om samling");
        no.put("cmd.clear", "Tøm samling");
        no.put("cmd.sort", "Sorter samling");
        no.put("cmd.sum", "Sum av slaghastighet");
        no.put("cmd.print.mood", "Skriv ut etter humør (synkende)");
        no.put("cmd.filter.name", "Filter etter navn");
        no.put("cmd.remove.index", "Slett etter indeks");
        no.put("cmd.remove.id", "Slett etter ID");
        no.put("cmd.insert", "Sett inn ved indeks");
        no.put("cmd.add", "Legg til objekt");
        no.put("cmd.update", "Oppdater objekt");
        no.put("cmd.help", "Hjelp");
        no.put("cmd.execute", "Kjør skript");

        no.put("msg.confirm.clear", "Tøm HELE samlingen?");
        no.put("msg.confirm.delete", "Slett objekt \"{0}\"?");
        no.put("msg.success.saved", "Lagret vellykket");
        no.put("msg.success.deleted", "Slettet vellykket");
        no.put("msg.error.owner", "Bare eieren kan endre dette objektet!");
        no.put("msg.error.network", "Nettverksfeil: {0}");

        no.put("canvas.title", "Visualisering");
        no.put("canvas.click.info", "Objektinformasjon");
        no.put("canvas.no.data", "Ingen objekter å vise");

        BUNDLES.put(NO, Collections.unmodifiableMap(no));

        // === Lithuanian ===
        Map<String, String> lt = new HashMap<>();
        lt.put("window.title", "HumanBeing kolekcija");
        lt.put("window.auth.title", "Autorizacija");
        lt.put("window.main.title", "Pagrindinis langas");

        lt.put("auth.username", "Prisijungimo vardas");
        lt.put("auth.password", "Slaptažodis");
        lt.put("auth.login", "Prisijungti");
        lt.put("auth.register", "Registruotis");
        lt.put("auth.welcome", "Sveiki atvykę, {0}!");
        lt.put("auth.error.empty", "Prisijungimo vardas ir slaptažodis negali būti tušti");
        lt.put("auth.error.failed", "Klaida: {0}");
        lt.put("auth.connected", "Prisijungta prie serverio");
        lt.put("auth.not.connected", "Nėra ryšio su serveriu");

        lt.put("settings.language", "Kalba:");

        lt.put("main.user.label", "Naudotojas:");
        lt.put("main.menu.commands", "Komandos");
        lt.put("main.menu.help", "Pagalba");
        lt.put("main.toolbar.refresh", "Atnaujinti");
        lt.put("main.toolbar.add", "Pridėti");
        lt.put("main.toolbar.delete", "Ištrinti");
        lt.put("main.toolbar.logout", "Atsijungti");
        lt.put("main.search.placeholder", "Ieškoti...");

        lt.put("table.id", "ID");
        lt.put("table.name", "Pavadinimas");
        lt.put("table.coordinates", "Koordinatės");
        lt.put("table.creationDate", "Sukūrimo data");
        lt.put("table.realHero", "Tikras didvyris");
        lt.put("table.hasToothpick", "Turi krapštuką");
        lt.put("table.impactSpeed", "Smūgio greitis");
        lt.put("table.weaponType", "Ginklo tipas");
        lt.put("table.mood", "Nuotaika");
        lt.put("table.car", "Automobilis");
        lt.put("table.owner", "Savininkas");

        lt.put("table.filter", "Filtras:");
        lt.put("table.sort.asc", "Rūšiuoti didėjančia tvarka");
        lt.put("table.sort.desc", "Rūšiuoti mažėjančia tvarka");
        lt.put("table.edit", "Redaguoti");
        lt.put("table.remove", "Ištrinti");

        lt.put("dialog.add.title", "Pridėti objektą");
        lt.put("dialog.add.header", "Įveskite naujo objekto duomenis");
        lt.put("dialog.edit.title", "Redaguoti objektą");
        lt.put("dialog.edit.header", "Redaguojama: {0}");
        lt.put("dialog.name", "Pavadinimas");
        lt.put("dialog.x", "X koordinatė");
        lt.put("dialog.y", "Y koordinatė (> -228)");
        lt.put("dialog.realHero", "Tikras didvyris?");
        lt.put("dialog.hasToothpick", "Ar turi krapštuką?");
        lt.put("dialog.impactSpeed", "Smūgio greitis (> -428)");
        lt.put("dialog.weaponType", "Ginklo tipas");
        lt.put("dialog.mood", "Nuotaika");
        lt.put("dialog.hasCar", "Ar turi automobilį?");
        lt.put("dialog.carName", "Automobilio markė");
        lt.put("dialog.save", "Išsaugoti");
        lt.put("dialog.cancel", "Atšaukti");

        lt.put("error.validation.title", "Validacijos klaida");
        lt.put("error.validation.header", "Prašome patikrinti įvestus duomenis");
        lt.put("error.validation.number", "Koordinatės ir greitis turi būti tinkami skaičiai!");
        lt.put("error.validation.name", "Pavadinimas negali būti tuščias!");
        lt.put("error.validation.y", "Y koordinatė turi būti didesnė nei -228!");
        lt.put("error.validation.speed", "Smūgio greitis turi būti didesnis nei -428!");

        lt.put("cmd.show", "Rodyti visus");
        lt.put("cmd.info", "Informacija apie kolekciją");
        lt.put("cmd.clear", "Išvalyti kolekciją");
        lt.put("cmd.sort", "Rūšiuoti kolekciją");
        lt.put("cmd.sum", "Smūgio greičių suma");
        lt.put("cmd.print.mood", "Spausdinti pagal nuotaiką (mažėjančia)");
        lt.put("cmd.filter.name", "Filtras pagal pavadinimą");
        lt.put("cmd.remove.index", "Ištrinti pagal indeksą");
        lt.put("cmd.remove.id", "Ištrinti pagal ID");
        lt.put("cmd.insert", "Įterpti ties indeksu");
        lt.put("cmd.add", "Pridėti objektą");
        lt.put("cmd.update", "Atnaujinti objektą");
        lt.put("cmd.help", "Pagalba");
        lt.put("cmd.execute", "Vykdyti scenarijų");

        lt.put("msg.confirm.clear", "Išvalyti VISĄ kolekciją?");
        lt.put("msg.confirm.delete", "Ištrinti objektą \"{0}\"?");
        lt.put("msg.success.saved", "Išsaugota sėkmingai");
        lt.put("msg.success.deleted", "Ištrinta sėkmingai");
        lt.put("msg.error.owner", "Tik savininkas gali keisti šį objektą!");
        lt.put("msg.error.network", "Tinklo klaida: {0}");

        lt.put("canvas.title", "Vizualizacija");
        lt.put("canvas.click.info", "Objekto informacija");
        lt.put("canvas.no.data", "Nėra objektų rodymui");

        BUNDLES.put(LT, Collections.unmodifiableMap(lt));
    }

    // Добавьте в I18N.java
    public static String getRaw(String key) {
        Map<String, String> bundle = BUNDLES.getOrDefault(currentLocale, Collections.emptyMap());
        return bundle.getOrDefault(key, "???" + key + "???");
    }

    // ========================================================================
    // 🔹 Публичные методы
    // ========================================================================

    public static void setLocale(Locale locale) {
        // Авто-конвертация "en" → "en_GB"
        if (locale == null || Locale.ENGLISH.equals(locale)) {
            locale = EN_GB;
        }

        if (!SUPPORTED_LOCALES.contains(locale)) {
            System.err.println("[I18N] Unsupported locale: " + locale + ", falling back to RU");
            locale = RU;
        }

        currentLocale = locale;

        // Обновляем форматтеры
        numberFormat = NumberFormat.getNumberInstance(currentLocale);
        dateTimeFormatter = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(currentLocale);

        localeProperty.set(locale);
        System.out.println("[I18N] Locale changed to: " + currentLocale);
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static ObjectProperty<Locale> localeProperty() {
        return localeProperty;
    }

    /**
     * Получение строки по ключу с поддержкой аргументов.
     * Пример: I18N.get("auth.welcome", "Alice") → "Welcome, Alice!"
     */
    public static String get(String key, Object... args) {
        Map<String, String> bundle = BUNDLES.getOrDefault(currentLocale, Collections.emptyMap());
        String pattern = bundle.getOrDefault(key, "???" + key + "???");

        if (args.length > 0 && !pattern.startsWith("???")) {
            try {
                return MessageFormat.format(pattern, args);
            } catch (Exception e) {
                System.err.println("[I18N] Format error for key: " + key);
            }
        }
        return pattern;
    }

    /** Форматирование числа с учётом локали */
    public static String formatNumber(double value) {
        return numberFormat.format(value);
    }

    public static String formatNumber(long value) {
        return numberFormat.format(value);
    }

    /** Форматирование даты с учётом локали */
    public static String formatDate(java.time.ZonedDateTime zdt) {
        if (zdt == null) return "";
        return dateTimeFormatter.format(zdt);
    }

    public static List<Locale> getSupportedLocales() {
        return SUPPORTED_LOCALES;
    }

    /** Человеко-читаемое название локали для ComboBox */
    public static String getLocaleDisplayName(Locale locale) {
        return switch (locale.toString()) {
            case "ru" -> "Русский";
            case "no_NO" -> "Norsk";
            case "lt_LT" -> "Lietuvių";
            case "en_GB" -> "English (UK)";
            default -> locale.getDisplayName(locale);
        };
    }

    /** Удалить все файлы .properties — они больше не нужны! */
    public static void cleanupPropertyFiles() {
        System.out.println("[I18N] Property files are no longer needed — all translations are in code!");
    }
}