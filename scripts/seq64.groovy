/*
for (item in launchpad.outDevs){
println "item: ${item}"
}
 */

//import org.neuroninterworks.midi.seq64.GroovyPlug;

//println "item: ${launchpad.mainSequencer}"

/*
launchpad.print("1");
launchpad.padPrinter.printScroll("   ");
launchpad.print("2");
launchpad.padPrinter.printScroll("   ");
launchpad.print("3");
launchpad.padPrinter.printScroll("   ");

launchpad.print("H e y  Y o ! ! ! ! ! !");
launchpad.padPrinter.printScroll("3 2 1   ");
launchpad.clearGrid();
 */



// example registration of shortkey functions and objects

// implement an class: simple hash
def panicbutton = [
    exec: { println "Hello panic ${this} ${launchpad} " },
] as org.neuroninterworks.midi.seq64.GroovyPlug



def clipnavigator_right = [
    exec: { println "clipnavigator_right " 
        println("xPos: " + launchpad.currentClip.clipinfo.xpos ) 
        println("xPos: " + launchpad.currentClip.clipinfo.ypos ) 
        if (launchpad.currentClip.clipinfo.xpos < 8){
            launchpad.switchView(launchpad.currentClip.clipinfo.xpos + 1, launchpad.currentClip.clipinfo.ypos);
            
        }
    },
] as org.neuroninterworks.midi.seq64.GroovyPlug


def clipnavigator_left = [
    exec: { println "clipnavigator_left " 
        println("xPos: " + launchpad.currentClip.clipinfo.xpos ) 
        println("xPos: " + launchpad.currentClip.clipinfo.ypos ) 
        if (launchpad.currentClip.clipinfo.xpos > 0){
            launchpad.switchView(launchpad.currentClip.clipinfo.xpos - 1, launchpad.currentClip.clipinfo.ypos);
            
        }
    },
] as org.neuroninterworks.midi.seq64.GroovyPlug
    
def clipnavigator_up = [
    exec: { println "clipnavigator_up " 
        println("xPos: " + launchpad.currentClip.clipinfo.xpos ) 
        println("xPos: " + launchpad.currentClip.clipinfo.ypos ) 
        if (launchpad.currentClip.clipinfo.ypos < 8){
            launchpad.switchView(launchpad.currentClip.clipinfo.xpos, launchpad.currentClip.clipinfo.ypos + 1);
            
        }
    },
] as org.neuroninterworks.midi.seq64.GroovyPlug

def clipnavigator_down = [
    exec: { println "clipnavigator_down " 
        println("xPos: " + launchpad.currentClip.clipinfo.xpos ) 
        println("xPos: " + launchpad.currentClip.clipinfo.ypos ) 
        if (launchpad.currentClip.clipinfo.ypos > 0){
            launchpad.switchView(launchpad.currentClip.clipinfo.xpos, launchpad.currentClip.clipinfo.ypos - 1);
            
        }
    },
] as org.neuroninterworks.midi.seq64.GroovyPlug


def clip_step_dec = [
    exec: { println "clip_step_dec " 
        println("xPos: " + launchpad.currentClip.clipinfo.xpos ) 
        println("xPos: " + launchpad.currentClip.clipinfo.ypos ) 
        if (launchpad.currentClip.clipinfo.gridXSize > 1){
            launchpad.currentClip.clipinfo.gridXSize--;
        }
    },
] as org.neuroninterworks.midi.seq64.GroovyPlug

def clip_step_inc = [
    exec: { println "clip_step_inc " 
        println("xPos: " + launchpad.currentClip.clipinfo.xpos ) 
        println("xPos: " + launchpad.currentClip.clipinfo.ypos ) 
        launchpad.currentClip.clipinfo.gridXSize++;
    },
] as org.neuroninterworks.midi.seq64.GroovyPlug


// implement an class: simple hash
def randombutton = [
    
    exec: { println "Random Notes: on " 

        ((javax.swing.JTextArea) it).append("Randomizing: " + launchpad.currentClip);
        
        if (launchpad.currentClip) { 
            def clip = launchpad.currentClip;
            clip.stop();    

    
 
            //    clip.events.each{ clip. println it }
            // create some 
            clip.deleteNotes();

            int notemax = 10;
            int noteprobtresh = 8;
            String out = new String();
            int note = 0;
            int trans = 0;
            int acc = 0;
            int slide = 0;
            for (int step=0; step < 16; step++) {
                if ( (Math.random()*notemax) < noteprobtresh ) {
                    note = (int) (Math.random()*12)+1;
                    trans = (int) ((Math.random()*4)-2);
                    acc = (int) (Math.random()*2);
                    slide = (int) (Math.random()*2);
                    time = (int) (Math.random()*2);
                    clip.setTB303Note(step, note , trans , acc, slide, 1);
                    //out = out + launchpad.notesByVal.get(note) +","+ trans +","+ acc +","+ slide +","+ time +"";
                    out = out + "clip.setTB303Note("+step+",\""+launchpad.notesByVal.get(note) +"\","+ trans +","+ acc +","+ slide +","+ time +")";
                } else {
                    out = out +  "----------";
                }
                
                out = out + "\n";
            }
            println(out);

            
            

            clip.startInSync()
            // move visible area to the notes we create
            //            clip.clipinfo.yoff=56  
            //    println "i found  a clip: ${clip} ";
            //    clip.events.each{ println it }
        }
    },
] as org.neuroninterworks.midi.seq64.GroovyPlug


