package order;

import java.util.*;

public class Order {
    private List<OrderTool> Tools = new ArrayList<>();

    public void addTool(OrderTool it) {
        Tools.add(it);
    }

    // 총합 계산에 필요한 함수
//    public int calculateTotal() {
//        int sum = 0;
//        return sum;
//    }
}
