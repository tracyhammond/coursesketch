// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: input/message.proto

#define INTERNAL_SUPPRESS_PROTOBUF_FIELD_DEPRECATION
#include "input/message.pb.h"

#include <algorithm>

#include <google/protobuf/stubs/common.h>
#include <google/protobuf/stubs/once.h>
#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/wire_format_lite_inl.h>
#include <google/protobuf/descriptor.h>
#include <google/protobuf/generated_message_reflection.h>
#include <google/protobuf/reflection_ops.h>
#include <google/protobuf/wire_format.h>
// @@protoc_insertion_point(includes)

namespace protobuf {
namespace srl {
namespace request {

namespace {

const ::google::protobuf::Descriptor* Request_descriptor_ = NULL;
const ::google::protobuf::internal::GeneratedMessageReflection*
  Request_reflection_ = NULL;
const ::google::protobuf::EnumDescriptor* Request_MessageType_descriptor_ = NULL;
const ::google::protobuf::Descriptor* LoginInformation_descriptor_ = NULL;
const ::google::protobuf::internal::GeneratedMessageReflection*
  LoginInformation_reflection_ = NULL;

}  // namespace


void protobuf_AssignDesc_input_2fmessage_2eproto() {
  protobuf_AddDesc_input_2fmessage_2eproto();
  const ::google::protobuf::FileDescriptor* file =
    ::google::protobuf::DescriptorPool::generated_pool()->FindFileByName(
      "input/message.proto");
  GOOGLE_CHECK(file != NULL);
  Request_descriptor_ = file->message_type(0);
  static const int Request_offsets_[5] = {
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(Request, requesttype_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(Request, login_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(Request, otherdata_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(Request, responsetext_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(Request, sessioninfo_),
  };
  Request_reflection_ =
    new ::google::protobuf::internal::GeneratedMessageReflection(
      Request_descriptor_,
      Request::default_instance_,
      Request_offsets_,
      GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(Request, _has_bits_[0]),
      GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(Request, _unknown_fields_),
      -1,
      ::google::protobuf::DescriptorPool::generated_pool(),
      ::google::protobuf::MessageFactory::generated_factory(),
      sizeof(Request));
  Request_MessageType_descriptor_ = Request_descriptor_->enum_type(0);
  LoginInformation_descriptor_ = file->message_type(1);
  static const int LoginInformation_offsets_[5] = {
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(LoginInformation, username_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(LoginInformation, password_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(LoginInformation, isloggedin_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(LoginInformation, isinstructor_),
    GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(LoginInformation, isregistering_),
  };
  LoginInformation_reflection_ =
    new ::google::protobuf::internal::GeneratedMessageReflection(
      LoginInformation_descriptor_,
      LoginInformation::default_instance_,
      LoginInformation_offsets_,
      GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(LoginInformation, _has_bits_[0]),
      GOOGLE_PROTOBUF_GENERATED_MESSAGE_FIELD_OFFSET(LoginInformation, _unknown_fields_),
      -1,
      ::google::protobuf::DescriptorPool::generated_pool(),
      ::google::protobuf::MessageFactory::generated_factory(),
      sizeof(LoginInformation));
}

namespace {

GOOGLE_PROTOBUF_DECLARE_ONCE(protobuf_AssignDescriptors_once_);
inline void protobuf_AssignDescriptorsOnce() {
  ::google::protobuf::GoogleOnceInit(&protobuf_AssignDescriptors_once_,
                 &protobuf_AssignDesc_input_2fmessage_2eproto);
}

void protobuf_RegisterTypes(const ::std::string&) {
  protobuf_AssignDescriptorsOnce();
  ::google::protobuf::MessageFactory::InternalRegisterGeneratedMessage(
    Request_descriptor_, &Request::default_instance());
  ::google::protobuf::MessageFactory::InternalRegisterGeneratedMessage(
    LoginInformation_descriptor_, &LoginInformation::default_instance());
}

}  // namespace

void protobuf_ShutdownFile_input_2fmessage_2eproto() {
  delete Request::default_instance_;
  delete Request_reflection_;
  delete LoginInformation::default_instance_;
  delete LoginInformation_reflection_;
}

void protobuf_AddDesc_input_2fmessage_2eproto() {
  static bool already_here = false;
  if (already_here) return;
  already_here = true;
  GOOGLE_PROTOBUF_VERIFY_VERSION;

  ::google::protobuf::DescriptorPool::InternalAddGeneratedFile(
    "\n\023input/message.proto\022\024protobuf.srl.requ"
    "est\"\276\002\n\007Request\022E\n\013requestType\030\001 \002(\0162).p"
    "rotobuf.srl.request.Request.MessageType:"
    "\005LOGIN\0225\n\005login\030\002 \001(\0132&.protobuf.srl.req"
    "uest.LoginInformation\022\021\n\totherData\030\003 \001(\014"
    "\022\024\n\014responseText\030\004 \001(\t\022\023\n\013sessionInfo\030\005 "
    "\001(\t\"w\n\013MessageType\022\t\n\005LOGIN\020\000\022\020\n\014DATA_RE"
    "QUEST\020\001\022\020\n\014DATA_SENDING\020\002\022\017\n\013RECOGNITION"
    "\020\003\022\013\n\007LOADING\020\004\022\016\n\nSUBMISSION\020\005\022\013\n\007PENDI"
    "NG\020\006\"w\n\020LoginInformation\022\020\n\010username\030\001 \002"
    "(\t\022\020\n\010password\030\002 \001(\t\022\022\n\nisLoggedIn\030\003 \001(\010"
    "\022\024\n\014isInstructor\030\004 \001(\010\022\025\n\risRegistering\030"
    "\005 \001(\010", 485);
  ::google::protobuf::MessageFactory::InternalRegisterGeneratedFile(
    "input/message.proto", &protobuf_RegisterTypes);
  Request::default_instance_ = new Request();
  LoginInformation::default_instance_ = new LoginInformation();
  Request::default_instance_->InitAsDefaultInstance();
  LoginInformation::default_instance_->InitAsDefaultInstance();
  ::google::protobuf::internal::OnShutdown(&protobuf_ShutdownFile_input_2fmessage_2eproto);
}

// Force AddDescriptors() to be called at static initialization time.
struct StaticDescriptorInitializer_input_2fmessage_2eproto {
  StaticDescriptorInitializer_input_2fmessage_2eproto() {
    protobuf_AddDesc_input_2fmessage_2eproto();
  }
} static_descriptor_initializer_input_2fmessage_2eproto_;

// ===================================================================

const ::google::protobuf::EnumDescriptor* Request_MessageType_descriptor() {
  protobuf_AssignDescriptorsOnce();
  return Request_MessageType_descriptor_;
}
bool Request_MessageType_IsValid(int value) {
  switch(value) {
    case 0:
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
      return true;
    default:
      return false;
  }
}

#ifndef _MSC_VER
const Request_MessageType Request::LOGIN;
const Request_MessageType Request::DATA_REQUEST;
const Request_MessageType Request::DATA_SENDING;
const Request_MessageType Request::RECOGNITION;
const Request_MessageType Request::LOADING;
const Request_MessageType Request::SUBMISSION;
const Request_MessageType Request::PENDING;
const Request_MessageType Request::MessageType_MIN;
const Request_MessageType Request::MessageType_MAX;
const int Request::MessageType_ARRAYSIZE;
#endif  // _MSC_VER
#ifndef _MSC_VER
const int Request::kRequestTypeFieldNumber;
const int Request::kLoginFieldNumber;
const int Request::kOtherDataFieldNumber;
const int Request::kResponseTextFieldNumber;
const int Request::kSessionInfoFieldNumber;
#endif  // !_MSC_VER

Request::Request()
  : ::google::protobuf::Message() {
  SharedCtor();
}

void Request::InitAsDefaultInstance() {
  login_ = const_cast< ::protobuf::srl::request::LoginInformation*>(&::protobuf::srl::request::LoginInformation::default_instance());
}

Request::Request(const Request& from)
  : ::google::protobuf::Message() {
  SharedCtor();
  MergeFrom(from);
}

void Request::SharedCtor() {
  _cached_size_ = 0;
  requesttype_ = 0;
  login_ = NULL;
  otherdata_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  responsetext_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  sessioninfo_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

Request::~Request() {
  SharedDtor();
}

void Request::SharedDtor() {
  if (otherdata_ != &::google::protobuf::internal::kEmptyString) {
    delete otherdata_;
  }
  if (responsetext_ != &::google::protobuf::internal::kEmptyString) {
    delete responsetext_;
  }
  if (sessioninfo_ != &::google::protobuf::internal::kEmptyString) {
    delete sessioninfo_;
  }
  if (this != default_instance_) {
    delete login_;
  }
}

void Request::SetCachedSize(int size) const {
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
}
const ::google::protobuf::Descriptor* Request::descriptor() {
  protobuf_AssignDescriptorsOnce();
  return Request_descriptor_;
}

const Request& Request::default_instance() {
  if (default_instance_ == NULL) protobuf_AddDesc_input_2fmessage_2eproto();
  return *default_instance_;
}

Request* Request::default_instance_ = NULL;

Request* Request::New() const {
  return new Request;
}

void Request::Clear() {
  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    requesttype_ = 0;
    if (has_login()) {
      if (login_ != NULL) login_->::protobuf::srl::request::LoginInformation::Clear();
    }
    if (has_otherdata()) {
      if (otherdata_ != &::google::protobuf::internal::kEmptyString) {
        otherdata_->clear();
      }
    }
    if (has_responsetext()) {
      if (responsetext_ != &::google::protobuf::internal::kEmptyString) {
        responsetext_->clear();
      }
    }
    if (has_sessioninfo()) {
      if (sessioninfo_ != &::google::protobuf::internal::kEmptyString) {
        sessioninfo_->clear();
      }
    }
  }
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
  mutable_unknown_fields()->Clear();
}

bool Request::MergePartialFromCodedStream(
    ::google::protobuf::io::CodedInputStream* input) {
#define DO_(EXPRESSION) if (!(EXPRESSION)) return false
  ::google::protobuf::uint32 tag;
  while ((tag = input->ReadTag()) != 0) {
    switch (::google::protobuf::internal::WireFormatLite::GetTagFieldNumber(tag)) {
      // required .protobuf.srl.request.Request.MessageType requestType = 1 [default = LOGIN];
      case 1: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_VARINT) {
          int value;
          DO_((::google::protobuf::internal::WireFormatLite::ReadPrimitive<
                   int, ::google::protobuf::internal::WireFormatLite::TYPE_ENUM>(
                 input, &value)));
          if (::protobuf::srl::request::Request_MessageType_IsValid(value)) {
            set_requesttype(static_cast< ::protobuf::srl::request::Request_MessageType >(value));
          } else {
            mutable_unknown_fields()->AddVarint(1, value);
          }
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(18)) goto parse_login;
        break;
      }

      // optional .protobuf.srl.request.LoginInformation login = 2;
      case 2: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_login:
          DO_(::google::protobuf::internal::WireFormatLite::ReadMessageNoVirtual(
               input, mutable_login()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(26)) goto parse_otherData;
        break;
      }

      // optional bytes otherData = 3;
      case 3: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_otherData:
          DO_(::google::protobuf::internal::WireFormatLite::ReadBytes(
                input, this->mutable_otherdata()));
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(34)) goto parse_responseText;
        break;
      }

      // optional string responseText = 4;
      case 4: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_responseText:
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_responsetext()));
          ::google::protobuf::internal::WireFormat::VerifyUTF8String(
            this->responsetext().data(), this->responsetext().length(),
            ::google::protobuf::internal::WireFormat::PARSE);
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(42)) goto parse_sessionInfo;
        break;
      }

      // optional string sessionInfo = 5;
      case 5: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_sessionInfo:
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_sessioninfo()));
          ::google::protobuf::internal::WireFormat::VerifyUTF8String(
            this->sessioninfo().data(), this->sessioninfo().length(),
            ::google::protobuf::internal::WireFormat::PARSE);
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectAtEnd()) return true;
        break;
      }

