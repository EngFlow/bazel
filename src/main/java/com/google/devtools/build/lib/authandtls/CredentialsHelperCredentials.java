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

package com.google.devtools.build.lib.authandtls;

import com.google.auth.Credentials;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.devtools.build.lib.authandtls.credentialhelper.CredentialHelper;
import com.google.devtools.build.lib.authandtls.credentialhelper.CredentialHelperProgram;
import com.google.devtools.build.lib.shell.Command;
import com.google.devtools.build.lib.util.CommandBuilder;
import com.google.devtools.build.lib.vfs.Path;
import com.google.devtools.build.lib.vfs.PathFragment;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Subclass of {@link Credentials} getting its credentials from a {@code <foo>-credentials-helper} binary.
 */
public class CredentialsHelperCredentials extends Credentials {
  private final String type;
  private final CredentialHelper credentialHelper;

  private final AtomicReference<Map<URI, Map<String, List<String>>>> metadataCache =
      new AtomicReference<>(new HashMap<>());

  public CredentialsHelperCredentials(
      String type,
      PathFragment credentialsHelperPath,
      Map<String, String> commandEnv,
      Path workspace) {
    this.type = Preconditions.checkNotNull(type);
    this.credentialHelper =
        new CredentialHelper(
            new BlazeCredentialHelperProgram(credentialsHelperPath, commandEnv, workspace));
  }

  @Override
  public String getAuthenticationType() {
    return type;
  }

  @Override
  public boolean hasRequestMetadataOnly() {
    // As of 2022-03-17, credentials-helpers only support authenticating with metadata (i.e. only
    // headers, not, e.g., mTLS).
    return true;
  }

  @Override
  public boolean hasRequestMetadata() {
    return true;
  }

  @Override
  public Map<String, List<String>> getRequestMetadata(URI uri) throws IOException {
    Preconditions.checkNotNull(uri);

    Map<URI, Map<String, List<String>>> cache = metadataCache.get();
    Map<String, List<String>> cachedMetadata = cache.get(uri);
    if (cachedMetadata != null) {
      return cachedMetadata;
    }

    try {
      ImmutableMultimap<String, String> headers = credentialHelper.get(uri);
      ImmutableMap.Builder<String, List<String>> metadata = ImmutableMap.builder();
      for (var key : headers.keySet()) {
        metadata.put(key, ImmutableList.copyOf(headers.get(key)));
      }
      return metadata.build();
    } catch (InterruptedException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void refresh() {
    metadataCache.set(new HashMap<>());
  }

  private static final class BlazeCredentialHelperProgram implements CredentialHelperProgram {
    private final PathFragment credentialsHelperPath;
    private final Map<String, String> commandEnv;
    private final Path workspace;

    public BlazeCredentialHelperProgram(
        PathFragment credentialsHelperPath,
        Map<String, String> commandEnv,
        Path workspace) {
      this.credentialsHelperPath = Preconditions.checkNotNull(credentialsHelperPath);
      this.commandEnv = Preconditions.checkNotNull(commandEnv);
      this.workspace = Preconditions.checkNotNull(workspace);
    }

    @Override
    public Command createCommand(String... args) {
      return new CommandBuilder()
          .addArgs(
              ImmutableList.<String>builder()
                  .add(credentialsHelperPath.getSafePathString())
                  .add(args)
                  .build())
          .setEnv(commandEnv)
          .setWorkingDir(workspace)
          .build();
    }
  }
}
