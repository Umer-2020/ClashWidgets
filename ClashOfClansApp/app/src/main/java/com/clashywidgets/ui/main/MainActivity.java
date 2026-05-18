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
        androidx.navigation.fragment.NavHostFragment navHostFragment = 
                (androidx.navigation.fragment.NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        // ── Data Initialization ──────────────────────────────────────────────
        com.clashywidgets.data.repository.UpgradeRepository.getInstance(this)
                .initializeDefaultSlotsIfNeeded();

        // ── Permissions ──────────────────────────────────────────────────────
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
