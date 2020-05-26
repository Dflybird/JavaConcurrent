import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @Author: gq
 * @Date: 2020/5/25 17:40
 */
public class OneTrack {

    private static final String[] PLAYERS_NAME = new String[]{"p1", "p2","p3", "p4","p5", "p6","p7", "p8","p9", "p10", "p11", "p12"};
    private static final int RUN_WAY_NUM = 5;

    //线程不安全链表，用于保存参赛选手信息
    private final List<Player> players = new LinkedList<>();

    //线程安全优先级队列，用于保存完成初赛选手信息
    private final PriorityBlockingQueue<Player> preliminary = new PriorityBlockingQueue<>();

    //线程安全优先级队列，用于保存完成决赛选手信息
    private final PriorityBlockingQueue<Player> finals = new PriorityBlockingQueue<>();

    public void track() {

        //许可数为5的信号量，代表5根赛道，使用信号量的顺序是随机的
        Semaphore runWay = new Semaphore(RUN_WAY_NUM, false);
        System.out.println("运动员准备进场。");

        //运动员报名，初始化运动员集合
        for (int i = 0; i < PLAYERS_NAME.length; i++) {
            Player player = new Player(PLAYERS_NAME[i], i+1, runWay);
            players.add(player);
        }
        System.out.println("运动员集合完毕，即将开始初赛。");

        //初始化5个裁判对应5个赛道，创建容量固定为5的前程池
        ExecutorService judgment = Executors.newFixedThreadPool(RUN_WAY_NUM);

        //初始化同步器作为发令枪
        int remain = players.size() % 5;
        StartingGun startingGun = StartingGunFactory.getStartingGun(StartingGunFactory.GunType.CYCLIC_BARRIER, RUN_WAY_NUM);
        for (int i = 0; i < players.size(); i++) {
            if (i == players.size()-remain){
                 startingGun = StartingGunFactory.getStartingGun(StartingGunFactory.GunType.COUNT_DOWN_LATCH, remain);
            }

            //初赛开始，等待选手跑完，将结果添加进队列
            Player p = players.get(i);
            p.setStartingGun(startingGun);
            Future<Result> future = judgment.submit(p);
            new FutureThread(future, p, preliminary).start();
        }

        System.out.println("初赛开始...");

        //等待所有选手跑完进行排名，前五进入决赛
        synchronized (preliminary) {
            while (preliminary.size() < players.size()) {
                try {
                    preliminary.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("----------------------------");
        System.out.println("初赛结果已经统计完毕，即将开始决赛。");

        //初赛结束，选出初赛前五名开始决赛，准备发令枪
        startingGun = StartingGunFactory.getStartingGun(StartingGunFactory.GunType.COUNT_DOWN_LATCH, RUN_WAY_NUM);
        System.out.println("决赛开始...");
        for (int i = 0; i < 5; i++) {
            Player p = preliminary.remove();
            p.setStartingGun(startingGun);

            //等待选手跑完，将结果添加进队列
            Future<Result> future = judgment.submit(p);
            new FutureThread(future, p, finals).start();
        }

        //等待所有参加决赛的选手跑完
        synchronized (finals) {
            while (finals.size() < 5) {
                try {
                    finals.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("-----------------------------");
        System.out.println("决赛结果已经统计完毕，即将开始颁奖。");


        for (int i = 0; i < 3; i++) {
            Player player = finals.remove();
            switch (i) {
                case 0:
                    System.out.println(player.getNumber() +  "号选手 " +player.getName() + " 以 " + player.getResult().getTime() + " 秒完成比赛，获得第一名。");
                    break;
                case 1:
                    System.out.println(player.getNumber() +  "号选手 " +player.getName() + " 以 " + player.getResult().getTime() + " 秒完成比赛，获得第二名。");
                    break;
                case 2:
                    System.out.println(player.getNumber() +  "号选手 " +player.getName() + " 以 " + player.getResult().getTime() + " 秒完成比赛，获得第三名。");
                    break;
            }
        }
        judgment.shutdown();
        System.out.println("比赛结束!");
    }
}