      default: {
      handle_uninterpreted:
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_END_GROUP) {
          return true;
        }
        DO_(::google::protobuf::internal::WireFormat::SkipField(
              input, tag, mutable_unknown_fields()));
        break;
      }
    }
  }
  return true;
#undef DO_
}

void Request::SerializeWithCachedSizes(
    ::google::protobuf::io::CodedOutputStream* output) const {
  // required .protobuf.srl.request.Request.MessageType requestType = 1 [default = LOGIN];
  if (has_requesttype()) {
    ::google::protobuf::internal::WireFormatLite::WriteEnum(
      1, this->requesttype(), output);
  }

  // optional .protobuf.srl.request.LoginInformation login = 2;
  if (has_login()) {
    ::google::protobuf::internal::WireFormatLite::WriteMessageMaybeToArray(
      2, this->login(), output);
  }

  // optional bytes otherData = 3;
  if (has_otherdata()) {
    ::google::protobuf::internal::WireFormatLite::WriteBytes(
      3, this->otherdata(), output);
  }

  // optional string responseText = 4;
  if (has_responsetext()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->responsetext().data(), this->responsetext().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    ::google::protobuf::internal::WireFormatLite::WriteString(
      4, this->responsetext(), output);
  }

  // optional string sessionInfo = 5;
  if (has_sessioninfo()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->sessioninfo().data(), this->sessioninfo().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    ::google::protobuf::internal::WireFormatLite::WriteString(
      5, this->sessioninfo(), output);
  }

  if (!unknown_fields().empty()) {
    ::google::protobuf::internal::WireFormat::SerializeUnknownFields(
        unknown_fields(), output);
  }
}

