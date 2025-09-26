package util;

public class TimerThread extends Thread {
    private int seconds;
    private volatile boolean running = true;

    public TimerThread(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public void run() {
        try {
            while (running && seconds > 0) {
                // 10초 단위 출력
                if (seconds % 10 == 0 && seconds > 10) {
                    System.out.println(" 남은 시간: " + seconds + "초");
                }
                // 10초 이하부터는 초 단위 출력
                else if (seconds <= 10) {
                    System.out.println(" 주의! 남은 시간: " + seconds + "초");
                }

                Thread.sleep(1000);
                seconds--;
            }

            if (running && seconds == 0) {
                System.out.println(" 시간이 초과되었습니다! 주문이 취소됩니다.");
                System.exit(0);
            }
        } catch (InterruptedException e) {
            System.out.println("타이머 오류: " + e.getMessage());
        }
    }

    public void stopTimer() {
        running = false;
    }
}
