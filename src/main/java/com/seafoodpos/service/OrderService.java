package com.seafoodpos.service;

import com.seafoodpos.db.OrderDAO;
import com.seafoodpos.model.Order;
import com.seafoodpos.model.OrderItem;
import com.seafoodpos.model.User;
import com.seafoodpos.util.SessionManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
    private final OrderDAO orderDAO = new OrderDAO();

    public Order createOrder(List<OrderItem> items) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Order must have at least one item");
        User cashier = SessionManager.getInstance().getCurrentUser();
        if (cashier == null) throw new IllegalStateException("No user logged in");

        Order order = new Order();
        order.setItems(items);
        order.setCashierId(cashier.getId());
        order.setCashierName(cashier.getFullName());
        order.setStatus("COMPLETED");
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderNumber(orderDAO.generateOrderNumber(LocalDate.now()));
        order.setTotalAmount(items.stream().mapToDouble(OrderItem::getLineTotal).sum());
        return orderDAO.save(order);
    }
}