// implement an class: simple hash
def acidbutton = [
    
    exec: { println "Random Notes " 

        if (launchpad.currentClip) { 
            def clip = launchpad.currentClip;

            clip.stop();    
            
            //            clip.clipinfo.yoff=(127-12)  // move visible area to the notes we create
        
            //    clip.events.each{ clip. println it }  // iterate over all notes
    
            // delete all
            clip.deleteNotes();
            
            // create some
            /*
            // create some:  setTB303Note(step, note, accent, slide, length)
            clip.setTB303Note(0, "A", -1, 0, 0, 1)
            clip.setTB303Note(1, "D", 0, 0, 1, 1)
            clip.setTB303Note(2, "D", 0, 0, 0, 1)
            clip.setTB303Note(3, "B", -1, 0, 0, 1)
            clip.setTB303Note(4, "B", -1, 0, 1, 1)
            clip.setTB303Note(5, "B", -1, 0, 1, 1)
            clip.setTB303Note(6, "B", -1, 0, 0, 1)
            clip.setTB303Note(7, "G#", 0, 0, 1, 1)
            clip.setTB303Note(8, "G#", 0, 0, 1, 1)
            clip.setTB303Note(9, "G#", 0, 0, 0, 1)
            clip.setTB303Note(10, "F#", 0, 0, 1, 1)
            clip.setTB303Note(11, "A#", 0, 0, 1, 1)
            clip.setTB303Note(12, "A#", 0, 0, 0, 1)
            clip.setTB303Note(13, "B", 0, 1, 1, 1)
            clip.setTB303Note(14, "B", 0, 1, 0, 1)
            clip.setTB303Note(15, "B", 0, 0, 0, 1)
             */
           
            // March acid pattern
            // create some:  setTB303Note(step, note, accent, slide, length)
            clip.setTB303Note(0, "C", 0, 1, 0, 1)
            clip.setTB303Note(1, "C", -1, 1, 0, 1)
            clip.setTB303Note(2, "C", 0, 1, 1, 1)
            clip.setTB303Note(3, "C", 1, 1, 0, 1)
            clip.setTB303Note(4, "C", 1, 1, 1, 1)
            clip.setTB303Note(5, "C", 0, 1, 1, 1)
            clip.setTB303Note(6, "G", 0, 1, 0, 1)
            clip.setTB303Note(7, "F#", 0, 1, 1, 1)
            clip.setTB303Note(8, "F", 0, 0, 1, 1)
            clip.setLoopEndPoint(9+12);

            /*    
            
            // create some:  setTB303Note(step, note, accent, slide, length)
            clip.setTB303Note(0, "F", 0, 0, 1, 0)
            clip.setTB303Note(1, "F", 0, 0, 1, 0)
            clip.setTB303Note(2, "F", 0, 0, 0, 0)
            clip.setTB303Note(3, "F", 0, 1, 0, 0)
            // clip.setTB303Note(4, "F", 0, 1, 0, 0)
            clip.setTB303Note(5, "F", -1, 0, 1, 0)
            clip.setTB303Note(6, "F", -1, 0, 0, 0)
            clip.setTB303Note(7, "F", 0, 1, 0, 0)
            clip.setTB303Note(8, "F", 0, 1, 0, 0)
            clip.setTB303Note(9, "D#", 0, 0, 1, 0)
            clip.setTB303Note(10, "D#", -1, 0, 1, 0)
            clip.setTB303Note(11, "D#", 0, 0, 0, 0)
            clip.setTB303Note(12, "D#", 1, 0, 1, 0)
            clip.setTB303Note(13, "F", 1, 0, 0, 0)
            clip.setTB303Note(14, "G#", 1, 1, 0, 0)
            clip.setTB303Note(15, "F", 1, 1, 0, 0)
             */

            clip.startInSync()
    
            //    println "i found  a clip: ${clip} ";
            //    clip.events.each{ println it }
        }

    },
] as org.neuroninterworks.midi.seq64.GroovyPlug



// implement an class: full fleged
 class FadeIn implements org.neuroninterworks.midi.seq64.GroovyPlug {
    public void exec(javax.swing.JTextArea output) {
        println "hello, world AGGRRRRRRRRRRR ${this}"
    }
} 



//launchpad.groovyPlugs.put("a",acidbutton); // register keyboard event, style 1

if (!launchpad.groovyPlugs.containsKey("p")) { launchpad.groovyPlugs.put("p",panicbutton);} // register keyboard event, style 1

