package com.example.practika;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    // Текущий фрагмент
    private Fragment currentFragment;

    // Экземпляры фрагментов (чтобы не пересоздавать)
    private final HomeFragment homeFragment = new HomeFragment();
    private final CatalogFragment catalogFragment = new CatalogFragment();
    private final ProjectsFragment projectsFragment = new ProjectsFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // По умолчанию показываем профиль
        currentFragment = profileFragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, profileFragment)
                .commit();

        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = homeFragment;
            } else if (itemId == R.id.nav_catalogs) {
                selectedFragment = catalogFragment;
            } else if (itemId == R.id.nav_projects) {
                selectedFragment = projectsFragment;
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = profileFragment;
            }

            if (selectedFragment != null && selectedFragment != currentFragment) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                currentFragment = selectedFragment;
            }
            return true;
        });
    }
}