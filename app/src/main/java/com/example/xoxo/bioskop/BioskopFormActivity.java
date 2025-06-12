package com.example.xoxo.bioskop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.xoxo.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BioskopFormActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etCinemaName, etCinemaAddress, etCinemaInfo, etCinemaPhone;
    private Spinner spinnerCity;
    private ImageView imgCinema, btnBack;
    private Button btnSave, btnCancel;

    private BioskopAdminManager adminManager;
    private Bioskop editingBioskop;
    private Uri selectedImageUri;
    private boolean isEditMode = false;
    private List<String> cities = new ArrayList<>(Arrays.asList("Jakarta", "Bandung", "Surabaya", "Malang", "Yogyakarta"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bioskop_form);

        initializeViews();
        setupSpinner();

        adminManager = new BioskopAdminManager(this);

        // Check if we're in edit mode
        if (getIntent().hasExtra("CINEMA_EDIT")) {
            isEditMode = true;
            editingBioskop = (Bioskop) getIntent().getSerializableExtra("CINEMA_EDIT");
            populateFormWithData();
        }

        setupListeners();
    }

    private void initializeViews() {
        etCinemaName = findViewById(R.id.etCinemaName);
        etCinemaAddress = findViewById(R.id.etCinemaAddress);
        etCinemaInfo = findViewById(R.id.etCinemaInfo);
        etCinemaPhone = findViewById(R.id.etCinemaPhone);
        spinnerCity = findViewById(R.id.spinnerCityForm);
        imgCinema = findViewById(R.id.imgCinema);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupSpinner() {
        // Get all cities from Firestore
        FirebaseFirestore.getInstance().collection("cities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        cities.clear();
                        for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                            String city = queryDocumentSnapshots.getDocuments().get(i).getString("name");
                            if (city != null) {
                                cities.add(city);
                            }
                        }
                        setupCityAdapter();
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to default cities
                    setupCityAdapter();
                });
    }

    private void setupCityAdapter() {
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, cities);
        spinnerCity.setAdapter(cityAdapter);

        // If in edit mode, select the city of the cinema being edited
        if (isEditMode && editingBioskop != null) {
            int position = cities.indexOf(editingBioskop.getCity());
            if (position >= 0) {
                spinnerCity.setSelection(position);
            }
        }
    }

    private void populateFormWithData() {
        etCinemaName.setText(editingBioskop.getNama());
        etCinemaAddress.setText(editingBioskop.getAddress());
        etCinemaInfo.setText(editingBioskop.getInfo());
        etCinemaPhone.setText(editingBioskop.getPhoneNumber());

        // Load image if available
        if (editingBioskop.getImageUrl() != null && !editingBioskop.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(editingBioskop.getImageUrl())
                    .placeholder(R.drawable.ic_cinema_placeholder)
                    .error(R.drawable.ic_cinema_placeholder)
                    .into(imgCinema);
        }

        btnSave.setText("Update Cinema");
    }

    private void setupListeners() {
        imgCinema.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> saveOrUpdateCinema());

        btnCancel.setOnClickListener(v -> finish());

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(imgCinema);
        }
    }

    private void saveOrUpdateCinema() {
        String name = etCinemaName.getText().toString().trim();
        String address = etCinemaAddress.getText().toString().trim();
        String info = etCinemaInfo.getText().toString().trim();
        String phone = etCinemaPhone.getText().toString().trim();
        String city = spinnerCity.getSelectedItem().toString();

        if (name.isEmpty() || address.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Nama, alamat, dan kota tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode && editingBioskop != null) {
            // Update existing cinema
            Bioskop updatedBioskop = editingBioskop.clone();
            updatedBioskop.setNama(name);
            updatedBioskop.setAddress(address);
            updatedBioskop.setInfo(info);
            updatedBioskop.setPhoneNumber(phone);
            updatedBioskop.setCity(city);

            adminManager.updateBioskop(updatedBioskop, selectedImageUri, new BioskopAdminManager.BioskopOperationCallback() {
                @Override
                public void onSuccess(Bioskop bioskop) {
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(BioskopFormActivity.this, "Gagal memperbarui bioskop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new cinema
            Bioskop newBioskop = new Bioskop(null, name, city, address, info, phone);

            adminManager.addBioskop(newBioskop, selectedImageUri, new BioskopAdminManager.BioskopOperationCallback() {
                @Override
                public void onSuccess(Bioskop bioskop) {
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(BioskopFormActivity.this, "Gagal menambahkan bioskop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}