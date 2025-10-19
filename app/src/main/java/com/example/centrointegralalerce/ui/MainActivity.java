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
    private String rolUsuario = "usuario"; // por defecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtener datos del Login
        esInvitado = getIntent().getBooleanExtra("INVITADO", false);
        String rol = getIntent().getStringExtra("ROL");
        if (rol != null) {
            rolUsuario = rol;
        }

        // Mensaje según rol
        if (esInvitado) {
            Toast.makeText(this, "Modo invitado - Funcionalidad limitada", Toast.LENGTH_LONG).show();
        } else if ("admin".equals(rolUsuario)) {
            Toast.makeText(this, "Bienvenido Administrador", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Bienvenido Usuario", Toast.LENGTH_SHORT).show();
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

                // Ejemplo: Solo admins pueden ver ciertas opciones en Configuración
                if (!"admin".equals(rolUsuario)) {
                    Toast.makeText(this, "Acceso limitado: solo administradores pueden modificar la configuración avanzada.", Toast.LENGTH_SHORT).show();
                }
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment, String title) {
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

    // Métodos públicos para que los fragments sepan si es invitado o admin
    public boolean isGuest() {
        return esInvitado;
    }

    public boolean isAdmin() {
        return "admin".equals(rolUsuario);
    }
}
