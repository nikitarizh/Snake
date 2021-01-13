package com.nikitarizh.snake;

import java.util.LinkedList;

import com.nikitarizh.snake.entities.Snake;
import com.nikitarizh.snake.entities.Tile;

import javafx.scene.canvas.GraphicsContext;

public class Game {

    public static final int FIELD_WIDTH = 14;
    public static final int FIELD_HEIGHT = 14;
    public static double WIDTH_MULT = 0;
    public static double HEIGHT_MULT = 0;

    public static final Tile STARTING_POINT = new Tile(FIELD_WIDTH / 2, FIELD_HEIGHT / 2);
    // public static final Tile STARTING_POINT = new Tile(0, 0);

    public static volatile Snake snake;
    public static volatile Tile food;
    public static volatile LinkedList<Tile> drawingQueue;
    public static Drawer drawer;
    
    public static final int DRAWING_FREQ = 40;
    public static final int MOVING_FREQ = 30;
    public static final int THINKING_FREQ = 200;

    public static final int DRAWING_PRIORITY = 0;
    public static final int MOVING_PRIORITY = 1;
    public static final int THINKING_PRIORITY = 2;

    public Game(GraphicsContext context) {
        init(context);

        snake.startMoving();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SnakeAI brain = new SnakeAI();
                brain.startThinking();
            }
        }).start();
    }

    public void changeSnakeDirection(String keyCode) {
        if (snake.isDead()) {
            startNewGame();
        }

        if (keyCode.equals("W") || keyCode.equals("Up")) {
            snake.setTopDirection();
        }
        else if (keyCode.equals("A") || keyCode.equals("Left")) {
            snake.setLeftDirection();
        }
        else if (keyCode.equals("S") || keyCode.equals("Down")) {
            snake.setBottomDirection();
        }
        else if (keyCode.equals("D") || keyCode.equals("Right")) {
            snake.setRightDirection();
        }
    }

    public static void foodAte() {
        drawer.stopDrawingLoop();
        // snake.stopMoving();

        if (snake.body().size() == FIELD_WIDTH * FIELD_HEIGHT - 1) {
            snake.head().setX(food.getX());
            snake.head().setY(food.getY());
            win();
            return;
        }

        generateFood();

        // snake.startMoving();
        drawer.startDrawingLoop();
    }

    private void init(GraphicsContext context) {
        snake = new Snake();
        food = new Tile(-1, -1);
        generateFood();

        drawingQueue = new LinkedList<Tile>();

        drawer = new Drawer(context);
    }

    private static void win() {
        snake.stopMoving();
        drawer.stopDrawingLoop();
    }

    private static void startNewGame() {
        drawer.stopDrawingLoop();
        snake.stopMoving();

        synchronized (snake) {
            snake.getNewSnake();
        }
        snake.startMoving();
        drawer.startDrawingLoop();
    }

    private static void generateFood() {
        int maxI = 0, maxJ = 0;
        double maxChance = -1;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_HEIGHT; j++) {
                boolean isOccupied = false;

                if (snake.head().getX() != i || snake.head().getY() != j) {
                    for (Tile tile : snake.body()) {
                        if (tile.getX() == i && tile.getY() == j) {
                            isOccupied = true;
                            break;
                        }
                    }
                }
                else {
                    isOccupied = true;
                }

                if (!isOccupied) {
                    double chance = Math.random();
                    if (chance >= maxChance) {
                        maxChance = chance;
                        maxI = i;
                        maxJ = j;
                    }
                }
            }
        }

        synchronized (food) {
            food.setX(maxI);
            food.setY(maxJ);
        }
    }

    public static int correctWidth(int width) {
        if (width >= 0 && width < Game.FIELD_WIDTH) {
            return width;
        }

        if (width < 0) {
            width += Game.FIELD_WIDTH;
        }
        else {
            width -= Game.FIELD_WIDTH + 1;
        }

        return width;
    }

    public static int correctHeight(int height) {
        if (height >= 0 && height < Game.FIELD_HEIGHT) {
            return height;
        }

        if (height < 0) {
            height += Game.FIELD_HEIGHT;
        }
        else {
            height -= Game.FIELD_HEIGHT + 1;
        }

        return height;
    }
}
