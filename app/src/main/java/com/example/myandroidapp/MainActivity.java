package com.example.myandroidapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast; // Import Toast for placeholder messages

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure this matches the new layout

        Button btnViewTenants = findViewById(R.id.btn_view_tenants);
        Button btnAddTenant = findViewById(R.id.btn_add_tenant);

        btnViewTenants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewTenantsActivity.class);
                startActivity(intent);
            }
        });

        btnAddTenant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTenantActivity.class);
                startActivity(intent);
            }
        });
    }
}
