package com.example.majorprojectticketbookingsystem;

import java.util.Date;

public class Transaction {
    private String transactionType;
    private double amount;
    private Date transactionDate;
    private String transactionStatusOrMethod;
    private String transactionIdOrBookingId;

    // Constructor
    public Transaction(String transactionType, double amount, Date transactionDate, String transactionStatusOrMethod,String transactionIdOrBookingId) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.transactionStatusOrMethod = transactionStatusOrMethod;
        this.transactionIdOrBookingId = transactionIdOrBookingId;
    }

    // Getters
    public String getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public String getTransactionStatusOrMethod()
    {
        return transactionStatusOrMethod;
    }

    public String getTransactionIdOrBookingId()
    {
        return transactionIdOrBookingId;
    }
}
