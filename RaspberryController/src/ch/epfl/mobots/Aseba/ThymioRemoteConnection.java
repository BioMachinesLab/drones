package ch.epfl.mobots.Aseba;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.mobots.AsebaNetwork;

public class ThymioRemoteConnection {

	protected AsebaNetwork recvInterface;
	protected final String NODE_REF = "thymio-II";
	protected final short NODE_ID;
	
//	protected String ledControlFile = "test-medulla.aesl";
	
	public ThymioRemoteConnection(AsebaNetwork recvInterface) {
		this.recvInterface = recvInterface;
		//to be used in robot to robot communication via aseba network.
		NODE_ID = recvInterface.GetNodeId(NODE_REF);
		System.out.println("ID: " + NODE_ID);
//		this.recvInterface.LoadScripts(ledControlFile);
	}
	
	// TODO: thymio node id in the aseba network (remapping ids may be needed, to check when MRS are considered)
	public short getNodeID(){
		return this.NODE_ID;
	}
	
	//thymio variables
	public List<String> getVariablesList(){
		return recvInterface.GetVariablesList(NODE_REF);
	}
	
	//in the aseba network
	public List<String> getNodesList(){
		return recvInterface.GetNodesList();
	}

	/*************************************
	 * + LOCAL INFRARED COMMUNICATION
	 * 
	 * Local communication Thymio can use its horizontal infrared distance
	 * sensors to communicate a value to peer robots within a range of about 15
	 * cm. This value is sent at 10 Hz while processing the distance sensors.
	 * Thymio sends an 11-bit value (but future firmware could use one of the
	 * bits for internal use, thus it is better to stay within 10 bits). To use
	 * the communication, call the prox.comm.enable(state) function, with 1 in
	 * state to enable communication or 0 to turn it off. If the communication
	 * is enabled, the value in the prox.comm.tx variable is transmitted every
	 * 100 ms. When Thymio receives a value, the event prox.comm is fired and
	 * the value is in the prox.comm.rx variable.
	 */

	public void enableLocalCommunication() {
		ArrayList<Short> list = new ArrayList<Short>(1);
		short value = 1;
		list.add(value);
		recvInterface.SetVariable(NODE_REF, "prox.comm.enable", list);
	}

	public void disableLocalCommunication() {
		ArrayList<Short> list = new ArrayList<Short>(1);
		short value = 0;
		list.add(value);
		recvInterface.SetVariable(NODE_REF, "prox.comm.enable", list);
	}

	public void setLocalCommunicationValue(short value) {
		ArrayList<Short> list = new ArrayList<Short>(1);
		list.add(value);
		recvInterface.SetVariable(NODE_REF, "prox.comm.tx", list);
	}

	public short getLocalCommunicationValue() {
		List<Short> list = recvInterface.GetVariable(NODE_REF, "prox.comm.rx");
		return list.get(0);
	}

	/***************************************************************
	 * SENSOR READINGS
	 *
	 * prox.horizontal[0] : front left prox.horizontal[1] : front middle-left
	 * prox.horizontal[2] : front middle prox.horizontal[3] : front middle-right
	 * prox.horizontal[4] : front right prox.horizontal[5] : back left
	 * prox.horizontal[6] : back right Thymio updates this array at a frequency
	 * of 10 Hz, and generates the prox event after every update.
	 */
	public List<Short> getProximitySensorValues() {
		return recvInterface.GetVariable(NODE_REF, "prox.horizontal");
	}

