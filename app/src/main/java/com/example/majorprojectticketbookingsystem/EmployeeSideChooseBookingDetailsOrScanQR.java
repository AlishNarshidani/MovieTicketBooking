package com.example.majorprojectticketbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EmployeeSideChooseBookingDetailsOrScanQR extends AppCompatActivity {

    CardView viewBookingsMovieWiseCard, viewBookingsDayWiseCard, scanTicketCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_side_choose_booking_details_or_scan_qr);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewBookingsMovieWiseCard = findViewById(R.id.viewBookingsMovieWiseCard);
        viewBookingsDayWiseCard = findViewById(R.id.viewBookingsDayWiseCard);
        scanTicketCard = findViewById(R.id.scanTicketCard);

        viewBookingsMovieWiseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeSideChooseBookingDetailsOrScanQR.this, EmployeeSideChooseUpcomingOrPastBookings.class);
                intent.putExtra("caller", "movieWise");
                startActivity(intent);
            }
        });

        viewBookingsDayWiseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeSideChooseBookingDetailsOrScanQR.this, EmployeeSideChooseUpcomingOrPastBookings.class);
                intent.putExtra("caller", "dayWise");
                startActivity(intent);
            }
        });

        scanTicketCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmployeeSideChooseBookingDetailsOrScanQR.this, EmployeeSideQRCodeScanActivity.class);
                startActivity(intent);
            }
        });

    }
}