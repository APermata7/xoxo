package com.example.xoxo;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StarredCinemaActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private StarredCinemaAdapter adapter;
    private List<Bioskop> starredCinemas = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starred_cinema);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerViewBioskop);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StarredCinemaAdapter(starredCinemas);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadStarredCinemas();
    }

    private void loadStarredCinemas() {
        db.collection("users").document(userId).collection("favorites") // Ubah dari starred_cinemas ke favorites
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        starredCinemas.clear();
                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String cinemaId = document.getId();
                            tasks.add(getCinemaDetails(cinemaId));
                        }

                        Tasks.whenAllComplete(tasks)
                                .addOnCompleteListener(combinedTask -> {
                                    adapter.notifyDataSetChanged();

                                    if (starredCinemas.isEmpty()) {
                                        Toast.makeText(this, "Tidak ada bioskop favorit", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Gagal memuat bioskop favorit: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Task<DocumentSnapshot> getCinemaDetails(String cinemaId) {
        return db.collection("cinemas").document(cinemaId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Bioskop cinema = document.toObject(Bioskop.class);
                        if (cinema != null) {
                            cinema.setId(document.getId());
                            starredCinemas.add(cinema);
                        }
                    }
                });
    }
}