// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package edu.ucf.student.jdavies.cnt5008.proto;

public final class Proto {
  private Proto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_proto_HostId_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_proto_HostId_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_proto_Beacon_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_proto_Beacon_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_proto_Header_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_proto_Header_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_proto_Response_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_proto_Response_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_proto_Message_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_proto_Message_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016messages.proto\022\005proto\"\"\n\006HostId\022\n\n\002ip\030" +
      "\001 \001(\005\022\014\n\004port\030\002 \001(\005\"n\n\006Beacon\022\035\n\006hostId\030" +
      "\001 \001(\0132\r.proto.HostId\022$\n\006status\030\002 \001(\0162\024.p" +
      "roto.Beacon.Status\"\037\n\006Status\022\013\n\007PRESENT\020" +
      "\000\022\010\n\004GONE\020\001\"i\n\006Header\022 \n\004mode\030\001 \001(\0162\022.pr" +
      "oto.Header.Mode\022\020\n\010sequence\030\002 \001(\005\022\020\n\010res" +
      "ponse\030\003 \001(\010\"\031\n\004Mode\022\007\n\003ACK\020\000\022\010\n\004NACK\020\001\"\'" +
      "\n\010Response\022\033\n\004host\030\001 \001(\0132\r.proto.HostId\"" +
      "X\n\007Message\022\035\n\006source\030\001 \001(\0132\r.proto.HostI" +
      "d\022\035\n\006header\030\002 \001(\0132\r.proto.Header\022\017\n\007payl",
      "oad\030\003 \001(\014B0\n%edu.ucf.student.jdavies.cnt" +
      "5008.protoB\005ProtoP\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_proto_HostId_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_proto_HostId_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_proto_HostId_descriptor,
        new java.lang.String[] { "Ip", "Port", });
    internal_static_proto_Beacon_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_proto_Beacon_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_proto_Beacon_descriptor,
        new java.lang.String[] { "HostId", "Status", });
    internal_static_proto_Header_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_proto_Header_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_proto_Header_descriptor,
        new java.lang.String[] { "Mode", "Sequence", "Response", });
    internal_static_proto_Response_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_proto_Response_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_proto_Response_descriptor,
        new java.lang.String[] { "Host", });
    internal_static_proto_Message_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_proto_Message_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_proto_Message_descriptor,
        new java.lang.String[] { "Source", "Header", "Payload", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
