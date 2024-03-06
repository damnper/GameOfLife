package org.yunusov.lifegameproject;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.Objects;

public class Main extends Application {

    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static int CELL_SIZE = 10; // Размер клетки
    private static int WIDTH = 800; // Ширина окна
    private static int HEIGHT = 600; // Высота окна
    private static int ROWS = HEIGHT / CELL_SIZE; // Количество строк
    private static int COLS = WIDTH / CELL_SIZE; // Количество столбцов

    private boolean[][] grid = new boolean[ROWS][COLS]; // Игровое поле
    private boolean[][] futureGrid = new boolean[ROWS][COLS];
    private boolean[][] deadGrid = new boolean[ROWS][COLS];

    private AnimationTimer timer; // Таймер для обновления состояния игры
    private long startTime;
    private long pauseStartTime = 0;
    Button startButton;
    Button stopButton;
    Button resumeButton;

    @Override
    public void start(Stage primaryStage) {
        BooleanProperty showError = new SimpleBooleanProperty(false);

        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.setCenter(canvas);

        Label timerLabel = createLabel("Timer: 0 seconds", "timer-label", "timer-label");

        Label titleLabel = createLabel("GAME OF LIFE", "title-label", "title-label");

        Label infoBlackColorLabel = createLabel("Black cells - still alive cells", "info-cell-black-label", "info-cell-black-label");
        Label infoGrayColorLabel = createLabel("Grey cells - cells will die for next step", "info-cell-gray-label", "info-cell-gray-label");
        Label infoRedColorLabel = createLabel("Red cells - cells will born for next step", "info-cell-red-label", "info-cell-red-label");

        startButton = createButton("Start",
                "start-button",
                "start-button:hover",
                event -> startGame(startButton, timerLabel, canvas.getGraphicsContext2D()));
        stopButton = createButton("Stop",
                "stop-button",
                "stop-button:hover",
                event -> stopGame(stopButton));
        resumeButton = createButton("Resume",
                "resume-button",
                "resume-button:hover",
                event -> resumeGame(resumeButton));

        var buttonsBox = getButtonsBox(startButton, stopButton, resumeButton);

        var settingsBox = getSettingsBox();

        var widthSetting = getWidthSetting(showError);

        var heightSetting = getHeightSetting(showError);

        var cellSizeSetting = getCellSizeSetting(showError);

        showErrorListener(showError, settingsBox, root);

        var setButton = getSetButton(widthSetting, heightSetting, cellSizeSetting, canvas);

        settingsBox.getChildren().addAll(infoBlackColorLabel, infoGrayColorLabel, infoRedColorLabel, timerLabel, widthSetting.widthLabel(), widthSetting.widthField(), heightSetting.heightLabel(), heightSetting.heightField(), cellSizeSetting.cellSizeLabel(), cellSizeSetting.cellSizeField(), setButton);

        setRoot(root, buttonsBox, settingsBox, titleLabel);

        var scene = getScene(root);

        serPrimaryStage(primaryStage, scene);

        // Заполнение игрового поля случайными значениями
        randomizeGrid();
    }

    private static VBox getSettingsBox() {
        VBox settingsBox = new VBox(10);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setMinWidth(250);
        settingsBox.setPadding(new Insets(10));
        return settingsBox;
    }

    private static HBox getButtonsBox(Button startButton, Button stopButton, Button resumeButton) {
        HBox buttonsBox = new HBox(10, startButton, stopButton, resumeButton);
        buttonsBox.setAlignment(Pos.CENTER); // выравниваем кнопки по центру
        buttonsBox.setPadding(new Insets(20)); // добавляем отступы вокруг кнопок
        return buttonsBox;
    }

    private static void serPrimaryStage(Stage primaryStage, Scene scene) {
        primaryStage.setScene(scene);
        primaryStage.setTitle("Game of Life");
        primaryStage.show();
    }

    private Scene getScene(BorderPane root) {
        Scene scene = new Scene(root);
        // Загрузка файла CSS из пути внутри ресурсов
        String cssPath = Objects.requireNonNullElseGet(getClass().getResource("styles.css"), () -> {
            throw new RuntimeException("Resource not found: styles.css");
        }).toExternalForm();
        scene.getStylesheets().add(cssPath);
        return scene;
    }

    private static void setRoot(BorderPane root, HBox buttonsBox, VBox settingsBox, Label titleLabel) {
        root.setBottom(buttonsBox);
        root.setRight(settingsBox);
        root.setTop(titleLabel);
    }

