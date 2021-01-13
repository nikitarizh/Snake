package com.nikitarizh.snake;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.nikitarizh.snake.entities.Tile;

public class SnakeAI {

    private ScheduledExecutorService thinkingLoop;
    private ArrayList<Tile> tiles;
    private LinkedList<Tile> cycle;
    private ArrayList<Boolean> used;
    private int currTile = 0;
    
    public SnakeAI() {
        tiles = new ArrayList<Tile>();
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
        int headPos = Game.snake.head().getY() * Game.FIELD_WIDTH + Game.snake.head().getX();
        findHamiltonianCycle(headPos, headPos, 0);
        System.out.println("CYCLE SIZE: " + cycle.size());

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

    private void think() {
        // if (Game.food == null || Game.snake == null || Game.snake.head() == null) {
        //     return;
        // }

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

    private void prepareForFinding() {
        for (int i = 0; i < Game.FIELD_HEIGHT; i++) {
            for (int j = 0; j < Game.FIELD_WIDTH; j++) {
                tiles.add(new Tile(j, i));
                used.add(false);
            }
        }
    }

    private int findHamiltonianCycle(int curr, int start, int time) {
        int timeout = 10;
        try {
            Thread.sleep(timeout);
        }
        catch (InterruptedException e) {}
        synchronized (Game.drawingQueue) {
            if (!Game.drawingQueue.contains(tiles.get(curr))) {
                Game.drawingQueue.add(tiles.get(curr));
            }
        }

        if (!pathExits(curr, start)) {
            // System.out.println("from " + curr);
            try {
                Thread.sleep(timeout);
            }
            catch (InterruptedException e) {}
            synchronized (Game.drawingQueue) {
                Game.drawingQueue.remove(tiles.get(curr));
            }
            
            used.set(tiles.get(curr).getY() * Game.FIELD_WIDTH + tiles.get(curr).getX(), false);
            return -1;
        }

        if (curr != start) {
            used.set(tiles.get(curr).getY() * Game.FIELD_WIDTH + tiles.get(curr).getX(), true);
        }

        if (curr == start && time != 0) {
            used.set(tiles.get(curr).getY() * Game.FIELD_WIDTH + tiles.get(curr).getX(), true);
            System.out.println("if");
            boolean last = true;
            for (boolean b : used) {
                if (!b) {
                    used.set(tiles.get(curr).getY() * Game.FIELD_WIDTH + tiles.get(curr).getX(), false);
                    System.out.println("false");
                    last = false;
                    return -1;
                }
            }
            if (last) {
                System.out.println("RETURNING 1");
                return 1;
            }
        }

        int j = 0;

        while (j < tiles.size()) {
            if (checkBond(curr, j) && !used.get(j) && curr != j) {
                int next = findHamiltonianCycle(j, start, 1);
                if (next != -1) {
                    cycle.addFirst(tiles.get(curr));
                    try {
                        Thread.sleep(timeout);
                    }
                    catch (InterruptedException e) {}
                    synchronized (Game.drawingQueue) {
                        Game.drawingQueue.remove(tiles.get(curr));
                    }
                    System.out.println("ret 1");
                    return 1;
                }
            }
            j++;
        }

        try {
            Thread.sleep(timeout);
        }
        catch (InterruptedException e) {}
        synchronized (Game.drawingQueue) {
            Game.drawingQueue.remove(tiles.get(curr));
        }
        
        used.set(tiles.get(curr).getY() * Game.FIELD_WIDTH + tiles.get(curr).getX(), false);
        return -1;
    }

    private boolean checkBond(int ind1, int ind2) {
        // System.out.println(ind1 + " " + ind2);
        Tile t1 = tiles.get(ind1);
        Tile t2 = tiles.get(ind2);
        return  (t1.getX() == t2.getX() && Math.abs(t1.getY() - t2.getY()) == 1) ||
                (t1.getY() == t2.getY() && Math.abs(t1.getX() - t2.getX()) == 1) ||
                (t1.getX() == t2.getX() && t1.getY() == t2.getY());
    }

    private boolean pathExits(int from, int to) {
        ArrayList<Boolean> used1 = new ArrayList<Boolean>(used.size());
        for (boolean b : used) {
            used1.add(b);
        }
        ArrayList<Boolean> used2 = new ArrayList<Boolean>(used.size());
        int usedAmount = 0;
        for (boolean b : used) {
            used2.add(b);
            if (b) {
                usedAmount++;
            }
        }

        // System.out.println(dfs(from, to, used1));
        // System.out.println(countPoints(from, used2) == tiles.size() - usedAmount);
        // System.out.println(dfs(from, to, used1) && (countPoints(from, used2) == (tiles.size() - usedAmount)));
        int points = countPoints(-1, used2);
        // System.out.println("Points: " + points);
        // System.out.println("Required: " + (tiles.size() - usedAmount - 1));
        return dfs(from, to, used1) && (points >= (tiles.size() - usedAmount - 1));
    }

    private boolean dfs(int from, int to, ArrayList<Boolean> used) {
        if (from == to) {
            return true;
        }

        used.set(from, true);

        for (int i = 0; i < tiles.size(); i++) {
            if (!used.get(i) && checkBond(from, i)) {
                if (dfs(i, to, used)) {
                    return true;
                }
            }
        }

        return false;
    }

    private int countPoints(int curr, ArrayList<Boolean> used) {
        if (curr != -1) {
            used.set(curr, true);
        }
        else {
            for (int i = 0; i < tiles.size(); i++) {
                if (!used.get(i)) {
                    return countPoints(i, used);
                }
            }
        }

        int result = 0;

        for (int i = 0; i < tiles.size(); i++) {
            if (!used.get(i) && checkBond(curr, i)) {
                result += countPoints(i, used) + 1;
            }
        }

        return result;
    }
}
