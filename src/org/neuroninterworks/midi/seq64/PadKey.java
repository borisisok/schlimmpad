/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neuroninterworks.midi.seq64;

/**
 *
 * @author boris
 */
public class PadKey {

    private int clear = 0;
    private int copy = 0;
    private int red = 0;
    private int green = 0;
    private int flags = 12;
    private boolean  pressed = false;
    
    public Launchpad.Clip ref=null;

    public boolean isSet() {
        if (green > 0 || red > 0) {
            return true;
        }
        return false;
    }

    public void setRed(int val) {
        this.red = val;
    }

    public int getRed() {
        return this.red;
    }

    public void setGreen(int val) {
        this.green = val;
    }

    public int getGreen() {
        return this.green;
    }

    public void setFlags(int val) {
        this.flags = flags;
    }

    public int getFlags() {
        return this.flags;
    }

    public int getVelocity() {
        return (16 * green) + red + flags;
    }
    public boolean isPressed() {
        System.out.println("isPressed: " + pressed);
        return pressed;
    }

    public void setPressed(boolean value) {
        System.out.println("setPressed: " + value);
        pressed = value;
    }

}
