package com.example.mauricioarce.addressbook;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mauricio Arce on 30/06/2015.
 */
public class MainActivity extends AppCompatActivity {

    private AddressReaderDbHelper helper;
    private List<ItemsGroup> contacts = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        recoverData();
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_contact:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                builder.setTitle("Add a contact");
                View v = inflater.inflate(R.layout.message_layout, null);
                final EditText contactName = (EditText) v.findViewById(R.id.text_cname);
                final EditText contactPhone = (EditText) v.findViewById(R.id.text_cphone);
                final EditText contactEmail = (EditText) v.findViewById(R.id.text_cemail);
                builder.setView(v);

                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = contactName.getText().toString();
                        String phone = contactPhone.getText().toString();
                        String email = contactEmail.getText().toString();

                        saveDatabase(name, phone, email);
                        MainActivity.this.fillExpandableList(name, phone, email);
                        updateUI();
                    }

                    private void saveDatabase(String name, String phone, String email) {
                        helper = new AddressReaderDbHelper(MainActivity.this);
                        SQLiteDatabase db = helper.getWritableDatabase();
                        ContentValues values = new ContentValues();

                        values.clear();
                        values.put(AddressContract.Columns.CONTACT_NAME, name);
                        values.put(AddressContract.Columns.CONTACT_NUMBER, phone);
                        values.put(AddressContract.Columns.CONTACT_EMAIL, email);

                        db.insertWithOnConflict(AddressContract.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    }
                });
                builder.setNegativeButton("Cancel",null);
                builder.create().show();
                return true;
            default:
                return false;
        }
    }

    private void recoverData() {
        helper = new AddressReaderDbHelper(MainActivity.this);
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] columns = new String[]{AddressContract.Columns._ID,
                AddressContract.Columns.CONTACT_NAME,
                AddressContract.Columns.CONTACT_NUMBER,
                AddressContract.Columns.CONTACT_EMAIL};
        Cursor cursor = db.query(AddressContract.TABLE_NAME,
                columns,
                null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String email = cursor.getString(3);
                fillExpandableList(name, phone, email);
            } while (cursor.moveToNext());
        }
    }

    private void fillExpandableList(String name, String phone, String email) {
        ItemsGroup group = new ItemsGroup(name);
        group.getChildren().add(phone);
        group.getChildren().add(email);
        contacts.add(group);
    }

    private void updateUI() {
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.exp_address);
        ContactsAdapter adapter = new ContactsAdapter(this, contacts);
        listView.setAdapter(adapter);
    }

}
