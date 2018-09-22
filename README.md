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


-----------------------------------------------------------------------------------------------------------------------------------------

HW1: Distributed scoreboard
---------------------------

Students will build a scoreboard that shows player scores for a game. The homework will use Zookeeper. Students will build a Zookeeper protocol that maintains two score lists. One score list contains the N most recent games and the second list maintains the N highest scores.

Students will also build two programs: watcher and player. A watcher process displays the two lists and updates the list in real time as necessary. A player process does one of three actions: join, leave, or post a score.


Watcher details
---------------

A watcher process runs continuously until is it aborted (with a ^C or a signal). It initially displays the two lists. This course is not concerned with GUIs and such cute stuff; therefore, the lists are displayed in the terminal. For example:

Most recent scores
------------------
|Captain America   |   109569 **|
|Thor              |    99874|
|Captain America   |   175010 **| 
|Smaug             |     5015 **|
|Thor              |   111111|
|Bob               |   202014 **|

Highest scores
--------------
Prof Freeh        972430194
Prof Freeh        883284030
Prof Freeh        873920103
Prof Freeh        859883839
Prof Freeh        859893835
Bob                  202014  **
The stars at the end of each line indicate that the player is online. Watcher accepts two parameters. The first is the IP address (and optional port number). The second sets the size of the lists. The maximum list size is 25.

The watcher will redisplay the list any time there is a change in the state. Basically, after every player action.

Watchers can come and go.


Player details
--------------

Each player has a unique name. There are three player actions. Player program takes a parameter that is the IP and port number of the server (as IP:port, ie, 12.34.45.87:6666).


Join
----

When the player process is started it will join. If there is currently a player with the same name, the join fails and the program aborts with an appropriate error message. The player is considered online as soon as it joins.

It is invoked as

player 12.34.45.87:6666 Thor


Post a score
------------

The player process continually prompts for a score. After a score is entered, the player process will post the score to the scoreboard. The scoreboard in turn will update the watcher processes. The player process will continue to accept scores until it is aborted (i.e., ^C).


Leave
-----

When the process is aborted, it leaves the game and goes offline. Appropriate updates are delivered to the watchers.


Automation
----------

The above is the interactive player. But for test and validation purposes it helps to let the process run automatically. This batch mode has three additional parameters (all integers) that follow the name in the order given below.

* count -- number of scores to post
* u_delay -- mean delay in seconds between posts
* u_score -- mean score to be posted

Use a random number generator with a normal distribution to vary the delay and score around the given means. The normal distribution is characterized by mean and variance. You should experiment a bit to determine a reasonable variance. You can play around with means and variances online at places such as this.


Servers
-------

A Zookeeper service is backed by a cluster of servers.


Design
------

The assignment description is intentionally light on details. This gives students the freedom to create--or in other words the requirement to design.

Your programs should catch errors and respond appropriately. In no case should the program crash. Errors should be reported (feedback is a necessary part of all programs), handled, if possible, otherwise the program should terminate gracefully.


Submission
----------

You will create a github repository for your assignment. Add the TA and grader as collaborators. Do not add the instructor as a collaborator.

Push your changes to the repository before the due date and time. Do not modify your code after the due date.


Testing
-------

The programs will be tested using the following commands.

* watcher 12.34.45.87:6666 N -- where N is an integer
* player 12.34.45.87:6666 name
* player 12.34.45.87:6666 "first last"
* player 12.34.45.87:6666 name count delay score

Programs will be tested with invalid input.

Most tests will create many concurrent processes on an arbitrary number of machines (actually VMs).

Expect the TA to further clarify homework submission in class and on the forum.


IP address
----------

Each client must know the IP address for the server node to which it must connect. By default the Zookeeper server will listen on port 6000. However, the server can listen on other ports. Specify the server IP address and optional port as follows.

xx.xx.xx.xx[:yyyy]


Environment
-----------

The testing will be performed on VCL nodes. The VCL instances will be Ubuntu 16.04 LTS Base.

If it does not run correctly in this environment, it is wrong no matter how many other environments it works on.


CSC 591 -- Data-Intensive Computing
