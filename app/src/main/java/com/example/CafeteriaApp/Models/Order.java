package com.example.CafeteriaApp.Models;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable
{
    public static final String STATUS_PENDING = "0";
    public static final String STATUS_PREPARING = "1";
    public static final String STATUS_READY = "2";
    public static final String STATUS_COLLECTED = "3";

    private String orderId;
    private String userId; // המזהה של המשתמש שביצע את ההזמנה
    private String orderCode; // מספר רנדומלי קצר (למשל 5967)
    private String orderStatus; 
    private String orderReceivedTime; // הזמן שמשמש כמפתח בעץ (YYYY-MM-DD...)
    private List<Product> products;
    private User user; // אובייקט המשתמש המלא
    private String requestedTime; // שעת האיסוף מה-Spinner
    private String paymentMethod;
    private boolean isPaid;
    private double totalPrice;
    private String summary;

    public Order() {}

    public Order(String orderId, String userId, String orderCode, String orderStatus, String orderReceivedTime, 
                 List<Product> products, User user, String paymentMethod, boolean isPaid, double totalPrice)
    {
        this.orderId = orderId;
        this.userId = userId;
        this.orderCode = orderCode;
        this.orderStatus = orderStatus;
        this.orderReceivedTime = orderReceivedTime;
        this.products = products;
        this.user = user;
        this.paymentMethod = paymentMethod;
        this.isPaid = isPaid;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getOrderReceivedTime() { return orderReceivedTime; }
    public void setOrderReceivedTime(String orderReceivedTime) { this.orderReceivedTime = orderReceivedTime; }

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
