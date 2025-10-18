package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.centrointegralalerce.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private Toolbar toolbar;
    private boolean esInvitado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Detectar modo invitado
        esInvitado = getIntent().getBooleanExtra("INVITADO", false);

        if (esInvitado) {
            Toast.makeText(this, "Modo invitado - Funcionalidad limitada", Toast.LENGTH_LONG).show();
        }

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Configurar toolbar
        setSupportActionBar(toolbar);

        // Cargar fragment inicial
        if (savedInstanceState == null) {
            loadFragment(new CalendarioFragment(), "Calendario");
            bottomNavigation.setSelectedItemId(R.id.nav_calendar);
        }

        // Configurar bottom navigation
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";

            int itemId = item.getItemId();
            if (itemId == R.id.nav_calendar) {
                selectedFragment = new CalendarioFragment();
                title = "Calendario";
            } else if (itemId == R.id.nav_activities_list) {
                selectedFragment = new ListaActividadesFragment();
                title = "Actividades";
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new ConfiguracionFragment();
                title = "Configuración";
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment, String title) {
        // Transición suave entre fragments
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,  // enter
                        android.R.anim.fade_out, // exit
                        android.R.anim.fade_in,  // popEnter
                        android.R.anim.fade_out  // popExit
                )
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    // Método para obtener si es invitado (otros fragments pueden usarlo)
    public boolean isGuest() {
        return esInvitado;
    }
}