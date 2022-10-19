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
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG      -> "localhost:9092",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG   -> "io.confluent.kafka.serializers.KafkaAvroSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "io.confluent.kafka.serializers.KafkaAvroSerializer",
        "schema.registry.url"                        -> "http://localhost:8081"
      )
    )

  val schemaKey: Schema = new Parser().parse(Source.fromURL(getClass.getResource("/avro/quickstart-key.avsc")).mkString)
  val genericKeyRecord: GenericRecord = new GenericData.Record(schemaKey)

  val schemaValue: Schema = new Parser().parse(Source.fromURL(getClass.getResource("/avro/quickstart-value.avsc")).mkString)
  val genericValueRecord: GenericRecord = new GenericData.Record(schemaValue)

  val scn: ScenarioBuilder = scenario("Kafka Test")
    .exec(session => {
      genericKeyRecord.put("key", "MyKey002")
      val newsession = session.set("genericKeyRecord", genericKeyRecord)
      newsession
    }
    )
    .exec(session => {
      genericValueRecord.put("name1", "TUTU1")
      genericValueRecord.put("name2", "TATA2")
      genericValueRecord.put("name3", "TOTO3")

      val newsession = session.set("genericValueRecord", genericValueRecord)
      newsession
    }
    )
    .exec(
      kafka("Avro Request")
        .send[GenericRecord, GenericRecord](session => session("genericKeyRecord").as[GenericRecord], session => session("genericValueRecord").as[GenericRecord])
    )



  setUp(
    scn
      .inject(constantUsersPerSec(10) during (5.seconds))
  )
    .protocols(kafkaConf)
}
