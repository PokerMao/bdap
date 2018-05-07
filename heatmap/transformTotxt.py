import os
import json
from collections import defaultdict

filepath = "data/smalldis/"

geocount = 0
mincount = 100000000
maxcount = 0
countmap = defaultdict(int)
with open("addressPoints.js", "w") as p:
    p.write("var addressPoints = [")
    for filename in os.listdir(filepath):
        lines = set()
        if filename.startswith("."): continue
        print(filename)
        with open(filepath+filename, "r") as f:
            for line in f:
                if line in lines:
                    continue
                lines.add(line)
                line = line.rstrip()
                if not line: continue
                resdata = json.loads(line)
                count = int(resdata["count"])
                if count < mincount:
                    mincount = count
                if count > maxcount:
                    maxcount = count
                if count < 10:
                    countmap[10] += 1
                elif count < 100:
                    countmap[100] += 1
                elif count < 1000:
                    countmap[1000] += 1
                else:
                    countmap[10000] += 1
                count = int(resdata["count"]) / (5000.0)
                p.write("[" + resdata["Latitude"] + "," + resdata["Longitude"] + "," + str(count) + "],")
    p.seek(-1, os.SEEK_END)
    p.truncate()
    p.write("];")
    print(mincount, maxcount)
    print(countmap)
