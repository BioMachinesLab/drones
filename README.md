# The CORATAM and HANCAD projects #

The sea represents one of Portugal’s main resources. Novel ways of exploring and exploiting maritime opportunities are of particular interest given the proposed expansion of Portugal’s continental shelf.

Land-based and air-based swarm robotics systems have been subjected to an extensive study but, that reality does not hold true for swarms in an aquatic envirnments, mainly because it is an environment where tasks are usually expensive to conduct, due to all the operational requesists of support crews and manned vehicles. We prupose an alternative approach, using **collectives of relatively simple and inexpensive aquatic drones** (swarms). This alternative approach, in which drones are easily replaceable, has a high potential of applicability on essential tasks such as prospecting sites for aquaculture, environmental monitoring, sea life localization, bridges inspection, sea border patrolling, and so on. Many of these tasks require distributed sensing, scalability, and robustness to faults, which can be facilitated by collectives of robots with decentralized control based on principles of self-organization.

The control system for our aquatic drone is based on the RaspberryPi and open source electronics. This project is beeing developed at [BioMachines Lab](http://biomachineslab.com), [ISCTE-IUL](http://iscte-iul.pt), and [Instituto de Telecomunicações](http://www.it.pt).

On the current prototype, identified with as Prototype V, we already equipped it with the control and propelling hardware, so we were able to test the developed software and hardware in a controlled environment. Currently this control system is composed by an RaspberryPi running a Raspian linux distribution, a high-gain wireless dongle to allow communications with remote control system, a GPS receiver and a triple axis magnetometer. The motors, which connect to a shaft with propellers, are controlled by electronic speed controller (ESC's) which receive their control signal from the RaspberryPi. All this system is then powered by two sets of LiPo batteries, one for controll and communications pruposes and the other (with a higher capacity) for the propulsion systems.

In this repository, you can find our code that runs both on the RaspberryPi for the drone's autonomous control, and on a remote computer for monitoring or sending commands. More information can be found at [http://biomachineslab.com](http://biomachineslab.com)

<iframe title="YouTube video player" width="480" height="390" src="http://www.youtube.com/watch?v=U68stGztNWA?autoplay=1" frameborder="0" allowfullscreen></iframe>

<a href='http://www.youtube.com/watch?feature=player_embedded&v=U68stGztNWA' target='_blank'><img src='http://img.youtube.com/vi/U68stGztNWA/0.jpg' width='425' height=344 /></a>

<a href='http://www.youtube.com/watch?feature=player_embedded&v=2bq_40mxUCY' target='_blank'><img src='http://img.youtube.com/vi/2bq_40mxUCY/0.jpg' width='425' height=344 /></a>
