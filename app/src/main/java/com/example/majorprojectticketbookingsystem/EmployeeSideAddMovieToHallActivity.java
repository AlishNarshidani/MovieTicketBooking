package com.example.majorprojectticketbookingsystem;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmployeeSideAddMovieToHallActivity extends AppCompatActivity {

    Movie movie;
    Hall hall;

    ProgressDialog progressDialog;

    EditText edtMovieTitle, edtMovieGenre, edtMovieReleaseDate, edtStartDateTime, edtEndDateTime;
    ImageView imgMoviePoster;
    Button btnSubmit, btnViewSchedule;
    LinearLayout rowsContainer; // Dynamic EditText container
    private Calendar startCalendar, endCalendar;

    private String hallId, theatreId, movieId, movieName;
    private List<EditText> rowPriceFields = new ArrayList<>(); // Store dynamically created EditTexts

    Timestamp showStartTime, showEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_add_movie_to_hall);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize Views
        edtMovieTitle = findViewById(R.id.edtMovieTitle);
        edtMovieGenre = findViewById(R.id.edtMovieGenre);
        edtMovieReleaseDate = findViewById(R.id.edtMovieReleaseDate);
        edtStartDateTime = findViewById(R.id.edtStartTime);
        edtEndDateTime = findViewById(R.id.edtEndTime);
        imgMoviePoster = findViewById(R.id.imgMoviePoster);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnViewSchedule = findViewById(R.id.btnViewSchedule);
        rowsContainer = findViewById(R.id.rowsContainer); // LinearLayout to hold EditText fields

        Intent intent = getIntent();
        if (intent != null) {
            movie = (Movie) intent.getSerializableExtra("movie");
            hall = (Hall) intent.getSerializableExtra("hall");

            hallId = hall.getHallId();
            theatreId = hall.getTheatreId();
            movieId = movie.getId();
            movieName = movie.getTitle();

            // Set Movie Details (Unchangeable)
            edtMovieTitle.setText(movie.getTitle());
            edtMovieGenre.setText(movie.getGenreNames());
            edtMovieReleaseDate.setText("Release Date " + movie.getReleaseDate());

            edtMovieTitle.setEnabled(false);
            edtMovieGenre.setEnabled(false);
            edtMovieReleaseDate.setEnabled(false);

            Glide.with(EmployeeSideAddMovieToHallActivity.this)
                    .load(movie.getPosterPath())
                    .error(R.drawable.cinemalogo)
                    .into(imgMoviePoster);

            startCalendar = Calendar.getInstance();
            endCalendar = Calendar.getInstance();

            edtStartDateTime.setOnClickListener(v -> showDateTimePicker(edtStartDateTime, startCalendar));
            edtEndDateTime.setOnClickListener(v -> showDateTimePicker(edtEndDateTime, endCalendar));


        }

        // Fetch total rows and dynamically generate input fields
        fetchTotalRowsFromFirestore();


        btnViewSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeSideAddMovieToHallActivity.this, EmployeeSideViewHallSchedule.class);
                intent.putExtra("hall", hall);
                intent.putExtra("movie", movie);
                startActivity(intent);
            }
        });


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = validateInputs();

                if(result == true)
                {
                    showStartTime = convertToTimestamp(edtStartDateTime.getText().toString().trim());
                    showEndTime = convertToTimestamp(edtEndDateTime.getText().toString().trim());

//                    for (int i = 0; i < rowPriceFields.size(); i++) {
//                        String price = rowPriceFields.get(i).getText().toString().trim();
//
//                        if (!price.isEmpty()) {
//                            Log.d("row price ", "row : " + (i+1) + " price : " + price); // Store non-empty values
//                        } else {
//                            Log.d("no price found", "onClick: ");
//                        }
//                    }

                    if (showStartTime != null && showEndTime != null) {
                        storeShowInFirestore(hallId, theatreId, movieId, movieName, showStartTime, showEndTime);
                    } else {
                        Toast.makeText(EmployeeSideAddMovieToHallActivity.this, "Invalid date format!", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(EmployeeSideAddMovieToHallActivity.this, "Please fill Proper Details!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void storeShowInFirestore(String hallId, String theatreId, String movieId, String movieName, Timestamp showStartTime, Timestamp showEndTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // ✅ Show Processing Loader
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding Movie To Hall...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // ✅ First, Check for Overlapping Shows
        db.collection("shows")
                .whereEqualTo("hall_id", hallId) // ✅ Ensure same hall
                .whereLessThan("showStartTime", showEndTime) // ✅ Existing show starts before my show ends
                .whereGreaterThan("showEndTime", showStartTime) // ✅ Existing show ends after my show starts
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        progressDialog.dismiss();
                        Log.d("Firestore", "Conflict Found! Another show is scheduled during this time.");
                        Toast.makeText(this, "Show timing conflicts with another scheduled show!", Toast.LENGTH_SHORT).show();
                    } else {
                        // ✅ No conflicts, proceed with adding the show
                        addShowToFirestore(hallId, theatreId, movieId, movieName, showStartTime, showEndTime);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error checking schedule conflicts", e);
                    Toast.makeText(this, "Error checking schedule conflicts!", Toast.LENGTH_SHORT).show();
                });
    }

    private void addShowToFirestore(String hallId, String theatreId, String movieId, String movieName, Timestamp showStartTime, Timestamp showEndTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // ✅ Path to the hall's seatsLayout in Firestore
        DocumentReference hallRef = db.collection("theatre").document(theatreId)
                .collection("halls").document(hallId);

        hallRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // ✅ Fetch existing seatsLayout
                Map<String, Object> seatsLayout = (Map<String, Object>) documentSnapshot.get("seatsLayout");

                if (seatsLayout != null) {

                    // update price in each row
                    for (int i = 0; i < rowPriceFields.size(); i++) {
                        String rowKey = "row" + (i + 1); // Example: row1, row2, row3...
                        String priceText = rowPriceFields.get(i).getText().toString().trim();

                        if (!priceText.isEmpty() && seatsLayout.containsKey(rowKey)) {
                            int price = Integer.parseInt(priceText); // ✅ Convert String to Double

                            List<Map<String, Object>> seatList = (List<Map<String, Object>>) seatsLayout.get(rowKey);
                            for (Map<String, Object> seat : seatList) {
                                seat.put("price", price); // ✅ Update price for each seat in this row
                                seat.put("isBooked", false); // Ensure all seats are unbooked initially
                            }
                        }
                    }

                    // ✅ Create a unique Show ID (document ID)
                    String showId = db.collection("shows").document().getId();

                    // ✅ Create show details map
                    Map<String, Object> showDetails = new HashMap<>();
                    showDetails.put("showId", showId);
                    showDetails.put("hall_id", hallId);
                    showDetails.put("theatreId", theatreId);
                    showDetails.put("movieId", movieId);
                    showDetails.put("movieName", movieName);
                    showDetails.put("showStartTime", showStartTime);
                    showDetails.put("showEndTime", showEndTime);
                    showDetails.put("seatsLayout", seatsLayout); // ✅ Copy seatsLayout

                    // ✅ Store the data in Firestore
                    db.collection("shows").document(showId).set(showDetails)
                            .addOnSuccessListener(aVoid -> {
                                // ✅ Check if the movie exists, if not, add it
                                checkAndAddMovie(db, movieId, movie);

                                Log.d("Firestore", "Show added successfully with seatsLayout!");
                                Toast.makeText(this, "Show added successfully!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Log.e("Firestore", "Error adding show", e);
                                Toast.makeText(this, "Error adding show!", Toast.LENGTH_SHORT).show();
                            });

                } else {
                    progressDialog.dismiss();
                    Log.e("Firestore", "No seatsLayout found in hall!");
                    Toast.makeText(this, "Seats layout not found!", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressDialog.dismiss();
                Log.e("Firestore", "Hall document not found!");
                Toast.makeText(this, "Hall not found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Log.e("Firestore", "Error fetching seats layout", e);
            Toast.makeText(this, "Error fetching seats layout!", Toast.LENGTH_SHORT).show();
        });
    }



//    private void addShowToFirestore(String hallId, String theatreId, String movieId, String movieName, Timestamp showStartTime, Timestamp showEndTime) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // ✅ Create a unique Show ID (document ID)
//        String showId = db.collection("shows").document().getId();
//
//        // ✅ Create show details map
//        Map<String, Object> showDetails = new HashMap<>();
//        showDetails.put("showId", showId);
//        showDetails.put("hall_id", hallId);
//        showDetails.put("theatreId", theatreId);
//        showDetails.put("movieId", movieId);
//        showDetails.put("movieName", movieName);
//        showDetails.put("showStartTime", showStartTime);
//        showDetails.put("showEndTime", showEndTime);
//
//        // ✅ Store the data in Firestore
//        db.collection("shows").document(showId).set(showDetails)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d("Firestore", "Show added successfully!");
//                    Toast.makeText(this, "Show added successfully!", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("Firestore", "Error adding show", e);
//                    Toast.makeText(this, "Error adding show!", Toast.LENGTH_SHORT).show();
//                });
//    }


    private void checkAndAddMovie(FirebaseFirestore db, String movieId, Movie movie) {
        db.collection("movies").document(movieId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // ✅ Movie does NOT exist, so add it
                        addMovieToFirestore(db, movieId, movie);
                    } else {
                        progressDialog.dismiss();
                        finish();
                        Log.d("Firestore", "Movie already exists, skipping insertion.");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error checking movie existence", e));
    }

    private void addMovieToFirestore(FirebaseFirestore db, String movieId, Movie movie) {
        Map<String, Object> movieDetails = new HashMap<>();
        movieDetails.put("id", movieId);
        movieDetails.put("title", movie.getTitle());
        movieDetails.put("original_title", movie.getOriginalTitle());
        movieDetails.put("release_date", movie.getReleaseDate());
        movieDetails.put("overview", movie.getOverview());
        movieDetails.put("popularity", movie.getPopularity());
        movieDetails.put("original_language", movie.getLanguage());
        movieDetails.put("vote_average", movie.getVoteAverage());
        movieDetails.put("vote_count", movie.getVoteCount());
        movieDetails.put("adult", movie.isAdult());
        movieDetails.put("video", movie.hasVideo());
        movieDetails.put("poster_path", movie.getPosterPath());
        movieDetails.put("backdrop_path", movie.getBackdropPath());
        movieDetails.put("genre_names", movie.getGenreNames());

        db.collection("movies").document(movieId).set(movieDetails)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Movie added successfully!");
                    progressDialog.dismiss();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e("Firestore", "Error adding movie", e);
                });
    }



    private Timestamp convertToTimestamp(String dateTimeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateTimeStr); // Convert string to Date
            return new Timestamp(date); // Convert Date to Firestore Timestamp
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Handle error properly
        }
    }

