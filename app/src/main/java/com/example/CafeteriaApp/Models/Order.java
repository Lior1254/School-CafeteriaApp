package com.example.CafeteriaApp.Models;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an order placed by a user.
 * Implements Serializable to allow passing between activities via Intent.
 */
public class Order implements Serializable
{
    // Status Constants for easier management
    public static final String STATUS_RECEIVED = "1";
    public static final String STATUS_PREPARING = "2";
    public static final String STATUS_READY = "3";
    public static final String STATUS_COLLECTED = "4";

    private String orderId;
    private String orderCode;
    private String orderStatus; // 1=Received, 2=Preparing, 3=Ready, 4=Collected
    private String orderReceivedTime;
    private String orderPreparedTime;
    private String estimatedReadyTime;
    private String orderCollectedTime;
    private boolean isOrderCollected;
    private List<Product> products;
    private User user;
    private String requestedTime;
    private String paymentMethod;
    private boolean isPaid;
    private double totalPrice;
    private String summary;

    // 1. Empty constructor required for Firebase Realtime Database
    public Order()
    {
    }

    // 2. Full constructor
    public Order(String orderId, String orderCode, String orderStatus, String orderReceivedTime, 
                 List<Product> products, User user, String paymentMethod, boolean isPaid, double totalPrice)
    {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.orderStatus = orderStatus;
        this.orderReceivedTime = orderReceivedTime;
        this.products = products;
        this.user = user;
        this.paymentMethod = paymentMethod;
        this.isPaid = isPaid;
        this.totalPrice = totalPrice;
        this.isOrderCollected = false;
    }

    // 3. Getters and Setters (Firebase uses these to map the data)
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getOrderReceivedTime() { return orderReceivedTime; }
    public void setOrderReceivedTime(String orderReceivedTime) { this.orderReceivedTime = orderReceivedTime; }

    public String getOrderPreparedTime() { return orderPreparedTime; }
    public void setOrderPreparedTime(String orderPreparedTime) { this.orderPreparedTime = orderPreparedTime; }

    public String getEstimatedReadyTime() { return estimatedReadyTime; }
    public void setEstimatedReadyTime(String estimatedReadyTime) { this.estimatedReadyTime = estimatedReadyTime; }

    public String getOrderCollectedTime() { return orderCollectedTime; }
    public void setOrderCollectedTime(String orderCollectedTime) { this.orderCollectedTime = orderCollectedTime; }

    public boolean isOrderCollected() { return isOrderCollected; }
    public void setOrderCollected(boolean orderCollected) { isOrderCollected = orderCollected; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getRequestedTime() { return requestedTime; }
    public void setRequestedTime(String requestedTime) { this.requestedTime = requestedTime; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
