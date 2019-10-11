package com.theincgi.class_project_turncoat.models;

import android.service.quicksettings.Tile;
import android.util.Log;

import java.util.LinkedList;


public class GameState {
    public static final int BOARD_SIZE = 8;
    /**Indexed as [y][x]*/
    private final TileState[][] board;
    private TileState turn = TileState.NONE;
    private OnTurnListener onTurn;
    private Runnable onEndOfGame;
    public GameState() {
        board = new TileState[BOARD_SIZE][BOARD_SIZE];
        newGame();
    }


    public synchronized void newGame() {
        clearBoard();
        int center = BOARD_SIZE/2 -1;
        setTile(center+0, center+0, TileState.WHITE);
        setTile(center+1, center+1, TileState.WHITE);
        setTile(center+0, center+1, TileState.BLACK);
        setTile(center+1, center+0, TileState.BLACK);

        turn = TileState.pickRandomColor();
    }

    private synchronized void clearBoard() {
        for(int y = 0; y<board.length; y++)
            for(int x = 0; x<board[y].length; x++)
                setTile(x, y, TileState.NONE);
    }
    /**
     * Sets the TileState at this pos
     * returns: Previous {@link TileState}
     * When playing the game use
     * */
    public synchronized TileState setTile(int x, int y, TileState s) {
        try {
            return getTile(x, y);
        } finally {
            board[y][x] = s;
        }
    }
    public synchronized TileState getTile(int x, int y) {
        if(x<0 || y <0 || x>=BOARD_SIZE || y>=BOARD_SIZE) return TileState.NONE;
        return board[y][x];
    }
    public synchronized boolean playTile(Pos pos){
        return playTile(pos.getX(), pos.getY());
    }
    public synchronized boolean playTile(int x, int y){
        if(!isValidMove(x, y)) return false;
        setTile(x, y, turn);
        flipFrom(x, y);
        turn = turn.next();
        if(onTurn!=null)
            onTurn.onTurn(turn);
        checkEndOfGame();
        return true;
    }

    public synchronized void setOnTurn(OnTurnListener onTurn) {
        this.onTurn = onTurn;
    }

    public synchronized void setOnEndOfGame(Runnable onEndOfGame) {
        this.onEndOfGame = onEndOfGame;
    }

    private synchronized void checkEndOfGame() {
        if(getValidMoves().size() <= 0){
            turn = TileState.NONE;
            if(onEndOfGame!=null)
                onEndOfGame.run();
        }
    }

    private synchronized void flipFrom(int x, int y){
        for(int dy = -1; dy <= 1; dy++){
            for(int dx = -1; dx<=1; dx++){
                if(dx==0 && dy==0) continue;
                flipFrom(x+dx, y+dy, dx, dy);
            }
        }
    }
    private synchronized boolean flipFrom(int x, int y, int dx, int dy){
        if(getTile(x, y).equals(turn)) return true;
        if(getTile(x, y).isNone()) return false;
        boolean valid;
        if(valid = flipFrom(x+dx, y+dy, dx, dy))
            setTile(x, y, turn);
        return valid;
    }

    public synchronized TileState getTurn() {
        return turn;
    }



    /**
     * Check if the current player can place anything
     * */
    public synchronized boolean moveExists(){
        return getValidMoves().size() > 0;
    }

