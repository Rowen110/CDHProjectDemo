package com.cloudera.common

case class Header(messageId: String,timestamp: String,version: String,
                  channeld: String,dataSource:String,dataFamily: String
                 ) extends Serializable




