package com.cloudera

case class PassengerFlowProfile(organizId: String, devices: Array[Device]) extends Serializable

case class Device(deviceId: String, countType: String) extends Serializable

case class MyRecord(deviceId: String, cmdType: String, parentDeviceId: String, startTime: String, totalTime: Int, inNum: Int, outNum: Int) extends Serializable
