MAP=map0
PLAYER0=group4
PLAYER1=dumb
PLAYER2=dumb
PLAYER3=dumb
R=10
L=4
W=1
GUI=false
ROUNDS=10000

run:
	java outpost.sim.Outpost $(MAP) $(R) $(L) $(W) $(GUI) $(PLAYER0) $(PLAYER1) $(PLAYER2) $(PLAYER3) $(ROUNDS)

dumb:
	java outpost.sim.Outpost $(MAP) $(R) $(L) $(W) $(GUI) dumb dumb dumb dumb $(ROUNDS)

us:
	java outpost.sim.Outpost $(MAP) $(R) $(L) $(W) $(GUI) group4 group4 group4 group4 $(ROUNDS)

compile:
	javac outpost/sim/Outpost.java
