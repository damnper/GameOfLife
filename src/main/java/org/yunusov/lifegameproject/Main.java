package org.yunusov.lifegameproject;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int CELL_SIZE = 5; // Размер клетки
    private static final int WIDTH = 1000; // Ширина окна
    private static final int HEIGHT = 800; // Высота окна
    private static final int ROWS = HEIGHT / CELL_SIZE; // Количество строк
    private static final int COLS = WIDTH / CELL_SIZE; // Количество столбцов

    private boolean[][] grid = new boolean[ROWS][COLS]; // Игровое поле

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.setCenter(canvas);
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Game of Life");
        primaryStage.show();

        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Заполнение игрового поля случайными значениями
        randomizeGrid();

        new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 100_000_000) { // Ограничение частоты обновления
                    update(); // Обновление состояния клеток
                    draw(gc); // Визуализация состояния клеток
                    lastUpdate = now;
                }
            }
        }.start();
    }

    private void randomizeGrid() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                grid[row][col] = Math.random() < 0.5; // 50% шанс быть живой клетке
            }
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