//    private void showDateTimePicker(EditText editText, Calendar calendar) {
//        // Show Date Picker
//        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
//                (view, year, month, dayOfMonth) -> {
//                    calendar.set(Calendar.YEAR, year);
//                    calendar.set(Calendar.MONTH, month);
//                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//
//                    // Show Time Picker after Date is selected
//                    showTimePicker(editText, calendar);
//                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//
//        datePickerDialog.show();
//    }

    private void showDateTimePicker(EditText editText, Calendar calendar) {
        Calendar currentCalendar = Calendar.getInstance();
        // Show Date Picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Show Time Picker after Date is selected
                    showTimePicker(editText, calendar, currentCalendar);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker(EditText editText, Calendar selectedCalendar, Calendar currentCalendar) {
        boolean isToday = selectedCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                selectedCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                selectedCalendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    // ✅ Prevent past time selection if the selected date is today
                    if (isToday && (hourOfDay < currentCalendar.get(Calendar.HOUR_OF_DAY) ||
                            (hourOfDay == currentCalendar.get(Calendar.HOUR_OF_DAY) && minute < currentCalendar.get(Calendar.MINUTE)))) {
                        Toast.makeText(this, "You cannot select a past time!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ Set selected time
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCalendar.set(Calendar.MINUTE, minute);

                    // Format Date & Time
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    editText.setText(sdf.format(selectedCalendar.getTime()));

                    // ✅ Clear the error when a valid value is selected
                    editText.setError(null);
                },
                selectedCalendar.get(Calendar.HOUR_OF_DAY),
                selectedCalendar.get(Calendar.MINUTE),
                true);

        timePickerDialog.show();
    }


//    private void showTimePicker(EditText editText, Calendar calendar) {
//        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
//                (view, hourOfDay, minute) -> {
//                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                    calendar.set(Calendar.MINUTE, minute);
//
//                    // Format Date & Time
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//                    editText.setText(sdf.format(calendar.getTime()));
//
//                    // ✅ Clear the error when a valid value is selected
//                    editText.setError(null);
//
//                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
//
//        timePickerDialog.show();
//    }


    private boolean validateInputs() {
        boolean isValid = true; // ✅ Track overall validation

        String startDateTime = edtStartDateTime.getText().toString().trim();
        String endDateTime = edtEndDateTime.getText().toString().trim();

        // ✅ Check Start DateTime
        if (startDateTime.isEmpty()) {
            edtStartDateTime.setError("Please select Start Date & Time");
            isValid = false;
        } else {
            edtStartDateTime.setError(null); // ✅ Clear error if corrected
        }

        // ✅ Check End DateTime
        if (endDateTime.isEmpty()) {
            edtEndDateTime.setError("Please select End Date & Time");
            isValid = false;
        } else {
            edtEndDateTime.setError(null); // ✅ Clear error if corrected
        }

        // ✅ Ensure End Time is after Start Time (Only if both fields are filled)
        if (!startDateTime.isEmpty() && !endDateTime.isEmpty()) {
            if (!isEndDateTimeAfterStartDateTime(startCalendar, endCalendar)) {
                edtEndDateTime.setError("End time must be after Start time");
                isValid = false;
            } else {
                edtEndDateTime.setError(null); // ✅ Clear error if corrected
            }
        }

        // ✅ Validate Row Prices
        for (int i = 0; i < rowPriceFields.size(); i++) {
            String price = rowPriceFields.get(i).getText().toString().trim();

            if (!isValidPrice(price)) {
                rowPriceFields.get(i).setError("Enter valid price (1 - 999999)");
                isValid = false;
            } else {
                rowPriceFields.get(i).setError(null); // ✅ Clear error if corrected
            }
        }

        return isValid;
    }



//    private boolean validateInputs() {
//        String startDateTime = edtStartDateTime.getText().toString().trim();
//        String endDateTime = edtEndDateTime.getText().toString().trim();
//
//        if (startDateTime.isEmpty()) {
//            edtStartDateTime.setError("Please select Start Date & Time");
//            return false;
//        }
//        if (endDateTime.isEmpty()) {
//            edtEndDateTime.setError("Please select End Date & Time");
//            return false;
//        }
//
//        if (!isEndDateTimeAfterStartDateTime(startCalendar, endCalendar)) {
//            edtEndDateTime.setError("End time must be after Start time");
//            return false;
//        }
//
//        // Validate Row Prices
//        for (int i = 0; i < rowPriceFields.size(); i++) {
//            String price = rowPriceFields.get(i).getText().toString().trim();
//            if (!isValidPrice(price)) {
//                rowPriceFields.get(i).setError("Enter valid price (1 - 999999)");
//                return false;
//            }
//        }
//
//        return true;
//    }

    private boolean isValidPrice(String price) {
        if (price.isEmpty()) return false; // No empty fields
        try {
            int value = Integer.parseInt(price);
            return value > 0 && value <= 999999; // Between 1 and 999999
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isEndDateTimeAfterStartDateTime(Calendar start, Calendar end) {
        return end.after(start);
    }


    private void fetchTotalRowsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("theatre").document(theatreId)
                .collection("halls").document(hallId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        long totalRows = documentSnapshot.getLong("totalRows");
                        Log.d("Firestore", "Total Rows: " + totalRows);

                        if (totalRows > 0) {
                            createRowPriceFields((int) totalRows);
                        } else {
                            Toast.makeText(this, "No rows found in this hall!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching totalRows", e));
    }


    private void createRowPriceFields(int totalRows) {
        for (int i = 1; i <= totalRows; i++) {
            // Create a container layout for each row input
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            rowLayout.setOrientation(LinearLayout.VERTICAL);
            rowLayout.setPadding(0, 6, 0, 16); // Add spacing between rows

//            // Create a TextView label for the row
//            TextView rowLabel = new TextView(this);
//            rowLabel.setText("Price for Row " + i + ":");
//            rowLabel.setTextSize(16);
//            rowLabel.setTextColor(getResources().getColor(android.R.color.black));
//            rowLabel.setPadding(0, 0, 0, 8); // Space between label and input field

            // Create an EditText input field
            EditText rowPriceInput = new EditText(this);
            rowPriceInput.setHint("Enter price for Row " + i);
            rowPriceInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            rowPriceInput.setPadding(10, 10, 10, 10);
            rowPriceInput.setBackgroundResource(R.drawable.transparent_edittext); // Use a better background

            // Add elements to row layout
            //rowLayout.addView(rowLabel);
            rowLayout.addView(rowPriceInput);

            // Store for later use
            rowPriceFields.add(rowPriceInput);

            // Add to the main container
            rowsContainer.addView(rowLayout);
        }
    }



}