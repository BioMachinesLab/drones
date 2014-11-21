package main;

import io.*;
import io.input.*;
import io.output.*;
import network.*;
import network.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.Logger;
import behaviors.*;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.serial.SerialPortException;

import dataObjects.MotorSpeeds;

public interface Controller {
	
	public String getStatus();
	public void setStatus(String status);
	public String getInitMessages();
	
	public void processInformationRequest(Message request, ConnectionHandler conn);
	
	public List<MessageProvider> getMessageProviders();
	public IOManager getIOManager();
}