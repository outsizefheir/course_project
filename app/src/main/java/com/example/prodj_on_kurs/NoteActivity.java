package com.example.prodj_on_kurs;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NoteActivity extends AppCompatActivity {

    EditText titleBox;
    EditText headerBox;
    EditText contentBox;
    ImageButton saveButton;
    ImageButton deleteButton;

    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    Cursor noteCursor;
    long noteId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        titleBox = findViewById(R.id.title);
        headerBox = findViewById(R.id.header);
        contentBox = findViewById(R.id.content);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            noteId = extras.getLong("id");
        }

        if (noteId > 0) {
            noteCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE_NOTES + " where " +
                    DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(noteId)});
            noteCursor.moveToFirst();
            titleBox.setText(noteCursor.getString(1));
            headerBox.setText(noteCursor.getString(2));
            contentBox.setText(noteCursor.getString(3));
            noteCursor.close();
        } else {
            deleteButton.setVisibility(View.GONE);
        }
    }

    public void save(View view) {
        String title = titleBox.getText().toString();
        String header = headerBox.getText().toString();
        String content = contentBox.getText().toString();

        if (title.isEmpty() || header.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_TITLE, title);
        cv.put(DatabaseHelper.COLUMN_HEADER, header);
        cv.put(DatabaseHelper.COLUMN_CONTENT, content);

        if (noteId > 0) {
            db.update(DatabaseHelper.TABLE_NOTES, cv, DatabaseHelper.COLUMN_ID + "=" + String.valueOf(noteId), null);
        } else {
            db.insert(DatabaseHelper.TABLE_NOTES, null, cv);
        }
        goHome();
    }

    public void delete(View view){
        db.delete(DatabaseHelper.TABLE_NOTES, "_id = ?", new String[]{String.valueOf(noteId)});
        goHome();
    }

    private void goHome(){
        db.close();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