if (!launchpad.groovyPlugs.containsKey("w")) { launchpad.groovyPlugs.put("w",clipnavigator_up);} // register keyboard event, style 1
if (!launchpad.groovyPlugs.containsKey("s")) { launchpad.groovyPlugs.put("s",clipnavigator_down);} // register keyboard event, style 1
if (!launchpad.groovyPlugs.containsKey("a")) { launchpad.groovyPlugs.put("a",clipnavigator_left);} // register keyboard event, style 1
if (!launchpad.groovyPlugs.containsKey("d")) { launchpad.groovyPlugs.put("d",clipnavigator_right);} // register keyboard event, style 1

if (!launchpad.groovyPlugs.containsKey("q")) { launchpad.groovyPlugs.put("q",clip_step_inc);} // register keyboard event, style 1
if (!launchpad.groovyPlugs.containsKey("e")) { launchpad.groovyPlugs.put("e",clip_step_dec);} // register keyboard event, style 1


launchpad.groovyPlugs.put("r",randombutton); // register keyboard event, style 1
if (!launchpad.groovyPlugs.containsKey("i")) { launchpad.groovyPlugs.put("i",new FadeIn());} // register keyboard event, style 2





    
//    println "i found  a clip: ${clip} ";
//    clip.events.each{ println it }


// example thread create and terminate

//launchpad.groovyPlugs.remove("boserjunge2").run=false;  // terminate a thread 

/*
// start and register a thread
launchpad.groovyPlugs.put("boserjunge2", 
Thread.start{
boolean run = true;
while (run){
println "${this} please kill me";
sleep(1000); 
}
println launchpad.toString(this);
}
);
 */



//println launchpad.toString(launchpad.groovyPlugs); // example class reflection dump

/* SCRATCHPAD FOR THE GROOVY SHELL

 
// SP-505 / Programm change / acive pad bank
import javax.sound.midi.ShortMessage;
launchpad.cliphash.get(0+"_"+0)._send(ShortMessage.PROGRAM_CHANGE,1,0);
print "OK\n";



//// Threads

// Thread example - Animate a midi cc
import javax.sound.midi.ShortMessage;
name="cc1"
i=0
launchpad.groovyThreads.put(name, Thread.start {
    sleep 100
    while( launchpad.groovyThreads.get(name) != null ) {
        i=i+0.05
        v=Math.round(Math.sin(i)*63+63)
 //       launchpad.cliphash.get(0+"_"+0)._send(ShortMessage.PROGRAM_CHANGE,1,0);
        sleep 50
        print "thread is running   " + i + " " + v + "\n"
    }
    println "Thread stopped: " + name 
})

 



// MFB SL2 FREQ (14Bit)
byte [] data = new byte [2];
import javax.sound.midi.ShortMessage;
name="SL2FREQ"
i=0
launchpad.groovyThreads.put(name, Thread.start {
    sleep 100
    while( launchpad.groovyThreads.get(name) != null ) {
        i=i+0.02
        int v=Math.round(Math.sin(i)*8192+8192)
       data[0] = (byte) (v >> 7 & 0x7F);
       data[1] = (byte) (v & 0x7F);    
        launchpad.cliphash.get(4+"_"+0)._send(ShortMessage.CONTROL_CHANGE,15,( data[1] & 0x7F));
        launchpad.cliphash.get(4+"_"+0)._send(ShortMessage.CONTROL_CHANGE,47,( data[1] & 0x7F));
        sleep 200
        print "thread is running   " + i + "  " + v + "  " + Integer.toBinaryString(v) + "  " + Integer.toBinaryString( data[0] ) + "  " + Integer.toBinaryString( data[1] ) + "  " + ( data[0] ) + "  " + ( data[1] ) + "\n"
    }
    println "Thread stopped: " + name 
})


 
// Thread management / list
launchpad.groovyThreads.each {
   println "$it.key"
}


// Thread management / stop
launchpad.groovyThreads.remove("cc1")





//  Crazy Example -> Fibonacci with Functional Java
import fj.*
import fj.control.parallel.Strategy
import static fj.Function.curry as fcurry
import static fj.P1.curry as pcurry
import static fj.P1.fmap
import static fj.control.parallel.Actor.actor
import static fj.control.parallel.Promise.*
import static fj.data.List.range
import static java.util.concurrent.Executors.*

CUTOFF  = 12   // not worth parallelizing for small n
START   = 8
END     = 18
THREADS = 4

pool = newFixedThreadPool(THREADS)
su   = Strategy.executorStrategy(pool)
spi  = Strategy.executorStrategy(pool)
add  = fcurry({ a, b -> a + b } as F2)
nums = range(START, END + 1)

println "Calculating Fibonacci sequence in parallel..."

serialFib = { n -> n < 2 ? n : serialFib(n - 1) + serialFib(n - 2) }

print = { results ->
  def n = START
  results.each{ println "n=${n++} => $it" }
  pool.shutdown()
} as Effect

calc = { n ->
  n < CUTOFF ?
    promise(su, P.p(serialFib(n))) :
    calc.f(n - 1).bind(join(su, pcurry(calc).f(n - 2)), add)
} as F

out = actor(su, print)
join(su, fmap(sequence(su)).f(spi.parMapList(calc).f(nums))).to(out)

*/