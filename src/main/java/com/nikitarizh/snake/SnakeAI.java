package com.nikitarizh.snake;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.nikitarizh.snake.entities.Tile;

public class SnakeAI {

    private ScheduledExecutorService thinkingLoop;

    private ArrayList<Tile> tiles;
    private ArrayList<ArrayList<Tile>> tilesMatrix;
    private ArrayList<Boolean> used;

    private LinkedList<Tile> cycle;
    private int currTile = 0;

    private int drawingTimeOut = 0;

    
    public SnakeAI() {
        tiles = new ArrayList<Tile>(Game.FIELD_WIDTH * Game.FIELD_HEIGHT);

        tilesMatrix = new ArrayList<ArrayList<Tile>>(Game.FIELD_WIDTH * Game.FIELD_HEIGHT);
        for (int i = 0; i < Game.FIELD_WIDTH * Game.FIELD_HEIGHT; i++) {
            tilesMatrix.add(new ArrayList<Tile>());
        }

        cycle = new LinkedList<Tile>();

        used = new ArrayList<Boolean>(Game.FIELD_WIDTH * Game.FIELD_HEIGHT);
    }

    public void startThinking() {
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {

        }

        prepareForFinding();

        int headPos = getTileID(Game.snake.head());
        findHamiltonianCycle(headPos, headPos, true);

        Thread brain = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    think();
                }
                catch (Exception e) {
                    System.out.println("Exception in brain: " + e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
            }  
        });
        brain.setPriority(Thread.MIN_PRIORITY + Game.THINKING_PRIORITY);
        
        thinkingLoop = Executors.newScheduledThreadPool(1);
        thinkingLoop.scheduleAtFixedRate(() -> {
            try {
                brain.run();
            }
            catch (Exception e) {
                System.out.println("Exception in brain thread: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }, 0, 1000 / Game.THINKING_FREQ, TimeUnit.MILLISECONDS);
    }

    public void stopThinking() {
        thinkingLoop.shutdown();
    }

    private void prepareForFinding() {
        for (int i = 0; i < Game.FIELD_HEIGHT; i++) {
            for (int j = 0; j < Game.FIELD_WIDTH; j++) {
                Tile t = new Tile(j, i);

                if (j - 1 >= 0) {
                    tilesMatrix.get(getTileID(t)).add(new Tile(j - 1, i));
                }
                if (j + 1 < Game.FIELD_WIDTH) {
                    tilesMatrix.get(getTileID(t)).add(new Tile(j + 1, i));
                }
                if (i - 1 >= 0) {
                    tilesMatrix.get(getTileID(t)).add(new Tile(j, i - 1));
                }
                if (i + 1 < Game.FIELD_HEIGHT) {
                    tilesMatrix.get(getTileID(t)).add(new Tile(j, i + 1));
                }

                tiles.add(t);

                used.add(false);
            }
        }
    }

    private void think() {
        Tile target = cycle.get(currTile % cycle.size());

        int xOffset = Game.snake.head().getX() - target.getX();
        if (xOffset > 0) {
            Game.snake.setLeftDirection();
            return;
        }
        else if (xOffset < 0) {
            Game.snake.setRightDirection();
            return;
        }

        int yOffset = Game.snake.head().getY() - target.getY();
        if (yOffset > 0) {
            Game.snake.setTopDirection();
            return;
        }
        else if (yOffset < 0) {
            Game.snake.setBottomDirection();
            return;
        }

        if (Game.snake.head().getX() == target.getX() && Game.snake.head().getY() == target.getY()) {
            currTile++;
        }
    }

    private int findHamiltonianCycle(int curr, int start, boolean first) {
        if (drawingTimeOut > 0) {
            pushToDrawingQueue(curr);
        }

        if (!pathExits(curr, start)) {
            if (drawingTimeOut > 0) {
                removeFromDrawingQueue(curr);
            }
            
            used.set(curr, false);
            return -1;
        }

        if (curr != start) {
            used.set(curr, true);
        }

        if (curr == start && !first) {
            used.set(curr, true);
            boolean last = true;
            for (boolean b : used) {
                if (!b) {
                    used.set(curr, false);
                    last = false;
                    return -1;
                }
            }
            if (last) {
                return 1;
            }
        }

        int j = 0;

        while (j < tilesMatrix.get(curr).size()) {
            int tileId = getTileID(tilesMatrix.get(curr).get(j));
            if (!used.get(tileId)) {
                int next = findHamiltonianCycle(tileId, start, false);
                if (next != -1) {
                    cycle.addFirst(tiles.get(curr));

                    if (drawingTimeOut > 0) {
                        removeFromDrawingQueue(curr);
                    }

                    return 1;
                }
            }
            j++;
        }

        if (drawingTimeOut > 0) {
            removeFromDrawingQueue(curr);
        }
        
        used.set(curr, false);
        return -1;
    }

    private boolean pathExits(int from, int to) {
        ArrayList<Boolean> usedForDFS = new ArrayList<Boolean>(used.size());
        ArrayList<Boolean> usedForCountPoints = new ArrayList<Boolean>(used.size());

        for (boolean b : used) {
            usedForDFS.add(b);
        }

        int usedAmount = 0;
        for (boolean b : used) {
            usedForCountPoints.add(b);
            if (b) {
                usedAmount++;
            }
        }

        return countPoints(0, -1, usedForCountPoints) >= tiles.size() - usedAmount - 1;
    }

    private int countPoints(int curr, int from, ArrayList<Boolean> used) {
        if (from != -1) {
            used.set(from, true);
        }
        else {
            for (int i = 0; i < tiles.size(); i++) {
                if (!used.get(i)) {
                    return countPoints(i, i, used);
                }
            }
        }

        int result = 0;

        for (int i = 0; i < tilesMatrix.get(curr).size(); i++) {
            int tileID = getTileID(tilesMatrix.get(curr).get(i));
            if (!used.get(tileID)) {
                result += countPoints(tileID, curr, used) + 1;
            }
        }

        used.set(curr, true);
        
        return result;
    }

    private int getTileID(Tile tile) {
        return tile.getY() * Game.FIELD_WIDTH + tile.getX();
    }

    private void pushToDrawingQueue(int id) {
        try {
            Thread.sleep(drawingTimeOut);
        }
        catch (InterruptedException e) {}
        synchronized (Game.drawingQueue) {
            Tile curr = tiles.get(id);
            if (!Game.drawingQueue.contains(curr)) {
                Game.drawingQueue.add(curr);
            }
        }
    }

    private void removeFromDrawingQueue(int id) {
        try {
            Thread.sleep(drawingTimeOut);
        }
        catch (InterruptedException e) {}
        synchronized (Game.drawingQueue) {
            Game.drawingQueue.remove(tiles.get(id));
        }
    }
}
