# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: input/data.proto

from google.protobuf.internal import enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)




DESCRIPTOR = _descriptor.FileDescriptor(
  name='input/data.proto',
  package='protobuf.srl.query',
  serialized_pb='\n\x10input/data.proto\x12\x12protobuf.srl.query\"=\n\x0b\x44\x61taRequest\x12.\n\x05items\x18\x03 \x03(\x0b\x32\x1f.protobuf.srl.query.ItemRequest\"a\n\x0bItemRequest\x12\x0e\n\x06itemId\x18\x01 \x03(\t\x12,\n\x05query\x18\x02 \x01(\x0e\x32\x1d.protobuf.srl.query.ItemQuery\x12\x14\n\x0c\x61\x64vanceQuery\x18\x03 \x01(\x0c\"7\n\x08\x44\x61taSend\x12+\n\x05items\x18\x03 \x03(\x0b\x32\x1c.protobuf.srl.query.ItemSend\"j\n\x08ItemSend\x12,\n\x05query\x18\x01 \x01(\x0e\x32\x1d.protobuf.srl.query.ItemQuery\x12\x10\n\x08isInsert\x18\x02 \x01(\x08\x12\x0c\n\x04\x64\x61ta\x18\x03 \x01(\x0c\x12\x10\n\x08textData\x18\x04 \x03(\t\"=\n\nDataResult\x12/\n\x07results\x18\x01 \x03(\x0b\x32\x1e.protobuf.srl.query.ItemResult\"\\\n\nItemResult\x12,\n\x05query\x18\x01 \x01(\x0e\x32\x1d.protobuf.srl.query.ItemQuery\x12\x12\n\nreturnText\x18\x02 \x01(\t\x12\x0c\n\x04\x64\x61ta\x18\x03 \x01(\x0c*\xd8\x01\n\tItemQuery\x12\n\n\x06\x43OURSE\x10\x00\x12\x0e\n\nASSIGNMENT\x10\x01\x12\x12\n\x0e\x43OURSE_PROBLEM\x10\x02\x12\x10\n\x0c\x42\x41NK_PROBLEM\x10\x03\x12\r\n\tUSERGROUP\x10\x04\x12\x0f\n\x0b\x43LASS_GRADE\x10\x05\x12\r\n\tUSER_INFO\x10\x06\x12\x0c\n\x08SOLUTION\x10\x07\x12\x0e\n\nEXPERIMENT\x10\x08\x12\n\n\x06SCHOOL\x10\t\x12\x11\n\rCOURSE_SEARCH\x10\n\x12\x0f\n\x0b\x42\x41NK_SEARCH\x10\x0b\x12\x0c\n\x08REGISTER\x10\x0c')

_ITEMQUERY = _descriptor.EnumDescriptor(
  name='ItemQuery',
  full_name='protobuf.srl.query.ItemQuery',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='COURSE', index=0, number=0,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='ASSIGNMENT', index=1, number=1,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='COURSE_PROBLEM', index=2, number=2,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='BANK_PROBLEM', index=3, number=3,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='USERGROUP', index=4, number=4,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='CLASS_GRADE', index=5, number=5,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='USER_INFO', index=6, number=6,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='SOLUTION', index=7, number=7,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='EXPERIMENT', index=8, number=8,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='SCHOOL', index=9, number=9,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='COURSE_SEARCH', index=10, number=10,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='BANK_SEARCH', index=11, number=11,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='REGISTER', index=12, number=12,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=525,
  serialized_end=741,
)

ItemQuery = enum_type_wrapper.EnumTypeWrapper(_ITEMQUERY)
COURSE = 0
ASSIGNMENT = 1
COURSE_PROBLEM = 2
BANK_PROBLEM = 3
USERGROUP = 4
CLASS_GRADE = 5
USER_INFO = 6
SOLUTION = 7
EXPERIMENT = 8
SCHOOL = 9
COURSE_SEARCH = 10
BANK_SEARCH = 11
REGISTER = 12



_DATAREQUEST = _descriptor.Descriptor(
  name='DataRequest',
  full_name='protobuf.srl.query.DataRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='items', full_name='protobuf.srl.query.DataRequest.items', index=0,
      number=3, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
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
  serialized_start=40,
  serialized_end=101,
)


