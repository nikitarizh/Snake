package com.nikitarizh.snake;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;

public class MainSceneController {

    @FXML
    private Canvas canvas;

    private GraphicsContext context;

    private Game game;

    @FXML
    private void initialize() {
        Game.WIDTH_MULT = canvas.getWidth() / Game.FIELD_WIDTH;
        Game.HEIGHT_MULT = canvas.getHeight() / Game.FIELD_HEIGHT;

        context = canvas.getGraphicsContext2D();
        
        game = new Game(context);
    }

    @FXML
    void keyPressed(KeyEvent e) {
        game.changeSnakeDirection(e.getCode().getName());
    }
}
