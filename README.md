# Bigdata App Development

### Project Discription
In this project, we are aiming to provide meaningful information about restaurants in a given area in New York City. This is achieved by analysing and joining three different datasets: Yelp API, the new york city restaurant inspection results and New York noise complaints data. With an input of longitude and latitude, the application outputs the cleanliness and noise information about a square area around the input position. Technologies such as Spark SQL etc. are used to draw useful insight from the datasets.

input: Longitude and Latitude

output: Nearby restaurants with detail information

### Code Instruction
yelp.py: This scipt is used to query Yelp API using phone number as unique key to get restaurant data from Yelp.

format.py: This script is used to clean the data format into correct json array in oder to import to the Mongodb.

Website2/Restaurant-Information: This folder contains all source code for our UI.
### UI
Our UI is designed using Django framework with Mongodb and deployed to Heroku polatform. The link of our UI is here: https://restaurant-information.herokuapp.com/result/
