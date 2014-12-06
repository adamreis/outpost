
f = open('tournament_summary.csv', 'r')
lines = f.readlines()

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

LAND  = 6
WATER = 7
POSTS = 8

for i in range(len(lines) - 4):
  if i % 4 == 0:
    if lines[i].find('group4') == -1:
      i += 4
    else:
      maxscore = 0
      ourscore = 0
      for j in range(1, 5):
        if lines[i+j].split(',')[4] == 'group4':
          ourscore = lines[i+j].split(',')[WATER]
        else:
          if maxscore < lines[i+j].split(',')[WATER]:
            maxscore = lines[i+j].split(',')[WATER]
      if maxscore == ourscore:
        wins += 1
      games += 1  
          
print 'out of '+str(games)+' we won '+str(wins)
        
        
  

