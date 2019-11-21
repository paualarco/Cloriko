package com.cloriko

import java.time.{ Instant, LocalDate }

package object db {

  final case class userpods(
    username: String,
    pod_id: String,
    name: String)

  final case class Data(
    username: String,
    pod_id: String,
    name: String)

  case class CassandraConfig(
    host: String,
    port: Int,
    username: String,
    password: String,
    keyspace: String = "Cloriko",
    preparedStatementCacheSize: Int,
    sessionBuckets: Int)

}
