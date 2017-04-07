/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neuroninterworks.midi.seq64;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author boris
 */
public class MidiClock implements Runnable {

    private LaunchpadSequencer seq;

    private int bpm = 60;
    private long ppqnano = 0L;
    private int ppqmill = 0;
    private int ppqnpart = 0;
    private boolean running = false;

    /* 
    
     60 BPM
     1 Quaternote per secound
     1 Sec = 100 ms / 24 = 4,1666.. MS per Tick
    
     (( 600 / 120 ) * 100 ) / 24 => millsec
     (( 600 / 120 ) * 100 * 100000) / 24
    
     */
    /*
     long time;
     static int count = 0;
     static String buf_a = "0";
     static String buf_b = "0";
     static int bpm = 61;
     static long ppqnano = (6000000000L / bpm) / 24;
     static int ppqmill = (int) (6000000000L / bpm) / 24000000 ;
     static int ppqnpart =  (int) ( ppqnano - (ppqmill * 1000000));

     */
    public  MidiClock(LaunchpadSequencer l) {
        seq = l;
        setBpm(bpm);
    }

    public void setBpm(int b) {
        bpm = b;
        ppqnano = (6000000000L / bpm) / 24;
        ppqmill = (int) (6000000000L / bpm) / 24000000;
        ppqnpart = (int) (ppqnano - (ppqmill * 1000000));
    }

    public void start() {
        System.out.println("CLOCK START");
        running = true;
        if (running = false) {
            new Thread(this).start();
        }
    }

    public void stop() {
        System.out.println("CLOCK STOP");
        running = false;
    }

    /*
     public static void main(String[] args)
     throws Exception {
     System.out.println("Clock start at: " + System.nanoTime());
     System.out.println("PPQnano offset is: " + ppqnano);
     System.out.println("PPQmill offset is: " + ppqmill);
     System.out.println("PPQnpart offset is: " + ppqnpart);

     while (count++ < 10) {
     buf_a = buf_a + "\n" + System.nanoTime() ;
     buf_a = buf_a + "\n" + System.currentTimeMillis() ;
     }
     */
    /*
     long lasttick= System.nanoTime();
     while (count < 100) {
     if (lasttick + ppqnano >= System.nanoTime()){
     System.out.println("Tick!");
     count ++;
                    
     }
     */

    /*
     while (count < 10) {
     // thread to sleep for 1000 milliseconds plus 500 nanoseconds
     Thread.sleep(ppqmill, ppqnpart);
     System.out.println("Tick!");
     count ++;
     }
     */
    public void run() {

        try {

            while (running) {
                // Sleep in clock interval
                Thread.sleep(ppqmill, ppqnpart);
                seq.tick();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(MidiClock.class.getName()).log(Level.SEVERE, null, ex);
        }
         System.out.println("Clock thread exit");

    }
}
