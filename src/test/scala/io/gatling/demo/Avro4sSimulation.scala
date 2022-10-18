package io.gatling.demo

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import org.apache.avro.Schema
import org.apache.avro.Schema.Parser
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.kafka.clients.producer.ProducerConfig
import ru.tinkoff.gatling.kafka.Predef._
import ru.tinkoff.gatling.kafka.protocol.KafkaProtocol

import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps

class Avro4sSimulation extends Simulation {

  val kafkaConf: KafkaProtocol = kafka
    .topic("quickstart")
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG                   -> "1",
        // list of Kafka broker hostname and port pairs
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG      -> "localhost:9092",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "io.confluent.kafka.serializers.KafkaAvroSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "io.confluent.kafka.serializers.KafkaAvroSerializer",
        "schema.registry.url"                        -> "http://localhost:8081"
      )
    )

  val schemaKey: Schema = new Parser().parse(Source.fromURL(getClass.getResource("/avro/quickstart-key.avsc")).mkString)
  val genericKey: GenericRecord = new GenericData.Record(schemaKey)

  val schemaExample: Schema = new Parser().parse(Source.fromURL(getClass.getResource("/avro/quickstart-value.avsc")).mkString)
  val genericExample: GenericRecord = new GenericData.Record(schemaExample)

  val scn: ScenarioBuilder = scenario("Kafka Test")
    .exec(session => {
      genericKey.put("key", "MyKey002")
      val newsession = session.set("genericKey", genericKey)
      newsession
    }
    )
    .exec(session => {
      genericExample.put("name1", "TUTU1")
      genericExample.put("name2", "TATA2")
      genericExample.put("name3", "TOTO3")

      val newsession = session.set("genericExample", genericExample)
      newsession
    }
    )
    .exec(
      kafka(" GenericRecord Simple Request with Key")
        .send[GenericRecord, GenericRecord](session => session("genericKey").as[GenericRecord], session => session("genericExample").as[GenericRecord])
    )



  setUp(
    scn
      .inject(constantUsersPerSec(10) during (5.seconds)),
  )
    .protocols(kafkaConf)
}
