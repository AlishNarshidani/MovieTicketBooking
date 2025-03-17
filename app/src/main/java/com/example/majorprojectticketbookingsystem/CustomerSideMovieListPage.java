package com.example.majorprojectticketbookingsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CustomerSideMovieListPage extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_GPS = 101;

    FirebaseFirestore db;
    Set<String> uniqueMovieIds;
    List<Movie> movieList;

    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;

    AppCompatButton searchButton;
    EditText search_edit_text;
    TextView no_results_text;
    private ProgressBar progressBar;

    List<Movie> filteredMovieList;

    private String userCity = null; // Store user's city

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_side_movie_list_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchButton = findViewById(R.id.search_button);
        search_edit_text = findViewById(R.id.search_edit_text);
        no_results_text = findViewById(R.id.no_results_text);
        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns per row

        db = FirebaseFirestore.getInstance();
        uniqueMovieIds = new HashSet<>();
        movieList = new ArrayList<>();
        filteredMovieList = new ArrayList<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        //getListOfUpcomingMovies();
        checkLocationPermission();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String query = search_edit_text.getText().toString().trim();

                if(!query.isEmpty()) {
                    getListOfSearchedMovies();
                } else {
                    search_edit_text.setError("Please enter a movie name");
                    getListOfUpcomingMovies();
                }
            }
        });

        search_edit_text.setOnEditorActionListener((v, actionID, event) -> {
            if(actionID == EditorInfo.IME_ACTION_SEARCH) {
                String query = search_edit_text.getText().toString().trim();

                if(!query.isEmpty()) {
                    getListOfSearchedMovies();
                } else {
                    search_edit_text.setError("Please enter a movie name");
                    getListOfUpcomingMovies();
                }

                return true;
            }
            return false;
        });

    }

    // ✅ Handle Permission Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Location permission denied Try Allowing Manually!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("codes", "onActivityResult: "+"res: " +resultCode +"req: "+requestCode);
        if (requestCode == REQUEST_GPS) {
            checkGpsEnabled();
        }
    }

    private void checkGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is not enabled, prompt the user to turn it on
            promptForGps();
        } else {
            // GPS is enabled, proceed
            getUserLocation();
            //Toast.makeText(this, "already GPS ON", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Step 3: Request User Location
    private void getUserLocation() {
        progressBar.setVisibility(View.VISIBLE);
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(task -> {
                        Location location = task.getResult();
                        if (location != null) {
                            getCityName(location.getLatitude(), location.getLongitude());
                        } else {
                            Log.d("Location", "getUserLocation: No last known location, requesting new update...");
                            requestNewLocation();
                        }
                    });
        } catch (SecurityException e) {
            Log.e("Location", "Permission denied: " + e.getMessage());
            getListOfUpcomingMovies();
        }
    }

    // ✅ Step 4: Request Fresh Location If `getLastLocation()` is null
    private void requestNewLocation() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000) // High accuracy, 5 sec interval
                .setMinUpdateIntervalMillis(1000) // Minimum interval
                .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location latestLocation = locationResult.getLastLocation();
                    getCityName(latestLocation.getLatitude(), latestLocation.getLongitude());
                    fusedLocationProviderClient.removeLocationUpdates(this); // Stop updates after getting location
                } else {
                    Log.d("Location", "getUserLocation: Failed to get new location");
                    getListOfUpcomingMovies();
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        }
    }


    // ✅ Step 5: Reverse Geocode to Get City Name
    private void getCityName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                userCity = addresses.get(0).getLocality(); // Get city name
                Log.d("UserLocation", "City: " + userCity);
                SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userCity", userCity);
                editor.apply();
            }
        } catch (IOException e) {
            Log.e("Geocoder", "Error getting city name", e);
        }

        getListOfUpcomingMovies();
    }


    private void promptForGps() {
        new AlertDialog.Builder(this)
                .setMessage("GPS is required to continue. Please enable GPS.")
                .setCancelable(false)
                .setPositiveButton("Turn On", (dialog, id) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, REQUEST_GPS);
                })
                .setNegativeButton("No Thanks", (dialog, id) -> {
                    // Handle when user refuses to turn on GPS
                    Toast.makeText(this, "GPS is required to move further", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }


    // ✅ Step 1: Check Location Permission
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkGpsEnabled();
            //Toast.makeText(this, "Permission already given ", Toast.LENGTH_SHORT).show();
        } else {
            // ✅ Request location permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

        }
    }


    public void getListOfSearchedMovies() {
        String searchCriteria = search_edit_text.getText().toString().trim();

        // Check if the input is empty
        if (searchCriteria.isEmpty()) {
            search_edit_text.setError("Please enter a movie name");
            return; // Stop execution
        }

        // Check minimum character length (optional)
        if (searchCriteria.length() < 3) {
            search_edit_text.setError("Enter at least 3 characters");
            return;
        }

        filterMovies(searchCriteria);


    }


    private void filterMovies(String query) {
        filteredMovieList.clear(); // ✅ Clear previous filtered results

        if (!query.isEmpty()) {
            for (Movie movie : movieList) {
                if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredMovieList.add(movie); // ✅ Add matching movie
                }
            }
        }

        updateMovieUI(filteredMovieList); // ✅ Update RecyclerView
    }




    // ✅ Fetch upcoming shows
    public void getListOfUpcomingMovies()
    {
        Timestamp currentTimestamp = Timestamp.now();

        db.collection("shows")
                .whereGreaterThan("showStartTime", currentTimestamp) // ✅ Only future shows
                .get()
                .addOnSuccessListener(showSnapshots -> {
                    if (!showSnapshots.isEmpty()) {
                        uniqueMovieIds.clear();
                        movieList.clear();

                        List<String> theatreIds = new ArrayList<>();
                        Map<String, String> movieTheatreMap = new HashMap<>(); // Stores movieId -> theatreId

                        for (QueryDocumentSnapshot document : showSnapshots) {
                            String movieId = document.getString("movieId");
                            String theatreId = document.getString("theatreId");

                            if (movieId != null) {
                                //uniqueMovieIds.add(movieId); // ✅ Add to set (removes duplicates)
                                theatreIds.add(theatreId);
                                movieTheatreMap.put(movieId, theatreId);
                            }
                        }

//                        if (!uniqueMovieIds.isEmpty()) {
//                            fetchMovieDetails(uniqueMovieIds); // ✅ Fetch movie details
//                        } else {
//                            Log.d("Movies", "No unique movies found.");
//                        }
                        if (!theatreIds.isEmpty()) {
                            filterMoviesByCity(theatreIds, movieTheatreMap); // ✅ Fetch movie details
                        } else {
                            Log.d("Movies", "No nearby theatre found.");
                            Toast.makeText(this, "No nearby theatre has Shows", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.d("Movies", "No upcoming shows available.");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching shows", e));
    }


    // ✅ Fetch theatres & filter movies based on user city
    private void filterMoviesByCity(List<String> theatreIds, Map<String, String> movieTheatreMap) {
        Set<String> filteredMovieIds = new HashSet<>();
        int[] remainingCalls = {theatreIds.size()}; // To track async calls

        for (String theatreId : theatreIds) {
            db.collection("theatre").document(theatreId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String location = documentSnapshot.getString("location"); // Get address
                            Log.d("TheatreLocation", "Theatre: " + theatreId + ", Location: " + location);

                            if (location != null && userCity != null && location.toLowerCase().contains(userCity.toLowerCase())) {
                                // ✅ Get movieId associated with this theatre
                                for (Map.Entry<String, String> entry : movieTheatreMap.entrySet()) {
                                    if (entry.getValue().equals(theatreId)) {
                                        filteredMovieIds.add(entry.getKey());
                                    }
                                }
                            }
                        }

                        remainingCalls[0]--;
                        if (remainingCalls[0] == 0) { // ✅ All theatres processed
                            if (!filteredMovieIds.isEmpty()) {
                                fetchMovieDetails(filteredMovieIds); // ✅ Fetch movie details
                            } else {
                                Log.d("Movies", "No movies found in user's city.");
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching theatre details", e));
        }
    }



    // ✅ Function to fetch movie details
    private void fetchMovieDetails(Set<String> movieIds) {
        for (String movieId : movieIds) {
            Log.d("TAG", "fetchMovieDetails: "+movieId);
            db.collection("movies")
                    .document(movieId)
                    .get()
                    .addOnSuccessListener(movieSnapshot -> {
                        if (movieSnapshot.exists()) {
                            // ✅ Extract movie data
                            String id = movieSnapshot.getString("id");
                            String title = movieSnapshot.getString("title");
                            String originalTitle = movieSnapshot.getString("original_title");
                            String releaseDate = movieSnapshot.getString("release_date");
                            String overview = movieSnapshot.getString("overview");
                            double popularity = movieSnapshot.getDouble("popularity");
                            String originalLanguage = movieSnapshot.getString("original_language");
                            double voteAverage = movieSnapshot.getDouble("vote_average");
                            int voteCount = movieSnapshot.getLong("vote_count").intValue();
                            boolean adult = Boolean.TRUE.equals(movieSnapshot.getBoolean("adult"));
                            boolean video = Boolean.TRUE.equals(movieSnapshot.getBoolean("video"));
                            String posterPath = movieSnapshot.getString("poster_path");
                            String backdropPath = movieSnapshot.getString("backdrop_path");
                            String genreNames = movieSnapshot.getString("genre_names");

                            // ✅ Create Movie object
                            Movie movie = new Movie(id, title, originalTitle, releaseDate, overview, popularity, originalLanguage,
                                    voteAverage, voteCount, adult, video, posterPath, backdropPath, genreNames);

                            movieList.add(movie); // ✅ Add movie to list

                            if (movieList.size() == movieIds.size()) {
                                updateMovieUI(movieList); // ✅ Update UI once all movies are fetched
                            }

                        } else {
                            Log.d("Movies", "Movie not found: " + movieId);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Error fetching movie: " + movieId, e));
        }
    }


    // ✅ Function to update RecyclerView
    private void updateMovieUI(List<Movie> movies) {
        progressBar.setVisibility(View.GONE);
        // ✅ Update RecyclerView or any UI component
        movieAdapter = new MovieAdapter(CustomerSideMovieListPage.this, movies, "customer");
        recyclerView.setAdapter(movieAdapter);
    }



}