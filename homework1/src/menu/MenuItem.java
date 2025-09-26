package menu;

public abstract class MenuItem {
    private int id;
    private String name;
    private int price;

    public MenuItem(int id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    // get 메소드 프라이빗 가져오기 위함
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return id + ") " + name + " - " + price + "원";
    }
}