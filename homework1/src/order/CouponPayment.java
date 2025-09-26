package order;

import java.util.Scanner;

public class CouponPayment extends BasicPayment {
    private Scanner sc = new Scanner(System.in);
    private static final int COUPON_CODE = 12345;  // 정해진 쿠폰 코드
    private static final int DISCOUNT = 1000;

    @Override
    public void processPayment(int total) {
        System.out.print("쿠폰을 가지고 계십니까? (Y/N): ");
        String answer = sc.next();

        if (answer.equalsIgnoreCase("Y")) {
            System.out.print("쿠폰 코드를 입력해주세요: ");
            int code = sc.nextInt();
            if (code == COUPON_CODE) {
                System.out.println("쿠폰이 적용되었습니다! " + DISCOUNT + "원 할인");
                total -= DISCOUNT;
                System.out.println("할인 적용 후 결제 금액: " + total + "원");
            } else {
                System.out.println("잘못된 쿠폰 코드입니다. 할인 적용 불가");
            }
        }

        // 부모(BasicPayment)의 결제 로직 실행
        super.processPayment(total);
    }
}
