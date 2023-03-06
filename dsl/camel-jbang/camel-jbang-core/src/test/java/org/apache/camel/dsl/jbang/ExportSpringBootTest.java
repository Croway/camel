package org.apache.camel.dsl.jbang;

import org.apache.camel.dsl.jbang.core.commands.CamelJBangMain;
import org.apache.camel.dsl.jbang.core.commands.Export;
import org.apache.camel.dsl.jbang.core.commands.Init;

import org.junit.jupiter.api.Test;

import org.assertj.core.api.Assertions;

import java.io.PrintWriter;
import java.io.StringWriter;

import picocli.CommandLine;

public class ExportSpringBootTest {

	@Test
	public void test() {
		CamelJBangMain main = new CamelJBangMain();
		CommandLine commandLine = new CommandLine(main)
				.addSubcommand("init", new CommandLine(new Init(main)))
				.addSubcommand("export", new CommandLine(new Export(main)));

		StringWriter sw = new StringWriter();
		commandLine.setOut(new PrintWriter(sw));

		int exitCode = commandLine.execute("init", "test.yaml", "--directory=target/data");
		Assertions.assertThat(exitCode).isEqualTo(0);

		exitCode = commandLine.execute("export", "--runtime=spring-boot",
				"--directory=target/data",
				"--gav=com.foo:acme:1.0-SNAPSHOT",
				"--dep=org.apache.camel.springboot:camel-timer-starter",
				"--camel-spring-boot-version=3.20.1.redhat-00030",
				"--additional-properties=first-maven-plugin-version=foo,second-maven-plugin-version=bar");
		Assertions.assertThat(exitCode).isEqualTo(0);
	}
}
