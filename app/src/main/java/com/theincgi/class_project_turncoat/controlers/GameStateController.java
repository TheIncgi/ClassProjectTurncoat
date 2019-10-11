package com.theincgi.class_project_turncoat.controlers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.theincgi.class_project_turncoat.activities.BoardActivity;
import com.theincgi.class_project_turncoat.models.CPU;
import com.theincgi.class_project_turncoat.models.GameState;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameStateController {
    private final GameState gs;
    private final BoardActivity activity;
    private final boolean p1IsCpu, p2IsCpu;
    private final Random random = new Random();
    CPU cpu;
    private ScheduledExecutorService scheduler;
    public GameStateController(final BoardActivity activity, final GameState gs, final boolean p1IsCpu, final boolean p2IsCpu){
        this.activity = activity;
        this.gs = gs;
        this.p1IsCpu = p1IsCpu;
        this.p2IsCpu = p2IsCpu;
        scheduler = Executors.newSingleThreadScheduledExecutor();


        gs.setOnEndOfGame(new Runnable() {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            ScheduledFuture sortTimer;
            @Override
            public void run() {
                Log.i("GameStateController#", "*** End of game ***");
                sortTimer = scheduler.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("GameStateController#", "*** SORT ***");
                        if(gs.doSortStep()){
                            sortTimer.cancel(false );
                        }
                        activity.refreshAllTiles();
                    }
                }, 350, 200, TimeUnit.MILLISECONDS);
            }
        });
        if(p2IsCpu){
            cpu = new CPU(gs);
            GameState.OnTurnListener onTurnListener;
            gs.setOnTurn(onTurnListener = new GameState.OnTurnListener() {
                final Runnable doCpuTurn = new Runnable() {
                    @Override public void run() {
                        cpu.takeTurn();
                        activity.refreshAllTiles();
                    }
                };
                @Override public void onTurn(GameState.TileState turn) {
                    boolean isCpuTurn = (turn.isWhite() && p1IsCpu) || (turn.isBlack() && p2IsCpu);
                    if(isCpuTurn){
                        scheduler.schedule(doCpuTurn, p1IsCpu&&p2IsCpu? 200 : 750+random.nextInt(2000), TimeUnit.MILLISECONDS);
                    }
                }
            });
            onTurnListener.onTurn(getTurn()); //if its cpu's turn, gets it started
        }
    }





    public int getBoardSize() {
        return gs.BOARD_SIZE;
    }

    public void onTileClick(int xTile, int yTile){
        if((getTurn().isWhite() && !p1IsCpu) ||
           (getTurn().isBlack() && !p2IsCpu)){
            gs.playTile(xTile, yTile);
        }

        activity.refreshAllTiles();
    }



    public GameState.TileState getTileState(int x, int y){
        return gs.getTile(x, y);
    }

    public GameState.TileState getTurn() {
        return gs.getTurn();
    }
}
