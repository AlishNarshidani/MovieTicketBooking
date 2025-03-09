package com.example.majorprojectticketbookingsystem;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MovieApiService {
    private static final String API_KEY = "2f7f3a881fa4878268fd8d5cdca3c46a";
//    private static final String URL = "https://api.themoviedb.org/3/movie/upcoming?api_key=" + API_KEY + "&language=en-US&page=1";
    private RequestQueue requestQueue;

    public MovieApiService(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public interface DataCallback {
        void onSuccess(JSONObject response);
        void onError(VolleyError error);
    }

    public void getUpcomingMovies(final DataCallback callback)
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Get 15 days before today
        calendar.add(Calendar.DAY_OF_MONTH, -15);
        String dateAfter = dateFormat.format(calendar.getTime());

        // Reset to today
        calendar = Calendar.getInstance();

        // Get 15 days after today
        calendar.add(Calendar.DAY_OF_MONTH, 15);
        String dateBefore = dateFormat.format(calendar.getTime());

        Log.d("after date", dateAfter);
        Log.d("before date", dateBefore);

        String apiUrl = "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY +
                "&with_original_language=hi" +
                "&sort_by=release_date.desc" +
                "&primary_release_date.gte=" + dateAfter +
                "&primary_release_date.lte=" + dateBefore;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API_RESPONSE", "Response: " + response.toString());
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_ERROR", "Error: " + error.toString());
                        callback.onError(error);
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    public void getSearchedMovie(final DataCallback callback, String searchCriteria)
    {
        // https://api.themoviedb.org/3/search/movie?api_key=2f7f3a881fa4878268fd8d5cdca3c46a&with_original_language=hi&query=Badass%20Ravi%20Kumar
        String apiUrl = "https://api.themoviedb.org/3/search/movie?api_key=" + API_KEY +
                "&with_original_language=hi" +
                "&sort_by=release_date.desc&query=" + searchCriteria;


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API_RESPONSE", "Response: " + response.toString());
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_ERROR", "Error: " + error.toString());
                        callback.onError(error);
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);

    }

    public void getMovieTrailerAndTeaser(final DataCallback callback, String movieId)
    {
        // https://api.themoviedb.org/3/movie/{movie_id}?api_key=2f7f3a881fa4878268fd8d5cdca3c46a&append_to_response=videos
        String apiUrl = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + API_KEY + "&append_to_response=videos";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API_RESPONSE", "Response: " + response.toString());
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_ERROR", "Error: " + error.toString());
                        callback.onError(error);
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}
