package gui.panels;

import gui.RobotGUI;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import threads.UpdateThread;
import dataObjects.SystemInformationsData;

public class SystemInfoPanel extends UpdatePanel {
	private static final long serialVersionUID = 8457762280133417243L;
	private RobotGUI gui;

	// Hardware
	private JPanel hardwareInformationsPanel;
	private JTextField textFieldSerialNumber;
	private JTextField textFieldCPURevision;
	private JTextField textFieldCPUArchitect;
	private JTextField textFieldCPUPartNumb;
	private JTextField textFieldCPUTemp;
	private JTextField textFieldCPUVoltage;
	private JTextField textFieldIsHardFloatAbi;
	private JTextField textFieldBoardType;
	private JTextField textFieldHardwareRevision;

	// Memory
	private JPanel memoryInformationsPanel;
	private JTextField textFieldTotalMemory;
	private JTextField textFieldUsedMemory;
	private JTextField textFieldFreeMemory;
	private JTextField textFieldSharedMemory;
	private JTextField textFieldMemoryBuffers;
	private JTextField textFieldCachedMemory;
	private JTextField textFieldSDRAM_c_voltage;
	private JTextField textFieldSDRAM_i_voltage;
	private JTextField textFieldSDRAM_p_voltage;

	// Operative System
	private JPanel osInformationsPanel;
	private JTextField textFieldOSName;
	private JTextField textFieldOSVersion;
	private JTextField textFieldOSArch;
	private JTextField textFieldOSFirmwareBuild;
	private JTextField textFieldOSFirmwareDate;

	// Java
	private JPanel javaEnvInformationsPanel;
	private JTextField textFieldJavaVendor;
	private JTextField textFieldJavaVendorURL;
	private JTextField textFieldJavaVersion;
	private JTextField textFieldJavaVM;
	private JTextField textFieldJavaRuntime;

	// Network
	private JPanel networkInformationsPanel;
	private JTextField textFieldHostname;
	private JTextField textFieldIPAddresses;
	private JTextField textFieldFQDNs;
	private JTextField textFieldNameServers;

	// Clock
	private JPanel clockInformationsPanel;
	private JTextField textFieldARMFreq;
	private JTextField textFieldCoreFreq;
	private JTextField textFieldISPFreq;
	private JTextField textFieldUARTFreq;
	private JTextField textFieldPWMFreq;
	private JTextField textFieldEMMCFreq;
	private JTextField textFieldHDMIFreq;
	private JTextField textFieldDPIFreq;
	
	private UpdateThread thread;
	
	private long sleepTime = 10000;

	public SystemInfoPanel(RobotGUI gui) {
		this.gui = gui;
		setBorder(BorderFactory.createTitledBorder("System Informations"));
		setLayout(null);
		setMinimumSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(585, 520));

