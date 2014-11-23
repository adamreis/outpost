import sys


def writeMapOriginBlob(f):
    for i in xrange(1, 21):
        for j in xrange(1, 26):
            f.write('{} {}\n'.format(i,j))

def usage():
    print """
    usage: python map_gen.py <map_name>

        ex: python map_gen.py mapOriginBlob
    """

if __name__ == '__main__':
    boards = {'mapOriginBlob' : writeMapOriginBlob}

    if len(sys.argv) < 2 or sys.argv[1] not in boards:
        usage()
        exit(1)
    
    boardName = sys.argv[1]

    with open(boardName, 'w') as board:
        boards[boardName](board)