    public synchronized void swap(Pos p1, Pos p2){
        swap(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    /**
     * Used when sorting the board to show the winner
     * */
    public synchronized void swap(int x1, int y1, int x2, int y2){
        TileState p1 = setTile(x1,y1, getTile(x2, y2));
        setTile(x2, y2, p1);
    }

    public synchronized LinkedList<Pos> getValidMoves(){
        LinkedList<Pos> out = new LinkedList<>();
        for(int y = 0; y<BOARD_SIZE; y++){
            for (int x = 0; x < BOARD_SIZE; x++) {
                if(isValidMove(x, y))
                    out.add(new Pos(x, y));
            }
        }
        return out;
    }

    public synchronized boolean isValidMove(int x, int y){
        return getTile(x,y).isNone() && moveScore(x, y) > 0;
    }
    public synchronized int moveScore(int x, int y){
        if(!getTile(x, y).isNone()) return 0; //already occupied
        int sum = 0;
        for(int dy = -1; dy <= 1; dy++){
            for(int dx = -1; dx<=1; dx++){
                if(dx==0 && dy==0) continue;
                sum+=Math.max(0,moveScore(x+dx, y+dy, dx, dy));
            }
        }
        return  sum;
    }
    /**
     * Calculate number of tiles converted in some direction if placed on
     * x-dx, y-dy
     * returns -1 if the move is invalid
     * */
    private synchronized int moveScore(int x, int y, int dx, int dy) {
        TileState ts = getTile(x, y);
        if(ts.isNone()){
            return -1;
        }else if(ts.equals(turn)){
            return 0;
        }
        int r = moveScore(x+dx, y+dy, dx, dy);
        return r==-1? -1 : 1+r;
    }

    /**
     * returns true if done sorting
     * */
    public synchronized boolean doSortStep() {
        try {
            Pos firstNonWhite = null;
            Pos firstNonBlack = null;
            Pos nextWhite = null;
            Pos nextBlack = null;
            final int TILES = BOARD_SIZE * BOARD_SIZE;
            boolean didSomeSorting = false;

            for (int i = 0; i < TILES; i++) {
                int x = i % BOARD_SIZE;
                int y = i / BOARD_SIZE;
                if (!getTile(x, y).isWhite()) {
                    firstNonWhite = new Pos(i);
                    break;
                }
            }
            for (int i = firstNonWhite.getIndex() + 1; i < TILES; i++) {
                int x = i % BOARD_SIZE;
                int y = i / BOARD_SIZE;
                if (getTile(x, y).isWhite()) {
                    nextWhite = new Pos(i);
                    break;
                }
            }
            if (firstNonWhite != null && nextWhite != null) {
                swap(firstNonWhite, nextWhite);
                didSomeSorting = true;
            }


            for (int i = TILES - 1; i >= 0; i--) {
                int x = i % BOARD_SIZE;
                int y = i / BOARD_SIZE;
                if (!getTile(x, y).isBlack()) {
                    firstNonBlack = new Pos(i);
                    break;
                }
            }
            for (int i = firstNonBlack.getIndex() - 1; i >= 0; i--) {
                int x = i % BOARD_SIZE;
                int y = i / BOARD_SIZE;
                if (getTile(x, y).isBlack()) {
                    nextBlack = new Pos(i);
                    break;
                }
            }
            if (firstNonBlack != null && nextBlack != null) {
                swap(firstNonBlack, nextBlack);
                didSomeSorting = true;
            }
            return !didSomeSorting;
        }catch (Throwable t){
            Log.e("GameState", "Sorting error:", t);
            return true;
        }
    }

    public static enum TileState {
        NONE,
        WHITE,
        BLACK;

        public boolean isNone() {return this.equals(NONE);}
        public boolean isWhite() {return this.equals(WHITE);}
        public boolean isBlack() {return this.equals(BLACK);}
        public TileState next() {return this.isBlack()?WHITE:BLACK;}
        public static TileState pickRandomColor(){
            return Math.random() < .5? WHITE : BLACK;
        }
        /**
         * Used for serialization (for saving the game state when exiting)
         * */
        public int toInt(){
            switch (this){
                case NONE: return 0;
                case BLACK: return 1;
                case WHITE: return 2;
                default: return -1;
            }
        }
        /**
         * Used for deserialization
         * */
        public static TileState fromInt(int i){
            switch (i){
                case 0: return NONE;
                case 1: return BLACK;
                case 2: return WHITE;
                default:
                    return null;
            }
        }
    }
    public static class Pos {
        private final int x, y;
        public Pos(int x, int y){
            this.x = x;
            this.y = y;
        }
        public Pos(int index){
            this.x = index%BOARD_SIZE;
            this.y = index/BOARD_SIZE;
        }
        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
        public int getIndex(){
            return x+y*BOARD_SIZE;
        }
    }
    public interface OnTurnListener{
        void onTurn(TileState turn);
    }
}
