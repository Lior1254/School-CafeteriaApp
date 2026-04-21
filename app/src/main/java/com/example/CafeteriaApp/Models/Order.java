package com.example.CafeteriaApp.Models;

import com.google.firebase.database.PropertyName;
import java.io.Serializable;
import java.util.List;

public class Order implements Serializable
{
    public static final String STATUS_PENDING = "0";
    public static final String STATUS_PREPARING = "1";
    public static final String STATUS_READY = "2";
    public static final String STATUS_COLLECTED = "3";

    private String orderId;
    private String userId; 
    private String orderCode;
    private String orderStatus; 
    private String orderReceivedTime;
    private List<Product> products;
    private User user;
    private String requestedTime;
    private String paymentMethod;
    private boolean isPaid;
    private double totalPrice;
    private String summary;
    private String generalNotes;
    private int role = User.ROLE_USER;

    public Order() {}

    public Order(String orderId, String userId, String orderCode, String orderStatus, String orderReceivedTime, String requestedTime,
                 List<Product> products, User user, String paymentMethod, boolean isPaid, double totalPrice)
    {
        this.orderId = orderId;
        this.userId = userId;
        this.orderCode = orderCode;
        this.orderStatus = orderStatus;
        this.orderReceivedTime = orderReceivedTime;
        this.requestedTime = requestedTime;
        this.products = products;
        this.user = user;
        this.paymentMethod = paymentMethod;
        this.isPaid = isPaid;
        this.totalPrice = totalPrice;
        if (user != null) {
            this.role = user.getRole();
        }
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    @PropertyName("orderStatus")
    public String getOrderStatus() { return orderStatus; }
    
    @PropertyName("orderStatus")
    public void setOrderStatus(Object status) {
        if (status instanceof Long) {
            this.orderStatus = String.valueOf(status);
        } else if (status instanceof String) {
            this.orderStatus = (String) status;
        } else {
            this.orderStatus = STATUS_PENDING;
        }
    }

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

    @PropertyName("paid")
    public boolean isPaid() { return isPaid; }
    @PropertyName("paid")
    public void setPaid(boolean paid) { isPaid = paid; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getGeneralNotes() { return generalNotes; }
    public void setGeneralNotes(String generalNotes) { this.generalNotes = generalNotes; }

    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }
}