	/*****
	 * Thymio holds 2 ground distance sensors. These sensors are located at the
	 * front of the robot. As black grounds appear like no ground at all (black
	 * absorbs the infrared light), these sensors can be used to follow a line
	 * on the ground. Three arrays hold the values of these sensors: ------
	 * prox.ground.ambiant : ambient light intensity at the ground, varies
	 * between 0 (no light) and 1023 (maximum light)
	 * 
	 * ------ prox.ground.reflected : amount of light received when the sensor
	 * emits infrared, varies between 0 (no reflected light) and 1023 (maximum
	 * reflected light)
	 * 
	 * ------ prox.ground.delta : difference between reflected light and ambient
	 * light, linked to the distance and to the ground colour. For each array,
	 * the index 0 corresponds to the left sensor and the index 1 to the right
	 * sensor. As with the distance sensors, Thymio updates this array at a
	 * frequency of 10 Hz.
	 */
	public List<Short> getGroundAmbientLightIntensity() {
		return recvInterface.GetVariable(NODE_REF, "prox.ground.ambiant");
	}

	public List<Short> getGroundReflectedLight() {
		return recvInterface.GetVariable(NODE_REF, "prox.ground.reflected");
	}

	public List<Short> getGroundDelta() {
		return recvInterface.GetVariable(NODE_REF, "prox.ground.delta");
	}

	/*****
	 * Thymio contains a 3-axes accelerometer. An array of 3 variables, acc,
	 * holds the values of the acceleration along these 3 axes:
	 * 
	 * acc[0] : x-axis (from right to left, positive towards left)
	 * 
	 * acc[1] : y-axis (from front to back, positive towards the rear)
	 * 
	 * acc[2] : z-axis (from top to bottom, positive towards ground) The values
	 * in this array vary from -32 to 32, where 1 g (the acceleration of the
	 * earth's gravity) corresponding to the value 23. Thymio updates this array
	 * at a frequency of 16 Hz, and generates the acc event after every update.
	 * Moreover, when a shock is detected, a tap event is emitted
	 * 
	 * @return a list with the accelerometers readings
	 */
	public List<Short> getAccelerometer() {
		return recvInterface.GetVariable(NODE_REF, "acc");
	}

	/**************
	 * Temperature sensor The temperature variable holds the current temperature
	 * in tenths of a degree Celsius. Thymio updates this value at 1 Hz and
	 * generates the temperature event after every update.
	 */
	public List<Short> getTemperature() {
		return recvInterface.GetVariable(NODE_REF, "temperature");
	}

	/*****************************************************************************
	 * CONTROL LEDs
	 * *************************************************************
	 * ************** Thymio holds many LEDs spread around its body. Most of
	 * them are associated with sensors and can highlight their activations: by
	 * default, the intensity of the LED is linked to the sensor value. However,
	 * once LEDs are used in the code, the programmer takes over control and
	 * they no longer reflect the sensor values.
	 * 
	 * Native functions allow the various LEDs to be controlled. For all LEDs,
	 * their intensity values range from 0 (off) to 32 (fully lit).
	 * 
	 * NOTE: There are also other LEDs that the user cannot control: 3 green
	 * LEDs on the top of the robot show the battery voltage a blue and a red
	 * LED on the back of the robot show the charge status a red LED on the back
	 * of the robot shows the SD-card status
	 */

	/****************************************************************
	 * The LED circle on top of the robot 8 yellow LEDs make up a circle on top
	 * of the robot, around the buttons.
	 * 
	 * Default activation: reflects the values of the accelerometer. All LEDs
	 * are off when the robot is horizontal. When the robot tilts, a single LED
	 * shows the lowest point, with an intensity proportional to the tilt angle.
	 * 
	 * leds.circle(led 0, led 1, led 2, led 3, led 4, led 5, led 6, led 7) where
	 * led 0 sets the intensity of the LED at the front of the robot, the others
	 * are numbered clockwise.
	 * 
	 */
	public void setTopLEDCircle(short led0, short led1, short led2, short led3,
			short led4, short led5, short led6, short led7) {
		List<Short> ledIntensity = new ArrayList<Short>(8);
		ledIntensity.add(led0);
		ledIntensity.add(led1);
		ledIntensity.add(led2);
		ledIntensity.add(led3);
		ledIntensity.add(led4);
		ledIntensity.add(led5);
		ledIntensity.add(led6);
		ledIntensity.add(led7);
		recvInterface.SendEventName("setTopLEDCircle", ledIntensity);
	}

