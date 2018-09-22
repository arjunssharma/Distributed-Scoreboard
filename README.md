Steps to Run the Program:
---------------------------

1) Launch ZooKeeper Server viz zkServer.

2) Launch Terminal and execute command "make", it will run the program and ask to enter commands. 
Input Watcher details in the format provided on homework page. For e.g. "watcher 12.34.45.87:6666 N -- where N is an integer"

3) Launch New Terminal and execute command "make", it will run the program and ask to enter commands.
Input Player details in the format provided on homework page. Below are the sample inputs:
* player 12.34.45.87:6666 name
* player 12.34.45.87:6666 "first last"
* player 12.34.45.87:6666 name count delay score

4) Similarly many terminals (VMs) can be launched and players can participate in the game.
