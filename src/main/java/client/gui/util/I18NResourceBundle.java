package client.gui.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Адаптер, который превращает встроенные переводы I18N в ResourceBundle.
 * Позволяет использовать синтаксис %key в FXML файлах.
 */
public class I18NResourceBundle extends ResourceBundle {

    @Override
    protected Object handleGetObject(String key) {
        // FXML вызывает этот метод для получения текста по ключу
        return I18N.getRaw(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        // Возвращаем все ключи текущей локали
        return Collections.enumeration(
                I18N.BUNDLES.getOrDefault(I18N.getLocale(), Collections.emptyMap()).keySet()
        );
    }
}