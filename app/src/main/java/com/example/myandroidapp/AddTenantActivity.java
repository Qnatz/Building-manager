package com.example.myandroidapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myandroidapp.data.TenantContract;
import com.example.myandroidapp.data.TenantDbHelper;

public class AddTenantActivity extends AppCompatActivity {

    private EditText mTenantNameEditText;
    private EditText mApartmentNumberEditText;
    private EditText mPhoneNumberEditText;
    private EditText mOtherContactInfoEditText;
    private Button mSaveTenantButton;

    private TenantDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tenant);

        // Initialize UI elements
        mTenantNameEditText = findViewById(R.id.et_tenant_name);
        mApartmentNumberEditText = findViewById(R.id.et_apartment_number);
        mPhoneNumberEditText = findViewById(R.id.et_phone_number);
        // Assuming R.id.et_email corresponds to "other_contact_info" for now as per contract
        // If et_email and et_notes are separate and need to be stored, the contract and DB need updates.
        // For this step, et_email will be mapped to COLUMN_NAME_OTHER_CONTACT_INFO
        mOtherContactInfoEditText = findViewById(R.id.et_email); // Or a dedicated "other info" EditText

        mSaveTenantButton = findViewById(R.id.btn_save_tenant);

        // Instantiate TenantDbHelper
        mDbHelper = new TenantDbHelper(this);

        // Set OnClickListener for the Save Tenant button
        mSaveTenantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTenant();
            }
        });
    }

    private void saveTenant() {
        // Clear previous errors
        mTenantNameEditText.setError(null);
        mApartmentNumberEditText.setError(null);
        mPhoneNumberEditText.setError(null);
        // mOtherContactInfoEditText.setError(null); // No validation specified for this

        // Get text from EditText fields
        String nameString = mTenantNameEditText.getText().toString().trim();
        String apartmentNumberString = mApartmentNumberEditText.getText().toString().trim();
        String phoneNumberString = mPhoneNumberEditText.getText().toString().trim();
        String otherContactInfoString = mOtherContactInfoEditText.getText().toString().trim();

        boolean isValid = true;

        // Validate Tenant Name
        if (nameString.isEmpty()) {
            mTenantNameEditText.setError("Name is required");
            isValid = false;
        }

        // Validate Apartment Number
        if (apartmentNumberString.isEmpty()) {
            mApartmentNumberEditText.setError("Apartment number is required");
            isValid = false;
        }

        // Validate Phone Number (if not empty)
        if (!phoneNumberString.isEmpty()) {
            // Allows digits, spaces, +, -, (, ) and checks length (e.g. 7-15 chars)
            if (!phoneNumberString.matches("^[\\d\\s()+-]*$") || phoneNumberString.length() < 7 || phoneNumberString.length() > 15) {
                mPhoneNumberEditText.setError("Invalid phone number format (7-15 chars, digits, spaces, +, -, ())");
                isValid = false;
            }
        }

        if (!isValid) {
            Toast.makeText(AddTenantActivity.this, "Please correct the errors.", Toast.LENGTH_SHORT).show();
            return; // Stop before saving
        }

        // Get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create ContentValues object
        ContentValues cv = new ContentValues();
        cv.put(TenantContract.TenantEntry.COLUMN_NAME_NAME, nameString);
        cv.put(TenantContract.TenantEntry.COLUMN_NAME_APARTMENT_NUMBER, apartmentNumberString);
        cv.put(TenantContract.TenantEntry.COLUMN_NAME_PHONE_NUMBER, phoneNumberString);
        cv.put(TenantContract.TenantEntry.COLUMN_NAME_OTHER_CONTACT_INFO, otherContactInfoString);

        // Insert new row
        long newRowId = db.insert(TenantContract.TenantEntry.TABLE_NAME, null, cv);

        // Check newRowId and show Toast message
        if (newRowId == -1) {
            Toast.makeText(this, "Error saving tenant.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Tenant saved successfully.", Toast.LENGTH_SHORT).show();
            finish(); // Optionally finish activity after successful save
        }
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close(); // Close the database helper when the activity is destroyed
        super.onDestroy();
    }
}
