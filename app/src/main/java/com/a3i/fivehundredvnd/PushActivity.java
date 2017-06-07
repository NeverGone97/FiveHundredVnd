package com.a3i.fivehundredvnd;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.a3i.fivehundredvnd.model.FakeData;
import com.a3i.fivehundredvnd.model.User1;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PushActivity extends AppCompatActivity implements ChildEventListener, ValueEventListener, OnMapReadyCallback,
        LocationListener, View.OnClickListener {
    public static final String ALL_LOCATION = "all_location";
    public static final String MESSAGES_CHILD1 = "messages1";
    public static final String MESSAGES_CHILD2 = "messages2";
    public static final String MESSAGES_CHILD3 = "messages3";
    private ChildEventListener mListener;
    Handler handler;
    FakeData fakeData;
    GeoFire geoFire;
    User1 thisUser;
    boolean stop=false;
    DatabaseReference firebaseDatabase;
    TextView tvYourName;
    SupportMapFragment mapFragment;
    GeoQuery geoQuery;
    private Circle searchCircle;
    private Map<String, Marker> markers;
    private GoogleMap mMap;
    Location myLocation = null;
    LocationManager locationManager;
    MarkerOptions markerOptions = new MarkerOptions();
    Button btTurnLeft;
    Button btTurnRight;
    Switch btTurnOnSimulate;
    TestThread testThread;
    Thread thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        btTurnLeft = (Button) findViewById(R.id.btTurnLeft);
        btTurnLeft.setVisibility(View.VISIBLE);
        btTurnRight = (Button) findViewById(R.id.btTurnRight);
        btTurnRight.setVisibility(View.VISIBLE);
        tvYourName = (TextView) findViewById(R.id.tvYourName);
        btTurnOnSimulate = (Switch) findViewById(R.id.btTurnOnSimulate);
        btTurnLeft.setOnClickListener(this);
        btTurnRight.setOnClickListener(this);
        testThread=new TestThread();

        fakeData = new FakeData();
        thisUser = fakeData.getRandomUser();
        tvYourName.setText("Your name: " + thisUser.getName());
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        // Clear the input
        flagZoomfocus = false;
        firebaseDatabase.addChildEventListener(this);
        firebaseDatabase.addValueEventListener(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapFragment.getMapAsync(this);
        geoFire = new GeoFire(firebaseDatabase.child(ALL_LOCATION));
        geoQuery = geoFire.queryAtLocation(new GeoLocation(0, 0), 4);
        updateThread();
        //-----------------------------------
        handler=new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
//                Toast.makeText(PushActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        };
            markers = new HashMap<>();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {
                Log.d(TAG, "onKeyEntered: " + key + "_" + location.toString());
                //if (key != String.valueOf(thisUser.getId())) {
                if (true) {
                    //      Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
                    final Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.latitude, location.longitude))
                            .title(fakeData.getListUser().get(Integer.parseInt(key)).getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car15doc)));
                    DatabaseReference userNode = firebaseDatabase.child(ALL_LOCATION + "/" + key);
                    userNode.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "onDataChange: user " + dataSnapshot.toString());
                            if (marker != null) {
                                if (dataSnapshot.child("bear").getValue() != null) {
                                    float direction = Float.parseFloat(dataSnapshot.child("bear").getValue().toString());
                                    marker.setRotation(direction);
                                }
                                markers.put(key, marker);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }
            @Override
            public void onKeyExited(String key) {
                Log.d(TAG, "onKeyExited: " + key + "_");
                Marker marker = markers.get(key);
                if (marker != null) {
                    marker.remove();
                    markers.remove(key);
                }
            }

            @Override
            public void onKeyMoved(final String key, final GeoLocation location) {
                Log.d(TAG, "onKeyMoved: " + key + "_" + location.toString());
               // if (key != String.valueOf(thisUser.getId())) {
                if (true) {
                    DatabaseReference userNode = firebaseDatabase.child(ALL_LOCATION + "/" + key);
                    userNode.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "onDataChange: user " + dataSnapshot.toString());
                            Marker marker = markers.get(key);
                            if (marker != null) {
                                marker.setPosition(new LatLng(location.latitude, location.longitude));
                                if (dataSnapshot.child("bear").getValue() != null) {
                                    float direction = Float.parseFloat(dataSnapshot.child("bear").getValue().toString());
                                    marker.setRotation(direction);
                                }


                                markers.put(key, marker);
                                   marker.setPosition(new LatLng(location.latitude, location.longitude));
                             //   animateMarkerTo(marker, location.latitude, location.longitude);
//
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
        btTurnOnSimulate.setChecked(false);
        btTurnOnSimulate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            //    simulateMovement(isChecked);
            if(isChecked){
                thongbao("ok bat roi");
//                updateThread();

            }else {
                thongbao("ok tat roi" );
//                updateMylocation(myLocation);
            }


            }
        });
    }
    Location startSimulate=null;
    double acceleration=0.00001;


    void simulateMovement(boolean isSimulate) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (isSimulate) {
            locationManager.removeUpdates(this);
        }else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);
        }
    }
    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 3000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final LatLng startPosition = marker.getPosition();
        handler.post(new Runnable() {
            @Override
            public void run() {
                float elapsed = SystemClock.uptimeMillis() - start;
                float t = elapsed / DURATION_MS;
                float v = interpolator.getInterpolation(t);

                double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                marker.setPosition(new LatLng(currentLat, currentLng));

                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    String TAG = "xxx";

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        //   Log.d(TAG, "onChildAdded: " + s);

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        //    Log.d(TAG, "onChildChanged: " + dataSnapshot.toString());
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d(TAG, "onChildRemoved: ");
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, "onChildMoved: " + s);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        //   Location post = dataSnapshot.child("message").getValue(Location.class);
        //  Log.d(TAG, "onDataChange: " + new Gson().toJson(post));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "onCancelled: ");
    }

    GeoHash geoHash = null;
    GeoHash geoHash2 = null;
    GeoHash geoHash3 = null;
    GeoHash geoHash4= null;
    GeoHash geoHash5= null;

    float bearing = 0;

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: ");
        geoQuery.setCenter(new GeoLocation(location.getLatitude(), location.getLongitude()));
  //      updateLocation(location);
//        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(thisUser.getId())).child("location").setValue(myLocation);
//        geoFire.setLocation(String.valueOf(thisUser.getId()), new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
//            @Override
//            public void onComplete(String key, DatabaseError error) {
//                if (error != null) {
//                    Log.d(TAG, "There was an error saving the location to GeoFire: " + error);
//                } else {
//                    Log.d(TAG, "Location saved on server successfully!");
//                }
//            }
//        });


    }

    public void updateLocation(Location location) {

        geoQuery.setCenter(new GeoLocation(location.getLatitude(), location.getLongitude()));
        if (myLocation != null) {
            bearing = myLocation.bearingTo(location);
            if (Math.abs(bearing) > 10) {
                firebaseDatabase.child(ALL_LOCATION + "/" + String.valueOf(thisUser.getId()) + "/bear").setValue(bearing);
            }

        } else {
            firebaseDatabase.child(ALL_LOCATION + "/" + String.valueOf(thisUser.getId()) + "/bear").setValue(0);
        }
        myLocation = location;
        updateMylocation(location);
        geoHash = new GeoHash(location.getLatitude(), location.getLongitude());
        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(thisUser.getId())).child("l/0").setValue(myLocation.getLatitude());
        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(thisUser.getId())).child("l/1").setValue(myLocation.getLongitude());
        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(thisUser.getId())).child("g").setValue(geoHash.getGeoHashString());
    }


    public void updateThread() {
        thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop) {
                    for (int i = 0; i < testThread.getLocations().size(); i++) {
                        geoHash = new GeoHash(testThread.getLocations().get(i).latitude, testThread.getLocations().get(i).longitude);
//                        geoHash2 = new GeoHash(testThread.getLocations1().get(i).latitude, testThread.getLocations1().get(i).longitude);
//                        geoHash3 = new GeoHash(testThread.getLocations2().get(i).latitude, testThread.getLocations2().get(i).longitude);
//                        geoHash4 = new GeoHash(testThread.getLocations3().get(i).latitude, testThread.getLocations3().get(i).longitude);
//                        geoHash5 = new GeoHash(testThread.getLocations4().get(i).latitude, testThread.getLocations4().get(i).longitude);
                        if (i!=0 ) {

                            Location currlocation=new Location(LocationManager.NETWORK_PROVIDER);
                            currlocation.setLatitude(testThread.getLocations().get(i).latitude);
                            currlocation.setLongitude(testThread.getLocations().get(i).longitude);

                            Location previousLocation=new Location(LocationManager.NETWORK_PROVIDER);
                            previousLocation.setLatitude(testThread.getLocations().get(i-1).latitude);
                            previousLocation.setLongitude(testThread.getLocations().get(i-1).longitude);

                            bearing = previousLocation.bearingTo(currlocation);
                            if (Math.abs(bearing) > 10) {
                                firebaseDatabase.child(ALL_LOCATION + "/" + String.valueOf(1) + "/bear").setValue(bearing);
                            }else {
                                firebaseDatabase.child(ALL_LOCATION + "/" + String.valueOf(1) + "/bear").setValue(0);
                            }

                        } else {
                            firebaseDatabase.child(ALL_LOCATION + "/" + String.valueOf(thisUser.getId()) + "/bear").setValue(0);
                        }
                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(1)).child("l/0").setValue(testThread.getLocations().get(i).latitude);
                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(1)).child("l/1").setValue(testThread.getLocations().get(i).longitude);
                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(1)).child("g").setValue(geoHash.getGeoHashString());

