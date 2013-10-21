// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: input/commands.proto

#ifndef PROTOBUF_input_2fcommands_2eproto__INCLUDED
#define PROTOBUF_input_2fcommands_2eproto__INCLUDED

#include <string>

#include <google/protobuf/stubs/common.h>

#if GOOGLE_PROTOBUF_VERSION < 2005000
#error This file was generated by a newer version of protoc which is
#error incompatible with your Protocol Buffer headers.  Please update
#error your headers.
#endif
#if 2005000 < GOOGLE_PROTOBUF_MIN_PROTOC_VERSION
#error This file was generated by an older version of protoc which is
#error incompatible with your Protocol Buffer headers.  Please
#error regenerate this file with a newer version of protoc.
#endif

#include <google/protobuf/generated_message_util.h>
#include <google/protobuf/message.h>
#include <google/protobuf/repeated_field.h>
#include <google/protobuf/extension_set.h>
#include <google/protobuf/unknown_field_set.h>
// @@protoc_insertion_point(includes)

namespace protobuf {
namespace srl {
namespace action {
namespace commands {

// Internal implementation detail -- do not call these.
void  protobuf_AddDesc_input_2fcommands_2eproto();
void protobuf_AssignDesc_input_2fcommands_2eproto();
void protobuf_ShutdownFile_input_2fcommands_2eproto();

class PackageShape;
class AddStroke;
class RemoveShape;
class AddShape;
class ForceInterpretation;

// ===================================================================

class PackageShape : public ::google::protobuf::Message {
 public:
  PackageShape();
  virtual ~PackageShape();

  PackageShape(const PackageShape& from);

  inline PackageShape& operator=(const PackageShape& from) {
    CopyFrom(from);
    return *this;
  }

  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }

  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }

  static const ::google::protobuf::Descriptor* descriptor();
  static const PackageShape& default_instance();

  void Swap(PackageShape* other);

  // implements Message ----------------------------------------------

  PackageShape* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const PackageShape& from);
  void MergeFrom(const PackageShape& from);
  void Clear();
  bool IsInitialized() const;

  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:

  ::google::protobuf::Metadata GetMetadata() const;

  // nested types ----------------------------------------------------

  // accessors -------------------------------------------------------

  // required string newContainerId = 1;
  inline bool has_newcontainerid() const;
  inline void clear_newcontainerid();
  static const int kNewContainerIdFieldNumber = 1;
  inline const ::std::string& newcontainerid() const;
  inline void set_newcontainerid(const ::std::string& value);
  inline void set_newcontainerid(const char* value);
  inline void set_newcontainerid(const char* value, size_t size);
  inline ::std::string* mutable_newcontainerid();
  inline ::std::string* release_newcontainerid();
  inline void set_allocated_newcontainerid(::std::string* newcontainerid);

  // repeated string shapesToBeContained = 2;
  inline int shapestobecontained_size() const;
  inline void clear_shapestobecontained();
  static const int kShapesToBeContainedFieldNumber = 2;
  inline const ::std::string& shapestobecontained(int index) const;
  inline ::std::string* mutable_shapestobecontained(int index);
  inline void set_shapestobecontained(int index, const ::std::string& value);
  inline void set_shapestobecontained(int index, const char* value);
  inline void set_shapestobecontained(int index, const char* value, size_t size);
  inline ::std::string* add_shapestobecontained();
  inline void add_shapestobecontained(const ::std::string& value);
  inline void add_shapestobecontained(const char* value);
  inline void add_shapestobecontained(const char* value, size_t size);
  inline const ::google::protobuf::RepeatedPtrField< ::std::string>& shapestobecontained() const;
  inline ::google::protobuf::RepeatedPtrField< ::std::string>* mutable_shapestobecontained();

  // @@protoc_insertion_point(class_scope:protobuf.srl.action.commands.PackageShape)
 private:
  inline void set_has_newcontainerid();
  inline void clear_has_newcontainerid();

