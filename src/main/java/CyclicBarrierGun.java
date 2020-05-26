import java.util.concurrent.CyclicBarrier;

/**
 * @Author: gq
 * @Date: 2020/5/26 14:49
 */
public class CyclicBarrierGun implements StartingGun {

    private CyclicBarrier cyclicBarrier;

    public CyclicBarrierGun(int parties) {
        this.cyclicBarrier = new CyclicBarrier(parties);
    }

    @Override
    public void starting() throws Exception {
        cyclicBarrier.await();
    }
}
