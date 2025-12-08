package com.example.CafeteriaApp.Models;

import java.util.List;

/**
 * Represents an order placed by a user.
 * Contains order details such as ID, summary, price, status, and various timestamps.
 */
public class Order
{
    private String orderId;
    private String orderCode;
    private String orderStatus;// 1=Received, 2=Preparing, 3=Ready, 4=Collected
    private String orderReceivedTime; //Received
    private String orderPreparedTime; //Preparing
    private String estimatedReadyTime; //Ready
    private String orderCollectedTime; //Collected
    private boolean isOrderCollected;
    private List<Product> products;
    private User user;
    private String requestedTime;//can replace with cart
    private String paymentMethod;
    private boolean isPaid;
    private String summary; //need to think if i need it;,i can do it with ai



}
