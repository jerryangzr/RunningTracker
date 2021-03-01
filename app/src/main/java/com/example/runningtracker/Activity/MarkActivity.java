package com.example.runningtracker.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.runningtracker.R;
import com.google.android.material.chip.ChipGroup;

public class MarkActivity extends AppCompatActivity {
    ChipGroup mChipGroup;
    EditText commentEt;
    String journeyReview, journeyComment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // apply different App Theme based on previous choice before setting up activity view
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.DarkTheme);
        else
            setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark);

        // set the "Up" button for user to go back to parent activity
        ActionBar backBar = getSupportActionBar();
        backBar.setDisplayHomeAsUpEnabled(true);

        mChipGroup = findViewById(R.id.chipGroup);
        commentEt = findViewById(R.id.comment);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClickUpdate(View view) {

        // get the chip id from the chip group
        int id = mChipGroup.getCheckedChipId();
        switch (id) {
            case -1:
                Log.d("chip", "User didn't choose any chips.");
                journeyReview = "No review";
                break;
            case R.id.good:
                Log.d("chip", "Journey: " + "good");
                journeyReview = "Good";
                break;
            case R.id.normal:
                Log.d("chip", "Journey: " + "normal");
                journeyReview = "Normal";
                break;
            case R.id.bad:
                Log.d("chip", "Journey: " + "bad");
                journeyReview = "Bad";
                break;
        }

        journeyComment = commentEt.getText().toString();   // get the comment from user

        if(journeyComment.isEmpty())        // set the default message when comment is empty
            journeyComment = "No comment";

        Log.d("update", "onClickUpdate: " + journeyComment);
        Log.d("update", "onClickUpdate: " + journeyReview);

        Intent intent = new Intent();       // send the result back to ResultActivity
        intent.putExtra("review",journeyReview);
        intent.putExtra("comment",journeyComment);
        setResult(RESULT_OK,intent);
        this.finish();
    }
}
