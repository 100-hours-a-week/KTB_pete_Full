package order;

import menu.MenuTool;
import java.awt.*;

public class OrderTool {
    private MenuTool menuTool;

    public OrderTool(MenuTool Tool) {
        this.menuTool = Tool;
    }

    public int getTotalPrice() {
        return menuTool.getPrice();  // getter 사용
    }

    @Override
    public String toString() {
        return menuTool.getName() + " = " + getTotalPrice() + "원"; // getter 사용
    }
}

