package gui;

public class FoodItem {
    public String id, name, price, imagePath, category, unit, status;

    public FoodItem(String id, String name, String price, String imagePath, String category, String unit, String status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.category = category;
        this.unit = unit;
        this.status = status;
    }
}