::google::protobuf::uint8* Request::SerializeWithCachedSizesToArray(
    ::google::protobuf::uint8* target) const {
  // required .protobuf.srl.request.Request.MessageType requestType = 1 [default = LOGIN];
  if (has_requesttype()) {
    target = ::google::protobuf::internal::WireFormatLite::WriteEnumToArray(
      1, this->requesttype(), target);
  }

  // optional .protobuf.srl.request.LoginInformation login = 2;
  if (has_login()) {
    target = ::google::protobuf::internal::WireFormatLite::
      WriteMessageNoVirtualToArray(
        2, this->login(), target);
  }

  // optional bytes otherData = 3;
  if (has_otherdata()) {
    target =
      ::google::protobuf::internal::WireFormatLite::WriteBytesToArray(
        3, this->otherdata(), target);
  }

  // optional string responseText = 4;
  if (has_responsetext()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->responsetext().data(), this->responsetext().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    target =
      ::google::protobuf::internal::WireFormatLite::WriteStringToArray(
        4, this->responsetext(), target);
  }

  // optional string sessionInfo = 5;
  if (has_sessioninfo()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->sessioninfo().data(), this->sessioninfo().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    target =
      ::google::protobuf::internal::WireFormatLite::WriteStringToArray(
        5, this->sessioninfo(), target);
  }

  if (!unknown_fields().empty()) {
    target = ::google::protobuf::internal::WireFormat::SerializeUnknownFieldsToArray(
        unknown_fields(), target);
  }
  return target;
}

