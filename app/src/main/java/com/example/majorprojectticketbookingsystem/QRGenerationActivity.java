package com.example.majorprojectticketbookingsystem;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRGenerationActivity extends AppCompatActivity {

    Booking booking;
    private ImageView qrCodeImageView;
    String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qrgeneration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        qrCodeImageView = findViewById(R.id.qrCodeImage);

        booking = (Booking) getIntent().getSerializableExtra("booking");

        if (booking != null) {
            bookingId = booking.getBookingId();

            // Generate QR code and set it in ImageView
            Bitmap qrCodeBitmap = generateQRCode(bookingId);

            if(qrCodeBitmap != null) {
                qrCodeImageView.setImageBitmap(qrCodeBitmap);
            }
        }
    }


    private Bitmap generateQRCode(String text)
    {
        try {
            // Create BarcodeEncoder instance
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

            // Encode the text to a Bitmap QR code
            return barcodeEncoder.encodeBitmap(text, com.google.zxing.BarcodeFormat.QR_CODE, 1024, 1024); // Size of the QR code
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}