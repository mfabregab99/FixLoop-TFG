package com.marcal.fixloop;

import android.os.Bundle;
import android.view.Menu;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.marcal.fixloop.databinding.ActivityMainBinding;
import com.marcal.fixloop.utils.SessionManager;

/**
 * Activitat principal que conté la navegació inferior (BottomNavigationView)
 * i el contenidor per als diferents Fragments (Home, Inbox, Profile, etc.)
 * Gestiona la visibilitat dels menús segons el rol de l'usuari
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Configuració de la navegació amb Android Jetpack Navigation
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // --- Lògica de rols  ---

        // Recuperem el tipus d'usuari utilitzant el SessionManager
        String userType = sessionManager.getUserType();

        // Si és reparador, amaguem opcions que no són per a ell
        if ("reparador".equalsIgnoreCase(userType)) {
            Menu menu = navView.getMenu();

            menu.removeItem(R.id.navigation_favorits); // El reparador no guarda favorits
            menu.removeItem(R.id.navigation_crear);    // El reparador no crea sol·licituds
        }
    }
}