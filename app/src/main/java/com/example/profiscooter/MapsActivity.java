package com.example.profiscooter;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.profiscooter.databinding.ActivityMapsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private DatabaseReference locationReference;
    private FirebaseUser user;
    private String userID;
    LatLng scooterLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        user = FirebaseAuth.getInstance().getCurrentUser();
        locationReference = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("location");

        userID = user.getUid();






    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        locationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserLocation userLocation = snapshot.getValue(UserLocation.class);

                if(userLocation != null) {

                    if(userLocation.latitude != 0 && userLocation.longitude != 0) {
                        double latitude = userLocation.latitude;
                        double longitude = userLocation.longitude;
                        scooterLocation = new LatLng(latitude, longitude);
                        Toast.makeText(MapsActivity.this, "Date: " + userLocation.date, Toast.LENGTH_SHORT).show();
                        if (scooterLocation != null) {
                            mMap.addMarker(new MarkerOptions().position(scooterLocation).title("Scooter location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(scooterLocation, 15));
                        }
                    }

                }
                if(userLocation == null) {
                    Toast.makeText(MapsActivity.this, "GPS not connected", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapsActivity.this, "Something wrong happened!", Toast.LENGTH_LONG).show();
            }
        });


        // Add a marker in Sydney and move the camera

    }
}