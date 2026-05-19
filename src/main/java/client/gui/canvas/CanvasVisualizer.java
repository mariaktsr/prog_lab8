package client.gui.canvas;

import common.model.HumanBeing;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer;
import java.util.*;
import java.util.function.Consumer;

/**
 * Управляет отрисовкой объектов на Canvas и их анимацией появления.
 */
public class CanvasVisualizer {

    private final Canvas canvas;
    private final GraphicsContext gc;
    // Хранит прогресс анимации для каждого объекта (от 0.0 до 1.0)
    private final Map<HumanBeing, Double> animationProgress = new HashMap<>();
    private final Map<String, Color> userColors = new HashMap<>();
    private final Color[] palette = {
            Color.rgb(255, 99, 71), Color.rgb(60, 179, 113), Color.rgb(30, 144, 255),
            Color.rgb(255, 165, 0), Color.rgb(147, 112, 219), Color.rgb(255, 20, 147)
    };
    private int colorIdx = 0;

    // Обработчик клика по объекту (будет передан из MainController)
    private Consumer<HumanBeing> clickHandler;

    public CanvasVisualizer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    /**
     * Устанавливает обработчик клика по объекту на холсте.
     */
    public void setClickHandler(Consumer<HumanBeing> handler) {
        this.clickHandler = handler;
        canvas.setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));
    }

    /**
     * Запускает цикл анимации (вызывается один раз при инициализации).
     */
    public void startAnimationLoop() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateAnimations();
                draw();
            }
        }.start();
    }

    /**
     * Синхронизирует список объектов с текущим состоянием.
     * Новые объекты добавляются с анимацией (progress = 0).
     */
    public void syncData(List<HumanBeing> newHumans) {
        // Определяем новые объекты
        Set<HumanBeing> currentSet = new HashSet<>(animationProgress.keySet());
        for (HumanBeing h : newHumans) {
            if (!currentSet.contains(h)) {
                animationProgress.put(h, 0.0); // Начинаем анимацию появления
            }
        }
        // Удаляем объекты, которых больше нет в списке
        animationProgress.keySet().retainAll(newHumans);
    }

    // Обновляет прогресс анимации (плавное появление)
    private void updateAnimations() {
        Iterator<Map.Entry<HumanBeing, Double>> it = animationProgress.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<HumanBeing, Double> entry = it.next();
            double progress = entry.getValue();
            if (progress < 1.0) {
                entry.setValue(Math.min(progress + 0.05, 1.0)); // Скорость анимации
            }
        }
    }

    // Основная отрисовка
    private void draw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        double scale = 10.0; // Масштаб координат

        for (Map.Entry<HumanBeing, Double> entry : animationProgress.entrySet()) {
            HumanBeing h = entry.getKey();
            double progress = entry.getValue();

            if (h.getCoordinates() == null) continue;

            double x = h.getCoordinates().getX();
            double y = h.getCoordinates().getY();

            // Преобразование координат модели в координаты Canvas
            double canvasX = centerX + x * scale;
            double canvasY = centerY - y * scale; // Y инвертирован

            // Параметры анимации: радиус и прозрачность зависят от progress
            double currentRadius = 15 * progress;
            double opacity = progress;

            gc.setGlobalAlpha(opacity);
            gc.setFill(getColorForUser(h.getOwner().getLogin()));

            // Рисуем круг (примитив)
            gc.fillOval(canvasX - currentRadius, canvasY - currentRadius,
                    currentRadius * 2, currentRadius * 2);

            // Обводка
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeOval(canvasX - currentRadius, canvasY - currentRadius,
                    currentRadius * 2, currentRadius * 2);

            gc.setGlobalAlpha(1.0); // Сброс прозрачности
        }
    }

    // Получение цвета для пользователя (кеширование)
    private Color getColorForUser(String login) {
        return userColors.computeIfAbsent(login != null ? login : "unknown",
                k -> palette[colorIdx++ % palette.length]);
    }

    // Обработка клика мыши
    private void handleClick(double mx, double my) {
        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        double scale = 10.0;

        // Ищем объект, в который попал клик
        for (HumanBeing h : animationProgress.keySet()) {
            if (h.getCoordinates() == null) continue;
            double x = h.getCoordinates().getX();
            double y = h.getCoordinates().getY();
            double canvasX = centerX + x * scale;
            double canvasY = centerY - y * scale;

            // Расстояние от клика до центра объекта
            double dist = Math.sqrt(Math.pow(mx - canvasX, 2) + Math.pow(my - canvasY, 2));

            // Если попали в радиус объекта (15 пикселей)
            if (dist < 18) {
                if (clickHandler != null) clickHandler.accept(h);
                return;
            }
        }
    }
}