int Request::ByteSize() const {
  int total_size = 0;

  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    // required .protobuf.srl.request.Request.MessageType requestType = 1 [default = LOGIN];
    if (has_requesttype()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::EnumSize(this->requesttype());
    }

    // optional .protobuf.srl.request.LoginInformation login = 2;
    if (has_login()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::MessageSizeNoVirtual(
          this->login());
    }

    // optional bytes otherData = 3;
    if (has_otherdata()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::BytesSize(
          this->otherdata());
    }

    // optional string responseText = 4;
    if (has_responsetext()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->responsetext());
    }

    // optional string sessionInfo = 5;
    if (has_sessioninfo()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->sessioninfo());
    }

  }
  if (!unknown_fields().empty()) {
    total_size +=
      ::google::protobuf::internal::WireFormat::ComputeUnknownFieldsSize(
        unknown_fields());
  }
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = total_size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
  return total_size;
}

void Request::MergeFrom(const ::google::protobuf::Message& from) {
  GOOGLE_CHECK_NE(&from, this);
  const Request* source =
    ::google::protobuf::internal::dynamic_cast_if_available<const Request*>(
      &from);
  if (source == NULL) {
    ::google::protobuf::internal::ReflectionOps::Merge(from, this);
  } else {
    MergeFrom(*source);
  }
}

void Request::MergeFrom(const Request& from) {
  GOOGLE_CHECK_NE(&from, this);
  if (from._has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (from.has_requesttype()) {
      set_requesttype(from.requesttype());
    }
    if (from.has_login()) {
      mutable_login()->::protobuf::srl::request::LoginInformation::MergeFrom(from.login());
    }
    if (from.has_otherdata()) {
      set_otherdata(from.otherdata());
    }
    if (from.has_responsetext()) {
      set_responsetext(from.responsetext());
    }
    if (from.has_sessioninfo()) {
      set_sessioninfo(from.sessioninfo());
    }
  }
  mutable_unknown_fields()->MergeFrom(from.unknown_fields());
}

