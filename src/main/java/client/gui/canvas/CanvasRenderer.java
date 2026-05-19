package client.gui.canvas;

import common.model.HumanBeing;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CanvasRenderer {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Map<HumanBeing, Double> animationProgress = new HashMap<>();
    private final Map<String, Color> userColors = new HashMap<>();

    // Палитра цветов для пользователей
    private static final Color[] PALETTE = {
            Color.rgb(255, 99, 71), Color.rgb(60, 179, 113), Color.rgb(30, 144, 255),
            Color.rgb(255, 165, 0), Color.rgb(147, 112, 219), Color.rgb(255, 20, 147),
            Color.rgb(0, 191, 255), Color.rgb(50, 205, 50)
    };
    private int colorIndex = 0;
    private Consumer<HumanBeing> clickHandler;

    // ⚙️ НАСТРОЙКИ МАСШТАБА
    private static final double SCALE = 4.0;       // 1 единица координат = 4 пикселя
    private static final double BASE_RADIUS = 12.0; // Базовый радиус круга

    public CanvasRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        bindCanvasSize();
    }

    private void bindCanvasSize() {
        if (canvas.getParent() instanceof AnchorPane pane) {
            canvas.widthProperty().bind(pane.widthProperty());
            canvas.heightProperty().bind(pane.heightProperty());
        }
    }

    public void setClickHandler(Consumer<HumanBeing> handler) {
        this.clickHandler = handler;
        canvas.setOnMouseClicked(e -> handleClick(e.getX(), e.getY()));
    }

    public void startRenderingLoop() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateAnimations();
                draw();
            }
        }.start();
    }

    public void syncData(List<HumanBeing> humans) {
        if (humans == null) return;

        for (HumanBeing h : humans) {
            if (!animationProgress.containsKey(h)) {
                animationProgress.put(h, 0.0);
                System.out.println("[CANVAS] Добавлен объект: " + h.getName() + " (" + h.getCoordinates() + ")");
            }
        }
        animationProgress.keySet().removeIf(h -> !humans.contains(h));
    }

    private void updateAnimations() {
        for (Map.Entry<HumanBeing, Double> entry : animationProgress.entrySet()) {
            double progress = entry.getValue();
            if (progress < 1.0) {
                entry.setValue(Math.min(progress + 0.08, 1.0));
            }
        }
    }

    private void draw() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w == 0 || h == 0) return;

        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.rgb(245, 245, 250)); // Лёгкий фон
        gc.fillRect(0, 0, w, h);

        double centerX = w / 2;
        double centerY = h / 2;

        // 📏 Рисуем сетку
        drawGrid(w, h, centerX, centerY);

        //  Рисуем оси
        drawAxes(w, h, centerX, centerY);

        //  Рисуем объекты
        drawObjects(centerX, centerY);
    }

    private void drawGrid(double w, double h, double centerX, double centerY) {
        gc.setStroke(Color.rgb(220, 220, 230));
        gc.setLineWidth(1);

        // Вертикальные линии сетки
        for (double x = centerX; x < w; x += SCALE * 5) {
            gc.strokeLine(x, 0, x, h);
            gc.strokeLine(centerX - (x - centerX), 0, centerX - (x - centerX), h);
        }

        // Горизонтальные линии сетки
        for (double y = centerY; y < h; y += SCALE * 5) {
            gc.strokeLine(0, y, w, y);
            gc.strokeLine(0, centerY - (y - centerY), w, centerY - (y - centerY));
        }
    }

    private void drawAxes(double w, double h, double centerX, double centerY) {
        // Ось X (горизонтальная)
        gc.setStroke(Color.rgb(80, 80, 100));
        gc.setLineWidth(2);
        gc.strokeLine(0, centerY, w, centerY);

        // Стрелка оси X
        gc.setFill(Color.rgb(80, 80, 100));
        gc.fillPolygon(
                new double[]{w - 10, w - 20, w - 20},
                new double[]{centerY, centerY - 6, centerY + 6},
                3
        );

        // Подпись оси X
        gc.setFill(Color.rgb(60, 60, 80));
        gc.setFont(Font.font("System Bold", 12));
        gc.fillText("X", w - 25, centerY - 10);

        // Ось Y (вертикальная)
        gc.setStroke(Color.rgb(80, 80, 100));
        gc.setLineWidth(2);
        gc.strokeLine(centerX, 0, centerX, h);

        // Стрелка оси Y
        gc.setFill(Color.rgb(80, 80, 100));
        gc.fillPolygon(
                new double[]{centerX, centerX - 6, centerX + 6},
                new double[]{10, 20, 20},
                3
        );

        // Подпись оси Y
        gc.fillText("Y", centerX + 10, 25);

        // Начало координат
        gc.setFill(Color.rgb(60, 60, 80));
        gc.setFont(Font.font("System", 10));
        gc.fillText("(0,0)", centerX + 5, centerY + 15);
    }

    private void drawObjects(double centerX, double centerY) {
        for (Map.Entry<HumanBeing, Double> entry : animationProgress.entrySet()) {
            HumanBeing human = entry.getKey();
            double progress = entry.getValue();

            if (human.getCoordinates() == null) continue;

            double x = human.getCoordinates().getX();
            double y = human.getCoordinates().getY();

            // Преобразование координат в пиксели
            double pixelX = centerX + x * SCALE;
            double pixelY = centerY - y * SCALE; // Y инвертирован

            double currentRadius = BASE_RADIUS * progress;
            double opacity = progress;

            gc.setGlobalAlpha(opacity);
            Color userColor = getUserColor(human.getOwner().getLogin());
            gc.setFill(userColor);

            gc.fillOval(pixelX - currentRadius, pixelY - currentRadius,
                    currentRadius * 2, currentRadius * 2);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(pixelX - currentRadius, pixelY - currentRadius,
                    currentRadius * 2, currentRadius * 2);

            // 🏷️ Подпись (ID или имя)
            gc.setGlobalAlpha(1.0);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("System Bold", 10));
            String label = human.getName().length() > 6 ?
                    human.getName().substring(0, 5) + "." : human.getName();
            gc.fillText(label, pixelX - gc.getFont().getSize() * 2, pixelY + 4);
        }
    }

    private Color getUserColor(String login) {
        return userColors.computeIfAbsent(login != null ? login : "unknown", k -> {
            Color c = PALETTE[colorIndex % PALETTE.length];
            colorIndex++;
            return c;
        });
    }

    private void handleClick(double mx, double my) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double centerX = w / 2;
        double centerY = h / 2;

        for (HumanBeing hObj : animationProgress.keySet()) {
            if (hObj.getCoordinates() == null) continue;

            double x = hObj.getCoordinates().getX();
            double y = hObj.getCoordinates().getY();
            double pixelX = centerX + x * SCALE;
            double pixelY = centerY - y * SCALE;

            double dist = Math.hypot(mx - pixelX, my - pixelY);
            if (dist <= BASE_RADIUS + 3) {
                if (clickHandler != null) clickHandler.accept(hObj);
                return;
            }
        }
    }
}