package menu;

public class SetMenu extends MenuItem {
    private Burger burger;
    private Side side;
    private Drink drink;
    private int discount;

    public SetMenu(int id, String name, Burger burger, Side side, Drink drink, int discount) {
        super(id, name, 0);
        this.burger = burger;
        this.side = side;
        this.drink = drink;
        this.discount = discount;
    }

    @Override
    public int getPrice() {
        return burger.getPrice() + side.getPrice() + drink.getPrice() - discount;
    }

    @Override
    public String toString() {
        return getId() + ") " + getName() + " - 총 " + getPrice() + "원"
                + " (버거: " + burger.getName()
                + ", 사이드: " + side.getName()
                + ", 음료: " + drink.getName()
                + ", 할인: " + discount + "원)";
    }
}