void Request::CopyFrom(const ::google::protobuf::Message& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

void Request::CopyFrom(const Request& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool Request::IsInitialized() const {
  if ((_has_bits_[0] & 0x00000001) != 0x00000001) return false;

  if (has_login()) {
    if (!this->login().IsInitialized()) return false;
  }
  return true;
}

void Request::Swap(Request* other) {
  if (other != this) {
    std::swap(requesttype_, other->requesttype_);
    std::swap(login_, other->login_);
    std::swap(otherdata_, other->otherdata_);
    std::swap(responsetext_, other->responsetext_);
    std::swap(sessioninfo_, other->sessioninfo_);
    std::swap(_has_bits_[0], other->_has_bits_[0]);
    _unknown_fields_.Swap(&other->_unknown_fields_);
    std::swap(_cached_size_, other->_cached_size_);
  }
}

::google::protobuf::Metadata Request::GetMetadata() const {
  protobuf_AssignDescriptorsOnce();
  ::google::protobuf::Metadata metadata;
  metadata.descriptor = Request_descriptor_;
  metadata.reflection = Request_reflection_;
  return metadata;
}


// ===================================================================

#ifndef _MSC_VER
const int LoginInformation::kUsernameFieldNumber;
const int LoginInformation::kPasswordFieldNumber;
const int LoginInformation::kIsLoggedInFieldNumber;
const int LoginInformation::kIsInstructorFieldNumber;
const int LoginInformation::kIsRegisteringFieldNumber;
#endif  // !_MSC_VER

LoginInformation::LoginInformation()
  : ::google::protobuf::Message() {
  SharedCtor();
}

void LoginInformation::InitAsDefaultInstance() {
}

LoginInformation::LoginInformation(const LoginInformation& from)
  : ::google::protobuf::Message() {
  SharedCtor();
  MergeFrom(from);
}

void LoginInformation::SharedCtor() {
  _cached_size_ = 0;
  username_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  password_ = const_cast< ::std::string*>(&::google::protobuf::internal::kEmptyString);
  isloggedin_ = false;
  isinstructor_ = false;
  isregistering_ = false;
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
}

LoginInformation::~LoginInformation() {
  SharedDtor();
}

void LoginInformation::SharedDtor() {
  if (username_ != &::google::protobuf::internal::kEmptyString) {
    delete username_;
  }
  if (password_ != &::google::protobuf::internal::kEmptyString) {
    delete password_;
  }
  if (this != default_instance_) {
  }
}

void LoginInformation::SetCachedSize(int size) const {
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
}
const ::google::protobuf::Descriptor* LoginInformation::descriptor() {
  protobuf_AssignDescriptorsOnce();
  return LoginInformation_descriptor_;
}

const LoginInformation& LoginInformation::default_instance() {
  if (default_instance_ == NULL) protobuf_AddDesc_input_2fmessage_2eproto();
  return *default_instance_;
}

LoginInformation* LoginInformation::default_instance_ = NULL;

LoginInformation* LoginInformation::New() const {
  return new LoginInformation;
}

void LoginInformation::Clear() {
  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (has_username()) {
      if (username_ != &::google::protobuf::internal::kEmptyString) {
        username_->clear();
      }
    }
    if (has_password()) {
      if (password_ != &::google::protobuf::internal::kEmptyString) {
        password_->clear();
      }
    }
    isloggedin_ = false;
    isinstructor_ = false;
    isregistering_ = false;
  }
  ::memset(_has_bits_, 0, sizeof(_has_bits_));
  mutable_unknown_fields()->Clear();
}

bool LoginInformation::MergePartialFromCodedStream(
    ::google::protobuf::io::CodedInputStream* input) {
#define DO_(EXPRESSION) if (!(EXPRESSION)) return false
  ::google::protobuf::uint32 tag;
  while ((tag = input->ReadTag()) != 0) {
    switch (::google::protobuf::internal::WireFormatLite::GetTagFieldNumber(tag)) {
      // required string username = 1;
      case 1: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_username()));
          ::google::protobuf::internal::WireFormat::VerifyUTF8String(
            this->username().data(), this->username().length(),
            ::google::protobuf::internal::WireFormat::PARSE);
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(18)) goto parse_password;
        break;
      }

      // optional string password = 2;
      case 2: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_LENGTH_DELIMITED) {
         parse_password:
          DO_(::google::protobuf::internal::WireFormatLite::ReadString(
                input, this->mutable_password()));
          ::google::protobuf::internal::WireFormat::VerifyUTF8String(
            this->password().data(), this->password().length(),
            ::google::protobuf::internal::WireFormat::PARSE);
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(24)) goto parse_isLoggedIn;
        break;
      }

      // optional bool isLoggedIn = 3;
      case 3: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_VARINT) {
         parse_isLoggedIn:
          DO_((::google::protobuf::internal::WireFormatLite::ReadPrimitive<
                   bool, ::google::protobuf::internal::WireFormatLite::TYPE_BOOL>(
                 input, &isloggedin_)));
          set_has_isloggedin();
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(32)) goto parse_isInstructor;
        break;
      }

      // optional bool isInstructor = 4;
      case 4: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_VARINT) {
         parse_isInstructor:
          DO_((::google::protobuf::internal::WireFormatLite::ReadPrimitive<
                   bool, ::google::protobuf::internal::WireFormatLite::TYPE_BOOL>(
                 input, &isinstructor_)));
          set_has_isinstructor();
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectTag(40)) goto parse_isRegistering;
        break;
      }

      // optional bool isRegistering = 5;
      case 5: {
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_VARINT) {
         parse_isRegistering:
          DO_((::google::protobuf::internal::WireFormatLite::ReadPrimitive<
                   bool, ::google::protobuf::internal::WireFormatLite::TYPE_BOOL>(
                 input, &isregistering_)));
          set_has_isregistering();
        } else {
          goto handle_uninterpreted;
        }
        if (input->ExpectAtEnd()) return true;
        break;
      }

      default: {
      handle_uninterpreted:
        if (::google::protobuf::internal::WireFormatLite::GetTagWireType(tag) ==
            ::google::protobuf::internal::WireFormatLite::WIRETYPE_END_GROUP) {
          return true;
        }
        DO_(::google::protobuf::internal::WireFormat::SkipField(
              input, tag, mutable_unknown_fields()));
        break;
      }
    }
  }
  return true;
