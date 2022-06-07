# UPS_Amazon

**Welcome to our UPS Code**
### Contributors: Gaunish garg (gg147), Ning Ding (nd157)


* **The project involves creation of asynchronous, event-based multi-threaded UPS server using Java, Gradle, deployed via docker. It communicates with other team's Amazon server, web simulation server**

* **It involves a simulated world which has users placing orders via amazon website, server. Then the request is forwared to UPS Server and truck picks order from warehouse, delivers to user home location.**

* **It also involves a website created using Django which allows UPS users to login, check status, see status history of orders and displays map of order location via PlotPy** 

The servers communicate via Google Protocol buffer. The website also allows users to search for package via package id without logging in.

The status history of order involves truck location and time of order at various stages ( ready at warehouse, truck pickup, on the way, delivered)

* The code is present in folder UPS

* Differential features writeup : UPS_Differential_features.pdf

* Writeup : Protocol.pdf

* Writeup source- Writeup.txt
