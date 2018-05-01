import os
import json

filepath = "data/noisecountgeo/"

geocount = 0
with open("addressPoints.js", "w") as p:
    p.write("var addressPoints = [")
    for filename in os.listdir(filepath):
        if filename.startswith("."): continue
        print(filename)
        with open(filepath+filename, "r") as f:
            for line in f:
                line = line.rstrip()
                if not line: continue
                resdata = json.loads(line)
                count = int(resdata["count"])/1000
                p.write("[" + resdata["Latitude"] + "," + resdata["Longitude"] + "," + str(count) + "],")
    p.seek(-1, os.SEEK_END)
    p.truncate()
    p.write("];")
