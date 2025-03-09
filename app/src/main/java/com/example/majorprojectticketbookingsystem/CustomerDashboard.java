package com.example.majorprojectticketbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CustomerDashboard extends AppCompatActivity {

    ImageView profileIcon;

    CardView movieBookingCard, newsCard, walletCard, notificationCard, bookingHistoryCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileIcon = findViewById(R.id.profileIcon);
        movieBookingCard = findViewById(R.id.movieBookingCard);
        newsCard = findViewById(R.id.newsCard);
        walletCard = findViewById(R.id.walletCard);
        notificationCard = findViewById(R.id.notificationCard);
        bookingHistoryCard = findViewById(R.id.bookingHistoryCard);

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerDashboard.this, CustomerProfilePage.class);
                startActivity(intent);
            }
        });

        movieBookingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerDashboard.this, CustomerSideMovieListPage.class);
                startActivity(intent);
            }
        });



    }


}