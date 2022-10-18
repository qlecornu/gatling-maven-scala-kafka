package io.gatling.demo

import io.gatling.core.Predef._
import io.gatling.core.feeder.Feeder
import io.gatling.core.structure.ScenarioBuilder
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders
import ru.tinkoff.gatling.kafka.Predef._
import ru.tinkoff.gatling.kafka.protocol.KafkaProtocol

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._

class KafkaSimulation extends Simulation {

    val kafkaConf: KafkaProtocol = kafka
      .topic("quickstart")
      .properties(Map(ProducerConfig.ACKS_CONFIG -> "1"))

    val kafkaProtocolC: KafkaProtocol = kafka.requestReply
      .producerSettings(
        Map(
          ProducerConfig.ACKS_CONFIG              -> "1",
          ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
        ),
      )
      .consumeSettings(
        Map(
          "bootstrap.servers" -> "localhost:9092"
        ),
      )
      .timeout(5.seconds)
    val c                             = new AtomicInteger(0)
    val feeder: Feeder[Int]           = Iterator.continually(Map("kekey" -> c.incrementAndGet()))

    val headers: Headers = new RecordHeaders().add("test-header", "test_value".getBytes)

    val scn: ScenarioBuilder = scenario("Basic")
      .feed(feeder)
      .exec(
        kafka("ReqRep").requestReply
          .requestTopic("quickstart")
          .replyTopic("quickstart")
          .send[String, String]("#{kekey}", """{ "m": "dkf" }""", headers)
          .check(jsonPath("$.m").is("dkf"))
      )

    setUp(
      scn.inject(atOnceUsers(5))
    )
      .protocols(kafkaProtocolC)
      .maxDuration(120.seconds)
}
