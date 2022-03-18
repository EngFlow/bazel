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

package com.google.devtools.build.lib.authandtls.credentialhelper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.devtools.build.lib.authandtls.credentialhelper.proto.GetRequest;
import com.google.devtools.build.lib.authandtls.credentialhelper.proto.GetResponse;
import com.google.devtools.build.lib.shell.Command;
import com.google.devtools.build.lib.shell.CommandException;
import com.google.devtools.build.lib.shell.CommandResult;
import com.google.devtools.build.lib.shell.FutureCommandResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class CredentialHelper {
  private final CredentialHelperProgram program;

  public CredentialHelper(CredentialHelperProgram program) {
    this.program = Preconditions.checkNotNull(program);
  }

  public ImmutableMultimap<String, String> get(URI uri) throws InterruptedException, IOException {
    Preconditions.checkNotNull(uri);

    GetRequest request = GetRequest.newBuilder().setUri(uri.toString()).build();

    ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();
    try {
      ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();

      Command command = program.createCommand("get");
      FutureCommandResult resultFuture =
          command.executeAsync(
              new ByteArrayInputStream(request.toByteArray()), stdoutStream, stderrStream, true);
      CommandResult result = resultFuture.get();

      if (!result.getTerminationStatus().success()) {
        throw new IOException(
            String.format(
                Locale.US,
                "Failed to get credentials from helper: %s",
                new String(stderrStream.toByteArray(), StandardCharsets.UTF_8)));
      }

      GetResponse response = GetResponse.parseFrom(stdoutStream.toByteArray());

      ImmutableMultimap.Builder<String, String> metadata = ImmutableMultimap.builder();
      for (GetResponse.Header header : response.getHeaderList()) {
        metadata.putAll(header.getName(), header.getValueList());
      }
      return metadata.build();
    } catch (CommandException e) {
      throw new IOException(
          String.format(
              Locale.US,
              "Failed to run credentials helper: %s",
              new String(stderrStream.toByteArray(), StandardCharsets.UTF_8)), e);
    }
  }
}
