package com.soumio.inceptiontutorial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;


public class Map extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap map;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private  static final int REQUEST_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
    }

    private void fetchLastLocation()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null)
                {
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

                    mapFragment.getMapAsync(Map.this);
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng liveLoc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLng Tvrtko = new LatLng(44.538031, 18.678409);
        LatLng Zena = new LatLng(44.539970, 18.681975);
        LatLng Fontana = new LatLng(44.539318, 18.674973);
        LatLng Barok = new LatLng(44.538993, 18.674754);
        LatLng Pcrkva = new LatLng(44.537904, 18.679962);
        LatLng Fet = new LatLng(44.537768, 18.674891);
        LatLng IsmetMesa = new LatLng(44.537069, 18.678215);
        LatLng Kapija = new LatLng(44.538556, 18.676807);
        LatLng Test = new LatLng(44.544781, 18.661566);

        ArrayList<Marker> loc = new ArrayList<Marker>();
        map.addMarker(new MarkerOptions().position(liveLoc).title("Your position").icon(BitmapDescriptorFactory.fromResource(R.drawable.myloc)));
        Marker Zen = map.addMarker(new MarkerOptions().position(Zena).title("Zena panonsko").icon(BitmapDescriptorFactory.fromResource(R.drawable.loc)));
        Marker Tv = map.addMarker(new MarkerOptions().position(Tvrtko).title("Tvrtko I Kotromanic").icon(BitmapDescriptorFactory.fromResource(R.drawable.loc)));
        Marker Font = map.addMarker(new MarkerOptions().position(Fontana).title("Fontana Trg slobode").icon(BitmapDescriptorFactory.fromResource(R.drawable.loc)));
        Marker Bar = map.addMarker(new MarkerOptions().position(Barok).title("Barok zgrada").icon(BitmapDescriptorFactory.fromResource(R.drawable.loc)));
        Marker Pcrv = map.addMarker(new MarkerOptions().position(Pcrkva).title("Pravoslavna crkva").icon(BitmapDescriptorFactory.fromResource(R.drawable.loc)));
        Marker Ft = map.addMarker(new MarkerOptions().position(Fet).title("FET").icon(BitmapDescriptorFactory.fromResource(R.drawable.loc)));
        Marker IM = map.addMarker(new MarkerOptions().position(IsmetMesa).title("Ismet i Mesa").icon(BitmapDescriptorFactory.fromResource(R.drawable.loc)));
        Marker Kap = map.addMarker(new MarkerOptions().position(Kapija).title("Kapija").icon(BitmapDescriptorFactory.fromResource(R.drawable.loc)));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(liveLoc,16));

        loc.add(0,Zen);
        loc.add(1,Tv);
        loc.add(2,Font);
        loc.add(3,Bar);
        loc.add(4,Pcrv);
        loc.add(5,Ft);
        loc.add(6,IM);
        loc.add(7,Kap);
        float[] results = new float[7];
        LatLng latLng = new LatLng(liveLoc.latitude, liveLoc.longitude);
        map.addCircle(new CircleOptions()
                .center(latLng)
                .radius(300)
                .strokeWidth(0f)
                .fillColor(0x550000FF));
        double pos1 = liveLoc.latitude;
        double pos2 = liveLoc.longitude;
        for(Marker x : loc){
            int i = 0;

            double dis1 = x.getPosition().latitude;
            double dis2 = x.getPosition().longitude;
            Location.distanceBetween(pos1, pos2, dis1, dis2,results);
            if(results[i] > 300) {
                x.setVisible(false);
            }
            i++;
        }

    }


}
