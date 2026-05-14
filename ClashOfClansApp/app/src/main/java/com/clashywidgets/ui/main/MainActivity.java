package com.clashywidgets.ui.main;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.clashywidgets.R;
import com.clashywidgets.databinding.ActivityMainBinding;

/**
 * Single-activity host.
 *
 * Responsibilities:
 *  - Enforces portrait-only orientation (belt-and-suspenders alongside the Manifest setting).
 *  - Wires the Navigation Component to the BottomNavigationView.
 *  - All actual content lives in Fragments navigated via nav_graph.xml.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── Portrait lock ────────────────────────────────────────────────────
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // ── ViewBinding ──────────────────────────────────────────────────────
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ── Navigation ───────────────────────────────────────────────────────
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        // ── Data Initialization ──────────────────────────────────────────────
        com.clashywidgets.data.repository.UpgradeRepository.getInstance(this)
                .initializeDefaultSlotsIfNeeded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
