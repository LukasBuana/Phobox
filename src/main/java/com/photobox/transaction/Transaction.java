package com.photobox.transaction;

public class Transaction {
    private String id;
    private String status;
    private String packageName;
    private int price;
    private String paymentMethod;
    private String startedAt;
    private String resolvedAt;

    public Transaction(String id, String status, String packageName, int price, String paymentMethod, String startedAt, String resolvedAt) {
        this.id = id;
        this.status = status;
        this.packageName = packageName;
        this.price = price;
        this.paymentMethod = paymentMethod;
        this.startedAt = startedAt;
        this.resolvedAt = resolvedAt;
    }

    public String getId() { return id; }
    public String getStatus() { return status; }
    public String getPackageName() { return packageName; }
    public int getPrice() { return price; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStartedAt() { return startedAt; }
    public String getResolvedAt() { return resolvedAt; }
}