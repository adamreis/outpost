
f = open('winners.txt', 'r')

groups = {'group1': 0,
          'group2': 0,
          'group3': 0,
          'group4': 0,
          'group5': 0,
          'group6': 0,
          'group7': 0,
          'group8': 0,
          'group9': 0}

wins  = 0
games = 0

for line in f:
  groups[line.strip()] += 1

print groups
