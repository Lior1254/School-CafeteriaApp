package com.example.CafeteriaApp.Models;

/**
 * Represents an order placed by a user.
 * Contains order details such as ID, summary, price, status, and various timestamps.
 */
public class Order
{
    private String id;
    private String summary;
    private double price;
    private int statusCode; // 1=Received, 2=Preparing, 3=Ready, 4=Collected
    private String statusText;
    private String requestedTime;
    private String estimatedReadyTime;
    private String orderReceivedTime;
    private String orderPreparedTime;
    private String orderCollectedTime;

    /**
     * Default constructor for Firebase.
     */
    public Order()
    {
    }

    /**
     * Constructs an Order with initial details.
     *
     * @param id                 Unique order ID.
     * @param summary            Short summary of the order items.
     * @param price              Total price of the order.
     * @param statusCode         Numeric status code (1-4).
     * @param statusText         Text description of the status.
     * @param requestedTime      Time the order was requested.
     * @param estimatedReadyTime Estimated time for the order to be ready.
     * @param orderReceivedTime  Timestamp when the order was received by the system.
     */
    public Order(String id, String summary, double price, int statusCode, String statusText,
                 String requestedTime, String estimatedReadyTime, String orderReceivedTime)
    {
        this.id = id;
        this.summary = summary;
        this.price = price;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.requestedTime = requestedTime;
        this.estimatedReadyTime = estimatedReadyTime;
        this.orderReceivedTime = orderReceivedTime;
        this.orderPreparedTime = null;
        this.orderCollectedTime = null;
    }

    // Getters
    public String getId()
    {
        return id;
    }

    public String getSummary()
    {
        return summary;
    }

    public double getPrice()
    {
        return price;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public String getStatusText()
    {
        return statusText;
    }

    public String getRequestedTime()
    {
        return requestedTime;
    }

    public String getEstimatedReadyTime()
    {
        return estimatedReadyTime;
    }

    public String getOrderReceivedTime()
    {
        return orderReceivedTime;
    }

    public String getOrderPreparedTime()
    {
        return orderPreparedTime;
    }

    public String getOrderCollectedTime()
    {
        return orderCollectedTime;
    }

    // Setters
    public void setOrderPreparedTime(String orderPreparedTime)
    {
        this.orderPreparedTime = orderPreparedTime;
    }

    public void setOrderCollectedTime(String orderCollectedTime)
    {
        this.orderCollectedTime = orderCollectedTime;
    }
}
