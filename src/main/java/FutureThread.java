import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @Author: gq
 * @Date: 2020/5/25 18:19
 */
public class FutureThread extends Thread {

    private Future<Result> future;
    private Player player;

    private final PriorityBlockingQueue<Player> achievementQueue;

    public FutureThread(Future<Result> future, Player player, PriorityBlockingQueue<Player> achievementQueue) {
        this.future = future;
        this.player = player;
        this.achievementQueue = achievementQueue;
    }

    @Override
    public void run() {
        if (future == null) {
            onError();
            return;
        }
        try {
            if (future.get() == null)
                onError();
        } catch (InterruptedException | ExecutionException e) {
            onError();
            e.printStackTrace();
        }
        achievementQueue.put(player);

        synchronized (achievementQueue) {
            achievementQueue.notify();
        }
        System.out.println(player.getNumber() + " 号选手 " + player.getName() + " 完成此轮比赛。");
    }

    private void onError(){
        System.out.println(player.getNumber() + " 号选手 " + player.getName() + " 退出此轮比赛。");
    }
}