  ::google::protobuf::UnknownFieldSet _unknown_fields_;

  ::std::string* newcontainerid_;
  ::google::protobuf::RepeatedPtrField< ::std::string> shapestobecontained_;

  mutable int _cached_size_;
  ::google::protobuf::uint32 _has_bits_[(2 + 31) / 32];

  friend void  protobuf_AddDesc_input_2fcommands_2eproto();
  friend void protobuf_AssignDesc_input_2fcommands_2eproto();
  friend void protobuf_ShutdownFile_input_2fcommands_2eproto();

  void InitAsDefaultInstance();
  static PackageShape* default_instance_;
};
// -------------------------------------------------------------------

class AddStroke : public ::google::protobuf::Message {
 public:
  AddStroke();
  virtual ~AddStroke();

  AddStroke(const AddStroke& from);

  inline AddStroke& operator=(const AddStroke& from) {
    CopyFrom(from);
    return *this;
  }

  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }

  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }

  static const ::google::protobuf::Descriptor* descriptor();
  static const AddStroke& default_instance();

  void Swap(AddStroke* other);

  // implements Message ----------------------------------------------

  AddStroke* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const AddStroke& from);
  void MergeFrom(const AddStroke& from);
  void Clear();
  bool IsInitialized() const;

  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:

  ::google::protobuf::Metadata GetMetadata() const;

  // nested types ----------------------------------------------------

  // accessors -------------------------------------------------------

  // required bytes stroke = 1;
  inline bool has_stroke() const;
  inline void clear_stroke();
  static const int kStrokeFieldNumber = 1;
  inline const ::std::string& stroke() const;
  inline void set_stroke(const ::std::string& value);
  inline void set_stroke(const char* value);
  inline void set_stroke(const void* value, size_t size);
  inline ::std::string* mutable_stroke();
  inline ::std::string* release_stroke();
  inline void set_allocated_stroke(::std::string* stroke);

  // @@protoc_insertion_point(class_scope:protobuf.srl.action.commands.AddStroke)
 private:
  inline void set_has_stroke();
  inline void clear_has_stroke();

  ::google::protobuf::UnknownFieldSet _unknown_fields_;

  ::std::string* stroke_;

  mutable int _cached_size_;
  ::google::protobuf::uint32 _has_bits_[(1 + 31) / 32];

  friend void  protobuf_AddDesc_input_2fcommands_2eproto();
  friend void protobuf_AssignDesc_input_2fcommands_2eproto();
  friend void protobuf_ShutdownFile_input_2fcommands_2eproto();

  void InitAsDefaultInstance();
  static AddStroke* default_instance_;
};
// -------------------------------------------------------------------

class RemoveShape : public ::google::protobuf::Message {
 public:
  RemoveShape();
  virtual ~RemoveShape();

  RemoveShape(const RemoveShape& from);