#undef DO_
}

void LoginInformation::SerializeWithCachedSizes(
    ::google::protobuf::io::CodedOutputStream* output) const {
  // required string username = 1;
  if (has_username()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->username().data(), this->username().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    ::google::protobuf::internal::WireFormatLite::WriteString(
      1, this->username(), output);
  }

  // optional string password = 2;
  if (has_password()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->password().data(), this->password().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    ::google::protobuf::internal::WireFormatLite::WriteString(
      2, this->password(), output);
  }

  // optional bool isLoggedIn = 3;
  if (has_isloggedin()) {
    ::google::protobuf::internal::WireFormatLite::WriteBool(3, this->isloggedin(), output);
  }

  // optional bool isInstructor = 4;
  if (has_isinstructor()) {
    ::google::protobuf::internal::WireFormatLite::WriteBool(4, this->isinstructor(), output);
  }

  // optional bool isRegistering = 5;
  if (has_isregistering()) {
    ::google::protobuf::internal::WireFormatLite::WriteBool(5, this->isregistering(), output);
  }

  if (!unknown_fields().empty()) {
    ::google::protobuf::internal::WireFormat::SerializeUnknownFields(
        unknown_fields(), output);
  }
}

::google::protobuf::uint8* LoginInformation::SerializeWithCachedSizesToArray(
    ::google::protobuf::uint8* target) const {
  // required string username = 1;
  if (has_username()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->username().data(), this->username().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    target =
      ::google::protobuf::internal::WireFormatLite::WriteStringToArray(
        1, this->username(), target);
  }

  // optional string password = 2;
  if (has_password()) {
    ::google::protobuf::internal::WireFormat::VerifyUTF8String(
      this->password().data(), this->password().length(),
      ::google::protobuf::internal::WireFormat::SERIALIZE);
    target =
      ::google::protobuf::internal::WireFormatLite::WriteStringToArray(
        2, this->password(), target);
  }

  // optional bool isLoggedIn = 3;
  if (has_isloggedin()) {
    target = ::google::protobuf::internal::WireFormatLite::WriteBoolToArray(3, this->isloggedin(), target);
  }

  // optional bool isInstructor = 4;
  if (has_isinstructor()) {
    target = ::google::protobuf::internal::WireFormatLite::WriteBoolToArray(4, this->isinstructor(), target);
  }

  // optional bool isRegistering = 5;
  if (has_isregistering()) {
    target = ::google::protobuf::internal::WireFormatLite::WriteBoolToArray(5, this->isregistering(), target);
  }

  if (!unknown_fields().empty()) {
    target = ::google::protobuf::internal::WireFormat::SerializeUnknownFieldsToArray(
        unknown_fields(), target);
  }
  return target;
}

