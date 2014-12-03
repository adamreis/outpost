MAP=map0
PLAYER0=group4
PLAYER1=dumb
PLAYER2=dumb
PLAYER3=dumb
G1=group1
G2=group2
G3=group3
G5=group5
G6=group6
G7=group7
G8=group8
G9=group9
R=7
L=12
W=3
GUI=false
ROUNDS=4444

run:
	java outpost.sim.Outpost $(MAP) $(R) $(L) $(W) $(GUI) $(PLAYER0) $(PLAYER1) $(PLAYER2) $(PLAYER3) $(ROUNDS)

dumb:
	java outpost.sim.Outpost $(MAP) $(R) $(L) $(W) $(GUI) dumb dumb dumb dumb $(ROUNDS)

us:
	java outpost.sim.Outpost $(MAP) $(R) $(L) $(W) $(GUI) group4 group4 group4 group4 $(ROUNDS)

config1:
	java outpost.sim.Outpost map0 7 12 3 true $(PLAYER0) $(PLAYER1) $(PLAYER2) $(PLAYER3) 4444

config2:
	java outpost.sim.Outpost map0 7 40 10 true $(PLAYER0) $(PLAYER1) $(PLAYER2) $(PLAYER3) 4444

config3:
	java outpost.sim.Outpost map1 7 12 3 true $(PLAYER0) $(PLAYER1) $(PLAYER2) $(PLAYER3) 4444

config4:
	java outpost.sim.Outpost map1 7 40 10 true $(PLAYER0) $(PLAYER1) $(PLAYER2) $(PLAYER3) 4444

even:
	java outpost.sim.Outpost map1 7 40 10 true $(G2) $(G4) $(G6) $(G8) 4444

odd:
	java outpost.sim.Outpost map1 7 40 10 true $(G1) $(G3) $(G4) $(G5) 4444

compile:
	javac outpost/sim/Outpost.java

