package com.github.wreulicke.ast.sandbox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MatchTest {
	
	@Test
	public void test() {
		assertThat(Match.of("xxx")
			.Case(""::equals, "")
			.Case("v"::equals, "zzz")
			.option().isEmpty()).isTrue();
	}
	
	@Test
	public void matchTest() {
		assertThat(Match.of("xxx").Case("xxx"::equals, () -> "vvv").Case("vvv"::equals, "xxx").option().get())
			.isEqualTo("vvv");
	}
	
}