int LoginInformation::ByteSize() const {
  int total_size = 0;

  if (_has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    // required string username = 1;
    if (has_username()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->username());
    }

    // optional string password = 2;
    if (has_password()) {
      total_size += 1 +
        ::google::protobuf::internal::WireFormatLite::StringSize(
          this->password());
    }

    // optional bool isLoggedIn = 3;
    if (has_isloggedin()) {
      total_size += 1 + 1;
    }

    // optional bool isInstructor = 4;
    if (has_isinstructor()) {
      total_size += 1 + 1;
    }

    // optional bool isRegistering = 5;
    if (has_isregistering()) {
      total_size += 1 + 1;
    }

  }
  if (!unknown_fields().empty()) {
    total_size +=
      ::google::protobuf::internal::WireFormat::ComputeUnknownFieldsSize(
        unknown_fields());
  }
  GOOGLE_SAFE_CONCURRENT_WRITES_BEGIN();
  _cached_size_ = total_size;
  GOOGLE_SAFE_CONCURRENT_WRITES_END();
  return total_size;
}

void LoginInformation::MergeFrom(const ::google::protobuf::Message& from) {
  GOOGLE_CHECK_NE(&from, this);
  const LoginInformation* source =
    ::google::protobuf::internal::dynamic_cast_if_available<const LoginInformation*>(
      &from);
  if (source == NULL) {
    ::google::protobuf::internal::ReflectionOps::Merge(from, this);
  } else {
    MergeFrom(*source);
  }
}

void LoginInformation::MergeFrom(const LoginInformation& from) {
  GOOGLE_CHECK_NE(&from, this);
  if (from._has_bits_[0 / 32] & (0xffu << (0 % 32))) {
    if (from.has_username()) {
      set_username(from.username());
    }
    if (from.has_password()) {
      set_password(from.password());
    }
    if (from.has_isloggedin()) {
      set_isloggedin(from.isloggedin());
    }
    if (from.has_isinstructor()) {
      set_isinstructor(from.isinstructor());
    }
    if (from.has_isregistering()) {
      set_isregistering(from.isregistering());
    }
  }
  mutable_unknown_fields()->MergeFrom(from.unknown_fields());
}

void LoginInformation::CopyFrom(const ::google::protobuf::Message& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

void LoginInformation::CopyFrom(const LoginInformation& from) {
  if (&from == this) return;
  Clear();
  MergeFrom(from);
}

bool LoginInformation::IsInitialized() const {
  if ((_has_bits_[0] & 0x00000001) != 0x00000001) return false;

  return true;
}

void LoginInformation::Swap(LoginInformation* other) {
  if (other != this) {
    std::swap(username_, other->username_);
    std::swap(password_, other->password_);
    std::swap(isloggedin_, other->isloggedin_);
    std::swap(isinstructor_, other->isinstructor_);
    std::swap(isregistering_, other->isregistering_);
    std::swap(_has_bits_[0], other->_has_bits_[0]);
    _unknown_fields_.Swap(&other->_unknown_fields_);
    std::swap(_cached_size_, other->_cached_size_);
  }
}

::google::protobuf::Metadata LoginInformation::GetMetadata() const {
  protobuf_AssignDescriptorsOnce();
  ::google::protobuf::Metadata metadata;
  metadata.descriptor = LoginInformation_descriptor_;
  metadata.reflection = LoginInformation_reflection_;
  return metadata;
}


// @@protoc_insertion_point(namespace_scope)

}  // namespace request
}  // namespace srl
}  // namespace protobuf

// @@protoc_insertion_point(global_scope)