	/*********************************
	 * The RGB LEDs There are two RGB LEDs on the top of robot, driven together.
	 * These are the LEDs that show the behaviour of the robot. There are two
	 * other RGB LEDs on the bottom of the robot, which can be driven
	 * separately.
	 * 
	 * Default activation: off when in Aseba mode.
	 * 
	 * leds.top(red, green, blue) sets the intensities of the top LEDs.
	 * leds.bottom.left(red, green, blue) sets the intensities of the
	 * bottom-left LED. leds.bottom.right(red, green, blue) sets the intensities
	 * of the bottom-right LED.
	 */
	public void setTopRGBLED(short red, short green, short blue) {
		ArrayList<Short> ledIntensity = new ArrayList<Short>(3);
		ledIntensity.add(red);
		ledIntensity.add(green);
		ledIntensity.add(blue);
		recvInterface.SetVariable(NODE_REF, "leds.top", ledIntensity);
	}

	public void setBottomLeftRGBLED(short red, short green, short blue) {
		ArrayList<Short> ledIntensity = new ArrayList<Short>(3);
		ledIntensity.add(red);
		ledIntensity.add(green);
		ledIntensity.add(blue);
		recvInterface.SetVariable(NODE_REF, "leds.bottom.left", ledIntensity);
	}

	public void setBottomRightRGBLED(short red, short green, short blue) {
		ArrayList<Short> ledIntensity = new ArrayList<Short>(3);
		ledIntensity.add(red);
		ledIntensity.add(green);
		ledIntensity.add(blue);
		recvInterface.SetVariable(NODE_REF, "leds.bottom.right", ledIntensity);
	}

	/*************************************
	 * The LEDs of proximity sensors. Every proximity sensor has a companion red
	 * LED on its side (the front sensor has two LEDs, one on each side).
	 * 
	 * Default activation: on when an object is close to the associated sensor,
	 * with an intensity inversely proportional to the distance.
	 * 
	 * leds.prox.h(led 1, led 2, led 3, led 4, led 5, led 6, led 7, led 8) sets
	 * the LEDs of the front and back horizontal sensors. led 1 to led 6
	 * correspond to the front LEDs, from left to right, while led 7 and led 8
	 * correspond to the left and right back LEDs. leds.prox.v(led 1, led 2)
	 * sets the LEDs associated with the bottom sensors, left and right.
	 */
	public void setHorizontalProximityLED(short led0, short led1, short led2,
			short led3, short led4, short led5, short led6, short led7) {
		ArrayList<Short> ledIntensity = new ArrayList<Short>(8);
		ledIntensity.add(led0);
		ledIntensity.add(led1);
		ledIntensity.add(led2);
		ledIntensity.add(led3);
		ledIntensity.add(led4);
		ledIntensity.add(led5);
		ledIntensity.add(led6);
		ledIntensity.add(led7);
		recvInterface.SetVariable(NODE_REF, "leds.prox.h", ledIntensity);
	}

	public void setVerticalProximityLED(short led0, short led1) {
		ArrayList<Short> ledIntensity = new ArrayList<Short>(2);
		ledIntensity.add(led0);
		ledIntensity.add(led1);
		recvInterface.SetVariable(NODE_REF, "leds.prox.v", ledIntensity);
	}

	/*******
	 * The Button LEDs Four red LEDs are placed between the buttons.
	 * 
	 * Default activation: For each arrow button, one LED lights up when it is
	 * pressed. When the centre button is pressed, all four LEDs light up.
	 * 
	 * leds.buttons(led 1, led 2, led 3, led 4) control these LEDs, with led 1
	 * corresponding to the front LED, then clockwise numbering.
	 */
	public void setButtonLED(short led0, short led1, short led2, short led3) {
		ArrayList<Short> ledIntensity = new ArrayList<Short>(4);
		ledIntensity.add(led0);
		ledIntensity.add(led1);
		ledIntensity.add(led2);
		ledIntensity.add(led3);
		recvInterface.SetVariable(NODE_REF, "leds.buttons", ledIntensity);
	}

