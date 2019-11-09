package com.cloriko.cassandra

import java.time.LocalDate

import com.cloriko.db.{CassandraConfig, userpods}
import com.datastax.driver.core.{Cluster, ConsistencyLevel, QueryOptions}
import com.typesafe.scalalogging.LazyLogging
import io.getquill.{CassandraMonixContext, SnakeCase}
import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.Future

class Cassandra(cassandraConfig: CassandraConfig) extends LazyLogging {

  private lazy val cluster: Cluster = Cluster.builder
    .addContactPoint(cassandraConfig.host)
    .withPort(cassandraConfig.port)
    .withCredentials(cassandraConfig.username, cassandraConfig.password)
    .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM))
    .build()

  private implicit val scheduler = Scheduler.Implicits.global

  lazy val ctx: CassandraMonixContext[SnakeCase] =
    new CassandraMonixContext[SnakeCase](
      SnakeCase,
      cluster,
      cassandraConfig.keyspace,
      cassandraConfig.preparedStatementCacheSize)

  import ctx._

  def createTable() = {
    cluster.connect("cloriko").execute("CREATE TABLE USERPODS(username text, podId text, name text, PRIMARY KEY ((username), podId)) ;")
  }

  val q = quote {
    query[userpods].insert(_.username -> "MyName", _.podid -> "1011L", _.name -> "d")
  }

  def q2 =
    ctx.run {
      quote {
        query[userpods].insert(lift(userpods("a2", "aa", "v")))
      }
    }
}
