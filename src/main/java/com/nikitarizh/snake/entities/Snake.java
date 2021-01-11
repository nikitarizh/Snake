package com.nikitarizh.snake.entities;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.nikitarizh.snake.Game;

public class Snake {

    private final int DEFAULT_BODY_SIZE = 4;

    private Tile head;
    private ArrayList<Tile> body;
    
    private int xSpeed;
    private int ySpeed;

    private Timer movingLoop;

    private boolean isDead = false;

    public Snake() {

        generateHead();
        generateBody();

        xSpeed = 0;
        ySpeed = 0;
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

    public void startMoving() {
        movingLoop = new Timer();
        movingLoop.schedule(new TimerTask(){
            @Override
            public void run() {
                move();

                // lol
                maybeDie();
                maybeEat();
            }
        }, 0, 1000 / Game.MOVING_FREQ);
    }

    public void stopMoving() {
        movingLoop.cancel();
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

    private void generateHead() {
        head = new Tile(10, 10);
    }

    private void generateBody() {
        body = new ArrayList<Tile>();

        for (int i = 1; i <= DEFAULT_BODY_SIZE; i++) {
            body.add(new Tile(10, 10 + i));
        }
    }

    private void move() {
        if (xSpeed == 0 && ySpeed == 0) {
            return;
        }

        int lastPosX = head.getX();
        int lastPosY = head.getY();

        head.setX(Game.correctWidth(head.getX() + xSpeed));
        head.setY(Game.correctHeight(head.getY() + ySpeed));

        for (Tile tile : body) {
            int buffX = tile.getX();
            int buffY = tile.getY();

            tile.setX(Game.correctWidth(lastPosX));
            tile.setY(Game.correctHeight(lastPosY));

            lastPosX = buffX;
            lastPosY = buffY;
        }
    }

    // maybe no
    private void maybeEat() {
        if (head.getX() == Game.food.getX() && head.getY() == Game.food.getY()) {
            grow();
            Game.foodAte();
        }
    }

    // maybe no
    private void maybeDie() {
        for (Tile tile : body) {
            if (tile.getX() == head.getX() && tile.getY() == head.getY()) {
                die();
            }
        }
    }

    private void grow() {
        synchronized(body) {
            body.add(new Tile(head.getX(), head.getY()));
        }
    }

    private void die() {
        isDead = true;
        stopMoving();
        // generateHead();
        // generateBody();
    }
}