  inline RemoveShape& operator=(const RemoveShape& from) {
    CopyFrom(from);
    return *this;
  }

  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }

  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }

  static const ::google::protobuf::Descriptor* descriptor();
  static const RemoveShape& default_instance();

  void Swap(RemoveShape* other);

  // implements Message ----------------------------------------------

  RemoveShape* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const RemoveShape& from);
  void MergeFrom(const RemoveShape& from);
  void Clear();
  bool IsInitialized() const;

  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:

  ::google::protobuf::Metadata GetMetadata() const;

  // nested types ----------------------------------------------------

  // accessors -------------------------------------------------------

  // required string shapeToRemoveId = 1;
  inline bool has_shapetoremoveid() const;
  inline void clear_shapetoremoveid();
  static const int kShapeToRemoveIdFieldNumber = 1;
  inline const ::std::string& shapetoremoveid() const;
  inline void set_shapetoremoveid(const ::std::string& value);
  inline void set_shapetoremoveid(const char* value);
  inline void set_shapetoremoveid(const char* value, size_t size);
  inline ::std::string* mutable_shapetoremoveid();
  inline ::std::string* release_shapetoremoveid();
  inline void set_allocated_shapetoremoveid(::std::string* shapetoremoveid);

  // @@protoc_insertion_point(class_scope:protobuf.srl.action.commands.RemoveShape)
 private:
  inline void set_has_shapetoremoveid();
  inline void clear_has_shapetoremoveid();

  ::google::protobuf::UnknownFieldSet _unknown_fields_;

  ::std::string* shapetoremoveid_;

  mutable int _cached_size_;
  ::google::protobuf::uint32 _has_bits_[(1 + 31) / 32];

  friend void  protobuf_AddDesc_input_2fcommands_2eproto();
  friend void protobuf_AssignDesc_input_2fcommands_2eproto();
  friend void protobuf_ShutdownFile_input_2fcommands_2eproto();

  void InitAsDefaultInstance();
  static RemoveShape* default_instance_;
};
// -------------------------------------------------------------------

class AddShape : public ::google::protobuf::Message {
 public:
  AddShape();
  virtual ~AddShape();

  AddShape(const AddShape& from);

  inline AddShape& operator=(const AddShape& from) {
    CopyFrom(from);
    return *this;
  }

  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }

  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }

  static const ::google::protobuf::Descriptor* descriptor();
  static const AddShape& default_instance();

  void Swap(AddShape* other);

  // implements Message ----------------------------------------------

  AddShape* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const AddShape& from);
  void MergeFrom(const AddShape& from);
  void Clear();
  bool IsInitialized() const;

  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:

  ::google::protobuf::Metadata GetMetadata() const;

  // nested types ----------------------------------------------------

  // accessors -------------------------------------------------------

  // required bytes shape = 1;
  inline bool has_shape() const;
  inline void clear_shape();
  static const int kShapeFieldNumber = 1;
  inline const ::std::string& shape() const;
  inline void set_shape(const ::std::string& value);
  inline void set_shape(const char* value);
  inline void set_shape(const void* value, size_t size);
  inline ::std::string* mutable_shape();
  inline ::std::string* release_shape();
  inline void set_allocated_shape(::std::string* shape);

  // @@protoc_insertion_point(class_scope:protobuf.srl.action.commands.AddShape)
 private:
  inline void set_has_shape();
  inline void clear_has_shape();

  ::google::protobuf::UnknownFieldSet _unknown_fields_;

  ::std::string* shape_;

  mutable int _cached_size_;
  ::google::protobuf::uint32 _has_bits_[(1 + 31) / 32];

  friend void  protobuf_AddDesc_input_2fcommands_2eproto();
  friend void protobuf_AssignDesc_input_2fcommands_2eproto();
  friend void protobuf_ShutdownFile_input_2fcommands_2eproto();

  void InitAsDefaultInstance();
  static AddShape* default_instance_;
};
// -------------------------------------------------------------------

class ForceInterpretation : public ::google::protobuf::Message {
 public:
  ForceInterpretation();
  virtual ~ForceInterpretation();

  ForceInterpretation(const ForceInterpretation& from);

  inline ForceInterpretation& operator=(const ForceInterpretation& from) {
    CopyFrom(from);
    return *this;
  }

  inline const ::google::protobuf::UnknownFieldSet& unknown_fields() const {
    return _unknown_fields_;
  }

  inline ::google::protobuf::UnknownFieldSet* mutable_unknown_fields() {
    return &_unknown_fields_;
  }

  static const ::google::protobuf::Descriptor* descriptor();
  static const ForceInterpretation& default_instance();

  void Swap(ForceInterpretation* other);

  // implements Message ----------------------------------------------

  ForceInterpretation* New() const;
  void CopyFrom(const ::google::protobuf::Message& from);
  void MergeFrom(const ::google::protobuf::Message& from);
  void CopyFrom(const ForceInterpretation& from);
  void MergeFrom(const ForceInterpretation& from);
  void Clear();
  bool IsInitialized() const;

