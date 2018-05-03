import json


temp = open("result.json","wt")
data = json.load(open("test.json"))
result = []
count = 0
new_object = {}
prev_name = ""

for object in data:
	count = count + 1
	new_object = {}
	new_object["id"] = count
	for (k,v) in object.items():
		if (k == "longitude") | (k == "latitude"):
			v = float(v)
		new_object[k] = v
	if new_object["name"] != prev_name:
		result.append(new_object)
		prev_name = new_object["name"]

with open("result.json", 'w') as outfile:
	json.dump(result, outfile)
