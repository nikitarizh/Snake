package com.nikitarizh.snake.entities;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.nikitarizh.snake.controller.Game;

public class Snake {

    private final int DEFAULT_BODY_SIZE = 4;

    private volatile Tile head;
    private volatile ArrayList<Tile> body;
    
    private volatile int xSpeed;
    private volatile int ySpeed;

    private ScheduledExecutorService movingLoop;

    private boolean isDead = false;

    public Snake() {
        head = new Tile(-1, -1);
        body = new ArrayList<Tile>();

        getNewSnake();
    }

    public Tile head() {
        return head;
    }

    public ArrayList<Tile> body() {
        return body;
    }

    public boolean isDead() {
        return this.isDead;
    }

    
    
    public void getNewSnake() {
        setDefaultHead();
        setDefaultBody();

        xSpeed = 0;
        ySpeed = 0;

        resurrect();
    }
    
    public void startMoving() {
        Thread movingThread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    move();
                }
                catch (Exception e) {
                    System.out.println("Exception in moving: " + e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
                
            }
        });
        movingThread.setPriority(Thread.MIN_PRIORITY + Game.MOVING_PRIORITY);

        movingLoop = Executors.newSingleThreadScheduledExecutor();
        movingLoop.scheduleAtFixedRate(() -> {
            try {
                movingThread.run();
            }
            catch (Exception e) {
                System.out.println("Exception in moving thread: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
            // lol
            try {
                maybeDie();
                maybeEat();
            }
            catch (Exception e) {
                System.out.println("Exception in maybe dying or eating: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }, 0, 1000 / Game.MOVING_FREQ, TimeUnit.MILLISECONDS);
    }

    public void stopMoving() {
        movingLoop.shutdown();
    }

    public void setTopDirection() {
        xSpeed = 0;
        ySpeed = -1;
    }

    public void setLeftDirection() {
        xSpeed = -1;
        ySpeed = 0;
    }

    public void setBottomDirection() {
        xSpeed = 0;
        ySpeed = 1;
    }

    public void setRightDirection() {
        xSpeed = 1;
        ySpeed = 0;
    }



    private void setDefaultHead() {
        synchronized (head) {
            head.setX(Game.STARTING_POINT.getX());
            head.setY(Game.STARTING_POINT.getY());
        }
    }

    private void setDefaultBody() {
        synchronized (body) {
            body.clear();

            for (int i = 1; i <= DEFAULT_BODY_SIZE; i++) {
                body.add(new Tile(head.getX(), head.getY() + i));
            }
        }
    }



    private void move() {
        if (xSpeed == 0 && ySpeed == 0) {
            return;
        }

        int lastPosX = head.getX();
        int lastPosY = head.getY();

        synchronized (head) {
            head.setX(Game.correctWidth(head.getX() + xSpeed));
            head.setY(Game.correctHeight(head.getY() + ySpeed));
        }

        synchronized (body) {
            for (Tile tile : body) {
                int buffX = tile.getX();
                int buffY = tile.getY();
    
                tile.setX(Game.correctWidth(lastPosX));
                tile.setY(Game.correctHeight(lastPosY));
    
                lastPosX = buffX;
                lastPosY = buffY;
            }
        }

    }

    // maybe no
    private void maybeEat() {
        synchronized (head) {
            if (head.getX() == Game.food().getX() && head.getY() == Game.food().getY()) {
                grow();
                Game.foodAte();
            }
        }
    }

    private void grow() {
        synchronized (body) {
            body.add(new Tile(head.getX(), head.getY()));
        }
    }

    // maybe no
    private void maybeDie() {
        synchronized (body) {
            for (Tile tile : body) {
                if (tile.getX() == head.getX() && tile.getY() == head.getY()) {
                    die();
                    return;
                }
            }
        }
    }

    private void die() {
        isDead = true;
        stopMoving();
        // getNewSnake();
    }

    private void resurrect() {
        this.isDead = false;
    }
}
