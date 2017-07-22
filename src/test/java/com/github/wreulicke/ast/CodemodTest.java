package com.github.wreulicke.ast;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.Test;

import com.github.wreulicke.ast.jdt.Migrator;

public class CodemodTest {
	
	private static String getContent(Path path) {
		try {
			return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	@Test
	public void test() throws MalformedTreeException, BadLocationException {
		
		String source = getContent(Paths.get("./src/test/resources/OldController"));
		String expected = getContent(Paths.get("./src/test/resources/NewController"));
		String rewrited = new Migrator().migrate(source);
		assertThat(rewrited).isEqualTo(expected);
	}
}
