# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: input/sketch.proto

from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)




DESCRIPTOR = _descriptor.FileDescriptor(
  name='input/sketch.proto',
  package='protobuf.srl.sketch',
  serialized_pb='\n\x12input/sketch.proto\x12\x13protobuf.srl.sketch\"\x8a\x01\n\nSRL_Sketch\x12\x10\n\x08\x63ourseId\x18\x01 \x01(\x04\x12\x14\n\x0c\x61ssignmentId\x18\x02 \x01(\x04\x12\x11\n\tproblemId\x18\x03 \x01(\x04\x12\x10\n\x08\x64omainId\x18\x04 \x01(\t\x12/\n\x06sketch\x18\x05 \x03(\x0b\x32\x1f.protobuf.srl.sketch.SRL_Object\"\x90\x01\n\nSRL_Object\x12\x38\n\x04type\x18\x01 \x02(\x0e\x32*.protobuf.srl.sketch.SRL_Object.ObjectType\x12\x0e\n\x06object\x18\x02 \x02(\x0c\"8\n\nObjectType\x12\t\n\x05SHAPE\x10\x00\x12\n\n\x06STROKE\x10\x01\x12\t\n\x05POINT\x10\x02\x12\x08\n\x04LINE\x10\x03\"\xc0\x01\n\tSRL_Shape\x12\n\n\x02id\x18\x01 \x02(\t\x12\x0c\n\x04time\x18\x02 \x02(\x04\x12\x0c\n\x04name\x18\x03 \x01(\t\x12\x15\n\risUserCreated\x18\x04 \x01(\x08\x12<\n\x0finterpretations\x18\x05 \x03(\x0b\x32#.protobuf.srl.sketch.Interpretation\x12\x36\n\rsubComponents\x18\x06 \x03(\x0b\x32\x1f.protobuf.srl.sketch.SRL_Object\"d\n\nSRL_Stroke\x12\n\n\x02id\x18\x01 \x02(\t\x12\x0c\n\x04time\x18\x02 \x02(\x04\x12\x0c\n\x04name\x18\x03 \x01(\t\x12.\n\x06points\x18\x04 \x03(\x0b\x32\x1e.protobuf.srl.sketch.SRL_Point\"x\n\tSRL_Point\x12\n\n\x02id\x18\x01 \x02(\t\x12\x0c\n\x04time\x18\x02 \x02(\x04\x12\x0c\n\x04name\x18\x03 \x01(\t\x12\t\n\x01x\x18\x04 \x02(\x01\x12\t\n\x01y\x18\x05 \x02(\x01\x12\x10\n\x08pressure\x18\x06 \x01(\x01\x12\x0c\n\x04size\x18\x07 \x01(\x01\x12\r\n\x05speed\x18\x08 \x01(\x01\"F\n\x0eInterpretation\x12\x0c\n\x04name\x18\x01 \x02(\t\x12\x12\n\nconfidence\x18\x02 \x02(\x01\x12\x12\n\ncomplexity\x18\x03 \x01(\x01')



_SRL_OBJECT_OBJECTTYPE = _descriptor.EnumDescriptor(
  name='ObjectType',
  full_name='protobuf.srl.sketch.SRL_Object.ObjectType',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='SHAPE', index=0, number=0,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='STROKE', index=1, number=1,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='POINT', index=2, number=2,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='LINE', index=3, number=3,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=273,
  serialized_end=329,
)


_SRL_SKETCH = _descriptor.Descriptor(
  name='SRL_Sketch',
  full_name='protobuf.srl.sketch.SRL_Sketch',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='courseId', full_name='protobuf.srl.sketch.SRL_Sketch.courseId', index=0,
      number=1, type=4, cpp_type=4, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='assignmentId', full_name='protobuf.srl.sketch.SRL_Sketch.assignmentId', index=1,
      number=2, type=4, cpp_type=4, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='problemId', full_name='protobuf.srl.sketch.SRL_Sketch.problemId', index=2,
      number=3, type=4, cpp_type=4, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='domainId', full_name='protobuf.srl.sketch.SRL_Sketch.domainId', index=3,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='sketch', full_name='protobuf.srl.sketch.SRL_Sketch.sketch', index=4,
      number=5, type=11, cpp_type=10, label=3,
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
  serialized_start=44,
  serialized_end=182,
)


_SRL_OBJECT = _descriptor.Descriptor(
  name='SRL_Object',
  full_name='protobuf.srl.sketch.SRL_Object',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='type', full_name='protobuf.srl.sketch.SRL_Object.type', index=0,
      number=1, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='object', full_name='protobuf.srl.sketch.SRL_Object.object', index=1,
      number=2, type=12, cpp_type=9, label=2,
      has_default_value=False, default_value="",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _SRL_OBJECT_OBJECTTYPE,
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  serialized_start=185,
  serialized_end=329,
)


