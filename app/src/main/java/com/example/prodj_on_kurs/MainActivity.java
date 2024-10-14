package com.example.prodj_on_kurs;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

public class MainActivity extends AppCompatActivity {

    ListView noteList;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor noteCursor;
    SimpleCursorAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteList = findViewById(R.id.list);
        noteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        databaseHelper = new DatabaseHelper(getApplicationContext());

        ImageButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNote();
            }
        });

        EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchNotes(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        db = databaseHelper.getReadableDatabase();
        noteCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE_NOTES, null);

        String[] headers = new String[]{DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_HEADER, DatabaseHelper.COLUMN_CONTENT};
        int[] viewIds = new int[]{R.id.titleTextView, R.id.headerTextView, R.id.contentTextView};
        noteAdapter = new SimpleCursorAdapter(this, R.layout.list_item_layout,
                noteCursor, headers, viewIds, 0);
        noteList.setAdapter(noteAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
        if (noteCursor != null) {
            noteCursor.close();
        }
    }

    public void addNote() {
        Intent intent = new Intent(this, NoteActivity.class);
        startActivity(intent);
    }
    private String previousSearchText = "";
    private Cursor searchCursor;

    public void searchNotes(final String keyword) {
        if (!keyword.isEmpty() && !keyword.equals(previousSearchText)) {
            previousSearchText = keyword;
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    db = databaseHelper.getReadableDatabase();
                    String query = "SELECT * FROM " + DatabaseHelper.TABLE_NOTES +
                            " WHERE " + DatabaseHelper.COLUMN_TITLE + " LIKE ? OR " +
                            DatabaseHelper.COLUMN_HEADER + " LIKE ? OR " +
                            DatabaseHelper.COLUMN_CONTENT + " LIKE ?";
                    String[] args = new String[]{"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"};
                    searchCursor = db.rawQuery(query, args);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            noteAdapter.changeCursor(searchCursor);
                        }
                    });
                }
            });
        } else if (keyword.isEmpty() && !previousSearchText.isEmpty()) {
            onResume();
        }
    }

}

