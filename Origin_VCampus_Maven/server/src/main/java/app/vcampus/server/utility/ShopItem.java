package app.vcampus.server.utility;

// 设为 public，以便其他包可以访问
public class ShopItem {
    private final String name;
    private final double price;
    private final String imagePath;

    public ShopItem(String name, double price, String imagePath) {
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getImagePath() { return imagePath; }
}