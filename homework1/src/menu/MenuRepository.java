package menu;

import java.util.*;

public class MenuRepository {
    private List<Burger> burgers = new ArrayList<>();
    private List<Side> sides = new ArrayList<>();
    private List<Drink> drinks = new ArrayList<>();

    public MenuRepository() {
        // 사이드, 음료
        Side fries = new Side(1, "감자튀김", 1800);
        Side cheeseStick = new Side(2, "치즈스틱", 2000);
        sides.add(fries);
        sides.add(cheeseStick);

        Drink cola = new Drink(1, "콜라", 1500);
        Drink cider = new Drink(2, "사이다", 1500);
        drinks.add(cola);
        drinks.add(cider);

        // 버거
        burgers.add(new Burger(1, "불고기버거", 4500));
        burgers.add(new Burger(2, "치즈버거", 5000));
        burgers.add(new Burger(3, "데리버거", 3800));

        // 세트버거
        burgers.add(new SetBurger(4, "불고기버거 세트", 4500, fries, cola, 1000));
        burgers.add(new SetBurger(5, "치즈버거 세트", 5000, cheeseStick, cider, 1000));
        burgers.add(new SetBurger(6, "데리버거 세트", 3800, cheeseStick, cider, 1000));
    }

    // get 메소드 프라이빗 사용 위함
    public List<Burger> getBurgers() {
        return burgers;
    }
    public List<Side> getSides() {
        return sides;
    }
    public List<Drink> getDrinks() {
        return drinks;
    }
}

