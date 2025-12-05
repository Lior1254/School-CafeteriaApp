package com.example.CafeteriaApp.Models;

public class Order {
    private String id;
    private String summary;
    private double price;
    private int statusCode; // 1=התקבלה, 2=בהכנה, 3=מוכנה, 4=נאספה
    private String statusText;
    private String requestedTime;
    private String estimatedReadyTime;
    private String orderReceivedTime;
    private String orderPreparedTime;
    private String orderCollectedTime;

    public Order(String id, String summary, double price, int statusCode, String statusText, String requestedTime, String estimatedReadyTime, String orderReceivedTime) {
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
    public String getId() { return id; }
    public String getSummary() { return summary; }
    public double getPrice() { return price; }
    public int getStatusCode() { return statusCode; }
    public String getStatusText() { return statusText; }
    public String getRequestedTime() { return requestedTime; }
    public String getEstimatedReadyTime() { return estimatedReadyTime; }
    public String getOrderReceivedTime() { return orderReceivedTime; }
    public String getOrderPreparedTime() { return orderPreparedTime; }
    public String getOrderCollectedTime() { return orderCollectedTime; }

    // Setters
    public void setOrderPreparedTime(String orderPreparedTime) { this.orderPreparedTime = orderPreparedTime; }
    public void setOrderCollectedTime(String orderCollectedTime) { this.orderCollectedTime = orderCollectedTime; }
}