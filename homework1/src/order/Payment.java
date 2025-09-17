package order;

import java.util.Scanner;

public class Payment {
    private Scanner sc = new Scanner(System.in);

    // 결제 메서드
    public void methodPayment(int totalMoney) {
        while (true) {
            System.out.print("결제 금액을 입력해주세요: ");
            int payment = sc.nextInt();

            // 금액 조건문
            if (payment < totalMoney) {
                System.out.println("금액이 부족합니다. 다시 입력해주세요.");
            } else if (payment == totalMoney) {
                System.out.println("이용해 주셔서 감사합니다.");
                break;
            } else {
                System.out.println("직원들의 노고에 감사하여 팁을 주시다니 감사합니다.");
                System.out.println("다음에 더 좋은 서비스로 모시겠습니다.");
                break;
            }
        }
    }
}
