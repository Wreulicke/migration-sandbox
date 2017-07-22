package com.github.wreulicke.ast.jdt;

import java.util.Hashtable;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class Migrator {
	private ASTParser parser;
	
	
	public Migrator() {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Hashtable<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		this.parser = parser;
	}
	
	public String migrate(String source) {
		parser.setSource(source.toCharArray());
		CompilationUnit node = (CompilationUnit) parser.createAST(null);
		node.recordModifications();
		
		AST ast = node.getAST();
		ImportRepository repository = new ImportRepository(node.imports(), ast);
		node.accept(new Modifier(ast, repository));
		
		Document document = new Document(source);
		TextEdit edit = node.rewrite(document, null);
		try {
			edit.apply(document);
		} catch (MalformedTreeException | BadLocationException e) {
			throw new RuntimeException(e);
		}
		return document.get();
	}
}
