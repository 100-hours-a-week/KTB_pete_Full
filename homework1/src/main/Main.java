package main;

import java.util.*;
import menu.*;
import order.*;
import util.TimerThread;

public class Main {
    private Scanner sc = new Scanner(System.in);
    private MenuRepository repo = new MenuRepository();

    public void start() {
        System.out.println("안녕하세요 고객님 롯데리아입니다.");
        // 메뉴판 전체 출력
        System.out.println("\n=== 버거 메뉴 ===");
        for (Burger b : repo.getBurgers()) {
            System.out.println(b);
        }

        System.out.println("\n=== 세트 메뉴 ===");
        for (SetMenu s : repo.getSetMenus()) {
            System.out.println(s);
        }

        System.out.println("\n=== 사이드 메뉴 ===");
        for (Side side : repo.getSides()) {
            System.out.println(side);
        }

        System.out.println("\n=== 음료 메뉴 ===");
        for (Drink drink : repo.getDrinks()) {
            System.out.println(drink);
        }
        System.out.println("\n====================");

        Order order = new Order();

        // 세트 메뉴 선택
        System.out.print("세트 메뉴를 주문하시겠습니까? (번호 입력, 없으면 0): ");
        int setNum = sc.nextInt();
        if (setNum >= 1 && setNum <= repo.getSetMenus().size()) {
            order.addItem(new OrderItem(repo.getSetMenus().get(setNum - 1)));
        }

        // 타이머 가동
        TimerThread timer = new TimerThread(60); // 30초 제한
        timer.start();

        // 버거 선택
        System.out.print("버거를 주문하시겠습니까? (번호 입력, 없으면 0): ");
        int burgerNum = sc.nextInt();
        if (burgerNum >= 1 && burgerNum <= repo.getBurgers().size()) {
            order.addItem(new OrderItem(repo.getBurgers().get(burgerNum - 1)));
        }

        // 사이드 선택
        System.out.print("사이드를 주문하시겠습니까? (번호 입력, 없으면 0): ");
        int sideNum = sc.nextInt();
        if (sideNum >= 1 && sideNum <= repo.getSides().size()) {
            order.addItem(new OrderItem(repo.getSides().get(sideNum - 1)));
        }

        // 음료 선택
        System.out.print("음료를 주문하시겠습니까? (번호 입력, 없으면 0): ");
        int drinkNum = sc.nextInt();
        if (drinkNum >= 1 && drinkNum <= repo.getDrinks().size()) {
            order.addItem(new OrderItem(repo.getDrinks().get(drinkNum - 1)));
        }
        // 총액 계산
        int total = order.calculateTotal();
        System.out.println("총액: " + total + "원");

        // 결제
        Payment payment = new CouponPayment();
        payment.processPayment(total);

        // 타이머 종료
        timer.stopTimer();

        System.out.println("주문이 완료되었습니다.");
        System.out.println("이용해 주셔서 감사합니다.");
    }

    public static void main(String[] args) {
        new Main().start();
    }
}
