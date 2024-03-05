package org.yunusov.lifegameproject;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

public class Main extends Application {

    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static int CELL_SIZE = 10; // Размер клетки
    private static int WIDTH = 800; // Ширина окна
    private static int HEIGHT = 600; // Высота окна
    private static int ROWS = HEIGHT / CELL_SIZE; // Количество строк
    private static int COLS = WIDTH / CELL_SIZE; // Количество столбцов

    private boolean[][] grid = new boolean[ROWS][COLS]; // Игровое поле
    private AnimationTimer timer; // Таймер для обновления состояния игры

    @Override
    public void start(Stage primaryStage) {
        BooleanProperty showError = new SimpleBooleanProperty(false);

        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.setCenter(canvas);

        Label titleLabel = new Label("GAME OF LIFE");
        titleLabel.setStyle("-fx-font-family: Arial; -fx-font-size: 100px; -fx-font-weight: bold; -fx-text-fill: black;");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setPadding(new Insets(10));

        Button startButton = new Button("Start");
        startButton.setPrefSize(200, 80); // устанавливаем размер кнопки
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 36px;"); // устанавливаем цвет и стиль текста
        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-size: 36px;")); // изменяем цвет при наведении мыши
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 36px;")); // возвращаем исходный цвет при уходе мыши
        startButton.setOnAction(event -> startGame(canvas.getGraphicsContext2D()));

        Button stopButton = new Button("Stop");
        stopButton.setPrefSize(200, 80); // устанавливаем размер кнопки
        stopButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 36px;"); // устанавливаем цвет и стиль текста
        stopButton.setOnMouseEntered(e -> stopButton.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-size: 36px;")); // изменяем цвет при наведении мыши
        stopButton.setOnMouseExited(e -> stopButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 36px;")); // возвращаем исходный цвет при уходе мыши
        stopButton.setOnAction(event -> stopGame());

        HBox buttonsBox = new HBox(10, startButton, stopButton);
        buttonsBox.setAlignment(Pos.CENTER); // выравниваем кнопки по центру
        buttonsBox.setPadding(new Insets(20)); // добавляем отступы вокруг кнопок

        VBox settingsBox = new VBox(10);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setMinWidth(250);
        settingsBox.setPadding(new Insets(10));

        Label widthLabel = new Label("Width:");
        widthLabel.setStyle("-fx-font-size: 24px;"); // Устанавливаем размер шрифта
        TextField widthField = new TextField(Integer.toString(WIDTH));
        widthField.setStyle("-fx-font-size: 24px;"); // Устанавливаем размер шрифта
        widthField.setPrefWidth(100);

        widthField.setOnKeyTyped(e -> {
            String text = widthField.getText();
            try {
                if (!text.matches("\\d*")) {
                    widthField.setText(text.replaceAll("\\D", ""));
                }
                int width = Integer.parseInt(widthField.getText());
                if (width > SCREEN_WIDTH - 200) { // 200 - ширина кнопок и сеттингс
                    widthField.setText(Integer.toString(SCREEN_WIDTH - 200));
                } else {
                    showError.set(false);
                }
            } catch (RuntimeException exception) {
                showError.set(true);
            }
        });

        Label heightLabel = new Label("Height:");
        heightLabel.setStyle("-fx-font-size: 24px;"); // Устанавливаем размер шрифта
        TextField heightField = new TextField(Integer.toString(HEIGHT));
        heightField.setStyle("-fx-font-size: 24px;");
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

        Label cellSizeLabel = new Label("Cell Size:");
        cellSizeLabel.setStyle("-fx-font-size: 24px;"); // Устанавливаем размер шрифта
        TextField cellSizeField = new TextField(Integer.toString(CELL_SIZE));
        cellSizeField.setStyle("-fx-font-size: 24px;"); // Устанавливаем размер шрифта
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

        showError.addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                Label error = new Label("Некорректные значения");
                error.setStyle("-fx-font-family: Arial; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red;");
                error.setAlignment(Pos.CENTER);
                error.setPadding(new Insets(10));
                settingsBox.getChildren().add(error);
                root.setRight(settingsBox);
            } else {
                // Удаляем надпись об ошибке, если значения верны
                settingsBox.getChildren().removeIf(node -> node instanceof Label && ((Label) node).getText().equals("Некорректные значения"));
            }
        });

        Button setButton = new Button("Set");
        setButton.setStyle("-fx-background-color: blue; -fx-text-fill: white; -fx-font-size: 24px;"); // устанавливаем цвет и стиль текста
        setButton.setOnMouseEntered(e -> setButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-size: 24px;")); // изменяем цвет при наведении мыши
        setButton.setOnMouseExited(e -> setButton.setStyle("-fx-background-color: blue; -fx-text-fill: white; -fx-font-size: 24px;")); // возвращаем исходный цвет при уходе мыши
        setButton.setOnAction(event -> {
            WIDTH = Integer.parseInt(widthField.getText());
            HEIGHT = Integer.parseInt(heightField.getText());
            CELL_SIZE = Integer.parseInt(cellSizeField.getText());

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

        settingsBox.getChildren().addAll(widthLabel, widthField, heightLabel, heightField, cellSizeLabel, cellSizeField, setButton);

        root.setBottom(buttonsBox);
        root.setRight(settingsBox);
        root.setTop(titleLabel);

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Game of Life");
        primaryStage.show();

        // Заполнение игрового поля случайными значениями
        randomizeGrid();
    }

    private void randomizeGrid() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                grid[row][col] = Math.random() < 0.5; // 50% шанс быть живой клетке
            }
        }
    }

    private void startGame(GraphicsContext gc) {
        if (timer == null) {
            timer = new AnimationTimer() {
                private long lastUpdate = 0;

                @Override
                public void handle(long now) {
                    if (now - lastUpdate >= 100_000_000) { // Ограничение частоты обновления
                        update(); // Обновление состояния клеток
                        draw(gc); // Визуализация состояния клеток
                        lastUpdate = now;
                    }
                }
            };
            timer.start();
        }
    }

    private void stopGame() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void update() {
        boolean[][] nextGeneration = new boolean[ROWS][COLS];

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int neighbors = countNeighbors(row, col);
                if (grid[row][col]) {
                    nextGeneration[row][col] = (neighbors == 2 || neighbors == 3);
                } else {
                    nextGeneration[row][col] = (neighbors == 3);
                }
            }
        }

        grid = nextGeneration;
    }

    private int countNeighbors(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int r = (row + i + ROWS) % ROWS;
                int c = (col + j + COLS) % COLS;
                if (!(i == 0 && j == 0) && grid[r][c]) {
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
    }

    public static void main(String[] args) {
        launch(args);
    }
}
