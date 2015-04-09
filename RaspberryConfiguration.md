# Raspberry Configuration Instructions #
This wiki page aggregrates the configurations need to be made on a standard [Raspbian OS image](http://www.raspberrypi.org/downloads/), in order to get the raspberry operative system usable on our project (considering the use of a Raspberry B 512Mb).

**Contents:**
  1. [Initial Configurations](RaspberryConfiguration#Initial_Configurations.md)
  1. [SSH Server Configurations](RaspberryConfiguration#SSH_Server_Configurations.md)
  1. [Network Configurations](RaspberryConfiguration#Network_Configurations.md)
    1. [Basic Network Configurations](RaspberryConfiguration#Basic_Network_Configurations.md)
      1. [Hostapd Configuration Option (create an access point)](RaspberryConfiguration#Hostapd_Configuration_Option_(create_an_access_point).md)
      1. [rc.local configuration (create an ad-hoc network)](RaspberryConfiguration#rc.local_configuration_(create_an_ad-hoc_network).md)
    1. [DHCP Server Configuration](RaspberryConfiguration#DHCP_Server_Configuration.md)
  1. [Hardware Interfaces, Buses, Initialization Scrips Configurations](RaspberryConfiguration#Hardware_Interfaces,_Buses,_Initialization_Scrips_Configurations.md)
    1. [Installing Servoblaster](RaspberryConfiguration#Installing_Servoblaster.md)
    1. [Installing WiringPi](RaspberryConfiguration#Installing_WiringPi.md)
    1. [Installing Java and Pi4J](RaspberryConfiguration#Installing_Java_and_Pi4J.md)
    1. [Enable I2C](RaspberryConfiguration#Enable_I2C.md)
    1. [Initialization Scripts](RaspberryConfiguration#Initialization_Scripts.md)
  1. [Other Configurations](RaspberryConfiguration#Other_Configurations.md)

<br>
<hr />
<h2>Initial Configurations</h2>
1. Launch raspberry configuration tool using <i>sudo raspi-config</i> (in case it is the first boot of the OS, the configuration tool will be shown automatically) and make the following configurations:<br>
<ul><li>Expand filesystem<br>
</li><li>Internationalization Options<br>
<ul><li>Locale en_US.UTF-8 UTF-8<br>
</li><li>Time zone: Europe > Lisbon<br>
</li><li>Keyboard Layout: choose <i>Generic 105-keys (intl) PC</i>><i>Other</i>><i>Portuguese</i>><i>Portuguese</i> (or change <i>GB</i> to <i>PT</i> in <i>/etc/default/keyboard</i> file, using vi, nano, vim or any other kind of editor - <b>do not forget sudo!</b>)<br>
</li></ul></li><li>Overclock set to High (950MHz ARM option)<br>
</li><li>Advanced Options<br>
<ul><li>Set the desired hostname (<i>biomachinesdrone</i> in our case)<br>
</li><li>Enable SSH server<br>
</li><li>Configure only 16MB of memory for the GPU (memory split)<br>
</li><li>Enable loading of I2C kernel module on boot<br>
</li><li>Disable the login shell accessibility over serial (by other words, select option "No" on the serial menu)</li></ul></li></ul>

2. Reboot Raspberry Pi (<i>sudo reboot</i>). If any error appears related to a non-set LOCALE, LANGUAGE or LC_ALL, add the following to <i>/etc/default/locale</i> (using <i>nano</i>,<i>vi</i> or other editor):<br>
<pre><code>LANGUAGE=en_US:en<br>
LC_ALL=en_US.UTF-8<br>
</code></pre>

3. Delete unnecessary files:<br>
<pre><code>cd ~ &amp;&amp; rm -rf python_games<br>
</code></pre>

4. Install needed applications:<br>
<pre><code>sudo apt-get update &amp;&amp; sudo apt-get install -y screen isc-dhcp-server deborphan sysv-rc-conf i2c-tools hostapd<br>
</code></pre>

5. Remove unnecessary applications (go make a mug of coffee, this will take awhile....):<br>
<pre><code>sudo apt-get remove --yes --purge --auto-remove libx11-.* ^libx ^python3 ^aptitude libraspberrypi-doc xkb-data fonts-freefont-ttf luajit omxplayer penguinspuzzle gnome-themes-standard* menu-xdg console-setup* desktop-file-utils* gsfonts* gsfonts-x11* libgnome-keyring0* libgtk-3-common* libgtk2.0-common* libjavascriptcoregtk-3.0-0* libqt4-xml* libqtcore4* libqtdbus4* libsoup-gnome2.4-1* libxcb-glx0* libxcb-render0* libxcb-shape0* libxcb-shm0* libxcb-util0* libxcb1* libxfont1* lxde-icon-theme* omxplayer* shared-mime-info* x11-common* xdg-utils* gcc-4\.[0-5].* samba-common smbclient strace traceroute ^vim raspberrypi-artwork sgml-base xml-core fonts-.* ^sane<br>
sudo apt-get remove --yes --purge --auto-remove $(deborphan)<br>
sudo apt-get remove --yes --purge --auto-remove deborphan<br>
</code></pre>

6. Update System<br>
<pre><code>sudo apt-get -y dist-upgrade &amp;&amp; sudo apt-get -y autoremove &amp;&amp; sudo apt-get -y autoclean<br>
</code></pre>

7. You can also limit the log's quantity and size by making some slightly modifications to <i>/etc/logrotate.conf</i> file, so the first lines become something like:<br>
<pre><code># see "man logrotate" for details<br>
# rotate log files weekly<br>
daily<br>
<br>
# keep 7 days worth of backlogs<br>
rotate 7<br>
<br>
# create new (empty) log files after rotating old ones<br>
create<br>
<br>
# uncomment this if you want your log files compressed<br>
compress<br>
</code></pre>

8. You can also change the <i>/etc/rsyslog.conf</i> in order to produce less quantity of logs (probabily is better to make a backup of the original file using <i>sudo cp /etc/rsyslog.conf /etc/rsyslog.conf.backup</i>). The final content can be something like:<br>
<pre><code>#  /etc/rsyslog.conf    Configuration file for rsyslog.<br>
<br>
$ModLoad imuxsock # provides support for local system logging<br>
$ModLoad imklog   # provides kernel logging support<br>
<br>
# To enable high precision timestamps, comment out the following line.<br>
$ActionFileDefaultTemplate RSYSLOG_TraditionalFileFormat<br>
<br>
# Set the default permissions for all log files.<br>
$FileOwner root<br>
$FileGroup adm<br>
$FileCreateMode 0640<br>
$DirCreateMode 0755<br>
$Umask 0022<br>
<br>
# Where to place spool and state files<br>
$WorkDirectory /var/spool/rsyslog<br>
<br>
# Include all config files in /etc/rsyslog.d/<br>
$IncludeConfig /etc/rsyslog.d/*.conf<br>
<br>
# First some standard log files.  Log by facility.<br>
*.*;auth,authpriv.none          -/var/log/syslog<br>
daemon.*                        -/var/log/daemon.log<br>
kern.*                          -/var/log/kern.log<br>
<br>
# Some "catch-all" log files.<br>
*.=debug;\<br>
        auth,authpriv.none;\<br>
        news.none;mail.none     -/var/log/debug<br>
</code></pre>

9. The file <i>/etc/logrotate.d/rsyslog</i> can also suffer some changes, ending up like shown bellow (probabily is better to make a backup of the original file using <i>sudo cp /etc/logrotate.d/rsyslog /etc/logrotate.d/rsyslog.backup</i>) :<br>
<pre><code>/var/log/syslog<br>
{<br>
        rotate 7<br>
        daily<br>
        missingok<br>
        notifempty<br>
        delaycompress<br>
        compress<br>
        postrotate<br>
                invoke-rc.d rsyslog rotate &gt; /dev/null<br>
        endscript<br>
}<br>
<br>
/var/log/daemon.log<br>
/var/log/kern.log<br>
/var/log/debug<br>
</code></pre>

10. In order to decrease log growth and increase boot velocity, change the system loglevel to 4 (KERN_WARNING) by addind <i>loglevel=4</i> on <i>/boot/cmdline.txt</i>, after <i>rootfstype=ext4</i>.<br>
<br>
<h4>Reference</h4>
<a href='https://rcptr.wordpress.com/2014/03/01/setting-up-rpi/'>Setting up the r.Pi</a><br>
<a href='http://www.cnx-software.com/2012/07/31/84-mb-minimal-raspbian-armhf-image-for-raspberry-pi/'>84 MB Minimal Raspbian ARMHF Image for Raspberry Pi</a><br>
<a href='http://raspberrypi.stackexchange.com/a/19490'>How to uninstall X Server and Desktop Manager when running as headless server?</a><br>
<a href='http://www.richardsramblings.com/2013/02/minimalist-raspberry-pi-server-image/'>Minimalist Raspberry Pi Server Image</a><br>
<a href='http://kiros.co.uk/blog/controlling-log-files-on-the-raspberry-pi-using-logrotate/'>Controlling log files on the Raspberry Pi (using logrotate)</a><br>
<a href='https://www.kernel.org/doc/Documentation/kernel-parameters.txt'>Kernel Parameters</a>
<br><br><br>

<hr />
<h2>SSH Server Configurations</h2>
1. On file <i>/etc/ssh/sshd_config</i> make the following changes (using an editor like nano or vim. Do not forget to edit as super user - using sudo):<br>
<ul><li>Uncomment line regarding the use of public keys (<i>AuthorizedKeysFile     %h/.ssh/authorized_keys</i>)<br>
</li><li>Uncomment line regarding GSSAPI Authentication (<i>GSSAPIAuthentication no</i>) in order to remove lag from ssh login<br>
</li><li>Add <i>UseDNS no</i> on the end of the file<br>
2. Create the <i>authorized_keys file</i> (using <i>mkdir ~/.ssh/ && touch ~/.ssh/authorized_keys</i>) and put your SSH public key in the file.</li></ul>

<h4>Reference</h4>
<a href='http://unix.stackexchange.com/questions/56941/what-is-the-point-of-sshd-usedns-option'>What is the point of SSHD UsedDNS</a>
<br><br><br>

<hr />
<h2>Network Configurations</h2>
<h3>Basic Network Configurations</h3>
1. Disable IPv6 <b>(Run <i>sudo -i</i> command first or you won't be able to run the following commands successfully. Run <i>exit</i> command at the end)</b>:<br>
<pre><code>echo "blacklist ipv6" &gt;&gt; /etc/modprobe.d/raspi-blacklist.conf<br>
sed -i "/::/s%^%#%g" /etc/hosts<br>
</code></pre>
You can test if IPv6 is enabled by runing <i>netstat -tunlp |grep p6 |wc -l</i> command after reboot. If the output is other than 0, then IPv6 is enable.<br>
<br>
2. Configure network interfaces by putting the following content in <i>/etc/network/interfaces</i> file (replacing the existent content):<br>
<pre><code>auto lo<br>
<br>
iface lo inet loopback<br>
<br>
allow-hotplug eth0<br>
iface eth0 inet static<br>
        address 10.40.50.243<br>
        netmask 255.255.255.0<br>
        gateway 10.40.50.1<br>
<br>
allow-hotplug wlan0<br>
iface wlan0 inet static<br>
        address 192.168.3.1<br>
        netmask 255.255.255.0<br>
<br>
iface default inet dhcp<br>
</code></pre>

3. Replace the content of <i>/etc/hosts</i> with the following:<br>
<pre><code>127.0.0.1       localhost<br>
<br>
10.40.50.243    biomachinesdrone<br>
192.168.3.1     biomachinesdrone<br>
</code></pre>

4. Add the following server name's IP to /etc/resolv.conf file (the last three entries are ISCTE-IUL DNS servers that we use on eth0 connection, so external user do not need to add them):<br>
<pre><code>domain biomachineswlan<br>
search biomachineswlan<br>
nameserver 192.168.3.1<br>
nameserver 10.10.20.4<br>
nameserver 10.19.90.11<br>
nameserver 193.136.188.249<br>
</code></pre>

5. As wisely suggested by Adafruit in <a href='https://learn.adafruit.com/setting-up-a-raspberry-pi-as-a-wifi-access-point/install-software#extra-removing-wpa-supplicant'>here</a>, run the following command:<br>
<pre><code>sudo mv /usr/share/dbus-1/system-services/fi.epitest.hostap.WPASupplicant.service ~/<br>
</code></pre>

<h4>Hostapd Configuration Option (create an access point)</h4>
1. If not installed yet (on initial configurations), install <i>hostapd</i>. Then, create file <i>/etc/hostapd/hostapd.conf</i> and put inside the following:<br>
<b>TO-DO: verificar outros parâmetros existentes e ver se conseguimos configurar o beacon</b>
<pre><code>interface=wlan0<br>
driver=nl80211<br>
ssid=biomachinesdrones_wlan<br>
hw_mode=g<br>
channel=11<br>
wpa=1<br>
wpa_passphrase=SECRETPASSWORD<br>
wpa_key_mgmt=WPA-PSK<br>
wpa_pairwise=TKIP CCMP<br>
wpa_ptk_rekey=600<br>
macaddr_acl=0<br>
</code></pre>

2. If you run <i>hostapd -d /etc/hostapd/hostapd.conf</i> you should already be able to create the ad-hoc network.<br>
<br>
3. Add <i>hostapd -B /etc/hostapd/hostapd.conf</i> to your <i>/etc/rc.local</i> file (before the script related with showing the IP address), in order to create the ad-hoc network on boot. Make also sure that <i>hostapd</i> is not in none of run levels call, by running <i>sudo update-rc.d -f hostapd remove</i>.<br>
<br>
<h4>rc.local configuration (create an ad-hoc network)</h4>
1. If you prefer to use an ad-hoc network, the configuration get much simpler than the previously presented one. For that purpose, add the following to <i>/etc/rc.local</i>:<br>
<pre><code>echo "\nReconfiguring WLAN0 &amp; DHCP Server"<br>
<br>
ifconfig wlan0 192.168.3.3 netmask 255.255.255.0<br>
service isc-dhcp-server start<br>
ifconfig wlan0 down;iwconfig wlan0 essid biomachines-02 mode ad-hoc;ifconfig wlan0 up<br>
ifconfig wlan0 down;iwconfig wlan0 essid biomachines-02 mode ad-hoc;ifconfig wlan0 up<br>
ifconfig wlan0 192.168.3.3 netmask 255.255.255.0<br>
</code></pre>
Pay atention that the IP address should be changed, in case you have multiple drones connected to the same ad-hoc.<br>
<br>
2. Change the <i>/etc/network/interfaces</i> content to match the following:<br>
<pre><code>auto lo<br>
<br>
iface lo inet loopback<br>
<br>
allow-hotplug eth0<br>
iface eth0 inet static<br>
	address 10.40.50.243<br>
        netmask 255.255.255.0<br>
        gateway 10.40.50.1<br>
	<br>
allow-hotplug wlan0<br>
iface wlan0 inet manual<br>
<br>
iface default inet dhcp<br>
</code></pre>

<h3>DHCP Server Configuration</h3>
1. Considering that you already installed the <i>isc-dhcp-server</i>, backup the <i>/etc/dhcp/dhcpd.conf</i> file (using the <i>sudo cp /etc/dhcp/dhcpd.conf /etc/dhcp/dhcpd.conf.backup</i>) and replace the <i>/etc/dhcp/dhcpd.conf</i> content with the following:<br>
<pre><code>ddns-update-style none;<br>
deny declines;<br>
deny bootp;<br>
<br>
# option definitions common to all supported networks...<br>
option domain-name "biomachineswlan";<br>
<br>
default-lease-time 86400;<br>
max-lease-time 604800;<br>
<br>
# If this DHCP server is the official DHCP server for the local<br>
# network, the authoritative directive should be uncommented.<br>
authoritative;<br>
<br>
# Use this to send dhcp log messages to a different log file (you also<br>
# have to hack syslog.conf to complete the redirection).<br>
log-facility local7;<br>
<br>
subnet 192.168.3.0 netmask 255.255.255.0 {<br>
        range 192.168.3.100 192.168.3.200;<br>
}<br>
</code></pre>

2. On <i>/etc/default/isc-dhcp-server</i> file, change the configurations regarding DHCP interfaces, by uncommenting (if commented) the last line of the file and change it to <i>INTERFACES=“wlan0"</i>

3. In order to create a special log, only to DHCP server, add the following line to <i>/etc/rsyslog.conf</i>:<br>
<pre><code># Logging for DHCP server<br>
local7.*                        /var/log/isc-dhcpd-server.log<br>
</code></pre>

4. To include the DHCP server file on the logrotate, on the bottom of <i>/etc/logrotate.d/rsyslog</i> add <i>/var/log/isc-dhcpd-server.log</i>.<br>
<br>
5. Remove DHCP from system startup by running the following command (we will start it from rc.local after the network has been initialized):<br>
<br>
<pre><code>sudo update-rc.d -f isc-dhcp-server remove<br>
</code></pre>

6. Finally make the DCHP server initialization permanent on boot, adding <i>service isc-dhcp-server start</i> to <i>/etc/rc.local</i> file (after the command regardind <i>hostapd</i>). The file should end up like this:<br>
<pre><code>hostapd -B /etc/hostapd/hostapd.conf<br>
service isc-dhcp-server start<br>
<br>
_IP=$(hostname -I) || true<br>
if [ "$_IP" ]; then<br>
  printf "\nMy IP address is %s\n" "$_IP"<br>
fi<br>
<br>
exit 0<br>
</code></pre>

<h4>Reference</h4>
<a href='http://manpages.ubuntu.com/manpages/precise/man5/interfaces.5.html'>/etc/network/interfaces Man Page</a><br>
<a href='http://itsacleanmachine.blogspot.pt/2013/02/wifi-access-point-with-raspberry-pi.html'>WiFi access point with Raspberry Pi</a><br>
<a href='https://learn.adafruit.com/setting-up-a-raspberry-pi-as-a-wifi-access-point/install-software'>Setting up a Raspberry Pi as a WiFi access point</a><br>
<a href='http://www.cyberciti.biz/faq/howto-ubuntu-debian-squeeze-dhcp-server-setup-tutorial/'>Ubuntu / Debian Linux: Setup An ISC DHCP Server For Your Network</a><br>
<a href='http://www.linuxhomenetworking.com/wiki/index.php/Quick_HOWTO_:_Ch08_:_Configuring_the_DHCP_Server#.VN_9q_msUi8'>Quick HOWTO : Ch08 : Configuring the DHCP Server</a><br>
<a href='http://prefetch.net/articles/iscdhcpd.html'>Installing, Configuring And Debugging The ISC DHCP Server</a>
<br><br><br>

<hr />
<h2>Hardware Interfaces, Buses, Initialization Scrips Configurations</h2>
<h3>Installing Servoblaster</h3>
In order to install servoblaster, you need to run first clone the project from github and compile the packages:<br>
<pre><code>mkdir -p ~/scripts/c &amp;&amp; cd ~/scripts/c<br>
git clone https://github.com/richardghirst/PiBits<br>
mv PiBits/ServoBlaster/ . &amp;&amp; rm -rf PiBits<br>
cd ServoBlaster/user<br>
</code></pre>

Inside <i>~/scripts/c/ServoBlaster/user</i> folder, comment the line regarding OPTS (<i>OPTS="--idle-timeout=2000"</i>) on file <i>init-script</i>, in order to remove the timeout function. Finally, run the following:<br>
<pre><code>sudo make install<br>
</code></pre>

To initiate the system with servoblaster out value in ESC's central position, add the following to <i>/etc/rc.local</i> (right before <i>exit 0</i>):<br>
<pre><code>echo 0=150 &gt; /dev/servoblaster<br>
echo 1=150 &gt; /dev/servoblaster<br>
</code></pre>

<h3>Installing WiringPi</h3>
In order to install WiringPi, you need to run first clone the project from GitHub and then build it:<br>
<pre><code>cd ~/scripts/c<br>
git clone git://git.drogon.net/wiringPi<br>
cd wiringPi<br>
git pull origin<br>
./build<br>
</code></pre>

<h3>Installing Java and Pi4J</h3>
Installation and configuration of Java is as simple as:<br>
<pre><code>sudo apt-get install --yes openjdk-7-jdk<br>
echo "export JAVA_HOME=\"/usr/lib/jvm/java-7-openjdk-armhf\"" &gt;&gt; ~/.bashrc<br>
echo "export PATH=$PATH:$JAVA_HOME/bin" &gt;&gt; ~/.bashrc<br>
echo "export CLASSPATH=/home/pi/RaspberryController/bin:/home/pi/CommonInterface/bin:/home/pi/Behaviors/bin:.:/home/pi/RaspberryController/lib/joda-time-2.4.jar:/opt/pi4j/lib/pi4j-core.jar:/opt/pi4j/lib/pi4j-device.jar:/opt/pi4j/lib/pi4j-gpio-extension.jar:/opt/pi4j/lib/pi4j-service.jar:/home/pi/CommonInterface/jcoord-1.0.jar" &gt;&gt; ~/.bashrc<br>
</code></pre>

Installation of Pi4J is as simple as:<br>
<pre><code>curl -s get.pi4j.com | sudo bash<br>
</code></pre>

<h3>Enable I2C</h3>
For use and debug of I2C, you should install <i>i2c-tools</i> (if not done previously on initial configurations).<br>
After, you should configure the I2C kernel modules to be loaded on boot, adding the following lines to the end of <i>/etc/modules</i>:<br>
<pre><code>i2c-bcm2708<br>
i2c-dev<br>
</code></pre>

On file <i>/etc/modprobe.d/raspi-blacklist.conf</i> you should comment (in case they already exists) or add the following lines:<br>
<pre><code>#blacklist spi-bcm2708<br>
#blacklist i2c-bcm2708<br>
</code></pre>

When using an Linux OS with 3.18 kernel or higher, add the following lines to the end of <i>/boot/config.txt</i>:<br>
<pre><code>dtparam=i2c1=on<br>
dtparam=i2c_arm=on<br>
</code></pre>

Last, reboot the system (<i>sudo reboot</i>), and if everything was correctly configures, you should be able to test I2C bus by using <i>sudo i2cdetect -y 1</i> command (which lists the devices on I2C bus, if any is connected).<br>
<br>
<h3>Initialization Scripts</h3>
<b>TO-DO: missing information about the system initialization script - status led</b>
<br>

rc.local<br>
<pre><code>#/home/pi/scripts/shell/print_wlan0_status.sh<br>
#/home/pi/scripts/shell/print_wlan0_status_v2.sh<br>
<br>
echo "Starting shutdown button deamon...."<br>
#/home/pi/scripts/c/ShutdownButtonHook<br>
#/home/pi/scripts/c/SystemStatusAndHaltHook<br>
</code></pre>

<h4>Reference</h4>
<a href='https://github.com/richardghirst/PiBits/blob/master/ServoBlaster/README.txt'>Servoblaster Readme</a><br>
<a href='http://wiringpi.com/download-and-install/'>WiringPi Installation</a><br>
<a href='http://elinux.org/RPi_Java_JDK_Installation'>RPI Java JDK Instalation</a>
<a href='http://pi4j.com/'>Pi4J Page</a><br>
<a href='http://pi4j.com/apidocs/index.html'>Pi4J JavaDoc</a><br>
<a href='https://learn.adafruit.com/adafruits-raspberry-pi-lesson-4-gpio-setup/configuring-i2c'>Configuring I2C</a>
<br><br><br>

<hr />
<h2>Other Configurations</h2>
In order to increase the User-Machine interface quality, there are some "hacks" to be made:<br>
<ul><li>To display system information on login, replace the <i>~/.profile</i> content by:<br>
<pre><code># if running bash<br>
if [ -n "$BASH_VERSION" ]; then<br>
    # include .bashrc if it exists<br>
    if [ -f "$HOME/.bashrc" ]; then<br>
        . "$HOME/.bashrc"<br>
    fi<br>
fi<br>
<br>
# set PATH so it includes user's private bin if it exists<br>
if [ -d "$HOME/bin" ] ; then<br>
    PATH="$HOME/bin:$PATH"<br>
fi<br>
<br>
echo ""<br>
echo -e '\E[33;40m'"\033[1mSystem Uptime Information and Who is logged in:\033[0m<br>
"<br>
w # uptime information and who is logged in<br>
<br>
#echo ""<br>
#echo -e '\E[33;40m'"\033[1mDisk Usage:\033[0m"<br>
#df -h -x tmpfs -x udev # disk usage, minus def and swap<br>
<br>
echo ""<br>
</code></pre>
You can also remove the <i>Message of The Day</i> (that present information about Debian GNU license) by deleting the content of <i>/etc/motd</i>.<br>
Finally, you can add extra information on the local console editing the content of <i>/etc/issue</i> file (<a href='http://www.linuxfromscratch.org/blfs/view/svn/postlfs/logon.html'>this website</a> contains more information about this).</li></ul>

<ul><li>To display colors on <i>bash</i> when using <i>root</i> user account, add the following lines to the bottom of <i>/root/.bashrc</i>:<br>
<pre><code>force_color_prompt=yes<br>
PS1='\[\e[0;31m\]\u@\h \[\e[m\]\e[1;34m\]\w\e[m\]\[\e[0;31m\] $ \[\e[m\]\[\e[0;37m\]'<br>
</code></pre></li></ul>

<ul><li>Remove unwanted programs from startup using <i>sudo sysv-rc-conf</i> command (considering that you already installed the application)</li></ul>

<ul><li>You can enable CGroups by adding <i>cgroup_enable=memory</i> before <i>elevator=deadline</i>, in <i>/boot/cmdline.txt</i> file.</li></ul>

<h4>Reference</h4>
<a href='http://www.tldp.org/LDP/abs/html/colorizing.html'>Colorizing Bash</a><br>
<a href='https://wiki.archlinux.org/index.php/Color_Bash_Prompt'>Color Configurations on Bash Prompt</a><br>
<a href='http://theos.in/desktop-linux/removing-unwanted-startup-debian-files-or-services/'>Removing Unwanted Startup Debian Files or Services</a>