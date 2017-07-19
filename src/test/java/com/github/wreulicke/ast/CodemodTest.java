package com.github.wreulicke.ast;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.junit.Test;

public class CodemodTest {
	@Test
	public void test() throws MalformedTreeException, BadLocationException {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Hashtable<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		String source = ("import org.springframework.web.bind.annotation.RequestMapping;\n"
				+ "public class CodemodTest {\n" +
				"	@RequestMapping(\"test\")\n" +
				"	public void test(){\n" +
				"		System.out.println(\"test\");\n" +
				"		ASTParser parser = ASTParser.newParser(AST.JLS8);\n" +
				"	}\n" +
				"	@RequestMapping(method=HttpMethod.GET)\n" +
				"	public void test(){\n" +
				"		System.out.println(\"test\");\n" +
				"		ASTParser parser = ASTParser.newParser(AST.JLS8);\n" +
				"	}\n" +
				"	@RequestMapping(path=\"/hogehoge\", method=HttpMethod.GET)\n" +
				"	public void test(){\n" +
				"		System.out.println(\"test\");\n" +
				"		ASTParser parser = ASTParser.newParser(AST.JLS8);\n" +
				"	}\n" +
				"}\n");
		parser.setSource(source.toCharArray());
		CompilationUnit node = (CompilationUnit) parser.createAST(null);
		node.recordModifications();
		AST ast = node.getAST();
		List imports = node.imports();
		node.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodDeclaration node) {
				Consumer<MethodDeclaration> addLogging = d -> {
					MethodInvocation invocation = ast.newMethodInvocation();
					ExpressionStatement e = ast.newExpressionStatement(invocation);
					FieldAccess access = ast.newFieldAccess();
					access.setName(ast.newSimpleName("out"));
					access.setExpression(ast.newSimpleName("System"));
					invocation.setExpression(access);
					invocation.setName(ast.newSimpleName("println"));
					List<Object> list = d.getBody().statements();
					list.add(e);
				};
				addLogging.accept(node);
				addLogging.accept(node);
				Object e = node.modifiers().get(0);
				if (e instanceof NormalAnnotation) {
					List<MemberValuePair> values = ((NormalAnnotation) e).values();
					Map<Boolean, List<MemberValuePair>> map = values.stream()
						.collect(Collectors.partitioningBy(p -> p.getName().getIdentifier().equals("method")));
					List<MemberValuePair> method = map.get(true);
					Optional.ofNullable(method.get(0)).map(p -> {
						String string = p.getValue().toString();
						switch (string) {
							case "HttpMethod.GET":
								return "GetMapping";
							case "HttpMethod.POST":
								return "PostMapping";
							case "HttpMethod.PUT":
								return "PutMapping";
							case "HttpMethod.DELETE":
								return "DeleteMapping";
							case "HttpMethod.PATCH":
								return "PatchMapping";
							default:
								throw new AssertionError();
						}
						
					}).map(p -> {
						List<MemberValuePair> pairs = map.get(false);
						Annotation annotation;
						if (pairs.isEmpty()) {
							annotation = ast.newMarkerAnnotation();
						} else {
							NormalAnnotation ex = ast.newNormalAnnotation();
							annotation = ex;
							pairs.stream()
								.map(pair -> ASTNode.copySubtree(ast.newMemberValuePair().getAST(), pair))
								.forEach(ex.values()::add);
						}
						annotation.setTypeName(ast.newSimpleName(p));
						return annotation;
					}).ifPresent(ex -> {
						node.modifiers().set(0, ex);
					});
				}
				return false;
			}
		});
		Document document = new Document(source);
		TextEdit edit = node.rewrite(document, null);
		edit.apply(document);
		String rewrite = document.get();
		System.out.println(rewrite);
		
	}
}
