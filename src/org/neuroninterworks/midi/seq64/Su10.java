/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neuroninterworks.midi.seq64;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Transmitter;

/**
 *
 * @author boris
 */
public class Su10 {

    String inputDeviceName = "1(UM-2G)";
    private MidiDevice inputDevice = null;

    public Su10() {
        try {
/*
            for (Info info : MidiSystem.getMidiDeviceInfo()) {
                System.out.println("MIDI: *" + info.getName()+"*");
            }
*/
            MidiDevice.Info infoClockIn = MidiCommon.getMidiDeviceInfo(inputDeviceName, false);
            inputDevice = MidiSystem.getMidiDevice(infoClockIn);
            Transmitter t = inputDevice.getTransmitter();
            t.setReceiver(new DumpReceiver(System.out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Su10 su = new Su10();
        try {
            while (true) {
                Thread.currentThread().sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
