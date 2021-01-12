package com.nikitarizh.snake;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

import com.nikitarizh.snake.entities.Tile;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Drawer {

    private GraphicsContext context;

    private ScheduledExecutorService drawingLoop;

    private final int TILE_SIZE = 1;

    private final Color SNAKE_HEAD_COLOR = Color.GREEN;
    private final Color SNAKE_BODY_COLOR = Color.YELLOW;
    private final Color SNAKE_DYING_BODY_COLOR = Color.ORANGE;
    private final Color FOOD_COLOR = Color.RED;
    private final Color BACKGROUND_COLOR = Color.BLACK;

    public Drawer(GraphicsContext context) {
        this.context = context;

        startDrawingLoop();
    }

    public void startDrawingLoop() {

        Thread drawingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                drawBackground();

                try {
                    drawSnake();
                }
                catch (Exception e) {
                    System.out.println("Exception in drawing snake: " + e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
    
                try {
                    drawFood();
                }
                catch (Exception e) {
                    System.out.println("Exception in drawing food: " + e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });
        drawingThread.setPriority(Thread.MIN_PRIORITY + Game.DRAWING_PRIORITY);

        drawingLoop = Executors.newSingleThreadScheduledExecutor();
        drawingLoop.scheduleAtFixedRate(() -> {
            try {
                drawingThread.run();
            }
            catch (Exception e) {
                System.out.println("Exception in drawing thread: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }, 0, 1000 / Game.DRAWING_FREQ, TimeUnit.MILLISECONDS);
    }

    public void stopDrawingLoop() {
        drawingLoop.shutdown();
    }

    private void drawBackground() {
        fillRect(0, 0, Game.FIELD_WIDTH, Game.FIELD_HEIGHT, BACKGROUND_COLOR);
    }

    private void drawSnake() {
        synchronized (Game.snake.head()) {
            fillRoundRect(Game.snake.head().getX(), Game.snake.head().getY(), TILE_SIZE, TILE_SIZE, SNAKE_HEAD_COLOR);
        }

        if (Game.snake.isDead()) {
            synchronized (Game.snake.body()) {
                for (Tile tile : Game.snake.body()) {
                    fillRoundRect(tile.getX(), tile.getY(), TILE_SIZE, TILE_SIZE, SNAKE_DYING_BODY_COLOR);
                }
            }
        }
        else {
            synchronized (Game.snake.body()) {
                for (Tile tile : Game.snake.body()) {
                    fillRoundRect(tile.getX(), tile.getY(), TILE_SIZE, TILE_SIZE, SNAKE_BODY_COLOR);
                }
            }
        }
    }

    private void drawFood() {
        synchronized (Game.food) {
            fillRoundRect(Game.food.getX(), Game.food.getY(), TILE_SIZE, TILE_SIZE, FOOD_COLOR);
        }
    }

    private void fillRect(double x, double y, double w, double h, Color color) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                context.setFill(color);
                context.fillRect(x * Game.WIDTH_MULT, y * Game.HEIGHT_MULT, w * Game.WIDTH_MULT, h * Game.HEIGHT_MULT);
            }
        });   
    }

    private void fillRoundRect(double x, double y, double w, double h, Color color) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                context.setFill(color);
                context.fillRoundRect(x * Game.WIDTH_MULT, y * Game.HEIGHT_MULT, w * Game.WIDTH_MULT, h * Game.HEIGHT_MULT, w * Game.WIDTH_MULT / 2,  h * Game.HEIGHT_MULT / 2);
            }
        });  
    }
}
