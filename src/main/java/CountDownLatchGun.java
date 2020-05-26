import java.util.concurrent.CountDownLatch;

/**
 * @Author: gq
 * @Date: 2020/5/26 14:47
 */
public class CountDownLatchGun implements StartingGun {

    private CountDownLatch countDownLatch;

    public CountDownLatchGun(int count) {
        this.countDownLatch = new CountDownLatch(count);
    }

    @Override
    public void starting() throws Exception {
        countDownLatch.countDown();
        countDownLatch.await();
    }
}
