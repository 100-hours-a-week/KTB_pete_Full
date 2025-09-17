package main;

import java.util.*;
import menu.*;
import order.*;

public class Main {
    private Scanner sc = new Scanner(System.in);
    private MenuRepository repo = new MenuRepository();

    public void start() {
        System.out.println("안녕하세요 고객님 롯데리아입니다.");
        Order order = new Order();

        // 버거 선택
        for (int i = 0; i < repo.getBurgers().size(); i++) {
            System.out.println(repo.getBurgers().get(i));
        }
        System.out.print("버거 번호를 선택해주세요: ");
        int burgerNum = sc.nextInt();
        if (burgerNum >= 1 && burgerNum <= repo.getBurgers().size()) {
            order.addTool(new OrderTool(repo.getBurgers().get(burgerNum - 1)));
        }

        // 사이드 선택
        for (int i = 0; i < repo.getSides().size(); i++) {
            System.out.println(repo.getSides().get(i));
        }
        System.out.print("사이드 번호를 선택해주세요: ");
        int sideNum = sc.nextInt();
        if (sideNum >= 1 && sideNum <= repo.getSides().size()) {
            order.addTool(new OrderTool(repo.getSides().get(sideNum - 1)));
        }

        // 음료 선택
        for (int i = 0; i < repo.getDrinks().size(); i++) {
            System.out.println(repo.getDrinks().get(i));
        }
        System.out.print("음료 번호를 선택해주세요: ");
        int drinkNum = sc.nextInt();
        if (drinkNum >= 1 && drinkNum <= repo.getDrinks().size()) {
            order.addTool(new OrderTool(repo.getDrinks().get(drinkNum - 1)));
        }

        // 총액 계산
        int total = order.calculateTotal();
        System.out.println("총액: " + total + "원");

        // 결제
        Payment payment = new Payment();
        payment.methodPayment(total);

        System.out.println("주문이 완료되었습니다.");
        System.out.println("이용해 주셔서 감사합니다.");
    }

    public static void main(String[] args) {
        new Main().start();
    }
}
