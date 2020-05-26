import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * @Author: gq
 * @Date: 2020/5/26 14:35
 */
public class StartingGunFactory {
    enum GunType{
        COUNT_DOWN_LATCH,
        CYCLIC_BARRIER
    }

    public static StartingGun getStartingGun(GunType gunType, int num) {
        switch (gunType) {
            case COUNT_DOWN_LATCH: return new CountDownLatchGun(num);
            case CYCLIC_BARRIER: return new CyclicBarrierGun(num);
        }
        throw new RuntimeException("没有发令枪类型。");
    }

}
