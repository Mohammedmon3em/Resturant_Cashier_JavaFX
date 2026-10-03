package com.seafoodpos.model;

public class MenuItem {
    private int     id;
    private String  name;
    private ItemType itemType;
    private Double  pricePerKg;
    private Double  priceSmall;
    private Double  priceLarge;
    private String  category;
    private boolean available;
    private int     displayOrder;

    public MenuItem() {}

    public MenuItem(int id, String name, ItemType itemType, Double pricePerKg,
                    Double priceSmall, Double priceLarge, String category,
                    boolean available, int displayOrder) {
        this.id           = id;
        this.name         = name;
        this.itemType     = itemType;
        this.pricePerKg   = pricePerKg;
        this.priceSmall   = priceSmall;
        this.priceLarge   = priceLarge;
        this.category     = category;
        this.available    = available;
        this.displayOrder = displayOrder;
    }

    public double calculateFishPrice(double weightKg) {
        if (pricePerKg == null || pricePerKg <= 0) return 0;
        return pricePerKg * weightKg;
    }

    public double calculateJuicePrice(JuiceSize size) {
        if (size == JuiceSize.SMALL) return priceSmall != null ? priceSmall : 0;
        return priceLarge != null ? priceLarge : 0;
    }

    public int      getId()            { return id; }
    public void     setId(int id)      { this.id = id; }
    public String   getName()          { return name; }
    public void     setName(String n)  { this.name = n; }
    public ItemType getItemType()      { return itemType; }
    public void     setItemType(ItemType t) { this.itemType = t; }
    public Double   getPricePerKg()    { return pricePerKg; }
    public void     setPricePerKg(Double p){ this.pricePerKg = p; }
    public Double   getPriceSmall()    { return priceSmall; }
    public void     setPriceSmall(Double p){ this.priceSmall = p; }
    public Double   getPriceLarge()    { return priceLarge; }
    public void     setPriceLarge(Double p){ this.priceLarge = p; }
    public String   getCategory()      { return category; }
    public void     setCategory(String c){ this.category = c; }
    public boolean  isAvailable()      { return available; }
    public void     setAvailable(boolean a){ this.available = a; }
    public int      getDisplayOrder()  { return displayOrder; }
    public void     setDisplayOrder(int o){ this.displayOrder = o; }

    @Override public String toString() { return name; }
}
