package com.theincgi.class_project_turncoat.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.UiThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.theincgi.class_project_turncoat.R;
import com.theincgi.class_project_turncoat.controlers.GameStateController;
import com.theincgi.class_project_turncoat.models.GameState;

public class BoardActivity extends AppCompatActivity {
    TableLayout tableLayout;
    GameStateController gsc;
    ImageView wTurnIndicator, bTurnIndicator;
    public static final String EXTRA_GAMEMODE = "com.theincgi.turncoat.GAME_MODE";
    public static final String EXTRA_GAMEMODE_SINGLE = "SINGLE";
    public static final String EXTRA_GAMEMODE_MULTIPLAYER = "MULTIPLAYER";
    public static final String EXTRA_GAMEMODE_DEMO = "DEMO";
    BitmapDrawable socketDrawable, blackTile, whiteTile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_view);
        Intent intent = getIntent();

        tableLayout = findViewById(R.id.table_layout);
        wTurnIndicator = findViewById(R.id.white_turn_indicator);
        bTurnIndicator = findViewById(R.id.black_turn_indicator);
        tableLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                loadTextures();
                refreshAllTiles();
            }
        });
        String extra = intent.getStringExtra(EXTRA_GAMEMODE);
        boolean p1IsCpu = extra.equals(EXTRA_GAMEMODE_DEMO);
        boolean p2IsCpu = extra.equals(EXTRA_GAMEMODE_DEMO) || extra.equals(EXTRA_GAMEMODE_SINGLE);
        Log.i("BoardActivity#onCreate", "Creating GameStateController in mode " + extra);
        gsc = new GameStateController(this, new GameState(), p1IsCpu, p2IsCpu);
        
        addTiles();
        setTurnIndicator(gsc.getTurn());
    }

    private void  loadTextures(){
        int width = Math.min(tableLayout.getWidth(), tableLayout.getHeight()) / 8;
        {
            Bitmap tmp = BitmapFactory.decodeResource(getResources(), R.drawable.socket);
            Bitmap socket = Bitmap.createScaledBitmap(tmp, width, width, false);
            socketDrawable = new BitmapDrawable(getResources(), socket);
        }
        {
            Bitmap tmp = BitmapFactory.decodeResource(getResources(), R.drawable.black);
            Bitmap black = Bitmap.createScaledBitmap(tmp, width, width, false);
            blackTile = new BitmapDrawable(getResources(), black);
        }{
            Bitmap tmp = BitmapFactory.decodeResource(getResources(), R.drawable.white);
            Bitmap white = Bitmap.createScaledBitmap(tmp, width, width, false);
            whiteTile = new BitmapDrawable(getResources(), white);
        }
    }

    /**
     * Used during setup to create each image button
     * */
    private void addTiles(){
        Log.i("BoardActivity#addTiles", "Adding tiles...");
        View.OnClickListener ocl = new View.OnClickListener() {
            @Override public void onClick(View v) { BoardActivity.this.onClick(v); }
        };
        //Drawable drawable = getDrawable(R.drawable.socket);

        for (int y = 0; y < gsc.getBoardSize(); y++) {
            TableRow row = new TableRow(this);
            //row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tableLayout.addView(row);
            for (int x = 0; x < gsc.getBoardSize(); x++) {
                _ImageButton button = new _ImageButton(this, x, y);
               // button.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
                button.setPadding(0,0,0,0);
                button.setContentDescription(getString(R.string.tile_description));
                button.setOnClickListener(ocl);
                //button.setScaleType(ImageView.ScaleType.FIT_CENTER);
                //button.setAdjustViewBounds(true);
                //button.setImageDrawable(socketDrawable);
                row.addView(button);
            }
        }
    }

    /**
     * Used to update the view
     * */
    public void changeTile(int x, int y, GameState.TileState state){
        TableRow row = (TableRow) tableLayout.getChildAt(y);
        _ImageButton ib = (_ImageButton) row.getChildAt(x);
        ib.setImageDrawable(state.isNone()?
                        socketDrawable :
                        state.isWhite()?
                                whiteTile :
                                blackTile
                );
    }

    public void refreshAllTiles(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _refreshAllTiles();
            }
        });
    }
    private void _refreshAllTiles() {

        for(int y = 0; y < gsc.getBoardSize(); y++){
            for (int x = 0; x < gsc.getBoardSize(); x++) {
                changeTile(x, y, gsc.getTileState(x, y));
            }
        }
        setTurnIndicator(gsc.getTurn());
    }

    public void setTurnIndicator(GameState.TileState state){
        Log.i("BoardActivity#setTrn", state.toString());
        wTurnIndicator.setVisibility(state.isWhite()?View.VISIBLE : View.INVISIBLE);
        bTurnIndicator.setVisibility(state.isBlack()?View.VISIBLE : View.INVISIBLE);
    }

    public void onClick(View v){
        Log.i("BoardActivity#onClick", "OnClick called");
        if(v instanceof _ImageButton){
            _ImageButton ib = (_ImageButton) v;
            gsc.onTileClick(ib.x, ib.y);
        }
    }


    private static class _ImageButton extends AppCompatImageButton {
        final int x, y;
        public _ImageButton(Context context, int x, int y){
            super( context );
            this.x = x;
            this.y = y;
        }

    }
}