  int ByteSize() const;
  bool MergePartialFromCodedStream(
      ::google::protobuf::io::CodedInputStream* input);
  void SerializeWithCachedSizes(
      ::google::protobuf::io::CodedOutputStream* output) const;
  ::google::protobuf::uint8* SerializeWithCachedSizesToArray(::google::protobuf::uint8* output) const;
  int GetCachedSize() const { return _cached_size_; }
  private:
  void SharedCtor();
  void SharedDtor();
  void SetCachedSize(int size) const;
  public:

  ::google::protobuf::Metadata GetMetadata() const;

  // nested types ----------------------------------------------------

  // accessors -------------------------------------------------------

  // required bytes interpretation = 1;
  inline bool has_interpretation() const;
  inline void clear_interpretation();
  static const int kInterpretationFieldNumber = 1;
  inline const ::std::string& interpretation() const;
  inline void set_interpretation(const ::std::string& value);
  inline void set_interpretation(const char* value);
  inline void set_interpretation(const void* value, size_t size);
  inline ::std::string* mutable_interpretation();
  inline ::std::string* release_interpretation();
  inline void set_allocated_interpretation(::std::string* interpretation);

  // required string shapeId = 2;
  inline bool has_shapeid() const;
  inline void clear_shapeid();
  static const int kShapeIdFieldNumber = 2;
  inline const ::std::string& shapeid() const;
  inline void set_shapeid(const ::std::string& value);
  inline void set_shapeid(const char* value);
  inline void set_shapeid(const char* value, size_t size);
  inline ::std::string* mutable_shapeid();
  inline ::std::string* release_shapeid();
  inline void set_allocated_shapeid(::std::string* shapeid);

  // @@protoc_insertion_point(class_scope:protobuf.srl.action.commands.ForceInterpretation)
 private:
  inline void set_has_interpretation();
  inline void clear_has_interpretation();
  inline void set_has_shapeid();
  inline void clear_has_shapeid();

  ::google::protobuf::UnknownFieldSet _unknown_fields_;

  ::std::string* interpretation_;
  ::std::string* shapeid_;

  mutable int _cached_size_;
  ::google::protobuf::uint32 _has_bits_[(2 + 31) / 32];

  friend void  protobuf_AddDesc_input_2fcommands_2eproto();
  friend void protobuf_AssignDesc_input_2fcommands_2eproto();
  friend void protobuf_ShutdownFile_input_2fcommands_2eproto();

  void InitAsDefaultInstance();
  static ForceInterpretation* default_instance_;
};
// ===================================================================


// ===================================================================

// PackageShape

