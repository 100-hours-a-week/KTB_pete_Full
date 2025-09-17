package order;

import java.util.*;

public class Order {
    private List<OrderTool> Tools = new ArrayList<>();

    public void addTool(OrderTool it) {
        Tools.add(it);
    }

    // 총합 계산에 필요한 함수
    public int calculateTotal() {
        int sum = 0;
        for (int i = 0; i < Tools.size(); i++) {
            sum += Tools.get(i).getTotalPrice();
        }
        return sum;
    }
}
