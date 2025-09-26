package order;

import java.util.*;

public class Order {
    private List<OrderItem> Items = new ArrayList<>();

    public void addItem(OrderItem it) {
        Items.add(it);
    }

    // 총합 계산에 필요한 함수
    public int calculateTotal() {
        int sum = 0;
        for (int i = 0; i < Items.size(); i++) {
            sum += Items.get(i).getTotalPrice();
        }
        return sum;
    }
}