//                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(3)).child("l/0").setValue(testThread.getLocations2().get(i).latitude);
//                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(3)).child("l/1").setValue(testThread.getLocations2().get(i).longitude);
//                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(3)).child("g").setValue(geoHash3.getGeoHashString());
//
//                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(4)).child("l/0").setValue(testThread.getLocations3().get(i).latitude);
//                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(4)).child("l/1").setValue(testThread.getLocations3().get(i).longitude);
//                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(4)).child("g").setValue(geoHash4.getGeoHashString());
//
//                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(5)).child("l/0").setValue(testThread.getLocations4().get(i).latitude);
//                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(5)).child("l/1").setValue(testThread.getLocations4().get(i).longitude);
//                        firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(5)).child("g").setValue(geoHash5.getGeoHashString());

                        Log.d(TAG, "updateThread: " + testThread.getLocations().get(i).latitude);
                        SystemClock.sleep(3000);
                        Message message = handler.obtainMessage();
                        message.obj = firebaseDatabase;
                        handler.sendMessage(message);
                    }
                }
            }

        });
            thread.start();


//        for(int i=0;i<testThread.getLocations().size();i++){
//            geoHash = new GeoHash(testThread.getLocations().get(i).latitude, testThread.getLocations().get(i).longitude);
//            firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(1)).child("l/0").setValue(testThread.getLocations().get(i).latitude);
//            firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(1)).child("l/1").setValue(testThread.getLocations().get(i).longitude);
//            firebaseDatabase.child(ALL_LOCATION).child(String.valueOf(1)).child("g").setValue(geoHash.getGeoHashString());
//            Log.d(TAG, "updateThread: "+testThread.getLocations().get(i).latitude);
//            SystemClock.sleep(5000);
//        }

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3, 3, this);
        if (locationManager != null) {
            if (myLocation == null) {
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (myLocation == null) {
                myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
//            updateMylocation(myLocation);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // locationManager.removeUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        thread.interrupt();
    }

    boolean flagZoomfocus = false;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        Log.d(TAG, "onMapReady: ");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onMapReady: deny");
            return;
        }
        Log.d(TAG, "onResume: success1");
      //  mMap.setMyLocationEnabled(true);
        updateMylocation(myLocation);
    }

    public void updateMylocation(Location location) {
        //    Log.d(TAG, "updateMylocation: ");
        if (!flagZoomfocus && mMap != null && location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
            flagZoomfocus = true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btTurnLeft:
//                stop=false;
//                updateThread();
                break;
            case R.id.btTurnRight:
//                stop=true;
//                updateThread();
//            thread.interrupt();
                break;


        }

    }
public void thongbao(String s){
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
}

}
