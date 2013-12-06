# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: input/message.proto

from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)




DESCRIPTOR = _descriptor.FileDescriptor(
  name='input/message.proto',
  package='protobuf.srl.request',
  serialized_pb='\n\x13input/message.proto\x12\x14protobuf.srl.request\"\xbe\x02\n\x07Request\x12\x45\n\x0brequestType\x18\x01 \x02(\x0e\x32).protobuf.srl.request.Request.MessageType:\x05LOGIN\x12\x35\n\x05login\x18\x02 \x01(\x0b\x32&.protobuf.srl.request.LoginInformation\x12\x11\n\totherData\x18\x03 \x01(\x0c\x12\x14\n\x0cresponseText\x18\x04 \x01(\t\x12\x13\n\x0bsessionInfo\x18\x05 \x01(\t\"w\n\x0bMessageType\x12\t\n\x05LOGIN\x10\x00\x12\x10\n\x0c\x44\x41TA_REQUEST\x10\x01\x12\x10\n\x0c\x44\x41TA_SENDING\x10\x02\x12\x0f\n\x0bRECOGNITION\x10\x03\x12\x0b\n\x07LOADING\x10\x04\x12\x0e\n\nSUBMISSION\x10\x05\x12\x0b\n\x07PENDING\x10\x06\"w\n\x10LoginInformation\x12\x10\n\x08username\x18\x01 \x02(\t\x12\x10\n\x08password\x18\x02 \x01(\t\x12\x12\n\nisLoggedIn\x18\x03 \x01(\x08\x12\x14\n\x0cisInstructor\x18\x04 \x01(\x08\x12\x15\n\risRegistering\x18\x05 \x01(\x08')



_REQUEST_MESSAGETYPE = _descriptor.EnumDescriptor(
  name='MessageType',
  full_name='protobuf.srl.request.Request.MessageType',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='LOGIN', index=0, number=0,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='DATA_REQUEST', index=1, number=1,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='DATA_SENDING', index=2, number=2,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='RECOGNITION', index=3, number=3,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='LOADING', index=4, number=4,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='SUBMISSION', index=5, number=5,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='PENDING', index=6, number=6,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=245,
  serialized_end=364,
)


_REQUEST = _descriptor.Descriptor(
  name='Request',
  full_name='protobuf.srl.request.Request',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='requestType', full_name='protobuf.srl.request.Request.requestType', index=0,
      number=1, type=14, cpp_type=8, label=2,
      has_default_value=True, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='login', full_name='protobuf.srl.request.Request.login', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='otherData', full_name='protobuf.srl.request.Request.otherData', index=2,
      number=3, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value="",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='responseText', full_name='protobuf.srl.request.Request.responseText', index=3,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='sessionInfo', full_name='protobuf.srl.request.Request.sessionInfo', index=4,
      number=5, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _REQUEST_MESSAGETYPE,
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=46,
  serialized_end=364,
)


_LOGININFORMATION = _descriptor.Descriptor(
  name='LoginInformation',
  full_name='protobuf.srl.request.LoginInformation',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='username', full_name='protobuf.srl.request.LoginInformation.username', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='password', full_name='protobuf.srl.request.LoginInformation.password', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='isLoggedIn', full_name='protobuf.srl.request.LoginInformation.isLoggedIn', index=2,
      number=3, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='isInstructor', full_name='protobuf.srl.request.LoginInformation.isInstructor', index=3,
      number=4, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='isRegistering', full_name='protobuf.srl.request.LoginInformation.isRegistering', index=4,
      number=5, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=366,
  serialized_end=485,
)

_REQUEST.fields_by_name['requestType'].enum_type = _REQUEST_MESSAGETYPE
_REQUEST.fields_by_name['login'].message_type = _LOGININFORMATION
_REQUEST_MESSAGETYPE.containing_type = _REQUEST;
DESCRIPTOR.message_types_by_name['Request'] = _REQUEST
DESCRIPTOR.message_types_by_name['LoginInformation'] = _LOGININFORMATION

class Request(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _REQUEST

  # @@protoc_insertion_point(class_scope:protobuf.srl.request.Request)

class LoginInformation(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _LOGININFORMATION

  # @@protoc_insertion_point(class_scope:protobuf.srl.request.LoginInformation)


# @@protoc_insertion_point(module_scope)
