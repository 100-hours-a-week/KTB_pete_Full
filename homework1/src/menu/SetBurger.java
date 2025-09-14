package menu;

public class SetBurger extends Burger {
    private Side side;
    private Drink drink;
    private int discount;

    public SetBurger(int id, String name, int price, Side side, Drink drink, int discount) {
        super(id, name, price);
        this.side = side;
        this.drink = drink;
        this.discount = discount;
    }

    @Override
    public int getPrice() {
        return super.getPrice() + side.getPrice() + drink.getPrice() - discount;
    }

    @Override
    public String toString() {
        return super.getName() + " + 세트(사이드: " + side.getName() + ", 음료: " + drink.getName() +
                ", 할인: " + discount + "원)";
    }
}
