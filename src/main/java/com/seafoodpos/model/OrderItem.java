package com.seafoodpos.model;

public class OrderItem {
    private int      id;
    private int      orderId;
    private int      menuItemId;
    private String   itemName;
    private ItemType itemType;
    private Double   weightKg;
    private Double   pricePerKg;
    private JuiceSize juiceSize;
    private int      quantity = 1;
    private String   cookingMethod;
    private Double   unitPrice;
    private double   lineTotal;

    public OrderItem() {}

    public static OrderItem forFishMeal(int menuItemId, String itemName,
                                        double weightKg, double pricePerKg, String cookingMethod) {
        OrderItem item = new OrderItem();
        item.menuItemId = menuItemId;
        item.itemName   = itemName;
        item.itemType   = ItemType.FISH_MEAL;
        item.weightKg   = weightKg;
        item.pricePerKg = pricePerKg;
        item.unitPrice  = pricePerKg;
        item.lineTotal  = pricePerKg * weightKg;
        item.cookingMethod = cookingMethod;
        return item;
    }

    public static OrderItem forJuice(int menuItemId, String itemName,
                                     JuiceSize size, int quantity, double unitPrice) {
        OrderItem item = new OrderItem();
        item.menuItemId = menuItemId;
        item.itemName   = itemName;
        item.itemType   = ItemType.JUICE;
        item.juiceSize  = size;
        item.quantity   = quantity;
        item.unitPrice  = unitPrice;
        item.lineTotal  = unitPrice * quantity;
        return item;
    }

    public String getDetailDisplay() {
        if (itemType == ItemType.FISH_MEAL) {
            String w = weightKg != null ? String.format("%.3f kg", weightKg) : "";
            if (cookingMethod != null && !cookingMethod.isBlank()) {
                w += " (" + cookingMethod + ")";
            }
            return w;
        } else {
            String sizeStr = juiceSize != null ? juiceSize.getBilingualLabel() : "";
            return quantity > 1 ? sizeStr + " x " + quantity : sizeStr;
        }
    }

    public int       getId()           { return id; }
    public void      setId(int id)     { this.id = id; }
    public int       getOrderId()      { return orderId; }
    public void      setOrderId(int o) { this.orderId = o; }
    public int       getMenuItemId()   { return menuItemId; }
    public void      setMenuItemId(int m){ this.menuItemId = m; }
    public String    getItemName()     { return itemName; }
    public void      setItemName(String n){ this.itemName = n; }
    public ItemType  getItemType()     { return itemType; }
    public void      setItemType(ItemType t){ this.itemType = t; }
    public Double    getWeightKg()     { return weightKg; }
    public void      setWeightKg(Double w){ this.weightKg = w; }
    public Double    getPricePerKg()   { return pricePerKg; }
    public void      setPricePerKg(Double p){ this.pricePerKg = p; }
    public JuiceSize getJuiceSize()    { return juiceSize; }
    public void      setJuiceSize(JuiceSize s){ this.juiceSize = s; }
    public Double    getUnitPrice()    { return unitPrice; }
    public void      setUnitPrice(Double u){ this.unitPrice = u; }
    public double    getLineTotal()    { return lineTotal; }
    public void      setLineTotal(double l){ this.lineTotal = l; }
    public int       getQuantity()     { return quantity; }
    public void      setQuantity(int q){ this.quantity = q; }
    public String    getCookingMethod() { return cookingMethod; }
    public void      setCookingMethod(String c) { this.cookingMethod = c; }
}
