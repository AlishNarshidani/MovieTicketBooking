package com.example.majorprojectticketbookingsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSideMovieListPage extends AppCompatActivity {

    MovieApiService movieApiService;
    private List<Movie> movieList;
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;

    AppCompatButton searchButton;
    EditText search_edit_text;
    TextView no_results_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_movie_list_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        movieApiService = new MovieApiService(this);
        searchButton = findViewById(R.id.search_button);
        search_edit_text = findViewById(R.id.search_edit_text);
        no_results_text = findViewById(R.id.no_results_text);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns per row

        movieList = new ArrayList<>();

        getListOfUpcomingMovies();

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

    public void getListOfSearchedMovies()
    {
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

        movieApiService.getSearchedMovie(new MovieApiService.DataCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {


                    Log.d("NEW_API_RESPONSE", response.toString());


                    if(response.has("results"))
                    {
                        JSONArray jsonArray = response.getJSONArray("results");
                        movieList.clear();

                        // Show message if no results found
                        if (movieList.isEmpty()) {
                            no_results_text.setVisibility(View.VISIBLE);
                            no_results_text.setText("Sorry, try another search.");
                        } else {
                            no_results_text.setVisibility(View.GONE);
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {

                            no_results_text.setVisibility(View.VISIBLE);
                            no_results_text.setText("Try searching more specifically if the result you wanted is not found.");

                            JSONObject movieObj = jsonArray.getJSONObject(i);

                            // Extracting all available fields
                            String id = movieObj.optString("id", "NA");
                            String title = movieObj.optString("title", "No Title");
                            String originalTitle = movieObj.optString("original_title", "No Original Title");
                            String releaseDate = movieObj.optString("release_date", "Unknown Date");
                            String overview = movieObj.optString("overview", "No Overview Available");
                            double popularity = movieObj.optDouble("popularity", 0.0);
                            String language = movieObj.optString("original_language", "Unknown");
                            double voteAverage = movieObj.optDouble("vote_average", 0.0);
                            int voteCount = movieObj.optInt("vote_count", 0);
                            boolean isAdult = movieObj.optBoolean("adult", false);
                            boolean hasVideo = movieObj.optBoolean("video", false);

                            // Handling poster and backdrop images
                            String posterPath = movieObj.optString("poster_path", "");
                            String backdropPath = movieObj.optString("backdrop_path", "");

                            // Handling Genre IDs
                            JSONArray genreArray = movieObj.getJSONArray("genre_ids");
                            List<Integer> genreIds = new ArrayList<>();
                            for (int j = 0; j < genreArray.length(); j++) {
                                genreIds.add(genreArray.getInt(j));
                            }

                            // Add to movie list only if there's a valid poster
                            if (!posterPath.isEmpty()) {

                                posterPath = IMAGE_BASE_URL + posterPath;
                                backdropPath = IMAGE_BASE_URL + backdropPath;

                                movieList.add(new Movie(id, title, originalTitle, releaseDate, overview, popularity, language,
                                        voteAverage, voteCount, isAdult, hasVideo, posterPath, backdropPath, genreIds));
                            }
                        }

                        movieAdapter = new MovieAdapter(EmployeeSideMovieListPage.this, movieList, "employee");
                        recyclerView.setAdapter(movieAdapter);
                    }


                }
                catch (JSONException e)
                {
                    Log.e("EmployeeSideMovieListPage getSearchedMovie catch", "Error parsing JSON", e);
                    Toast.makeText(EmployeeSideMovieListPage.this, "JSON parsing error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(EmployeeSideMovieListPage.this, "API Request Failed", Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", error.toString());
            }
        }, searchCriteria);
    }

    public void getListOfUpcomingMovies()
    {
        movieApiService.getUpcomingMovies(new MovieApiService.DataCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    Log.d("NEW_API_RESPONSE", response.toString());


                    if(response.has("results"))
                    {
                        JSONArray jsonArray = response.getJSONArray("results");
                        movieList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject movieObj = jsonArray.getJSONObject(i);

                            // Extracting all available fields
                            String id = movieObj.optString("id", "NA");
                            String title = movieObj.optString("title", "No Title");
                            String originalTitle = movieObj.optString("original_title", "No Original Title");
                            String releaseDate = movieObj.optString("release_date", "Unknown Date");
                            String overview = movieObj.optString("overview", "No Overview Available");
                            double popularity = movieObj.optDouble("popularity", 0.0);
                            String language = movieObj.optString("original_language", "Unknown");
                            double voteAverage = movieObj.optDouble("vote_average", 0.0);
                            int voteCount = movieObj.optInt("vote_count", 0);
                            boolean isAdult = movieObj.optBoolean("adult", false);
                            boolean hasVideo = movieObj.optBoolean("video", false);

                            // Handling poster and backdrop images
                            String posterPath = movieObj.optString("poster_path", "");
                            String backdropPath = movieObj.optString("backdrop_path", "");

                            // Handling Genre IDs
                            JSONArray genreArray = movieObj.getJSONArray("genre_ids");
                            List<Integer> genreIds = new ArrayList<>();
                            for (int j = 0; j < genreArray.length(); j++) {
                                genreIds.add(genreArray.getInt(j));
                            }

                            // Add to movie list only if there's a valid poster
                            if (!posterPath.isEmpty()) {

                                posterPath = IMAGE_BASE_URL + posterPath;
                                backdropPath = IMAGE_BASE_URL + backdropPath;

                                movieList.add(new Movie(id, title, originalTitle, releaseDate, overview, popularity, language,
                                        voteAverage, voteCount, isAdult, hasVideo, posterPath, backdropPath, genreIds));
                            }
                        }

                        movieAdapter = new MovieAdapter(EmployeeSideMovieListPage.this, movieList, "employee");
                        recyclerView.setAdapter(movieAdapter);
                    }

                }
                catch (JSONException e)
                {
                    Log.e("EmployeeSideMovieListPage getListOfUpcomingMovies catch", "Error parsing JSON", e);
                    Toast.makeText(EmployeeSideMovieListPage.this, "JSON parsing error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(EmployeeSideMovieListPage.this, "API Request Failed", Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", error.toString());
            }
        });
    }



}