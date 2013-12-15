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
  serialized_pb='\n\x10input/data.proto\x12\x12protobuf.srl.query\"`\n\x0b\x44\x61taRequest\x12\x0e\n\x06userId\x18\x01 \x02(\t\x12\x11\n\tsessionId\x18\x02 \x02(\t\x12.\n\x05items\x18\x03 \x03(\x0b\x32\x1f.protobuf.srl.query.ItemRequest\"a\n\x0bItemRequest\x12\x0e\n\x06itemId\x18\x01 \x03(\t\x12,\n\x05query\x18\x02 \x01(\x0e\x32\x1d.protobuf.srl.query.ItemQuery\x12\x14\n\x0c\x61\x64vanceQuery\x18\x03 \x01(\x0c\"Z\n\x08\x44\x61taSend\x12\x0e\n\x06userId\x18\x01 \x02(\t\x12\x11\n\tsessionId\x18\x02 \x02(\t\x12+\n\x05items\x18\x03 \x03(\x0b\x32\x1c.protobuf.srl.query.ItemSend\"X\n\x08ItemSend\x12,\n\x05query\x18\x01 \x01(\x0e\x32\x1d.protobuf.srl.query.ItemQuery\x12\x10\n\x08isInsert\x18\x02 \x01(\x08\x12\x0c\n\x04\x64\x61ta\x18\x03 \x01(\x0c\"=\n\nDataResult\x12/\n\x07results\x18\x01 \x03(\x0b\x32\x1e.protobuf.srl.query.ItemResult\"H\n\nItemResult\x12,\n\x05query\x18\x01 \x01(\x0e\x32\x1d.protobuf.srl.query.ItemQuery\x12\x0c\n\x04\x64\x61ta\x18\x03 \x01(\x0c\"Q\n\x16\x41\x64vanceCourseGradePull\x12\x10\n\x08\x63ourseId\x18\x01 \x02(\t\x12\x15\n\rpullAllGrades\x18\x02 \x01(\x08\x12\x0e\n\x06userId\x18\x03 \x03(\t\"O\n\x14\x41\x64vanceUserGradePull\x12\x0e\n\x06userId\x18\x01 \x02(\t\x12\x15\n\rpullAllGrades\x18\x02 \x01(\x08\x12\x10\n\x08\x63ourseId\x18\x03 \x03(\t\"P\n\rAdvanceReview\x12\x10\n\x08\x63ourseId\x18\x01 \x02(\t\x12\x14\n\x0c\x61ssignmentId\x18\x02 \x02(\t\x12\x17\n\x0f\x63ourseProblemId\x18\x03 \x02(\t*\x9a\x01\n\tItemQuery\x12\n\n\x06\x43OURSE\x10\x00\x12\x0e\n\nASSIGNMENT\x10\x01\x12\x12\n\x0e\x43OURSE_PROBLEM\x10\x02\x12\x10\n\x0c\x42\x41NK_PROBLEM\x10\x03\x12\r\n\tUSERGROUP\x10\x04\x12\x0f\n\x0b\x43LASS_GRADE\x10\x05\x12\r\n\tUSER_INFO\x10\x06\x12\x0c\n\x08SOLUTION\x10\x07\x12\x0e\n\nEXPERIMENT\x10\x08')

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
  ],
  containing_type=None,
  options=None,
  serialized_start=803,
  serialized_end=957,
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



_DATAREQUEST = _descriptor.Descriptor(
  name='DataRequest',
  full_name='protobuf.srl.query.DataRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='userId', full_name='protobuf.srl.query.DataRequest.userId', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='sessionId', full_name='protobuf.srl.query.DataRequest.sessionId', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='items', full_name='protobuf.srl.query.DataRequest.items', index=2,
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
  serialized_end=136,
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
  serialized_start=138,
  serialized_end=235,
)


_DATASEND = _descriptor.Descriptor(
  name='DataSend',
  full_name='protobuf.srl.query.DataSend',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='userId', full_name='protobuf.srl.query.DataSend.userId', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='sessionId', full_name='protobuf.srl.query.DataSend.sessionId', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='items', full_name='protobuf.srl.query.DataSend.items', index=2,
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
  serialized_start=237,
  serialized_end=327,
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
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=329,
  serialized_end=417,
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
  serialized_start=419,
  serialized_end=480,
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
      name='data', full_name='protobuf.srl.query.ItemResult.data', index=1,
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
  serialized_start=482,
  serialized_end=554,
)


_ADVANCECOURSEGRADEPULL = _descriptor.Descriptor(
  name='AdvanceCourseGradePull',
  full_name='protobuf.srl.query.AdvanceCourseGradePull',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='courseId', full_name='protobuf.srl.query.AdvanceCourseGradePull.courseId', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='pullAllGrades', full_name='protobuf.srl.query.AdvanceCourseGradePull.pullAllGrades', index=1,
      number=2, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='userId', full_name='protobuf.srl.query.AdvanceCourseGradePull.userId', index=2,
      number=3, type=9, cpp_type=9, label=3,
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
  serialized_start=556,
  serialized_end=637,
)


_ADVANCEUSERGRADEPULL = _descriptor.Descriptor(
  name='AdvanceUserGradePull',
  full_name='protobuf.srl.query.AdvanceUserGradePull',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='userId', full_name='protobuf.srl.query.AdvanceUserGradePull.userId', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='pullAllGrades', full_name='protobuf.srl.query.AdvanceUserGradePull.pullAllGrades', index=1,
      number=2, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='courseId', full_name='protobuf.srl.query.AdvanceUserGradePull.courseId', index=2,
      number=3, type=9, cpp_type=9, label=3,
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
  serialized_start=639,
  serialized_end=718,
)


_ADVANCEREVIEW = _descriptor.Descriptor(
  name='AdvanceReview',
  full_name='protobuf.srl.query.AdvanceReview',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='courseId', full_name='protobuf.srl.query.AdvanceReview.courseId', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='assignmentId', full_name='protobuf.srl.query.AdvanceReview.assignmentId', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='courseProblemId', full_name='protobuf.srl.query.AdvanceReview.courseProblemId', index=2,
      number=3, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
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
  serialized_start=720,
  serialized_end=800,
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
DESCRIPTOR.message_types_by_name['AdvanceCourseGradePull'] = _ADVANCECOURSEGRADEPULL
DESCRIPTOR.message_types_by_name['AdvanceUserGradePull'] = _ADVANCEUSERGRADEPULL
DESCRIPTOR.message_types_by_name['AdvanceReview'] = _ADVANCEREVIEW

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

class AdvanceCourseGradePull(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ADVANCECOURSEGRADEPULL

  # @@protoc_insertion_point(class_scope:protobuf.srl.query.AdvanceCourseGradePull)

class AdvanceUserGradePull(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ADVANCEUSERGRADEPULL

  # @@protoc_insertion_point(class_scope:protobuf.srl.query.AdvanceUserGradePull)

class AdvanceReview(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _ADVANCEREVIEW

  # @@protoc_insertion_point(class_scope:protobuf.srl.query.AdvanceReview)


# @@protoc_insertion_point(module_scope)
