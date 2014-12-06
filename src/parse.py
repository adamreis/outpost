
f = open('tournament_summary.csv', 'r')

groups = {'group1': 0,
          'group2': 0,
          'group3': 0,
          'group4': 0,
          'group5': 0,
          'group6': 0,
          'group7': 0,
          'group8': 0,
          'group9': 0}

games = -1

for line in f:
  games += 1
  if games == 0:
    continue
  groups[line.split(',')[4]] += 1
    
print groups

