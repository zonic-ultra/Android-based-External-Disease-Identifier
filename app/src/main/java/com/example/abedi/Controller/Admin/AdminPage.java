package com.example.abedi.Controller.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;

import com.example.abedi.Controller.Admin.Disease_Identification.Identify_External_Diseases;
import com.example.abedi.Controller.Menu;
import com.google.android.material.navigation.NavigationView;
import com.example.abedi.R;

public class AdminPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        Button treatment_suggestion_btn = findViewById(R.id.treatment_suggestion_btn);

        treatment_suggestion_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPage.this, TreatmentSuggestionAdmin.class);
                startActivity(intent);
            }
        });

        Button record_history_btn = findViewById(R.id.record_history_btn);

        record_history_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPage.this, RecordHistoryAdmin.class);
                startActivity(intent);
            }
        });

        Button identify_disease = findViewById(R.id.identify_btn);

        identify_disease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminPage.this, Identify_External_Diseases.class);
                startActivity(intent);
            }
        });

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer Layout and Navigation View
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Setting up the Drawer Toggle to show the hamburger icon
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // navigation item listener
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Toast.makeText(AdminPage.this, "Home selected", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.nav_change_password) {
                // Handle Change Password navigation item click
                Intent intent = new Intent(AdminPage.this, ChangePassword.class);
                startActivity(intent);
            } else if (item.getItemId() == R.id.menu_icon) {
                Intent intent = new Intent(AdminPage.this, Menu.class);
                startActivity(intent);
            }
            // Close the drawer after selection
            drawerLayout.closeDrawers();
            return true;
        });
    }
}
