// Copyright 2022 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include <iostream>
#include <string>

#include "src/main/protobuf/credential_helper.pb.h"

namespace {

int CommandGet() {
  bazelcredentialhelper::GetRequest request;
  if (!request.ParseFromIstream(&std::cin)) {
    std::cerr << "Failed to parse GetRequest from stdin" << std::endl;
    return 1;
  }

  bazelcredentialhelper::GetResponse response;
  if (request.uri() == "https://singleheader.example.com") {
    bazelcredentialhelper::GetResponse::Header* header1 = response.add_header();
    header1->set_name("header1");
    header1->add_value("value1");
  } else if (request.uri() == "https://multipleheaders.example.com") {
    bazelcredentialhelper::GetResponse::Header* header1 = response.add_header();
    header1->set_name("header1");
    header1->add_value("value1");

    bazelcredentialhelper::GetResponse::Header* header2 = response.add_header();
    header2->set_name("header2");
    header2->add_value("value1");
    header2->add_value("value2");

    bazelcredentialhelper::GetResponse::Header* header3 = response.add_header();
    header3->set_name("header3");
    header3->add_value("value1");
    header3->add_value("value2");
    header3->add_value("value3");
  } else {
    std::cerr << "Unknown uri " << request.uri() << std::endl;
    return 1;
  }

  if (!response.SerializeToOstream(&std::cout)) {
    std::cerr << "Failed to write GetResponse to stdout" << std::endl;
    return 1;
  }
  std::cout.flush();
  return 0;
}

}  // namespace

int main(int argc, char *argv[]) {
  if (argc != 2) {
    std::cerr << "Usage: test_credential_helper <command>" << std::endl;
    return 1;
  }

  std::string command(argv[1]);
  if (command == "get") {
    return CommandGet();
  } else {
    std::cerr << "Unknown command " << command << std::endl;
    return 1;
  }
}
