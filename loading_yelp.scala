import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.udf


val spark = org.apache.spark.sql.SparkSession.builder.getOrCreate
//process business data
val test = spark.read.json("yelp")
val temp = test.select($"businesses")
val tmp = temp.select(explode($"businesses").as("flat_business"))
val tmp1 = tmp.select($"flat_business.phone", $"flat_business.review_count", $"flat_business.rating", $"flat_business.price",$"flat_business.id", $"flat_business.image_url")

//process nois data
val test1 = sc.wholeTextFiles("noisecountresult")
val noise = test1.map(_._2).flatMap(_.split("\n"))
val noise1 = noise.map(line => line.substring(1, line.length-1)).map(_.split(","))
val pair = noise1.map(line=>("+1"+line(0),line(1).toInt))
val df = pair.toDF("phone","noise")

//join business with noise
val data = tmp1.join(df,Seq("phone"))

//writing udf to add "+1" to phone numbers
val plusone: String => String = "+1"+_
val plusudf = udf(plusone)
//udf to transform yelp $$$ pricing to integer
val cnt: String=>Int = _.count('$'==)
val udfcnt = udf(cnt)
//udf to cast schema
val cas: String=>Int = _.toInt
val udfcast = udf(cas)

//process sanitary data
val san = spark.read.json("data1")
val sss = san.filter($"grade".isNotNull && $"SCORE".isNotNull).na.drop()
val sa = sss.select($"GRADE",$"Latitude",$"Longitude",$"PHONE",$"SCORE")
//take the highest score as the score for each restaurant
val ssr = sa.join(sss.groupBy($"PHONE").agg(min($"SCORE") as "ascore").withColumnRenamed("PHONE","aphone"),$"PHONE"===$"aphone" && $"SCORE" ===$"ascore").drop("aphone").drop("ascore")

//transform to same phone format
val sani = ssr.withColumn("PHONE",plusudf($"PHONE"))

//join former joined talbe with sanitary data
val result = data.join(sani, Seq("phone")).na.drop()
//filter only restaurants with photos
val output = result.drop($"id").drop($"score").filter($"image_url" =!= "")


//write single json file to disk for database input
output.repartition(1).write.mode("append").json("output.json")


val result1 = result.dropDuplicates().persist()
//take highest review count as the review count for each restaurant
val result2 = result1.join(result1.groupBy($"PHONE").agg(max($"review_count") as "reviews").withColumnRenamed("PHONE","aphone"),$"PHONE"===$"aphone" && $"review_count" ===$"reviews").drop("aphone").drop("review_count")
val result3 = result2.withColumn("price",udfcnt($"price")).withColumn("score",udfcast($"score")).na.drop()



//Linear regression
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}
import org.apache.log4j._
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.mllib.linalg.Vectors
Logger.getLogger(“org”).setLevel(Level.ERROR)

val assembler = new VectorAssembler().setInputCols(Array("price","reviews","score","noise")).setOutputCol("features")
val table = assembler.transform(result3).select($"rating".as("lable"),$"features")

//dummy varialbe with Grade A sanitary Grade
val sign = udf((x:Int)=>if(x<=15) 1 else 0)
val result4 = result3.withColumn("score",sign($"score"))
val table1 = assembler.transform(result4).select($"rating".as("lable"),$"features")
//log transform for noise
val result5 = result4.withColumn("noise",log1p($"noise"))
val table2 = assembler.transform(result5).select($"rating".as("lable"),$"features")

val lr = new LinearRegression()

val lrModel = lr.fit(table)

