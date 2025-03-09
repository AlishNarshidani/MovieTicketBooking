package com.example.majorprojectticketbookingsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSideMovieDetailsActivity extends AppCompatActivity {

    Movie movie;
    TextView txtMovieTitle, txtReleaseDate, txtGenre, txtRating, txtMovieDescription, txtMovieRuntime;
    ImageView imgMoviePoster;
    Button btnAddToTheatre;

    MovieApiService movieApiService;
    LinearLayout youtubeLinksContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_movie_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        movieApiService = new MovieApiService(this);

        movie = (Movie) getIntent().getSerializableExtra("movie");

        txtMovieTitle = findViewById(R.id.txtMovieTitle);
        txtReleaseDate = findViewById(R.id.txtReleaseDate);
        txtGenre = findViewById(R.id.txtGenre);
        txtRating = findViewById(R.id.txtRating);
        txtMovieDescription = findViewById(R.id.txtMovieDescription);
        txtMovieRuntime = findViewById(R.id.txtMovieRuntime);
        imgMoviePoster = findViewById(R.id.imgMoviePoster);
        btnAddToTheatre = findViewById(R.id.btnAddToTheatre);
        youtubeLinksContainer = findViewById(R.id.youtubeLinksContainer);


        if (movie != null) {
            String text = movie.getTitle() + " " + movie.getReleaseDate();
            Log.d("movie details", "onCreate: " + text);

            txtMovieTitle.setText(movie.getTitle());
            txtReleaseDate.setText("Release Date: "+movie.getReleaseDate());
            txtGenre.setText("Genres: "+movie.getGenreNames());
            txtRating.setText("Ratings: "+String.valueOf(movie.getVoteAverage()));
            txtMovieDescription.setText("Overview: "+movie.getOverview());

            Glide.with(EmployeeSideMovieDetailsActivity.this)
                    .load(movie.getPosterPath())
                    .error(R.drawable.cinemalogo)
                    .into(imgMoviePoster);

            fetchMovieTrailerAndTeaser(movie.getId());

        }


        btnAddToTheatre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get Theatre ID from SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                String theatreId = sharedPreferences.getString("theatreId", "");

                if (theatreId.isEmpty()) {
                    Toast.makeText(EmployeeSideMovieDetailsActivity.this, "Theatre ID not found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Start HallSelectionActivity and pass Theatre ID
                Intent intent = new Intent(EmployeeSideMovieDetailsActivity.this, EmployeeSideHallSelectionActivity.class);
                intent.putExtra("theatreId", theatreId);
                intent.putExtra("movie",movie);
                startActivity(intent);
            }
        });
    }

    public void fetchMovieTrailerAndTeaser(String movieId)
    {
        movieApiService.getMovieTrailerAndTeaser(new MovieApiService.DataCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {


                    Log.d("NEW_API_RESPONSE", response.toString());
                    // Extract runtime
                    String runtime = response.optString("runtime", "NA");
                    System.out.println("Movie Runtime: " + runtime + " minutes");
                    if(!runtime.equals("NA") && !runtime.equals("0")) {
                        txtMovieRuntime.setText("Runtime: " + runtime + " minutes");
                    } else {
                        txtMovieRuntime.setText("Runtime: NA");
                    }


                    List<String> videoLinks = new ArrayList<>();
                    if(response.has("videos"))
                    {
                        JSONObject videosObj = response.getJSONObject("videos");

                        if(videosObj.has("results"))
                        {
                            JSONArray resultsArray = videosObj.getJSONArray("results");

                            for (int i = 0; i < resultsArray.length(); i++) {
                                JSONObject video = resultsArray.getJSONObject(i);

                                String videoName = video.optString("name", ""); // Convert to lowercase for easier comparison
                                String videoKey = video.optString("key", "");
                                String videoType = video.optString("type", "");
                                boolean isOfficial = video.optBoolean("official", false);
                                String videoUrl = "https://www.youtube.com/watch?v=" + videoKey;

                                // Check if video is valid
                                if ((isOfficial || videoName.toLowerCase().contains("official")) && (videoType.equalsIgnoreCase("Trailer") || videoType.equalsIgnoreCase("Teaser"))) {
                                    videoLinks.add(videoUrl);

                                    addVideoButton(videoName, videoUrl);
                                }

                            }
                        }


                    }

                    if (videoLinks.isEmpty()) {
                        Log.d("videos","No valid trailers or teasers found.");
                    } else {
                        Log.d("videos", "Filtered Video Links:");
                        for (String link : videoLinks) {
                            Log.d("link", link + " ");
                        }
                    }



                }
                catch (JSONException e)
                {
                    Log.e("EmployeeSideMovieListPage getSearchedMovie catch", "Error parsing JSON", e);
                    Toast.makeText(EmployeeSideMovieDetailsActivity.this, "JSON parsing error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError error) {

            }
        },movieId);
    }



    private void addVideoButton(String videoTitle, String videoUrl) {
        // Find the container
        LinearLayout youtubeLinksContainer = findViewById(R.id.youtubeLinksContainer);

        // Create a new Button
        Button videoButton = new Button(this);
        videoButton.setText(videoTitle);
        videoButton.setTextSize(16);
        videoButton.setAllCaps(false);
        videoButton.setPadding(20, 10, 20, 10);
        videoButton.setBackgroundResource(R.drawable.red_rounded_button); // Custom button style
        videoButton.setTextColor(getResources().getColor(android.R.color.white));

        // Set click listener to open YouTube
        videoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            startActivity(intent);
        });

        // Add the button to the layout
        youtubeLinksContainer.addView(videoButton);

        // Add spacing between buttons
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) videoButton.getLayoutParams();
        params.setMargins(0, 20, 0, 10);
        videoButton.setLayoutParams(params);
    }



}