// required string newContainerId = 1;
inline bool PackageShape::has_newcontainerid() const {
  return (_has_bits_[0] & 0x00000001u) != 0;
}
inline void PackageShape::set_has_newcontainerid() {
  _has_bits_[0] |= 0x00000001u;
}
inline void PackageShape::clear_has_newcontainerid() {
  _has_bits_[0] &= ~0x00000001u;
}
inline void PackageShape::clear_newcontainerid() {
  if (newcontainerid_ != &::google::protobuf::internal::kEmptyString) {
    newcontainerid_->clear();
  }
  clear_has_newcontainerid();
}
inline const ::std::string& PackageShape::newcontainerid() const {
  return *newcontainerid_;
}
inline void PackageShape::set_newcontainerid(const ::std::string& value) {
  set_has_newcontainerid();
  if (newcontainerid_ == &::google::protobuf::internal::kEmptyString) {
    newcontainerid_ = new ::std::string;
  }
  newcontainerid_->assign(value);
}
inline void PackageShape::set_newcontainerid(const char* value) {
  set_has_newcontainerid();
  if (newcontainerid_ == &::google::protobuf::internal::kEmptyString) {
    newcontainerid_ = new ::std::string;
  }
  newcontainerid_->assign(value);
}
inline void PackageShape::set_newcontainerid(const char* value, size_t size) {
  set_has_newcontainerid();
  if (newcontainerid_ == &::google::protobuf::internal::kEmptyString) {
    newcontainerid_ = new ::std::string;
  }
  newcontainerid_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* PackageShape::mutable_newcontainerid() {
  set_has_newcontainerid();
  if (newcontainerid_ == &::google::protobuf::internal::kEmptyString) {
    newcontainerid_ = new ::std::string;
  }
  return newcontainerid_;
}
inline ::std::string* PackageShape::release_newcontainerid() {
  clear_has_newcontainerid();
  if (newcontainerid_ == &::google::protobuf::internal::kEmptyString) {
    return NULL;
  } else {
    ::std::string* temp = newcontainerid_;
    newcontainerid_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
    return temp;
  }
}
inline void PackageShape::set_allocated_newcontainerid(::std::string* newcontainerid) {
  if (newcontainerid_ != &::google::protobuf::internal::kEmptyString) {
    delete newcontainerid_;
  }
  if (newcontainerid) {
    set_has_newcontainerid();
    newcontainerid_ = newcontainerid;
  } else {
    clear_has_newcontainerid();
    newcontainerid_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  }
}

// repeated string shapesToBeContained = 2;
inline int PackageShape::shapestobecontained_size() const {
  return shapestobecontained_.size();
}
inline void PackageShape::clear_shapestobecontained() {
  shapestobecontained_.Clear();
}
inline const ::std::string& PackageShape::shapestobecontained(int index) const {
  return shapestobecontained_.Get(index);
}
inline ::std::string* PackageShape::mutable_shapestobecontained(int index) {
  return shapestobecontained_.Mutable(index);
}
inline void PackageShape::set_shapestobecontained(int index, const ::std::string& value) {
  shapestobecontained_.Mutable(index)->assign(value);
}
inline void PackageShape::set_shapestobecontained(int index, const char* value) {
  shapestobecontained_.Mutable(index)->assign(value);
}
inline void PackageShape::set_shapestobecontained(int index, const char* value, size_t size) {
  shapestobecontained_.Mutable(index)->assign(
    reinterpret_cast<const char*>(value), size);
}
inline ::std::string* PackageShape::add_shapestobecontained() {
  return shapestobecontained_.Add();
}
inline void PackageShape::add_shapestobecontained(const ::std::string& value) {
  shapestobecontained_.Add()->assign(value);
}
inline void PackageShape::add_shapestobecontained(const char* value) {
  shapestobecontained_.Add()->assign(value);
}
inline void PackageShape::add_shapestobecontained(const char* value, size_t size) {
  shapestobecontained_.Add()->assign(reinterpret_cast<const char*>(value), size);
}
inline const ::google::protobuf::RepeatedPtrField< ::std::string>&
PackageShape::shapestobecontained() const {
  return shapestobecontained_;
}
inline ::google::protobuf::RepeatedPtrField< ::std::string>*
PackageShape::mutable_shapestobecontained() {
  return &shapestobecontained_;
}

// -------------------------------------------------------------------

// AddStroke

// required bytes stroke = 1;
inline bool AddStroke::has_stroke() const {
  return (_has_bits_[0] & 0x00000001u) != 0;
}
inline void AddStroke::set_has_stroke() {
  _has_bits_[0] |= 0x00000001u;
}
inline void AddStroke::clear_has_stroke() {
  _has_bits_[0] &= ~0x00000001u;
}
inline void AddStroke::clear_stroke() {
  if (stroke_ != &::google::protobuf::internal::kEmptyString) {
    stroke_->clear();
  }
  clear_has_stroke();
}
inline const ::std::string& AddStroke::stroke() const {
  return *stroke_;
}
inline void AddStroke::set_stroke(const ::std::string& value) {
  set_has_stroke();
  if (stroke_ == &::google::protobuf::internal::kEmptyString) {
    stroke_ = new ::std::string;
  }
  stroke_->assign(value);
}
inline void AddStroke::set_stroke(const char* value) {
  set_has_stroke();
  if (stroke_ == &::google::protobuf::internal::kEmptyString) {
    stroke_ = new ::std::string;
  }
  stroke_->assign(value);
}
inline void AddStroke::set_stroke(const void* value, size_t size) {
  set_has_stroke();
  if (stroke_ == &::google::protobuf::internal::kEmptyString) {
    stroke_ = new ::std::string;
  }
  stroke_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* AddStroke::mutable_stroke() {
  set_has_stroke();
  if (stroke_ == &::google::protobuf::internal::kEmptyString) {
    stroke_ = new ::std::string;
  }
  return stroke_;
}
inline ::std::string* AddStroke::release_stroke() {
  clear_has_stroke();
  if (stroke_ == &::google::protobuf::internal::kEmptyString) {
    return NULL;
  } else {
    ::std::string* temp = stroke_;
    stroke_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
    return temp;
  }
}
inline void AddStroke::set_allocated_stroke(::std::string* stroke) {
  if (stroke_ != &::google::protobuf::internal::kEmptyString) {
    delete stroke_;
  }
  if (stroke) {
    set_has_stroke();
    stroke_ = stroke;
  } else {
    clear_has_stroke();
    stroke_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  }
}

// -------------------------------------------------------------------

// RemoveShape

// required string shapeToRemoveId = 1;
inline bool RemoveShape::has_shapetoremoveid() const {
  return (_has_bits_[0] & 0x00000001u) != 0;
}
inline void RemoveShape::set_has_shapetoremoveid() {
  _has_bits_[0] |= 0x00000001u;
}
inline void RemoveShape::clear_has_shapetoremoveid() {
  _has_bits_[0] &= ~0x00000001u;
}
inline void RemoveShape::clear_shapetoremoveid() {
  if (shapetoremoveid_ != &::google::protobuf::internal::kEmptyString) {
    shapetoremoveid_->clear();
  }
  clear_has_shapetoremoveid();
}
inline const ::std::string& RemoveShape::shapetoremoveid() const {
  return *shapetoremoveid_;
}
inline void RemoveShape::set_shapetoremoveid(const ::std::string& value) {
  set_has_shapetoremoveid();
  if (shapetoremoveid_ == &::google::protobuf::internal::kEmptyString) {
    shapetoremoveid_ = new ::std::string;
  }
  shapetoremoveid_->assign(value);
}
inline void RemoveShape::set_shapetoremoveid(const char* value) {
  set_has_shapetoremoveid();
  if (shapetoremoveid_ == &::google::protobuf::internal::kEmptyString) {
    shapetoremoveid_ = new ::std::string;
  }
  shapetoremoveid_->assign(value);
}
inline void RemoveShape::set_shapetoremoveid(const char* value, size_t size) {
  set_has_shapetoremoveid();
  if (shapetoremoveid_ == &::google::protobuf::internal::kEmptyString) {
    shapetoremoveid_ = new ::std::string;
  }
  shapetoremoveid_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* RemoveShape::mutable_shapetoremoveid() {
  set_has_shapetoremoveid();
  if (shapetoremoveid_ == &::google::protobuf::internal::kEmptyString) {
    shapetoremoveid_ = new ::std::string;
  }
  return shapetoremoveid_;
}
inline ::std::string* RemoveShape::release_shapetoremoveid() {
  clear_has_shapetoremoveid();
  if (shapetoremoveid_ == &::google::protobuf::internal::kEmptyString) {
    return NULL;
  } else {
    ::std::string* temp = shapetoremoveid_;
    shapetoremoveid_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
    return temp;
  }
}
inline void RemoveShape::set_allocated_shapetoremoveid(::std::string* shapetoremoveid) {
  if (shapetoremoveid_ != &::google::protobuf::internal::kEmptyString) {
    delete shapetoremoveid_;
  }
  if (shapetoremoveid) {
    set_has_shapetoremoveid();
    shapetoremoveid_ = shapetoremoveid;
  } else {
    clear_has_shapetoremoveid();
    shapetoremoveid_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  }
}

// -------------------------------------------------------------------

// AddShape

// required bytes shape = 1;
inline bool AddShape::has_shape() const {
  return (_has_bits_[0] & 0x00000001u) != 0;
}
inline void AddShape::set_has_shape() {
  _has_bits_[0] |= 0x00000001u;
}
inline void AddShape::clear_has_shape() {
  _has_bits_[0] &= ~0x00000001u;
}
inline void AddShape::clear_shape() {
  if (shape_ != &::google::protobuf::internal::kEmptyString) {
    shape_->clear();
  }
  clear_has_shape();
}
inline const ::std::string& AddShape::shape() const {
  return *shape_;
}
inline void AddShape::set_shape(const ::std::string& value) {
  set_has_shape();
  if (shape_ == &::google::protobuf::internal::kEmptyString) {
    shape_ = new ::std::string;
  }
  shape_->assign(value);
}
inline void AddShape::set_shape(const char* value) {
  set_has_shape();
  if (shape_ == &::google::protobuf::internal::kEmptyString) {
    shape_ = new ::std::string;
  }
  shape_->assign(value);
}
inline void AddShape::set_shape(const void* value, size_t size) {
  set_has_shape();
  if (shape_ == &::google::protobuf::internal::kEmptyString) {
    shape_ = new ::std::string;
  }
  shape_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* AddShape::mutable_shape() {
  set_has_shape();
  if (shape_ == &::google::protobuf::internal::kEmptyString) {
    shape_ = new ::std::string;
  }
  return shape_;
}
inline ::std::string* AddShape::release_shape() {
  clear_has_shape();
  if (shape_ == &::google::protobuf::internal::kEmptyString) {
    return NULL;
  } else {
    ::std::string* temp = shape_;
    shape_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
    return temp;
  }
}
inline void AddShape::set_allocated_shape(::std::string* shape) {
  if (shape_ != &::google::protobuf::internal::kEmptyString) {
    delete shape_;
  }
  if (shape) {
    set_has_shape();
    shape_ = shape;
  } else {
    clear_has_shape();
    shape_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  }
}

// -------------------------------------------------------------------

// ForceInterpretation

// required bytes interpretation = 1;
inline bool ForceInterpretation::has_interpretation() const {
  return (_has_bits_[0] & 0x00000001u) != 0;
}
inline void ForceInterpretation::set_has_interpretation() {
  _has_bits_[0] |= 0x00000001u;
}
inline void ForceInterpretation::clear_has_interpretation() {
  _has_bits_[0] &= ~0x00000001u;
}
inline void ForceInterpretation::clear_interpretation() {
  if (interpretation_ != &::google::protobuf::internal::kEmptyString) {
    interpretation_->clear();
  }
  clear_has_interpretation();
}
inline const ::std::string& ForceInterpretation::interpretation() const {
  return *interpretation_;
}
inline void ForceInterpretation::set_interpretation(const ::std::string& value) {
  set_has_interpretation();
  if (interpretation_ == &::google::protobuf::internal::kEmptyString) {
    interpretation_ = new ::std::string;
  }
  interpretation_->assign(value);
}
inline void ForceInterpretation::set_interpretation(const char* value) {
  set_has_interpretation();
  if (interpretation_ == &::google::protobuf::internal::kEmptyString) {
    interpretation_ = new ::std::string;
  }
  interpretation_->assign(value);
}
inline void ForceInterpretation::set_interpretation(const void* value, size_t size) {
  set_has_interpretation();
  if (interpretation_ == &::google::protobuf::internal::kEmptyString) {
    interpretation_ = new ::std::string;
  }
  interpretation_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* ForceInterpretation::mutable_interpretation() {
  set_has_interpretation();
  if (interpretation_ == &::google::protobuf::internal::kEmptyString) {
    interpretation_ = new ::std::string;
  }
  return interpretation_;
}
inline ::std::string* ForceInterpretation::release_interpretation() {
  clear_has_interpretation();
  if (interpretation_ == &::google::protobuf::internal::kEmptyString) {
    return NULL;
  } else {
    ::std::string* temp = interpretation_;
    interpretation_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
    return temp;
  }
}
inline void ForceInterpretation::set_allocated_interpretation(::std::string* interpretation) {
  if (interpretation_ != &::google::protobuf::internal::kEmptyString) {
    delete interpretation_;
  }
  if (interpretation) {
    set_has_interpretation();
    interpretation_ = interpretation;
  } else {
    clear_has_interpretation();
    interpretation_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  }
}

// required string shapeId = 2;
inline bool ForceInterpretation::has_shapeid() const {
  return (_has_bits_[0] & 0x00000002u) != 0;
}
inline void ForceInterpretation::set_has_shapeid() {
  _has_bits_[0] |= 0x00000002u;
}
inline void ForceInterpretation::clear_has_shapeid() {
  _has_bits_[0] &= ~0x00000002u;
}
inline void ForceInterpretation::clear_shapeid() {
  if (shapeid_ != &::google::protobuf::internal::kEmptyString) {
    shapeid_->clear();
  }
  clear_has_shapeid();
}
inline const ::std::string& ForceInterpretation::shapeid() const {
  return *shapeid_;
}
inline void ForceInterpretation::set_shapeid(const ::std::string& value) {
  set_has_shapeid();
  if (shapeid_ == &::google::protobuf::internal::kEmptyString) {
    shapeid_ = new ::std::string;
  }
  shapeid_->assign(value);
}
inline void ForceInterpretation::set_shapeid(const char* value) {
  set_has_shapeid();
  if (shapeid_ == &::google::protobuf::internal::kEmptyString) {
    shapeid_ = new ::std::string;
  }
  shapeid_->assign(value);
}
inline void ForceInterpretation::set_shapeid(const char* value, size_t size) {
  set_has_shapeid();
  if (shapeid_ == &::google::protobuf::internal::kEmptyString) {
    shapeid_ = new ::std::string;
  }
  shapeid_->assign(reinterpret_cast<const char*>(value), size);
}
inline ::std::string* ForceInterpretation::mutable_shapeid() {
  set_has_shapeid();
  if (shapeid_ == &::google::protobuf::internal::kEmptyString) {
    shapeid_ = new ::std::string;
  }
  return shapeid_;
}
inline ::std::string* ForceInterpretation::release_shapeid() {
  clear_has_shapeid();
  if (shapeid_ == &::google::protobuf::internal::kEmptyString) {
    return NULL;
  } else {
    ::std::string* temp = shapeid_;
    shapeid_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
    return temp;
  }
}
inline void ForceInterpretation::set_allocated_shapeid(::std::string* shapeid) {
  if (shapeid_ != &::google::protobuf::internal::kEmptyString) {
    delete shapeid_;
  }
  if (shapeid) {
    set_has_shapeid();
    shapeid_ = shapeid;
  } else {
    clear_has_shapeid();
    shapeid_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  }
}


// @@protoc_insertion_point(namespace_scope)

}  // namespace commands
}  // namespace action
}  // namespace srl
}  // namespace protobuf

#ifndef SWIG
namespace google {
namespace protobuf {


}  // namespace google
}  // namespace protobuf
#endif  // SWIG

// @@protoc_insertion_point(global_scope)

#endif  // PROTOBUF_input_2fcommands_2eproto__INCLUDED
