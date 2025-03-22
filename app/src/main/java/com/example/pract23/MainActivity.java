package com.example.pract23;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private TextView locationText;
    private TextView addressText;
    private Geocoder geocoder;
    private MapView mapView;
    private LocationManager locationManager;
    private Point currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey("a4e2c9cf-c919-49c8-b481-bdf431450cb8"); // Замените на ваш Yandex API-ключ
        MapKitFactory.initialize(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationText = findViewById(R.id.locationText);
        addressText = findViewById(R.id.addressText);
        mapView = findViewById(R.id.mapview);
        geocoder = new Geocoder(this, Locale.getDefault());

        locationManager = MapKitFactory.getInstance().createLocationManager();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(new LocationListener() {
                @Override
                public void onLocationUpdated(Location location) {
                    double latitude = location.getPosition().getLatitude();
                    double longitude = location.getPosition().getLongitude();
                    currentLocation = new Point(latitude, longitude);

                    locationText.setText(String.format("Location: Lat: %.6f, Long: %.6f",
                            latitude, longitude));

                    mapView.getMap().move(
                            new CameraPosition(currentLocation, 15.0f, 0.0f, 0.0f),
                            new Animation(Animation.Type.SMOOTH, 1), null);
                    mapView.getMap().getMapObjects().addPlacemark(currentLocation);

                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            String addressLine = address.getAddressLine(0);
                            addressText.setText("Address: " + (addressLine != null ? addressLine : "Unknown"));
                        } else {
                            addressText.setText("Address: Unable to determine");
                        }
                    } catch (IOException e) {
                        addressText.setText("Address: Error retrieving address");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLocationStatusUpdated(LocationStatus locationStatus) {
                    if (locationStatus == LocationStatus.NOT_AVAILABLE) {
                        locationText.setText("Location not available");
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                locationText.setText("Location permission denied");
                addressText.setText("Address: Permission denied");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        MapKitFactory.getInstance().onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }
}