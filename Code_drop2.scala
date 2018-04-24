import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StringType

val conf = new SparkConf().setAppName().setMaster(master)
val sc = new SparkContext(conf)
val sqlContext = new SQLContext(sc)

//Noise Data Related
def getgeodata(data:String, idx:Int) : String = {
    val fields:Array[String] = data.split(",")
    if (fields.length >= idx) {
        return fields(idx)
    }
    return ""
}
val request = sc.textFile("loudacre/request/311_Service_Requests_from_2011.csv")
val noiserequest = request.filter(line => line.split(",")(5).contains("Noise"))
val rawgeodata = noiserequest.map(line => (getgeodata(line, 50),
getgeodata(line, 51)))
val cleangeodata = rawgeodata.filter(line => line._1 != "" && line._2 != "")

//Convert Noise RDD to DataFrame

def dfSchema(columnNames: List[String]): StructType = {
    StructType(
        Seq(
            StructField(name=columnNames(0), dataType=StringType, nullable=false),
            StructField(name=columnNames(1), dataType=StringType, nullable=false)
        )
    )
}

import org.apache.spark.sql.Row
def row(line: (String, String)): Row = Row(line._1, line._2)

val schema = dfSchema(List("Latitude_noise", "Longitude_noise"))
val data = cleangeodata.map(row)
val noiseFrame = sqlContext.createDataFrame(data, schema)


//Inspetion related
val insp = sqlContext.read.json("loudacre/inspection")


//Calculate distance in km
import scala.math
def deg2rad(deg: Double): Double = deg * (math.Pi/180)


// def getDistanceFromLatLonInKm(lat1:Double, lon1:Double, lat2:Double, lon2:Double) : Double = {
//     val R = 6371
//     val dLat = deg2rad(lat2-lat1)
//     val dLon = deg2rad(lon2-lon1)
//     val a = math.sin(dLat/2) * math.sin(dLat/2) + math.cos(deg2rad(lat1)) * math.cos(deg2rad(lat2)) * math.sin(dLon/2) * math.sin(dLon/2)
//     val c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
//     val d = R * c
//     return d
// }

def getDistanceFromLatLonInKm(geo: Seq[String]) : Array[String] = {
    val lat1 = geo(1).toDouble
    val lon1 = geo(2).toDouble
    val lat2 = geo(3).toDouble
    val lon2 = geo(4).toDouble
    val R = 6371
    val dLat = deg2rad(lat2-lat1)
    val dLon = deg2rad(lon2-lon1)
    val a = math.sin(dLat/2) * math.sin(dLat/2) + math.cos(deg2rad(lat1)) * math.cos(deg2rad(lat2)) * math.sin(dLon/2) * math.sin(dLon/2)
    val c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
    val d = R * c
    return Array(geo(0), d.toString)
}

// val getDistanceFromLatLonInKm_udf = udf(getDistanceFromLatLonInKm _)

val inspgeo = insp.select("PHONE", "Latitude", "Longitude")

// val inspgeordd = inspgeo.rdd.map(line => line.toSeq.map{case x: Any => x.toString})

val noise_insp = inspgeo.crossJoin(noiseFrame)

val noise_insp_rdd = noise_insp.rdd.map(line => line.toSeq.map{case x: String => x.toString})

val result = noise_insp_rdd.map(getDistanceFromLatLonInKm).filter(line=>line(1) < 0.5).groupBy(line=>line(0)).map(line=>(line._1, line._2.size))

result.saveAsTextFile("loudacre/noiseresult")


// val temp1 = noise_insp.withColumn("Latitude", noise_insp("Latitude").cast("double"))

// val temp2 = noise_insp.withColumn("Longitude", noise_insp("Longitude").cast("double"))

// val inspWithCount = temp2.withColumn("count", val getDistanceFromLatLonInKm_udf(Array(temp2("Latitude"),temp2("Longitude"),
//     temp2("Latitude_noise"),temp2("Longitude_noise"))))


// import sqlContext.implicits._
// import org.apache.spark.sql.SQLImplicits$.StringToColumn
// val noise_insp_count = noise_insp.withColumn("NoiseCount",
//                                              getDistanceFromLatLonInKm(noise_insp("Latitude"),
//                                                                        noise_insp("Longitude"),
//                                                                        noise_insp("Latitude_noise"),
//                                                                        noise_insp("Longitude_noise")))


// val inspgeonoise = insp.select("PHONE")

// val inspgeordd = inspgeo.rdd.map(line => line.toSeq.map{case x: String => x.toString})

// val noiserdd = cleangeodata.map(line => Seq(line._1.toDouble,line._2.toDouble))
// /*
// scala> noiserdd.take(1)
// res23: Array[Seq[Double]] = Array(List(40.5912891456854, -73.94372651867668))
// */

// val noisecount = inspgeordd.map(line => Seq(line(0), ))

