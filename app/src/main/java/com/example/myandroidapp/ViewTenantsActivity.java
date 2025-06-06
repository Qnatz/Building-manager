package com.example.myandroidapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myandroidapp.data.TenantContract;
import com.example.myandroidapp.data.TenantDbHelper;

public class ViewTenantsActivity extends AppCompatActivity {

    private TenantDbHelper dbHelper;
    private ListView tenantListView;
    private SimpleCursorAdapter adapter;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tenants);

        dbHelper = new TenantDbHelper(this);
        tenantListView = findViewById(R.id.tenantListView);
        searchEditText = findViewById(R.id.searchEditText);

        displayTenants(null); // Display all tenants initially

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                displayTenants(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void displayTenants(String query) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                TenantContract.TenantEntry._ID,
                TenantContract.TenantEntry.COLUMN_NAME_NAME,
                TenantContract.TenantEntry.COLUMN_NAME_APARTMENT_NUMBER,
                TenantContract.TenantEntry.COLUMN_NAME_PHONE_NUMBER
        };

        String selection = null;
        String[] selectionArgs = null;

        if (query != null && !query.isEmpty()) {
            selection = TenantContract.TenantEntry.COLUMN_NAME_NAME + " LIKE ?";
            selectionArgs = new String[]{"%" + query + "%"};
        }

        Cursor cursor = db.query(
                TenantContract.TenantEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                TenantContract.TenantEntry.COLUMN_NAME_NAME + " ASC"
        );

        // Column names to map from the cursor to the TextViews in the list item layout
        String[] fromColumns = {
                TenantContract.TenantEntry.COLUMN_NAME_NAME,
                TenantContract.TenantEntry.COLUMN_NAME_APARTMENT_NUMBER,
                TenantContract.TenantEntry.COLUMN_NAME_PHONE_NUMBER
        };

        // View IDs in list_item_tenant.xml to map the data to
        int[] toViews = {
                R.id.textViewTenantName,
                R.id.textViewApartmentNumber,
                R.id.textViewPhoneNumber
        };

        // If an old adapter exists, close its cursor
        if (adapter != null && adapter.getCursor() != null && !adapter.getCursor().isClosed()) {
            adapter.changeCursor(null); // changeCursor will close the old cursor
        }

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_tenant,
                cursor,
                fromColumns, // Use the corrected 'fromColumns'
                toViews,
                0
        );

        tenantListView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        if (adapter != null && adapter.getCursor() != null && !adapter.getCursor().isClosed()) {
            adapter.getCursor().close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