	/******************
	 * The LED of the RC receiver This red LED is located close to the
	 * remote-control (infrared) receiver.
	 * 
	 * Default activation: blinks when the robot receives an RC5 code.
	 * 
	 * leds.rc(led) controls this LED.
	 */
	public void setRC5LED(short led) {
		ArrayList<Short> ledIntensity = new ArrayList<Short>(1);
		ledIntensity.add(led);
		recvInterface.SetVariable(NODE_REF, "leds.rc", ledIntensity);
	}

	/*****
	 * The LEDs of the temperature sensor These two LEDs (one red and one blue)
	 * are located close to the temperature sensor.
	 * 
	 * Default activation: red if the temperature is over 28°C, red and blue
	 * between 28° and 15°, blue if the temperature is below 15°.
	 * 
	 * leds.temperature(red, blue) controls this LED.
	 */

	public void setTemperatureLED(short red, short blue) {
		ArrayList<Short> ledIntensity = new ArrayList<Short>(2);
		ledIntensity.add(red);
		ledIntensity.add(blue);
		recvInterface.SetVariable(NODE_REF, "leds.temperature", ledIntensity);
	}

	/**********
	 * The microphone LED This blue LED is located close to the microphone.
	 * 
	 * Default activation: off.
	 * 
	 * leds.sound(led) controls this LED.
	 */
	public void setMicrophoneLED(short blue) {
		ArrayList<Short> ledIntensity = new ArrayList<Short>(1);
		ledIntensity.add(blue);
		recvInterface.SetVariable(NODE_REF, "leds.sound", ledIntensity);
	}

	/**********************************************************************
	 * ************************************* CONTROL MOTORS Motors You can
	 * change the wheel speeds by writing in these variables:
	 * 
	 * motor.left.target: requested speed for left wheel motor.right.target:
	 * requested speed for right wheel You can read the real wheel speeds from
	 * these variables:
	 * 
	 * motor.left.speed : real speed of left wheel motor.right.speed : real
	 * speed of right wheel
	 * 
	 * The values range from -500 to 500. A value of 500 approximately
	 * corresponds to a linear speed of 20 cm/s. You can read the value of the
	 * motor commands from the variables motor.left.pwm and motor.right.pwm.
	 */

	public void setTargetWheelSpeed(short leftTarget, short rightTarget){
		ArrayList<Short> left = new ArrayList<Short>(1), right = new ArrayList<Short>(1);
		left.add(leftTarget);
		right.add(rightTarget);
		//set
		recvInterface.SetVariable(NODE_REF, "motor.left.target", left);
		recvInterface.SetVariable(NODE_REF, "motor.right.target", right);
	}

	public short getLeftWheelSpeed(){
		return recvInterface.GetVariable(NODE_REF, "motor.left.speed").get(0);
	}

	public short getRightWheelSpeed(){
		return recvInterface.GetVariable(NODE_REF, "motor.right.speed").get(0);
	}
	
	public void SetColor(short v1, short v2, short v3){
		List<Short> values = new ArrayList<Short>(3);
		values.add(v1);
		values.add(v2);
		values.add(v3);
		recvInterface.SendEventName("SetColor", values);
	}
	
	/********
	 * TODO (not implemented): 
	 * 1 - sound intensity detection
	 * 2 - playing and recording sounds
	 * 3 - RC5 remote control
	 * 4 - timer
	 * 5 - read and write data from the SD card (thymio's)
	 * 6 - buttons pressed/released (5 capacitive buttons)
	 */
}
