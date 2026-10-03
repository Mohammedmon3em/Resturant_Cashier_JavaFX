package com.seafoodpos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Order {
    private int           id;
    private String        orderNumber;
    private int           cashierId;
    private String        cashierName;
    private double        totalAmount;
    private String        status;
    private String        notes;
    private LocalDateTime createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public double calculateTotal() {
        return items.stream().mapToDouble(OrderItem::getLineTotal).sum();
    }

    public List<OrderItem> getFishMeals() {
        return items.stream()
                .filter(i -> i.getItemType() == ItemType.FISH_MEAL)
                .collect(Collectors.toList());
    }

    public List<OrderItem> getJuices() {
        return items.stream()
                .filter(i -> i.getItemType() == ItemType.JUICE)
                .collect(Collectors.toList());
    }

    public boolean hasFishMeals() { return !getFishMeals().isEmpty(); }
    public boolean hasJuices()    { return !getJuices().isEmpty(); }

    public void addItem(OrderItem item)    { items.add(item); }
    public void removeItem(OrderItem item) { items.remove(item); }

    public int           getId()          { return id; }
    public void          setId(int id)    { this.id = id; }
    public String        getOrderNumber() { return orderNumber; }
    public void          setOrderNumber(String o){ this.orderNumber = o; }
    public int           getCashierId()   { return cashierId; }
    public void          setCashierId(int c){ this.cashierId = c; }
    public String        getCashierName() { return cashierName; }
    public void          setCashierName(String c){ this.cashierName = c; }
    public double        getTotalAmount() { return totalAmount; }
    public void          setTotalAmount(double t){ this.totalAmount = t; }
    public String        getStatus()      { return status; }
    public void          setStatus(String s){ this.status = s; }
    public String        getNotes()       { return notes; }
    public void          setNotes(String n){ this.notes = n; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public void          setCreatedAt(LocalDateTime c){ this.createdAt = c; }
    public List<OrderItem> getItems()     { return items; }
    public void          setItems(List<OrderItem> items){ this.items = items; }
}
