package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private AtomicInteger health;
    
    private int defaultDamageValue;

    private final CopyOnWriteArrayList<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());
    
    private boolean isPause;



    public Immortal(String name, CopyOnWriteArrayList<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = new AtomicInteger(health);
        this.defaultDamageValue=defaultDamageValue;
    }

    public void run() {

        while (true) {
            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);

            this.fight(im);
            synchronized(this){
                /*
                Thread.State state = getState();
                if(state == Thread.State.BLOCKED) {
                    System.out.println("Blocked");
                } else {
                    System.out.println("Not blocked");
                }*/
                while (isPause){
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Immortal.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                    
                    
            }
            
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public void fight(Immortal i2) {
        synchronized(immortalsPopulation) {
            if (getHealth() <= 0) {
                immortalsPopulation.remove(this);
                //updateCallback.processReport(this + " says:" +" I am already dead!\n");
            }else if (i2.getHealth() >0) {
                i2.health.addAndGet(- defaultDamageValue);
                this.health.addAndGet(defaultDamageValue);
                updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
            } else {
                immortalsPopulation.remove(i2);
                //updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
            }
        }
    }

    synchronized void pause(){
        isPause=true;
        this.notifyAll();
    }
    
    synchronized void continues(){
        isPause=false;
        this.notifyAll();
    }
    
    public void changeHealth(int v) {
        health.set(v);
    }

    public int getHealth() {
        return health.get();
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
