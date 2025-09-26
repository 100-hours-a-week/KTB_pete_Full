package order;

import menu.MenuItem;

public class OrderItem {
    private MenuItem menuItem;

    public OrderItem(MenuItem Tool) {
        this.menuItem = Tool;
    }

    public int getTotalPrice() {
        return menuItem.getPrice();  // getter 사용
    }

    @Override
    public String toString() {
        return menuItem.getName() + " = " + getTotalPrice() + "원"; // getter 사용
    }
}