		buildHardwareInformationsPanel();
		buildMemoryInformationsPanel();
		buildOSInformationsPanel();
		buildJavaEnvInformationsPanel();
		buildNetworkInformationsPanel();
		buildClockInformationsPanel();

		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.setBounds(447, 486, 89, 23);
		btnRefresh.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(thread != null)
					thread.interrupt();
			}
		});
		add(btnRefresh);
	}

	private void buildHardwareInformationsPanel() {
		hardwareInformationsPanel = new JPanel();
		hardwareInformationsPanel.setBounds(10, 16, 181, 305);
		hardwareInformationsPanel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), "Hardware",
				TitledBorder.LEFT, TitledBorder.TOP));
		hardwareInformationsPanel.setLayout(null);

		JLabel lblSerialNumber = new JLabel("Serial Numb.");
		lblSerialNumber.setBounds(10, 25, 65, 14);
		hardwareInformationsPanel.add(lblSerialNumber);

		JLabel lblCPURevision = new JLabel("CPU Rev.");
		lblCPURevision.setBounds(10, 56, 65, 14);
		hardwareInformationsPanel.add(lblCPURevision);

		JLabel lblCPUArchitect = new JLabel("CPU Arch.");
		lblCPUArchitect.setBounds(10, 87, 65, 14);
		hardwareInformationsPanel.add(lblCPUArchitect);

		JLabel lblCpuPart = new JLabel("CPU Part");
		lblCpuPart.setBounds(10, 118, 65, 14);
		hardwareInformationsPanel.add(lblCpuPart);

		JLabel lblCpuTemp = new JLabel("CPU Temp.");
		lblCpuTemp.setBounds(10, 149, 65, 14);
		hardwareInformationsPanel.add(lblCpuTemp);

		JLabel lblCpuVolt = new JLabel("CPU Volt.");
		lblCpuVolt.setBounds(10, 180, 65, 14);
		hardwareInformationsPanel.add(lblCpuVolt);

		JLabel lblRevision = new JLabel("Revision");
		lblRevision.setBounds(10, 211, 65, 14);
		hardwareInformationsPanel.add(lblRevision);

		JLabel lblHardFloatAbi = new JLabel("Hard Float");
		lblHardFloatAbi.setBounds(10, 242, 65, 14);
		hardwareInformationsPanel.add(lblHardFloatAbi);

		JLabel lblBoardTyp = new JLabel("Board Typ.");
		lblBoardTyp.setBounds(10, 273, 65, 14);
		hardwareInformationsPanel.add(lblBoardTyp);

		textFieldSerialNumber = new JTextField();
		textFieldSerialNumber.setEditable(false);
		textFieldSerialNumber.setBounds(85, 22, 86, 20);
		hardwareInformationsPanel.add(textFieldSerialNumber);
		textFieldSerialNumber.setColumns(10);

		textFieldCPURevision = new JTextField();
		textFieldCPURevision.setEditable(false);
		textFieldCPURevision.setBounds(85, 53, 86, 20);
		hardwareInformationsPanel.add(textFieldCPURevision);
		textFieldCPURevision.setColumns(10);

		textFieldCPUArchitect = new JTextField();
		textFieldCPUArchitect.setEditable(false);
		textFieldCPUArchitect.setText("");
		textFieldCPUArchitect.setBounds(85, 84, 86, 20);
		hardwareInformationsPanel.add(textFieldCPUArchitect);
		textFieldCPUArchitect.setColumns(10);

		textFieldCPUPartNumb = new JTextField();
		textFieldCPUPartNumb.setEditable(false);
		textFieldCPUPartNumb.setBounds(85, 115, 86, 20);
		hardwareInformationsPanel.add(textFieldCPUPartNumb);
		textFieldCPUPartNumb.setColumns(10);

		textFieldCPUTemp = new JTextField();
		textFieldCPUTemp.setEditable(false);
		textFieldCPUTemp.setBounds(85, 146, 86, 20);
		hardwareInformationsPanel.add(textFieldCPUTemp);
		textFieldCPUTemp.setColumns(10);

		textFieldCPUVoltage = new JTextField();
		textFieldCPUVoltage.setEditable(false);
		textFieldCPUVoltage.setBounds(85, 177, 86, 20);
		hardwareInformationsPanel.add(textFieldCPUVoltage);
		textFieldCPUVoltage.setColumns(10);

		textFieldIsHardFloatAbi = new JTextField();
		textFieldIsHardFloatAbi.setEditable(false);
		textFieldIsHardFloatAbi.setBounds(85, 239, 86, 20);
		hardwareInformationsPanel.add(textFieldIsHardFloatAbi);
		textFieldIsHardFloatAbi.setColumns(10);

		textFieldBoardType = new JTextField();
		textFieldBoardType.setEditable(false);
		textFieldBoardType.setBounds(85, 270, 86, 20);
		hardwareInformationsPanel.add(textFieldBoardType);
		textFieldBoardType.setColumns(10);

		textFieldHardwareRevision = new JTextField();
		textFieldHardwareRevision.setEditable(false);
		textFieldHardwareRevision.setBounds(85, 208, 86, 20);
		hardwareInformationsPanel.add(textFieldHardwareRevision);
		textFieldHardwareRevision.setColumns(10);

		add(hardwareInformationsPanel);
	}

	private void buildMemoryInformationsPanel() {
		memoryInformationsPanel = new JPanel();
		memoryInformationsPanel.setBounds(201, 16, 181, 305);
		memoryInformationsPanel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), "Memory",
				TitledBorder.LEFT, TitledBorder.TOP));
		memoryInformationsPanel.setLayout(null);

		JLabel lblTotalMemory = new JLabel("Total Mem.");
		lblTotalMemory.setBounds(10, 25, 65, 14);
		memoryInformationsPanel.add(lblTotalMemory);

		JLabel lblUsedMem = new JLabel("Used Mem.");
		lblUsedMem.setBounds(10, 56, 65, 14);
		memoryInformationsPanel.add(lblUsedMem);

		JLabel lblFreeMem = new JLabel("Free Mem.");
		lblFreeMem.setBounds(10, 87, 65, 14);
		memoryInformationsPanel.add(lblFreeMem);

		JLabel lblSharedMem = new JLabel("Shared Mem.");
		lblSharedMem.setBounds(10, 118, 65, 14);
		memoryInformationsPanel.add(lblSharedMem);

		JLabel lblMemBuff = new JLabel("Mem. Buff.");
		lblMemBuff.setBounds(10, 149, 65, 14);
		memoryInformationsPanel.add(lblMemBuff);

		JLabel lblCachedMem = new JLabel("Cached Mem.");
		lblCachedMem.setBounds(10, 180, 65, 14);
		memoryInformationsPanel.add(lblCachedMem);

		JLabel lblSdramCVolt = new JLabel("SDRAM C");
		lblSdramCVolt.setBounds(10, 211, 65, 14);
		memoryInformationsPanel.add(lblSdramCVolt);

		JLabel lblSdramIVolt = new JLabel("SDRAM I");
		lblSdramIVolt.setBounds(10, 242, 65, 14);
		memoryInformationsPanel.add(lblSdramIVolt);

		JLabel lblSdramPVolt = new JLabel("SDRAM P");
		lblSdramPVolt.setBounds(10, 273, 65, 14);
		memoryInformationsPanel.add(lblSdramPVolt);

		textFieldTotalMemory = new JTextField();
		textFieldTotalMemory.setEditable(false);
		textFieldTotalMemory.setBounds(85, 22, 86, 20);
		memoryInformationsPanel.add(textFieldTotalMemory);
		textFieldTotalMemory.setColumns(10);

		textFieldUsedMemory = new JTextField();
		textFieldUsedMemory.setEditable(false);
		textFieldUsedMemory.setBounds(85, 53, 86, 20);
		memoryInformationsPanel.add(textFieldUsedMemory);
		textFieldUsedMemory.setColumns(10);

		textFieldFreeMemory = new JTextField();
		textFieldFreeMemory.setEditable(false);
		textFieldFreeMemory.setBounds(85, 84, 86, 20);
		memoryInformationsPanel.add(textFieldFreeMemory);
		textFieldFreeMemory.setColumns(10);

		textFieldSharedMemory = new JTextField();
		textFieldSharedMemory.setEditable(false);
		textFieldSharedMemory.setBounds(85, 115, 86, 20);
		memoryInformationsPanel.add(textFieldSharedMemory);
		textFieldSharedMemory.setColumns(10);

		textFieldMemoryBuffers = new JTextField();
		textFieldMemoryBuffers.setEditable(false);
		textFieldMemoryBuffers.setBounds(85, 146, 86, 20);
		memoryInformationsPanel.add(textFieldMemoryBuffers);
		textFieldMemoryBuffers.setColumns(10);

		textFieldCachedMemory = new JTextField();
		textFieldCachedMemory.setEditable(false);
		textFieldCachedMemory.setBounds(85, 177, 86, 20);
		memoryInformationsPanel.add(textFieldCachedMemory);
		textFieldCachedMemory.setColumns(10);

		textFieldSDRAM_c_voltage = new JTextField();
		textFieldSDRAM_c_voltage.setEditable(false);
		textFieldSDRAM_c_voltage.setBounds(85, 208, 86, 20);
		memoryInformationsPanel.add(textFieldSDRAM_c_voltage);
		textFieldSDRAM_c_voltage.setColumns(10);

		textFieldSDRAM_i_voltage = new JTextField();
		textFieldSDRAM_i_voltage.setEditable(false);
		textFieldSDRAM_i_voltage.setBounds(85, 239, 86, 20);
		memoryInformationsPanel.add(textFieldSDRAM_i_voltage);
		textFieldSDRAM_i_voltage.setColumns(10);

		textFieldSDRAM_p_voltage = new JTextField();
		textFieldSDRAM_p_voltage.setEditable(false);
		textFieldSDRAM_p_voltage.setBounds(85, 270, 86, 20);
		memoryInformationsPanel.add(textFieldSDRAM_p_voltage);
		textFieldSDRAM_p_voltage.setColumns(10);

		add(memoryInformationsPanel);
	}

	private void buildOSInformationsPanel() {
		osInformationsPanel = new JPanel();
		osInformationsPanel.setBounds(10, 332, 181, 180);
		osInformationsPanel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), "Operative System",
				TitledBorder.LEFT, TitledBorder.TOP));
		osInformationsPanel.setLayout(null);

		JLabel lblOSName = new JLabel("OS Name");
		lblOSName.setBounds(10, 25, 46, 14);
		osInformationsPanel.add(lblOSName);

		JLabel lblOsVersion = new JLabel("OS Version");
		lblOsVersion.setBounds(10, 56, 65, 14);
		osInformationsPanel.add(lblOsVersion);

		JLabel lblOsArch = new JLabel("OS Arch.");
		lblOsArch.setBounds(10, 87, 46, 14);
		osInformationsPanel.add(lblOsArch);

		JLabel lblOsFirmBuild = new JLabel("Firm. Build");
		lblOsFirmBuild.setBounds(10, 118, 65, 14);
		osInformationsPanel.add(lblOsFirmBuild);

		JLabel lblOsFirmDate = new JLabel("Firm. Date");
		lblOsFirmDate.setBounds(10, 149, 65, 14);
		osInformationsPanel.add(lblOsFirmDate);

		textFieldOSName = new JTextField();
		textFieldOSName.setEditable(false);
		textFieldOSName.setBounds(85, 22, 86, 20);
		osInformationsPanel.add(textFieldOSName);
		textFieldOSName.setColumns(10);

		textFieldOSVersion = new JTextField();
		textFieldOSVersion.setEditable(false);
		textFieldOSVersion.setBounds(85, 53, 86, 20);
		osInformationsPanel.add(textFieldOSVersion);
		textFieldOSVersion.setColumns(10);

		textFieldOSArch = new JTextField();
		textFieldOSArch.setEditable(false);
		textFieldOSArch.setBounds(85, 84, 86, 20);
		osInformationsPanel.add(textFieldOSArch);
		textFieldOSArch.setColumns(10);

		textFieldOSFirmwareBuild = new JTextField();
		textFieldOSFirmwareBuild.setEditable(false);
		textFieldOSFirmwareBuild.setBounds(85, 115, 86, 20);
		osInformationsPanel.add(textFieldOSFirmwareBuild);
		textFieldOSFirmwareBuild.setColumns(10);

		textFieldOSFirmwareDate = new JTextField();
		textFieldOSFirmwareDate.setEditable(false);
		textFieldOSFirmwareDate.setBounds(85, 146, 86, 20);
		osInformationsPanel.add(textFieldOSFirmwareDate);
		textFieldOSFirmwareDate.setColumns(10);

		add(osInformationsPanel);
	}

	private void buildJavaEnvInformationsPanel() {
		javaEnvInformationsPanel = new JPanel();
		javaEnvInformationsPanel.setBounds(201, 332, 181, 180);
		javaEnvInformationsPanel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), "Java Environment",
				TitledBorder.LEFT, TitledBorder.TOP));
		javaEnvInformationsPanel.setLayout(null);

		JLabel lblJavaVendor = new JLabel("Java Vendor");
		lblJavaVendor.setBounds(10, 25, 65, 14);
		javaEnvInformationsPanel.add(lblJavaVendor);

		JLabel lblJavaVendorURL = new JLabel("Vendor URL");
		lblJavaVendorURL.setBounds(10, 56, 65, 14);
		javaEnvInformationsPanel.add(lblJavaVendorURL);

		JLabel lblJavaVersion = new JLabel("Java Version");
		lblJavaVersion.setBounds(10, 87, 65, 14);
		javaEnvInformationsPanel.add(lblJavaVersion);

		JLabel lblJavaVm = new JLabel("Java VM");
		lblJavaVm.setBounds(10, 118, 65, 14);
		javaEnvInformationsPanel.add(lblJavaVm);

		JLabel lblJavaRuntime = new JLabel("Java Runtime");
		lblJavaRuntime.setBounds(10, 149, 65, 14);
		javaEnvInformationsPanel.add(lblJavaRuntime);

		textFieldJavaVendor = new JTextField();
		textFieldJavaVendor.setEditable(false);
		textFieldJavaVendor.setBounds(85, 22, 86, 20);
		javaEnvInformationsPanel.add(textFieldJavaVendor);
		textFieldJavaVendor.setColumns(10);

		textFieldJavaVendorURL = new JTextField();
		textFieldJavaVendorURL.setEditable(false);
		textFieldJavaVendorURL.setBounds(85, 53, 86, 20);
		javaEnvInformationsPanel.add(textFieldJavaVendorURL);
		textFieldJavaVendorURL.setColumns(10);

		textFieldJavaVersion = new JTextField();
		textFieldJavaVersion.setEditable(false);
		textFieldJavaVersion.setBounds(85, 84, 86, 20);
		javaEnvInformationsPanel.add(textFieldJavaVersion);
		textFieldJavaVersion.setColumns(10);

		textFieldJavaVM = new JTextField();
		textFieldJavaVM.setEditable(false);
		textFieldJavaVM.setBounds(85, 115, 86, 20);
		javaEnvInformationsPanel.add(textFieldJavaVM);
		textFieldJavaVM.setColumns(10);

		textFieldJavaRuntime = new JTextField();
		textFieldJavaRuntime.setEditable(false);
		textFieldJavaRuntime.setBounds(85, 146, 86, 20);
		javaEnvInformationsPanel.add(textFieldJavaRuntime);
		textFieldJavaRuntime.setColumns(10);

		add(javaEnvInformationsPanel);
	}

	private void buildNetworkInformationsPanel() {
		networkInformationsPanel = new JPanel();
		networkInformationsPanel.setBounds(392, 332, 181, 145);
		networkInformationsPanel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), "Network",
				TitledBorder.LEFT, TitledBorder.TOP));
		networkInformationsPanel.setLayout(null);

		JLabel lblHostname = new JLabel("Hostname");
		lblHostname.setBounds(10, 25, 65, 14);
		networkInformationsPanel.add(lblHostname);

		JLabel lblIPAddresses = new JLabel("IP Addresses");
		lblIPAddresses.setBounds(10, 56, 65, 14);
		networkInformationsPanel.add(lblIPAddresses);

		JLabel lblFqdns = new JLabel("FQDN's");
		lblFqdns.setBounds(10, 87, 46, 14);
		networkInformationsPanel.add(lblFqdns);

		JLabel lblNameServers = new JLabel("Name Serv.");
		lblNameServers.setBounds(10, 118, 65, 14);
		networkInformationsPanel.add(lblNameServers);

		textFieldHostname = new JTextField();
		textFieldHostname.setEditable(false);
		textFieldHostname.setBounds(85, 22, 86, 20);
		networkInformationsPanel.add(textFieldHostname);
		textFieldHostname.setColumns(10);

		textFieldIPAddresses = new JTextField();
		textFieldIPAddresses.setEditable(false);
		textFieldIPAddresses.setBounds(85, 53, 86, 20);
		networkInformationsPanel.add(textFieldIPAddresses);
		textFieldIPAddresses.setColumns(10);

		textFieldFQDNs = new JTextField();
		textFieldFQDNs.setEditable(false);
		textFieldFQDNs.setBounds(85, 84, 86, 20);
		networkInformationsPanel.add(textFieldFQDNs);
		textFieldFQDNs.setColumns(10);

		textFieldNameServers = new JTextField();
		textFieldNameServers.setEditable(false);
		textFieldNameServers.setBounds(85, 115, 86, 20);
		networkInformationsPanel.add(textFieldNameServers);
		textFieldNameServers.setColumns(10);

		add(networkInformationsPanel);
	}

	private void buildClockInformationsPanel() {
		clockInformationsPanel = new JPanel();
		clockInformationsPanel.setBounds(392, 16, 181, 305);
		clockInformationsPanel.setBorder(BorderFactory.createTitledBorder(
				UIManager.getBorder("TitledBorder.border"), "Clock",
				TitledBorder.LEFT, TitledBorder.TOP));
		clockInformationsPanel.setLayout(null);

		JLabel lblARMFreq = new JLabel("ARM Freq.");
		lblARMFreq.setBounds(10, 25, 65, 14);
		clockInformationsPanel.add(lblARMFreq);

		JLabel lblCoreFreq = new JLabel("Core Freq.");
		lblCoreFreq.setBounds(10, 56, 65, 14);
		clockInformationsPanel.add(lblCoreFreq);

		JLabel lblIspFreq = new JLabel("ISP Freq.");
		lblIspFreq.setBounds(10, 87, 65, 14);
		clockInformationsPanel.add(lblIspFreq);

		JLabel lblUartFreq = new JLabel("UART Freq.");
		lblUartFreq.setBounds(10, 118, 65, 14);
		clockInformationsPanel.add(lblUartFreq);

		JLabel lblPwmFreq = new JLabel("PWM Freq.");
		lblPwmFreq.setBounds(10, 149, 65, 14);
		clockInformationsPanel.add(lblPwmFreq);

		JLabel lblEmmcFreq = new JLabel("EMMC Freq.");
		lblEmmcFreq.setBounds(10, 180, 65, 14);
		clockInformationsPanel.add(lblEmmcFreq);

		JLabel lblHdmiFreq = new JLabel("HDMI Freq.");
		lblHdmiFreq.setBounds(10, 211, 65, 14);
		clockInformationsPanel.add(lblHdmiFreq);

		JLabel lblDpiFreq = new JLabel("DPI Freq.");
		lblDpiFreq.setBounds(10, 242, 65, 14);
		clockInformationsPanel.add(lblDpiFreq);

		textFieldARMFreq = new JTextField();
		textFieldARMFreq.setEditable(false);
		textFieldARMFreq.setBounds(85, 22, 86, 20);
		clockInformationsPanel.add(textFieldARMFreq);
		textFieldARMFreq.setColumns(10);

		textFieldCoreFreq = new JTextField();
		textFieldCoreFreq.setEditable(false);
		textFieldCoreFreq.setBounds(85, 53, 86, 20);
		clockInformationsPanel.add(textFieldCoreFreq);
		textFieldCoreFreq.setColumns(10);

		textFieldISPFreq = new JTextField();
		textFieldISPFreq.setEditable(false);
		textFieldISPFreq.setBounds(85, 84, 86, 20);
		clockInformationsPanel.add(textFieldISPFreq);
		textFieldISPFreq.setColumns(10);

		textFieldUARTFreq = new JTextField();
		textFieldUARTFreq.setEditable(false);
		textFieldUARTFreq.setBounds(85, 115, 86, 20);
		clockInformationsPanel.add(textFieldUARTFreq);
		textFieldUARTFreq.setColumns(10);

		textFieldPWMFreq = new JTextField();
		textFieldPWMFreq.setEditable(false);
		textFieldPWMFreq.setBounds(85, 146, 86, 20);
		clockInformationsPanel.add(textFieldPWMFreq);
		textFieldPWMFreq.setColumns(10);

		textFieldEMMCFreq = new JTextField();
		textFieldEMMCFreq.setEditable(false);
		textFieldEMMCFreq.setBounds(85, 177, 86, 20);
		clockInformationsPanel.add(textFieldEMMCFreq);
		textFieldEMMCFreq.setColumns(10);

		textFieldHDMIFreq = new JTextField();
		textFieldHDMIFreq.setEditable(false);
		textFieldHDMIFreq.setBounds(85, 208, 86, 20);
		clockInformationsPanel.add(textFieldHDMIFreq);
		textFieldHDMIFreq.setColumns(10);

		textFieldDPIFreq = new JTextField();
		textFieldDPIFreq.setEditable(false);
		textFieldDPIFreq.setBounds(85, 239, 86, 20);
		clockInformationsPanel.add(textFieldDPIFreq);
		textFieldDPIFreq.setColumns(10);

		add(clockInformationsPanel);
	}

	public void displayData(SystemInformationsData data) {
		// Hardware
		textFieldSerialNumber.setText(data.getSerialNumber());
		textFieldCPURevision.setText(data.getCpuRevision());
		textFieldCPUArchitect.setText(data.getCpuArchitecture());
		textFieldCPUPartNumb.setText(data.getCpuPart());
		textFieldCPUTemp.setText(data.getCpuTemperature() + " �C");
		textFieldCPUVoltage.setText(data.getCpuVoltage() + " Volt");
		textFieldIsHardFloatAbi
				.setText(Boolean.toString(data.isHardFloatAbi()));
		textFieldBoardType.setText(data.getBoardType());
		textFieldHardwareRevision.setText(data.getHardwareRevision());

		// Memory
		NumberFormat formatter = new DecimalFormat("#0.00");
		textFieldTotalMemory.setText(formatter.format(data.getTotalMemory()
				/ (double) (1024 * 1024))
				+ " MBytes");
		textFieldUsedMemory.setText(formatter.format(data.getUsedMemory()
				/ (double) (1024 * 1024))
				+ " MBytes");
		textFieldFreeMemory.setText(formatter.format(data.getFreeMemory()
				/ (double) (1024 * 1024))
				+ " MBytes");
		textFieldSharedMemory.setText(formatter.format(data.getSharedMemory()
				/ (double) (1024 * 1024))
				+ " MBytes");
		textFieldMemoryBuffers.setText(formatter.format(data.getMemoryBuffers()
				/ (double) (1024 * 1024))
				+ " MBytes");
		textFieldCachedMemory.setText(formatter.format(data.getCachedMemory()
				/ (double) (1024 * 1024))
				+ " MBytes");
		textFieldSDRAM_c_voltage.setText(data.getSdram_c_voltage() + " Volt");
		textFieldSDRAM_i_voltage.setText(data.getSdram_i_voltage() + " Volt");
		textFieldSDRAM_p_voltage.setText(data.getSdram_p_voltage() + " Volt");

		// Operative System
		textFieldOSName.setText(data.getOsName());
		textFieldOSVersion.setText(data.getOsVersion());
		textFieldOSArch.setText(data.getOsArch());
		textFieldOSFirmwareBuild.setText(data.getOsFirmwareBuild());
		textFieldOSFirmwareDate.setText(data.getOsFirmwareDate());

		// Java
		textFieldJavaVendor.setText(data.getJavaVendor());
		textFieldJavaVendorURL.setText(data.getJavaVendorURL());
		textFieldJavaVersion.setText(data.getJavaVersion());
		textFieldJavaVM.setText(data.getJavaVirtualMachine());
		textFieldJavaRuntime.setText(data.getJavaRuntime());

		// Network
		textFieldHostname.setText(data.getHostname());

		String ipAddesses = "";
		for (String str : data.getIpAddresses())
			ipAddesses += str + "/";
		textFieldIPAddresses.setText(ipAddesses);

		String fqdns = "";
		for (String str : data.getFQDNs())
			fqdns += str + "/";
		textFieldFQDNs.setText(fqdns);

		String nameServers = "";
		for (String str : data.getNameServers())
			nameServers += str + "/";
		textFieldNameServers.setText(nameServers);

		// Clock
		textFieldARMFreq.setText(data.getArmClockFrequency() / 1E6 + " MHz");
		textFieldCoreFreq.setText(data.getCoreClockFrequency() / 1E6 + " MHz");
		textFieldISPFreq.setText(data.getIspClockFrequency() / 1E6 + " MHz");
		textFieldUARTFreq.setText(data.getUartClockFrequency() / 1E6 + " MHz");
		textFieldPWMFreq.setText(data.getPwmClockFrequency() / 1E3 + " KHz");
		textFieldEMMCFreq.setText(data.getEmmcClockFrequency() / 1E6 + " MHz");
		textFieldHDMIFreq.setText(data.getHdmiClockFrequency() / 1E6 + " MHz");
		textFieldDPIFreq.setText(data.getDpiClockFrequency() / 1E6 + " MHz");

		repaint();
	}

	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;
	}
	
	@Override
	public void threadWait() {
		try {
			synchronized(this){
				wait();
			}
		}catch(Exception e) {}
	}
	
	@Override
	public long getSleepTime() {
		return sleepTime;
	}
}