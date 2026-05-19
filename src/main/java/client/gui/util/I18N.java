package client.gui.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18N {

    private static final String BUNDLE_NAME = "client.gui.resources.messages";
    private static Locale currentLocale = Locale.ENGLISH;
    private static ResourceBundle resourceBundle;
    private static final ObjectProperty<Locale> localeProperty = new SimpleObjectProperty<>();

    static {
        setLocale(currentLocale);
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
        localeProperty.set(locale);
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static ObjectProperty<Locale> localeProperty() {
        return localeProperty;
    }

    // Метод без параметров (существующий)
    public static String get(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            System.err.println("[I18N] Missing key: " + key + " in locale: " + currentLocale);
            return "???" + key + "???";
        }
    }

    // === ДОБАВЬТЕ ЭТОТ МЕТОД для форматирования ===
    public static String get(String key, Object... args) {
        try {
            String pattern = resourceBundle.getString(key);
            return MessageFormat.format(pattern, args);
        } catch (Exception e) {
            System.err.println("[I18N] Missing key: " + key + " in locale: " + currentLocale);
            return "???" + key + "???";
        }
    }

    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static List<Locale> getSupportedLocales() {
        return Arrays.asList(
                Locale.ENGLISH,
                new Locale("ru"),
                new Locale("be"),
                new Locale("lt"),
                new Locale("es", "GT")
        );
    }
}