import java.net.InetAddress

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.{Milliseconds, Seconds, StreamingContext}
import org.apache.spark.{SparkConf, TaskContext}
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object StreamKafka {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("StreamKafka").setMaster("local")
    val streamingContext = new StreamingContext(sparkConf, Seconds(10))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "example",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("kafka-test")
    val stream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    stream.map(record => (record.key, record.value))

    stream.foreachRDD { rdd =>
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd.mapPartitions { iter =>
        val osr: OffsetRange = offsetRanges(TaskContext.get.partitionId)
        val host = InetAddress.getLocalHost().getHostName()
        val count = iter.size
        Seq(s"${host} ${osr.topic} ${osr.partition} ${count}").toIterator
      }.collect.sorted.foreach(println)
    }

    streamingContext.start()
    streamingContext.awaitTermination()
  }


}
