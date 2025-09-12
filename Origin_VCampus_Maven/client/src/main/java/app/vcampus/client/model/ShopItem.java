package app.vcampus.client.model;

// 设为 public，以便其他包可以访问
public class ShopItem {
    private final String name;
    private final double price;
    private final String imagePath;
    private final String ownerCardNumber; // 【新增】

    public ShopItem(String name, double price, String imagePath) {
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.ownerCardNumber = "root";
    }
    public ShopItem(String name, double price, String imagePath, String ownerCardNumber) {
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.ownerCardNumber = ownerCardNumber;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }
}