    private Button getSetButton(WidthSetting widthSetting, HeightSetting heightSetting, CellSizeSetting cellSizeSetting, Canvas canvas) {
        Button setButton = new Button("Set");
        setButton.getStyleClass().add("set-button"); // устанавливаем цвет и стиль текста
        setButton.setOnMouseEntered(e -> setButton.getStyleClass().add("set-button:hover")); // изменяем цвет при наведении мыши
        setButton.setOnAction(event -> {
            addStyleOnClick(setButton, "set-success");

            WIDTH = Integer.parseInt(widthSetting.widthField().getText());
            HEIGHT = Integer.parseInt(heightSetting.heightField().getText());
            CELL_SIZE = Integer.parseInt(cellSizeSetting.cellSizeField().getText());

            // Устанавливаем новые размеры для холста
            canvas.setWidth(WIDTH);
            canvas.setHeight(HEIGHT);

            // Обновляем количество строк и столбцов
            ROWS = HEIGHT / CELL_SIZE;
            COLS = WIDTH / CELL_SIZE;

            // Перерисовываем игровое поле с новыми параметрами
            grid = new boolean[ROWS][COLS];
            randomizeGrid();
        });
        return setButton;
    }

    private static void addStyleOnClick(Button setButton, String cssTitle) {
        // Добавление класса для изменения стиля
        setButton.getStyleClass().add(cssTitle);

        // Установка паузы для временного применения стиля
        PauseTransition pause = new PauseTransition(Duration.seconds(0.1));
        pause.setOnFinished(e -> {
            // Удаление класса после окончания паузы
            setButton.getStyleClass().remove(cssTitle);
        });
        pause.play(); // Запуск паузы
    }

