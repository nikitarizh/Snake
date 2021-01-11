package com.nikitarizh.snake;

import java.util.Timer;
import java.util.TimerTask;

import com.nikitarizh.snake.entities.Tile;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Drawer {

    private GraphicsContext context;

    private Timer drawingLoop;

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
        drawingLoop = new Timer();
        drawingLoop.schedule(new TimerTask() {
            @Override
            public void run() {
                drawBackground();

                drawSnake();

                drawFood();
            }
        }, 0, 1000 / Game.DRAWING_FREQ);
    }

    public void stopDrawingLoop() {
        drawingLoop.cancel();
    }

    private void drawBackground() {
        fillRect(0, 0, Game.FIELD_WIDTH, Game.FIELD_HEIGHT, BACKGROUND_COLOR);
    }

    private void drawSnake() {
        fillRect(Game.snake.head().getX(), Game.snake.head().getY(), TILE_SIZE, TILE_SIZE, SNAKE_HEAD_COLOR);
        if (Game.snake.isDead()) {
            for (Tile tile : Game.snake.body()) {
                synchronized(tile) {
                    fillRect(tile.getX(), tile.getY(), TILE_SIZE, TILE_SIZE, SNAKE_DYING_BODY_COLOR);
                }
            }
        }
        else {
            for (Tile tile : Game.snake.body()) {
                synchronized(tile) {
                    fillRect(tile.getX(), tile.getY(), TILE_SIZE, TILE_SIZE, SNAKE_BODY_COLOR);
                }
            }
        }
    }

    private void drawFood() {
        if (Game.food != null) {
            fillRect(Game.food.getX(), Game.food.getY(), TILE_SIZE, TILE_SIZE, FOOD_COLOR);
        }
    }

    private void fillRect(double x, double y, double w, double h, Color color) {
        context.setFill(color);
        context.fillRect(x * Game.WIDTH_MULT, y * Game.HEIGHT_MULT, w * Game.WIDTH_MULT, h * Game.HEIGHT_MULT);
    }
}
