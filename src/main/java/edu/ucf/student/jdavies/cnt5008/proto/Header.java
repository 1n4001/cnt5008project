// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: messages.proto

package edu.ucf.student.jdavies.cnt5008.proto;

/**
 * Protobuf type {@code proto.Header}
 */
public  final class Header extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:proto.Header)
    HeaderOrBuilder {
  // Use Header.newBuilder() to construct.
  private Header(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Header() {
    mode_ = 0;
    sequence_ = 0;
    response_ = false;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
  }
  private Header(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    int mutable_bitField0_ = 0;
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!input.skipField(tag)) {
              done = true;
            }
            break;
          }
          case 8: {
            int rawValue = input.readEnum();

            mode_ = rawValue;
            break;
          }
          case 16: {

            sequence_ = input.readInt32();
            break;
          }
          case 24: {

            response_ = input.readBool();
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return edu.ucf.student.jdavies.cnt5008.proto.Proto.internal_static_proto_Header_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return edu.ucf.student.jdavies.cnt5008.proto.Proto.internal_static_proto_Header_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            edu.ucf.student.jdavies.cnt5008.proto.Header.class, edu.ucf.student.jdavies.cnt5008.proto.Header.Builder.class);
  }

  /**
   * Protobuf enum {@code proto.Header.Mode}
   */
  public enum Mode
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>ACK = 0;</code>
     */
    ACK(0),
    /**
     * <code>NACK = 1;</code>
     */
    NACK(1),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>ACK = 0;</code>
     */
    public static final int ACK_VALUE = 0;
    /**
     * <code>NACK = 1;</code>
     */
    public static final int NACK_VALUE = 1;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new java.lang.IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static Mode valueOf(int value) {
      return forNumber(value);
    }

    public static Mode forNumber(int value) {
      switch (value) {
        case 0: return ACK;
        case 1: return NACK;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Mode>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Mode> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Mode>() {
            public Mode findValueByNumber(int number) {
              return Mode.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return edu.ucf.student.jdavies.cnt5008.proto.Header.getDescriptor().getEnumTypes().get(0);
    }

    private static final Mode[] VALUES = values();

    public static Mode valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private Mode(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:proto.Header.Mode)
  }

  public static final int MODE_FIELD_NUMBER = 1;
  private int mode_;
  /**
   * <code>.proto.Header.Mode mode = 1;</code>
   */
  public int getModeValue() {
    return mode_;
  }
  /**
   * <code>.proto.Header.Mode mode = 1;</code>
   */
  public edu.ucf.student.jdavies.cnt5008.proto.Header.Mode getMode() {
    edu.ucf.student.jdavies.cnt5008.proto.Header.Mode result = edu.ucf.student.jdavies.cnt5008.proto.Header.Mode.valueOf(mode_);
    return result == null ? edu.ucf.student.jdavies.cnt5008.proto.Header.Mode.UNRECOGNIZED : result;
  }

  public static final int SEQUENCE_FIELD_NUMBER = 2;
  private int sequence_;
  /**
   * <code>int32 sequence = 2;</code>
   */
  public int getSequence() {
    return sequence_;
  }

  public static final int RESPONSE_FIELD_NUMBER = 3;
  private boolean response_;
  /**
   * <code>bool response = 3;</code>
   */
  public boolean getResponse() {
    return response_;
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (mode_ != edu.ucf.student.jdavies.cnt5008.proto.Header.Mode.ACK.getNumber()) {
      output.writeEnum(1, mode_);
    }
    if (sequence_ != 0) {
      output.writeInt32(2, sequence_);
    }
    if (response_ != false) {
      output.writeBool(3, response_);
    }
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (mode_ != edu.ucf.student.jdavies.cnt5008.proto.Header.Mode.ACK.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(1, mode_);
    }
    if (sequence_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, sequence_);
    }
    if (response_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(3, response_);
    }
    memoizedSize = size;
    return size;
  }

  private static final long serialVersionUID = 0L;
  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof edu.ucf.student.jdavies.cnt5008.proto.Header)) {
      return super.equals(obj);
    }
    edu.ucf.student.jdavies.cnt5008.proto.Header other = (edu.ucf.student.jdavies.cnt5008.proto.Header) obj;

    boolean result = true;
    result = result && mode_ == other.mode_;
    result = result && (getSequence()
        == other.getSequence());
    result = result && (getResponse()
        == other.getResponse());
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + MODE_FIELD_NUMBER;
    hash = (53 * hash) + mode_;
    hash = (37 * hash) + SEQUENCE_FIELD_NUMBER;
    hash = (53 * hash) + getSequence();
    hash = (37 * hash) + RESPONSE_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getResponse());
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static edu.ucf.student.jdavies.cnt5008.proto.Header parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(edu.ucf.student.jdavies.cnt5008.proto.Header prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code proto.Header}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:proto.Header)
      edu.ucf.student.jdavies.cnt5008.proto.HeaderOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return edu.ucf.student.jdavies.cnt5008.proto.Proto.internal_static_proto_Header_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return edu.ucf.student.jdavies.cnt5008.proto.Proto.internal_static_proto_Header_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              edu.ucf.student.jdavies.cnt5008.proto.Header.class, edu.ucf.student.jdavies.cnt5008.proto.Header.Builder.class);
    }

    // Construct using edu.ucf.student.jdavies.cnt5008.proto.Header.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    public Builder clear() {
      super.clear();
      mode_ = 0;

      sequence_ = 0;

      response_ = false;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return edu.ucf.student.jdavies.cnt5008.proto.Proto.internal_static_proto_Header_descriptor;
    }

    public edu.ucf.student.jdavies.cnt5008.proto.Header getDefaultInstanceForType() {
      return edu.ucf.student.jdavies.cnt5008.proto.Header.getDefaultInstance();
    }

    public edu.ucf.student.jdavies.cnt5008.proto.Header build() {
      edu.ucf.student.jdavies.cnt5008.proto.Header result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public edu.ucf.student.jdavies.cnt5008.proto.Header buildPartial() {
      edu.ucf.student.jdavies.cnt5008.proto.Header result = new edu.ucf.student.jdavies.cnt5008.proto.Header(this);
      result.mode_ = mode_;
      result.sequence_ = sequence_;
      result.response_ = response_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof edu.ucf.student.jdavies.cnt5008.proto.Header) {
        return mergeFrom((edu.ucf.student.jdavies.cnt5008.proto.Header)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(edu.ucf.student.jdavies.cnt5008.proto.Header other) {
      if (other == edu.ucf.student.jdavies.cnt5008.proto.Header.getDefaultInstance()) return this;
      if (other.mode_ != 0) {
        setModeValue(other.getModeValue());
      }
      if (other.getSequence() != 0) {
        setSequence(other.getSequence());
      }
      if (other.getResponse() != false) {
        setResponse(other.getResponse());
      }
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      edu.ucf.student.jdavies.cnt5008.proto.Header parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (edu.ucf.student.jdavies.cnt5008.proto.Header) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private int mode_ = 0;
    /**
     * <code>.proto.Header.Mode mode = 1;</code>
     */
    public int getModeValue() {
      return mode_;
    }
    /**
     * <code>.proto.Header.Mode mode = 1;</code>
     */
    public Builder setModeValue(int value) {
      mode_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>.proto.Header.Mode mode = 1;</code>
     */
    public edu.ucf.student.jdavies.cnt5008.proto.Header.Mode getMode() {
      edu.ucf.student.jdavies.cnt5008.proto.Header.Mode result = edu.ucf.student.jdavies.cnt5008.proto.Header.Mode.valueOf(mode_);
      return result == null ? edu.ucf.student.jdavies.cnt5008.proto.Header.Mode.UNRECOGNIZED : result;
    }
    /**
     * <code>.proto.Header.Mode mode = 1;</code>
     */
    public Builder setMode(edu.ucf.student.jdavies.cnt5008.proto.Header.Mode value) {
      if (value == null) {
        throw new NullPointerException();
      }
      
      mode_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.proto.Header.Mode mode = 1;</code>
     */
    public Builder clearMode() {
      
      mode_ = 0;
      onChanged();
      return this;
    }

    private int sequence_ ;
    /**
     * <code>int32 sequence = 2;</code>
     */
    public int getSequence() {
      return sequence_;
    }
    /**
     * <code>int32 sequence = 2;</code>
     */
    public Builder setSequence(int value) {
      
      sequence_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 sequence = 2;</code>
     */
    public Builder clearSequence() {
      
      sequence_ = 0;
      onChanged();
      return this;
    }

    private boolean response_ ;
    /**
     * <code>bool response = 3;</code>
     */
    public boolean getResponse() {
      return response_;
    }
    /**
     * <code>bool response = 3;</code>
     */
    public Builder setResponse(boolean value) {
      
      response_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bool response = 3;</code>
     */
    public Builder clearResponse() {
      
      response_ = false;
      onChanged();
      return this;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return this;
    }


    // @@protoc_insertion_point(builder_scope:proto.Header)
  }

  // @@protoc_insertion_point(class_scope:proto.Header)
  private static final edu.ucf.student.jdavies.cnt5008.proto.Header DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new edu.ucf.student.jdavies.cnt5008.proto.Header();
  }

  public static edu.ucf.student.jdavies.cnt5008.proto.Header getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Header>
      PARSER = new com.google.protobuf.AbstractParser<Header>() {
    public Header parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
        return new Header(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Header> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Header> getParserForType() {
    return PARSER;
  }

  public edu.ucf.student.jdavies.cnt5008.proto.Header getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

