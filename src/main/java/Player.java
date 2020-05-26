import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

/**
 * @Author: gq
 * @Date: 2020/5/25 11:58
 */
public class Player implements Callable<Result>, Comparable<Player> {
    public static final int maxVelocity = 14;

    private String name;
    private int number;
    private Result result;

    private static final int minVelocity = 8;

    private Semaphore runWay;

    StartingGun startingGun;

    public Player(String name, int number, Semaphore runWay) {
        this.name = name;
        this.number = number;
        this.runWay = runWay;
    }

    @Override
    public Result call() throws Exception {
        //获得使用跑道权
        try {
            runWay.acquire();   //请求信号量，请求成功继续执行，请求失败阻塞等待
            return doRun();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            runWay.release();   //归还信号量
        }
        return null;
    }

    private Result doRun() throws Exception {
        //当前速度在最大速度与最小速度之间随机取得
        float velocity = (maxVelocity - minVelocity) * new Random().nextFloat() + minVelocity;

        //计算精确的小数作为跑步花费的时间
        float time = new BigDecimal(100).divide(BigDecimal.valueOf(velocity), 3, BigDecimal.ROUND_HALF_UP).floatValue();

        if (startingGun == null)
            throw new RuntimeException("没有发令枪，无法开始比赛");

        //准备好的线程阻塞等待，当条件满足所有线程一起执行
        startingGun.starting();
        System.out.println(number+ " 号选手"+ name + " 起跑");

        synchronized (this) {
            this.wait((long)(time * 1000)); // 释放这个对象的锁，time秒后重新抢占这个对象的锁
        }
        result = new Result(time);
        return result;
    }

    @Override
    public int compareTo(Player p) {

        Result anotherResult = p.getResult();

        if (result == null) {
            return -1;
        }
        if (anotherResult == null) {
            return 1;
        }

        return result.getTime() > anotherResult.getTime() ? 1 : -1;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public Result getResult() {
        return result;
    }

    public void setStartingGun(StartingGun startingGun) {
        this.startingGun = startingGun;
    }
}