    private static void showErrorListener(BooleanProperty showError, VBox settingsBox, BorderPane root) {
        showError.addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                Label error = new Label("Invalid settings");
                error.setStyle("-fx-font-family: Arial; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red;");
                error.setAlignment(Pos.CENTER);
                error.setPadding(new Insets(10));
                settingsBox.getChildren().add(error);
                root.setRight(settingsBox);
            } else {
                // Удаляем надпись об ошибке, если значения верны
                settingsBox.getChildren().removeIf(node -> node instanceof Label && ((Label) node).getText().equals("Invalid settings"));
            }
        });
    }

    private static CellSizeSetting getCellSizeSetting(BooleanProperty showError) {
        Label cellSizeLabel = new Label("Cell Size:");
        cellSizeLabel.getStyleClass().add("cell-label"); // Устанавливаем размер шрифта
        cellSizeLabel.setId("cell-label");
        TextField cellSizeField = new TextField(Integer.toString(CELL_SIZE));
        cellSizeField.getStyleClass().add("cell-label"); // Устанавливаем размер шрифта
        cellSizeField.setId("cell-label");
        cellSizeField.setPrefWidth(100);
        cellSizeField.setOnKeyTyped(e -> {
            String text = cellSizeField.getText();
            try {
                if (!text.matches("\\d*")) {
                    cellSizeField.setText(text.replaceAll("\\D", ""));
                }
                int cellSize = Integer.parseInt(cellSizeField.getText());
                if (cellSize < 1) {
                    cellSizeField.setText("1");
                } else if (cellSize > 15) {
                    cellSizeField.setText("15");
                } else {
                    showError.set(false);
                }
            } catch (RuntimeException exception) {
                showError.set(true);
            }
        });
        return new CellSizeSetting(cellSizeLabel, cellSizeField);
    }

    private record CellSizeSetting(Label cellSizeLabel, TextField cellSizeField) {
    }

    private static HeightSetting getHeightSetting(BooleanProperty showError) {
        Label heightLabel = new Label("Height:");
        heightLabel.getStyleClass().add("height-label"); // Устанавливаем размер шрифта
        heightLabel.setId("height-label");
        TextField heightField = new TextField(Integer.toString(HEIGHT));
        heightField.getStyleClass().add("height-label");
        heightField.setId("height-label");
        heightField.setPrefWidth(100);
        heightField.setOnKeyTyped(e -> {
            String text = heightField.getText();
            try {
                if (!text.matches("\\d*")) {
                    heightField.setText(text.replaceAll("\\D", ""));
                }
                int height = Integer.parseInt(heightField.getText());
                if (height > SCREEN_HEIGHT - 300) { // 200 - ширина кнопок и сеттингс
                    heightField.setText(Integer.toString(SCREEN_HEIGHT - 300));
                } else {
                    showError.set(false);
                }
            } catch (RuntimeException exception) {
                showError.set(true);
            }
        });
        return new HeightSetting(heightLabel, heightField);
    }

    private record HeightSetting(Label heightLabel, TextField heightField) {
    }

    private static WidthSetting getWidthSetting(BooleanProperty showError) {
        Label widthLabel = new Label("Width:");
        widthLabel.getStyleClass().add("width-label"); // Устанавливаем размер шрифта
        widthLabel.setId("width-label");
        TextField widthField = new TextField(Integer.toString(WIDTH));
        widthField.getStyleClass().add("width-label"); // Устанавливаем размер шрифта
        widthField.setPrefWidth(100);
        widthField.setId("width-label");

        widthField.setOnKeyTyped(e -> {
            String text = widthField.getText();
            try {
                if (!text.matches("\\d*")) {
                    widthField.setText(text.replaceAll("\\D", ""));
                }
                int width = Integer.parseInt(widthField.getText());
                if (width > SCREEN_WIDTH - 420) { // 200 - ширина кнопок и сеттингс
                    widthField.setText(Integer.toString(SCREEN_WIDTH - 420));
                } else {
                    showError.set(false);
                }
            } catch (RuntimeException exception) {
                showError.set(true);
            }
        });
        return new WidthSetting(widthLabel, widthField);
    }

    private record WidthSetting(Label widthLabel, TextField widthField) {
    }

    private Button createButton(String text, String cssBaseStyle, String cssOnMouseEntered, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.setPrefSize(200, 80);
        button.getStyleClass().add(cssBaseStyle);
        button.setOnAction(action);
        button.setOnMouseEntered(e -> button.setStyle(cssOnMouseEntered));
        return button;
    }

    private Label createLabel(String title, String style, String id) {
        Label label = new Label(title);
        label.getStyleClass().add(style);
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(10));
        label.setId(id); // Установка идентификатора
        return label;
    }

    private void randomizeGrid() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                grid[row][col] = Math.random() < 0.5; // 50% шанс быть живой клетке
            }
        }
    }

    public void startGame(Button startButton, Label timerLabel, GraphicsContext gc) {
        addStyleOnClick(startButton, "start-success");
        randomizeGrid();
        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000) { // Ограничение частоты обновления
                    update(); // Обновление состояния клеток
                    draw(gc); // Визуализация состояния клеток
                    lastUpdate = now;
                }
                long elapsedTime = (now - startTime) / 1_000_000_000; // Convert nanoseconds to seconds
                timerLabel.setText("Timer: " + elapsedTime + " seconds");

            }
        };
        startTime = System.nanoTime();
        timer.start();
    }

    private void resumeGame(Button resumeButton) {
        if (timer != null) {
            addStyleOnClick(resumeButton, "resume-success");
            startTime += System.nanoTime() - pauseStartTime;
            timer.start();
        }
    }

    private void stopGame(Button stopButton) {
        if (timer != null) {
            addStyleOnClick(stopButton, "stop-success");
            timer.stop();
            pauseStartTime = System.nanoTime();
        }
    }

    private void update() {
        boolean[][] nextGeneration = new boolean[ROWS][COLS];
        boolean[][] secondNextGeneration = new boolean[ROWS][COLS];
        boolean[][] deadGeneration = new boolean[ROWS][COLS];

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int neighbors = countNeighbors(row, col, grid);
                if (grid[row][col]) {
                    nextGeneration[row][col] = (neighbors == 2 || neighbors == 3);
                } else {
                    nextGeneration[row][col] = (neighbors == 3);
                }
            }
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int neighbors = countNeighbors(row, col, nextGeneration);
                if (nextGeneration[row][col]) {
                    secondNextGeneration[row][col] = (neighbors == 2 || neighbors == 3);
                } else {
                    secondNextGeneration[row][col] = (neighbors == 3);
                }
            }
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int neighbors = countNeighbors(row, col, nextGeneration);
                if (nextGeneration[row][col]) {
                    deadGeneration[row][col] = (neighbors < 2 || neighbors > 3);
                }
            }
        }

        grid = nextGeneration;
        futureGrid = secondNextGeneration;
        deadGrid = deadGeneration;
        validateFutureFromGrid();
    }

    private void validateFutureFromGrid() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (grid[row][col] == futureGrid[row][col]) {
                    futureGrid[row][col] = false;
                }
            }
        }
    }

    private int countNeighbors(int row, int col, boolean[][] gridType) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int r = (row + i + ROWS) % ROWS;
                int c = (col + j + COLS) % COLS;
                if (!(i == 0 && j == 0) && gridType[r][c]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void draw(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        gc.setFill(Color.BLACK);
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (grid[row][col]) {
                    gc.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        gc.setStroke(Color.RED);
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (futureGrid[row][col]) {
                    gc.strokeRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        gc.setFill(Color.GREY);
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (deadGrid[row][col]) {
                    gc.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
