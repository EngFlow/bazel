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

import static org.junit.Assert.assertThrows;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMultimap;
import java.io.IOException;
import java.net.URI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CredentialHelperTest {
  @Test
  public void knownUriWithSingleHeader() throws Exception {
    CredentialHelper helper = new CredentialHelper(new TestCredentialHelperProgram());

    ImmutableMultimap<String, String> metadata =
        helper.get(URI.create("https://singleheader.example.com"));
    assertThat(metadata)
        .containsExactlyEntriesIn(
            ImmutableMultimap.<String, String>builder()
                .putAll("header1", "value1")
                .build());
  }

  @Test
  public void knownUriWithMultipleHeaders() throws Exception {
    CredentialHelper helper = new CredentialHelper(new TestCredentialHelperProgram());

    ImmutableMultimap<String, String> metadata =
        helper.get(URI.create("https://multipleheaders.example.com"));
    assertThat(metadata)
        .containsExactlyEntriesIn(
            ImmutableMultimap.<String, String>builder()
                .putAll("header1", "value1")
                .putAll("header2", "value1", "value2")
                .putAll("header3", "value1", "value2", "value3")
                .build());
  }

  @Test
  public void unknownUri() {
    CredentialHelper helper = new CredentialHelper(new TestCredentialHelperProgram());

    IOException ioException =
        assertThrows(
            IOException.class,
            () -> helper.get(URI.create("https://unknown.example.com")));
    assertThat(ioException).hasMessageThat().contains("Unknown uri https://unknown.example.com");
  }
}
