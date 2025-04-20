package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BioskopActivity extends AppCompatActivity implements BioskopAdapter.BioskopFavoriteListener {

    private Spinner spinnerCity;
    private RecyclerView recyclerView;
    private BioskopAdapter adapter;

    private static Map<String, List<Bioskop>> bioskopPerKota = new HashMap<>();
    private String currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bioskop);

        ImageView headerProfileIcon = findViewById(R.id.bioskop_profile);
        headerProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(BioskopActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                overridePendingTransition(0, 0);
            }
        });

        spinnerCity = findViewById(R.id.spinnerCity);
        recyclerView = findViewById(R.id.recyclerViewBioskop);

        if (bioskopPerKota.isEmpty()) {
            initializeData();
        }

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>(bioskopPerKota.keySet()));
        spinnerCity.setAdapter(cityAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCity = parent.getItemAtPosition(position).toString();
                List<Bioskop> bioskops = bioskopPerKota.get(currentCity);
                adapter = new BioskopAdapter(bioskops, BioskopActivity.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentCity != null) {
            List<Bioskop> bioskops = bioskopPerKota.get(currentCity);
            adapter = new BioskopAdapter(bioskops, this);
            recyclerView.setAdapter(adapter);
        }
    }

    private void initializeData() {
        List<Bioskop> malangCinemas = new ArrayList<>();
        malangCinemas.add(new Bioskop("Araya XXI", false, "Jalan Blimbing Indah Megah no. 2, Araya Mall, Lantai 3", "0812-3456-7890"));
        malangCinemas.add(new Bioskop("Dieng", false, "Jalan Dieng no. 15, Dieng Plaza, Lantai 1", "0811-2345-6789"));
        malangCinemas.add(new Bioskop("Lippo Plaza Batu Cinepolis", false, "Jalan Diponegoro no. 78, Lippo Plaza Batu, Lantai 2", "0813-4567-8901"));
        malangCinemas.add(new Bioskop("Malang City Point CGV", false, "Jalan Tenes no. 12, Malang City Point, Lantai 4", "0814-5678-9012"));
        malangCinemas.add(new Bioskop("Malang Town Square Cinepolis", false, "Jalan Veteran no. 8, Malang Town Square, Lantai Upper Ground, Unit UG-03", "0815-6789-0123"));
        malangCinemas.add(new Bioskop("Transmart Mx Mall XXI", false, "Jalan Veteran no. 15, Transmart Mx Mall, Lantai 3", "0817-8901-2345"));
        malangCinemas.add(new Bioskop("Mandala", false, "Jalan Mandala no. 9, Daerah Mandala, Lantai 2", "0817-8901-2222"));
        bioskopPerKota.put("Malang", malangCinemas);

        List<Bioskop> surabayaCinemas = new ArrayList<>();
        surabayaCinemas.add(new Bioskop("Tunjungan Plaza XXI", false, "Jalan Basuki Rahmat no. 8-12, Tunjungan Plaza, Lantai 4", "0818-9012-3456"));
        surabayaCinemas.add(new Bioskop("Pakuwon Mall CGV", false, "Jalan Puncak Indah Lontar no. 2, Pakuwon Mall, Lantai 3", "0819-0123-4567"));
        surabayaCinemas.add(new Bioskop("Galaxy Mall Cinepolis", false, "Jalan Dharmahusada Indah Timur no. 35-37, Galaxy Mall, Lantai 2", "0820-1234-5678"));
        bioskopPerKota.put("Surabaya", surabayaCinemas);

        List<Bioskop> blitarCinemas = new ArrayList<>();
        blitarCinemas.add(new Bioskop("Blitar Square XXI", false, "Jalan Merdeka no. 39, Blitar Square, Lantai 2", "0821-2345-6789"));
        bioskopPerKota.put("Blitar", blitarCinemas);
    }

    @Override
    public void onFavoriteChanged(Bioskop bioskop, boolean isFavorite) {
    }

    public static Bioskop getBioskopByName(String name) {
        for (List<Bioskop> cinemaList : bioskopPerKota.values()) {
            for (Bioskop bioskop : cinemaList) {
                if (bioskop.getNama().equals(name)) {
                    return bioskop;
                }
            }
        }
        return null;
    }

    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.home) {
            Intent intent = new Intent(BioskopActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.bioskop) {
            Toast.makeText(this, "Kamu sedang di halaman Bioskop", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.profile || id == R.id.bioskop_profile) {
            Intent intent = new Intent(BioskopActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }
}