_ITEMREQUEST = _descriptor.Descriptor(
  name='ItemRequest',
  full_name='protobuf.srl.query.ItemRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='itemId', full_name='protobuf.srl.query.ItemRequest.itemId', index=0,
      number=1, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='query', full_name='protobuf.srl.query.ItemRequest.query', index=1,
      number=2, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='advanceQuery', full_name='protobuf.srl.query.ItemRequest.advanceQuery', index=2,
      number=3, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value="",
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
  serialized_start=103,
  serialized_end=200,
)


_DATASEND = _descriptor.Descriptor(
  name='DataSend',
  full_name='protobuf.srl.query.DataSend',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='items', full_name='protobuf.srl.query.DataSend.items', index=0,
      number=3, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
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
  serialized_start=202,
  serialized_end=257,
)


_ITEMSEND = _descriptor.Descriptor(
  name='ItemSend',
  full_name='protobuf.srl.query.ItemSend',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='query', full_name='protobuf.srl.query.ItemSend.query', index=0,
      number=1, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='isInsert', full_name='protobuf.srl.query.ItemSend.isInsert', index=1,
      number=2, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='data', full_name='protobuf.srl.query.ItemSend.data', index=2,
      number=3, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value="",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='textData', full_name='protobuf.srl.query.ItemSend.textData', index=3,
      number=4, type=9, cpp_type=9, label=3,
      has_default_value=False, default_value=[],
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
  serialized_start=259,
  serialized_end=365,
)


_DATARESULT = _descriptor.Descriptor(
  name='DataResult',
  full_name='protobuf.srl.query.DataResult',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='results', full_name='protobuf.srl.query.DataResult.results', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
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
  serialized_start=367,
  serialized_end=428,
)


_ITEMRESULT = _descriptor.Descriptor(
  name='ItemResult',
  full_name='protobuf.srl.query.ItemResult',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='query', full_name='protobuf.srl.query.ItemResult.query', index=0,
      number=1, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='returnText', full_name='protobuf.srl.query.ItemResult.returnText', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='data', full_name='protobuf.srl.query.ItemResult.data', index=2,
      number=3, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value="",
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
  serialized_start=430,
  serialized_end=522,
)

_DATAREQUEST.fields_by_name['items'].message_type = _ITEMREQUEST
_ITEMREQUEST.fields_by_name['query'].enum_type = _ITEMQUERY
_DATASEND.fields_by_name['items'].message_type = _ITEMSEND
_ITEMSEND.fields_by_name['query'].enum_type = _ITEMQUERY
_DATARESULT.fields_by_name['results'].message_type = _ITEMRESULT
_ITEMRESULT.fields_by_name['query'].enum_type = _ITEMQUERY
DESCRIPTOR.message_types_by_name['DataRequest'] = _DATAREQUEST
DESCRIPTOR.message_types_by_name['ItemRequest'] = _ITEMREQUEST
DESCRIPTOR.message_types_by_name['DataSend'] = _DATASEND
DESCRIPTOR.message_types_by_name['ItemSend'] = _ITEMSEND
DESCRIPTOR.message_types_by_name['DataResult'] = _DATARESULT
DESCRIPTOR.message_types_by_name['ItemResult'] = _ITEMRESULT

class DataRequest(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _DATAREQUEST

  # @@protoc_insertion_point(class_scope:protobuf.srl.query.DataRequest)

class ItemRequest(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ITEMREQUEST

  # @@protoc_insertion_point(class_scope:protobuf.srl.query.ItemRequest)

class DataSend(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _DATASEND

  # @@protoc_insertion_point(class_scope:protobuf.srl.query.DataSend)

class ItemSend(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ITEMSEND

  # @@protoc_insertion_point(class_scope:protobuf.srl.query.ItemSend)

class DataResult(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _DATARESULT

  # @@protoc_insertion_point(class_scope:protobuf.srl.query.DataResult)

class ItemResult(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ITEMRESULT

  # @@protoc_insertion_point(class_scope:protobuf.srl.query.ItemResult)


# @@protoc_insertion_point(module_scope)
