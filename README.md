# 🎬 Movie Ticket Booking System

> **Book movies, mark your entry, get the latest entertainment news — all in one app!**  
> A lightweight, secure, and user-friendly movie ticket booking app designed for both customers and theatre employees. Say goodbye to long queues and hello to instant bookings.

---

## 🚀 Introduction
The **Movie Ticket Booking System** is an Android application that makes booking movie tickets fast, simple, and enjoyable.  
It integrates **real-time seat availability**, **secure payments**, **booking history**, **shows management**, and **live movie news** — all in a smooth, ad-free experience.

Whether you’re a **moviegoer** looking for an easy way to reserve your favorite seats or a **theatre employee** managing shows and bookings, this system covers it all.

---

## 🎯 Features
- **🔐 Secure Login & Role-Based Access**  
  Firebase Authentication with customer/employee dashboards.
- **🎟️ Easy Ticket Booking**  
  Browse movies, select showtimes, choose seats, and confirm instantly.
- **🛠️ Employee Management**  
  Add, modify, or cancel shows, manage bookings, and scan tickets with QR codes.
- **📰 Latest Movie News**  
  Get live updates on movies and entertainment from APIs like TMDb & NewsAPI.
- **💳 Multiple Payment Options**  
  Razorpay integration for UPI, cards, and wallet payments with instant receipts.
- **📜 Booking History**  
  Track all past bookings, view payment status, and download tickets anytime.
- **📢 Notifications**  
  Real-time updates for booking confirmations, offers, and show reminders.

---

## 🏗️ Tech Stack
- **Frontend:** Android (Java/XML)
- **Backend:** Firebase Firestore, Firebase Realtime Database
- **Authentication:** Firebase Auth (Email/Password, Google Sign-In)
- **Payments:** Razorpay SDK (UPI, Cards, Wallet)
- **APIs:** TMDb API / NewsAPI for movie news
- **Push Notifications:** Firebase Cloud Messaging (FCM)
- **Version Control:** Git & GitHub

---

## 📦 Installation
1. **Clone the repository**
   ```bash
   git clone https://github.com/AlishNarshidani/MovieTicketBooking.git
   cd MovieTicketBooking
   ```

2. **Open in Android Studio**
   - Import the project folder.
   - Sync Gradle files.

3. **Setup Firebase**
   - Create a Firebase project.
   - Add `google-services.json` to `/app` folder.
   - Enable Firebase Auth, Firestore, and Cloud Messaging.

4. **Setup Payment Gateway**
   - Create a Razorpay account.
   - Add API keys to the project configuration.

5. **Run the App**
   - Connect your Android device or use an emulator.
   - Click **Run** in Android Studio.

---

## 📲 Usage
- **For Customers:**
  1. Sign up / log in.
  2. Browse movies & showtimes.
  3. Select seats and pay securely.
  4. Get booking confirmation & e-ticket.
  5. View booking history & receive reminders.

- **For Employees:**
  1. Log in with employee credentials.
  2. Add or manage movie shows.
  3. Scan tickets and mark entries.
  4. Manage cancellations & refunds.

---

## 📂 Modules Overview
1. **User Authentication** – Role-based dashboards for customers and employees.  
2. **Ticket Booking** – Seat selection, booking confirmation, and real-time updates.  
3. **Employee Management** – Add/update/cancel shows, scan tickets, manage bookings.  
4. **Movie News** – Fetch and display trending entertainment updates.  
5. **Payment Processing** – Secure transactions via Razorpay.  
6. **Booking History** – View and manage past reservations.  
7. **Notifications** – Real-time booking updates and offers.

---

## 🖼️ Screenshots
| Feature | Preview |
|---------|---------|
| Login & Sign-Up | ![Login](screenshots/login.png) |
| Booking Flow | ![Booking](screenshots/booking.png) |
| Payment | ![Payment](screenshots/payment.png) |
| Booking History | ![History](screenshots/history.png) |
| Employee Panel | ![Employee](screenshots/employee.png) |
| Movie News | ![News](screenshots/news.png) |

---

## 📈 Improvements Over Existing Platforms
- **Ad-Free & Lightweight** – Faster loading, no clutter.
- **Low/No Convenience Fee** – Affordable for all.
- **Small Theatre Support** – Easy onboarding for local cinemas.
- **Instant Refunds** – Automated and fast.
- **Better Seat Visualization** – Smooth and interactive seat layouts.

---

## 🔮 Future Enhancements
- 🍿 **Food & Beverage Pre-Booking**
- 📊 **Dynamic Pricing according to demand after adding show**
- 🌏 **Multi-Language Support**
- 💻 **Web Version**
- 🤝 **Integration with More Payment Gateways**
- 📈 **Movie/Show Performance Analytics Dashboard**
- 🎯 **AI-Powered Recommendations according to user's taste and trends in movies**

---

## 👨‍💻 Contributors
- **Alish Narsidani**

---
