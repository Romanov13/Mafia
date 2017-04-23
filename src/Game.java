import java.util.*;

/**
 * Created by samsung on 15.04.2017.
 */
public class Game implements Comparator<Player>{

    private String[] phrases = {"Ну что, кто мафия", "Я точно не мафия", "А может это ты?","А что все на меня смотрят?", "А ты чего молчишь?", "Я готов голосовать", "..."};
    private String[] nms = {"Леша", "Паша", "Дима", "Вася", "Света", "Даша", "Аня", "Лера", "Алина", "Миша"};
    private String[] nmsO = {"Лешу", "Пашу", "Диму", "Васю", "Свету", "Дашу", "Аню", "Леру", "Алину", "Мишу"};
    private String[] nmsC = {"Лешей", "Пашей", "Димой", "Васей", "Светой", "Дашей", "Аней", "Лерой", "Алиной", "Мишей"};
    private volatile ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Thread> threads;

    private static final int MAFIA_NUM = 4;

    private static final Object mafia = new Object();
    private static final Object nextRound = new Object();

    private static final Object endDiscussion = new Object();
    private static Game g;


    private void init(){
        threads = new ArrayList<>();

        for(int i = 0; i<nms.length; i++){
            Player p = new Player(g,nms[i], nmsO[i], nmsC[i]);
            players.add(p);
            Thread t = new Thread(p);
            threads.add(t);
            }



            int m = 0;
            while(!(m == MAFIA_NUM)){
                Player mf = getRandomPlayer();
                if(!mf.isMafia()){
                    mf.setMafia();
                    m++;
                }


        }
    }

    void discuss(Player thisP){
        int r = new Random().nextInt(phrases.length);
        String say = phrases[r];
        System.err.println(thisP.getName() + ": " + say);
        synchronized (endDiscussion) {
            try {
                endDiscussion.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void eliminate(Player thisP){
        Player p = getRandomPlayer();
        while (p.getName().equals(thisP.getName())){
            p = getRandomPlayer();
        }
        System.err.println(thisP.getName() + ": Я думаю мафия это " + p.getName());



        int v = p.getVotes();
        p.setVotes(v + 1);

        if (thisP.isMafia()) {
            synchronized (mafia){
                try {
                    mafia.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            synchronized (nextRound){
                try {
                    nextRound.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void shoot(Player thisP){
            String thisN = thisP.getName();
            System.err.println(thisN + " просыпаеся");

            Player p = getRandomPlayer();
            while (p.isMafia()) {
                p = getRandomPlayer();
            }
            System.err.println(thisN + " среляет в " + p.getOccusativeName());
            int s = p.getVotes();
            p.setVotes(s+1);
            synchronized (nextRound) {
                try {
                    nextRound.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


    }

    private Player getRandomPlayer() {
        int r = new Random().nextInt(players.size());
        return players.get(r);
    }

    public static void main(String[] args) {
        g = new Game();
        try (Scanner sc = new Scanner(System.in)) {
            g.init();

            for (Thread t : g.threads) {
                t.start();
            }
            while (g.players.size() > 2) {
                System.err.println("Начало обсужденя.");
                synchronized (nextRound) {

                    nextRound.notifyAll();
                }


                sc.nextLine();
                while (!waiting()) ;

                synchronized (endDiscussion) {
                    System.err.println("Время на обсуждение закончилось.");
                    endDiscussion.notifyAll();
                }
                sc.nextLine();
                while (!waiting()) ;

                Collections.sort(g.players, g.reversed());

                for (Player p : g.players) {
                    System.err.println(p.getName() + p.getVotes());
                }
                Player pl = g.players.get(0);
                boolean tie = false;
                ArrayList<Player> candidates = new ArrayList<>();
                for (Player p : g.players) {

                    if ((p.getVotes() == pl.getVotes()) && (!(p.equals(pl)))) {

                        candidates.add(p);
                        tie = true;
                    }

                }

                if (tie) {
                    System.err.print("Голоса разделлсь между");
                    for (Player p : candidates) {
                        System.err.print(" " + p.getCreativeName() + ",");
                    }
                    System.err.println(pl.getCreativeName() + ".\n");
                    pl = g.getRandomPlayer(candidates);
                }

                pl.eliminated();
                System.err.println(pl.getName() + " исключен");
                if (pl.isMafia()) {
                    System.err.println(pl.getName() + " был мафия");
                } else {
                    System.err.println(pl.getName() + " был гражданским");
                }
                g.players.remove(0);
                for (Player p : g.players) {
                    p.setVotes(0);
                }

                sc.nextLine();
                while (!waiting()) ;


                synchronized (mafia) {
                    System.err.println("Просыпается мафия.");
                    mafia.notifyAll();
                }
                sc.nextLine();
                while (!waiting()) ;

                Collections.sort(g.players, g.reversed());
                Player ps = g.players.get(0);
                boolean udecided = false;
                candidates = new ArrayList<>();


                for (Player p : g.players) {
                    System.err.println(p.getName() + ": " + p.getVotes() + " пуль ");
                }
                for (Player p : g.players) {
                    if ((p.getVotes() == ps.getVotes()) && (!(p.equals(ps)))) {

                        candidates.add(p);
                        udecided = true;
                    }

                }
                if (udecided) {
                    System.err.println("Мафия не определилась. Все осались живы.");
                } else {
                    ps.eliminated();
                    System.err.println(ps.getName() + " исключен");
                    if (ps.isMafia()) {
                        System.err.println(ps.getName() + " был мафия");
                    } else {
                        System.err.println(ps.getName() + " был гражданским");
                    }
                    g.players.remove(0);
                }
                for (Player p : g.players) {
                    p.setVotes(0);
                }


//            Player pl = g.players.get(0);
//            g.playerHashMap.remove(pl.getName());
//            g.players.remove(0);
            }
        }


    }

    private Player getRandomPlayer(ArrayList<Player> candidates) {
        int r = new Random().nextInt(candidates.size());
        return players.get(r);
    }

    private static boolean waiting() {
        boolean w = true;
        for(Thread t: g.threads){
            if(!(t.getState().equals(Thread.State.WAITING) || t.getState().equals(Thread.State.TERMINATED))){
                w = false;
                break;
            }
        }
        return w;
    }

    @Override
    public int compare(Player o1, Player o2) {
        return Integer.compare(o1.getVotes(), o2.getVotes());
    }
}
