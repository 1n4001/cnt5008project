// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package edu.ucf.student.jdavies.cnt5008.proto;

public interface MessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:proto.Message)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>bool reliable = 1;</code>
   */
  boolean getReliable();

  /**
   * <code>.proto.Message.Mode mode = 2;</code>
   */
  int getModeValue();
  /**
   * <code>.proto.Message.Mode mode = 2;</code>
   */
  edu.ucf.student.jdavies.cnt5008.proto.Message.Mode getMode();

  /**
   * <code>uint32 sequence = 3;</code>
   */
  int getSequence();

  /**
   * <code>bytes payload = 4;</code>
   */
  com.google.protobuf.ByteString getPayload();
}
