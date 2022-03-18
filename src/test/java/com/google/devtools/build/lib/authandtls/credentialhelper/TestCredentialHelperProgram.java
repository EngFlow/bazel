package com.google.devtools.build.lib.authandtls.credentialhelper;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.shell.Command;
import com.google.devtools.build.lib.util.CommandBuilder;
import com.google.devtools.build.runfiles.Runfiles;
import java.io.IOException;
import java.io.UncheckedIOException;

public class TestCredentialHelperProgram implements CredentialHelperProgram {
  private static final String TEST_CREDENTIAL_HELPER =
      "io_bazel/src/test/java/com/google/devtools/build/lib/authandtls/credentialhelper/test_credential_helper";

  private static final Runfiles runfiles;

  static {
    try {
      runfiles = Runfiles.create();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Command createCommand(String... args) {
    return new CommandBuilder()
        .addArgs(
            ImmutableList.<String>builder()
                .add(runfiles.rlocation(TEST_CREDENTIAL_HELPER))
                .add(args)
                .build())
        .useTempDir()
        .build();
  }
}
