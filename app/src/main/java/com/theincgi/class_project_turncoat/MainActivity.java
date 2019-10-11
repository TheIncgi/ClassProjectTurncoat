package com.theincgi.class_project_turncoat;

import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.theincgi.class_project_turncoat.activities.BoardActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Main#LIFECYCLE", "Created!");
    }

    public void onClick(View v){
        Intent intent = null;
        switch (v.getId()){
            case R.id.new_single:
                intent = new Intent(this, BoardActivity.class);
                intent.putExtra(BoardActivity.EXTRA_GAMEMODE, BoardActivity.EXTRA_GAMEMODE_SINGLE);
                break;
            case R.id.new_multi:
                intent = new Intent(this, BoardActivity.class);
                intent.putExtra(BoardActivity.EXTRA_GAMEMODE, BoardActivity.EXTRA_GAMEMODE_MULTIPLAYER);
                break;
            case R.id.demo_mode:
                intent = new Intent(this, BoardActivity.class);
                intent.putExtra(BoardActivity.EXTRA_GAMEMODE, BoardActivity.EXTRA_GAMEMODE_DEMO);
            default:
                Log.w("MainActivity#onClick", "Missing onClick");
        }
        if(intent == null) return;
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Main#LIFECYCLE", "Paused!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Main#LIFECYCLE", "Resumed!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Main#LIFECYCLE", "Stopped!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Main#LIFECYCLE", "Destroyed, Goodbye!");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        super.onSaveInstanceState(outState, outPersistentState);
//    }
}
