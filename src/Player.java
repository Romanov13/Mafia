/**
 * Created by samsung on 15.04.2017.
 */
public class Player implements Runnable {

    private Game game;
    private boolean isActive;
    private boolean isMafia = false;

    String getName() {
        return name;
    }

    private String name;

    String getOccusativeName() {
        return occusativeName;
    }

    String getCreativeName() {
        return creativeName;
    }

    private String occusativeName;
    private String creativeName;

    private volatile int votes = 0;

    synchronized int getVotes() {
        return votes;
    }

    synchronized void setVotes(int votes) {
        this.votes = votes;
    }




    Player(Game game, String name, String occusativeName, String creativeName) {
        this.game = game;
        this.name = name;
        this.occusativeName = occusativeName;
        this.creativeName = creativeName;
        isActive = true;

    }

    @Override
    public void run() {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(isActive) {

            game.discuss(this);

            game.eliminate(this);
            if(isMafia && isActive){
                game.shoot(this);
            }

        }
    }

    void eliminated(){
        isActive = false;
    }

    boolean isMafia() {
       return isMafia;
    }

    void setMafia() {
        isMafia = true;
    }
}
