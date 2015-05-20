package dataObjects;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;

import com.pi4j.system.NetworkInfo;
import com.pi4j.system.SystemInfo;

public class SystemInformationsData implements Serializable {
	private static final long serialVersionUID = 8860499614818991291L;

	// Hardware Informations
	private String serialNumber;
	private String cpuRevision;
	private String cpuArchitecture;
	private String cpuPart;
	private float cpuTemperature;
	private float cpuVoltage;
	private String hardwareRevision;
	private boolean isHardFloatAbi;
	private String boardType;

	// Memory Informations
	private long totalMemory;
	private long usedMemory;
	private long freeMemory;
	private long sharedMemory;
	private long memoryBuffers;
	private long cachedMemory;
	private float sdram_c_voltage;
	private float sdram_i_voltage;
	private float sdram_p_voltage;

	// Operative System Informations
	private String osName;
	private String osVersion;
	private String osArch;
	private String osFirmwareBuild;
	private String osFirmwareDate;

	// Java Environment Informations
	private String javaVendor;
	private String javaVendorURL;
	private String javaVersion;
	private String javaVirtualMachine;
	private String javaRuntime;

	// Network Informations
	private String hostname;
	private String[] ipAddresses;
	private String[] FQDNs;
	private String[] nameServers;

	// Clock Informations
	private long armClockFrequency;
	private long coreClockFrequency;
	private long ispClockFrequency;
	private long uartClockFrequency;
	private long pwmClockFrequency;
	private long emmcClockFrequency;
	private long hdmiClockFrequency;
	private long dpiClockFrequency;

	public SystemInformationsData() throws IOException, InterruptedException,
			ParseException {
		// Hardware Informations
		serialNumber = SystemInfo.getSerial();
		cpuRevision = SystemInfo.getCpuRevision();
		cpuArchitecture = SystemInfo.getCpuArchitecture();
		cpuPart = SystemInfo.getCpuPart();
		cpuTemperature = SystemInfo.getCpuTemperature();
		cpuVoltage = SystemInfo.getCpuVoltage();
		hardwareRevision = SystemInfo.getRevision();
		isHardFloatAbi = false;
		boardType = SystemInfo.getBoardType().name();

		// Memory Informations
		totalMemory = SystemInfo.getMemoryTotal();
		usedMemory = SystemInfo.getMemoryUsed();
		freeMemory = SystemInfo.getMemoryFree();
		sharedMemory = SystemInfo.getMemoryShared();
		memoryBuffers = SystemInfo.getMemoryBuffers();
		sdram_c_voltage = SystemInfo.getMemoryVoltageSDRam_C();
		sdram_i_voltage = SystemInfo.getMemoryVoltageSDRam_I();
		sdram_p_voltage = SystemInfo.getMemoryVoltageSDRam_P();

		// Operative System Informations
		osName = SystemInfo.getOsName();
		osVersion = SystemInfo.getOsVersion();
		osArch = SystemInfo.getOsArch();
		osFirmwareBuild = SystemInfo.getOsFirmwareBuild();
		osFirmwareDate = SystemInfo.getOsFirmwareDate();

		// Java Environment Informations
		javaVendor = SystemInfo.getJavaVendor();
		javaVendorURL = SystemInfo.getJavaVendorUrl();
		javaVersion = SystemInfo.getJavaVersion();
		javaVirtualMachine = SystemInfo.getJavaVirtualMachine();
		javaRuntime = SystemInfo.getJavaRuntime();

		// Network Informations
		hostname = NetworkInfo.getHostname();
		ipAddresses = NetworkInfo.getIPAddresses();
		FQDNs = NetworkInfo.getFQDNs();
		nameServers = NetworkInfo.getNameservers();

		// Clock Informations
		armClockFrequency = SystemInfo.getClockFrequencyArm();
		coreClockFrequency = SystemInfo.getClockFrequencyCore();
		ispClockFrequency = SystemInfo.getClockFrequencyISP();
		uartClockFrequency = SystemInfo.getClockFrequencyUART();
		pwmClockFrequency = SystemInfo.getClockFrequencyPWM();
		emmcClockFrequency = SystemInfo.getClockFrequencyEMMC();
		hdmiClockFrequency = SystemInfo.getClockFrequencyHDMI();
		dpiClockFrequency = SystemInfo.getClockFrequencyDPI();
	}
	
	public void update() {
		
		try {
			// Memory Informations
			totalMemory = SystemInfo.getMemoryTotal();
			usedMemory = SystemInfo.getMemoryUsed();
			freeMemory = SystemInfo.getMemoryFree();
			sharedMemory = SystemInfo.getMemoryShared();
			memoryBuffers = SystemInfo.getMemoryBuffers();
			sdram_c_voltage = SystemInfo.getMemoryVoltageSDRam_C();
			sdram_i_voltage = SystemInfo.getMemoryVoltageSDRam_I();
			sdram_p_voltage = SystemInfo.getMemoryVoltageSDRam_P();
		
		} catch (Exception e) {
			System.err.println("Problem updating SystemInformationData");
		}
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public String getCpuRevision() {
		return cpuRevision;
	}

	public String getCpuArchitecture() {
		return cpuArchitecture;
	}

	public String getCpuPart() {
		return cpuPart;
	}

	public float getCpuTemperature() {
		return cpuTemperature;
	}

	public float getCpuVoltage() {
		return cpuVoltage;
	}

	public String getHardwareRevision() {
		return hardwareRevision;
	}

	public boolean isHardFloatAbi() {
		return isHardFloatAbi;
	}

	public String getBoardType() {
		return boardType;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public long getUsedMemory() {
		return usedMemory;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public long getSharedMemory() {
		return sharedMemory;
	}

	public long getMemoryBuffers() {
		return memoryBuffers;
	}

	public long getCachedMemory() {
		return cachedMemory;
	}

	public float getSdram_c_voltage() {
		return sdram_c_voltage;
	}

	public float getSdram_i_voltage() {
		return sdram_i_voltage;
	}

	public float getSdram_p_voltage() {
		return sdram_p_voltage;
	}

	public String getOsName() {
		return osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public String getOsArch() {
		return osArch;
	}

	public String getOsFirmwareBuild() {
		return osFirmwareBuild;
	}

	public String getOsFirmwareDate() {
		return osFirmwareDate;
	}

	public String getJavaVendor() {
		return javaVendor;
	}

	public String getJavaVendorURL() {
		return javaVendorURL;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public String getJavaVirtualMachine() {
		return javaVirtualMachine;
	}

	public String getJavaRuntime() {
		return javaRuntime;
	}

	public String getHostname() {
		return hostname;
	}

	public String[] getIpAddresses() {
		return ipAddresses;
	}

	public String[] getFQDNs() {
		return FQDNs;
	}

	public String[] getNameServers() {
		return nameServers;
	}

	public long getArmClockFrequency() {
		return armClockFrequency;
	}

	public long getCoreClockFrequency() {
		return coreClockFrequency;
	}

	public long getIspClockFrequency() {
		return ispClockFrequency;
	}

	public long getUartClockFrequency() {
		return uartClockFrequency;
	}

	public long getPwmClockFrequency() {
		return pwmClockFrequency;
	}

	public long getEmmcClockFrequency() {
		return emmcClockFrequency;
	}

	public long getHdmiClockFrequency() {
		return hdmiClockFrequency;
	}

	public long getDpiClockFrequency() {
		return dpiClockFrequency;
	}
}
