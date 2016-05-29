import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

/**
 * This example code demonstrates how to perform simple
 * blinking LED logic of a GPIO pin on the Raspberry Pi
 * using the Pi4J library and with interrupt implementation
 *
 * @author Vasco Craveiro Costa
 */
public class BlinkGpioExample {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("<--Pi4J--> GPIO Blink Example ... started.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #07 as an output pins and blink
        final GpioPinDigitalOutput led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07);

        // continuously blink the led every 1/4 second
        led.blink(250);

        System.out.println(" ... the LED will continue blinking until the program is terminated.");
        System.out.println(" ... PRESS <CTRL-C> TO STOP THE PROGRAM.");
        
        try{
        	while(true) {
                Thread.sleep(500);
            }
        }catch(InterruptedException e){
			
        }finally{
        	led.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
            
            // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
            gpio.shutdown();   //<--- implement this method call if you wish to terminate the Pi4J GPIO controller
        }
    }
}