_SRL_SHAPE = _descriptor.Descriptor(
  name='SRL_Shape',
  full_name='protobuf.srl.sketch.SRL_Shape',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='protobuf.srl.sketch.SRL_Shape.id', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='time', full_name='protobuf.srl.sketch.SRL_Shape.time', index=1,
      number=2, type=4, cpp_type=4, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='name', full_name='protobuf.srl.sketch.SRL_Shape.name', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='isUserCreated', full_name='protobuf.srl.sketch.SRL_Shape.isUserCreated', index=3,
      number=4, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='interpretations', full_name='protobuf.srl.sketch.SRL_Shape.interpretations', index=4,
      number=5, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='subComponents', full_name='protobuf.srl.sketch.SRL_Shape.subComponents', index=5,
      number=6, type=11, cpp_type=10, label=3,
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
  serialized_start=332,
  serialized_end=524,
)


_SRL_STROKE = _descriptor.Descriptor(
  name='SRL_Stroke',
  full_name='protobuf.srl.sketch.SRL_Stroke',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='protobuf.srl.sketch.SRL_Stroke.id', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='time', full_name='protobuf.srl.sketch.SRL_Stroke.time', index=1,
      number=2, type=4, cpp_type=4, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='name', full_name='protobuf.srl.sketch.SRL_Stroke.name', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='points', full_name='protobuf.srl.sketch.SRL_Stroke.points', index=3,
      number=4, type=11, cpp_type=10, label=3,
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
  serialized_start=526,
  serialized_end=626,
)


_SRL_POINT = _descriptor.Descriptor(
  name='SRL_Point',
  full_name='protobuf.srl.sketch.SRL_Point',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='protobuf.srl.sketch.SRL_Point.id', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='time', full_name='protobuf.srl.sketch.SRL_Point.time', index=1,
      number=2, type=4, cpp_type=4, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='name', full_name='protobuf.srl.sketch.SRL_Point.name', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='x', full_name='protobuf.srl.sketch.SRL_Point.x', index=3,
      number=4, type=1, cpp_type=5, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='y', full_name='protobuf.srl.sketch.SRL_Point.y', index=4,
      number=5, type=1, cpp_type=5, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='pressure', full_name='protobuf.srl.sketch.SRL_Point.pressure', index=5,
      number=6, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='size', full_name='protobuf.srl.sketch.SRL_Point.size', index=6,
      number=7, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='speed', full_name='protobuf.srl.sketch.SRL_Point.speed', index=7,
      number=8, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=0,
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
  serialized_start=628,
  serialized_end=748,
)


_INTERPRETATION = _descriptor.Descriptor(
  name='Interpretation',
  full_name='protobuf.srl.sketch.Interpretation',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='protobuf.srl.sketch.Interpretation.name', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='confidence', full_name='protobuf.srl.sketch.Interpretation.confidence', index=1,
      number=2, type=1, cpp_type=5, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='complexity', full_name='protobuf.srl.sketch.Interpretation.complexity', index=2,
      number=3, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=0,
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
  serialized_start=750,
  serialized_end=820,
)

_SRL_SKETCH.fields_by_name['sketch'].message_type = _SRL_OBJECT
_SRL_OBJECT.fields_by_name['type'].enum_type = _SRL_OBJECT_OBJECTTYPE
_SRL_OBJECT_OBJECTTYPE.containing_type = _SRL_OBJECT;
_SRL_SHAPE.fields_by_name['interpretations'].message_type = _INTERPRETATION
_SRL_SHAPE.fields_by_name['subComponents'].message_type = _SRL_OBJECT
_SRL_STROKE.fields_by_name['points'].message_type = _SRL_POINT
DESCRIPTOR.message_types_by_name['SRL_Sketch'] = _SRL_SKETCH
DESCRIPTOR.message_types_by_name['SRL_Object'] = _SRL_OBJECT
DESCRIPTOR.message_types_by_name['SRL_Shape'] = _SRL_SHAPE
DESCRIPTOR.message_types_by_name['SRL_Stroke'] = _SRL_STROKE
DESCRIPTOR.message_types_by_name['SRL_Point'] = _SRL_POINT
DESCRIPTOR.message_types_by_name['Interpretation'] = _INTERPRETATION

class SRL_Sketch(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _SRL_SKETCH

  # @@protoc_insertion_point(class_scope:protobuf.srl.sketch.SRL_Sketch)

class SRL_Object(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _SRL_OBJECT

  # @@protoc_insertion_point(class_scope:protobuf.srl.sketch.SRL_Object)

class SRL_Shape(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _SRL_SHAPE

  # @@protoc_insertion_point(class_scope:protobuf.srl.sketch.SRL_Shape)

class SRL_Stroke(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _SRL_STROKE

  # @@protoc_insertion_point(class_scope:protobuf.srl.sketch.SRL_Stroke)

class SRL_Point(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _SRL_POINT

  # @@protoc_insertion_point(class_scope:protobuf.srl.sketch.SRL_Point)

class Interpretation(_message.Message):
  __metaclass__ = _reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _INTERPRETATION

  # @@protoc_insertion_point(class_scope:protobuf.srl.sketch.Interpretation)


# @@protoc_insertion_point(module_scope)
