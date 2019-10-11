package com.theincgi.class_project_turncoat.models;

import java.util.LinkedList;

public class CPU {
    GameState gs;
    public CPU(GameState gs){
        this.gs = gs;
    }

    /**
     * Simple CPU that chooses the first move with the highest number of turned tiles
     * */
    public GameState.Pos choose(){
        LinkedList<GameState.Pos> opts = gs.getValidMoves();
        int maxIndex=-1, maxScore=0;
        for (int i = 0; i < opts.size(); i++) {
            GameState.Pos p = opts.get(i);
            int score = gs.moveScore(p.getX(), p.getY());
            if(score > maxScore){
                maxIndex = i;
                maxScore = score;
            }
        }
        return opts.get(maxIndex);
    }

    public void takeTurn() {
        gs.playTile(choose());
    }
}
