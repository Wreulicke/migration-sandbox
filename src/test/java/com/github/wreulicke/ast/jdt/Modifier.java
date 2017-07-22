package com.github.wreulicke.ast.jdt;

import static javaslang.API.$;
import static javaslang.API.Case;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;

import com.google.common.base.Predicates;

import javaslang.API;
import javaslang.Function1;

public class Modifier extends ASTVisitor {
	AST ast;
	
	ImportRepository repository;
	
	
	public Modifier(AST ast, ImportRepository repository) {
		this.ast = ast;
		this.repository = repository;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		Object e = node.modifiers().get(0);
		if (repository.isAlreadyImported("org.springframework.web.bind.annotation.RequestMapping") == false) {
			return false;
		}
		API.Case(Predicates.instanceOf(NormalAnnotation.class), NormalAnnotation.class::cast)
			.apply(e)
			.filter(a -> a.getTypeName().getFullyQualifiedName().equals("RequestMapping"))
			.flatMap(a -> {
				MemberValuePairRepository pairRepository = new MemberValuePairRepository(a.values());
				return pairRepository.findMember("method").map(MemberValuePair::getValue).map(Expression::toString)
					.flatMap(identifier -> {
						return API.Match(identifier).option(
								Case($("RequestMethod.GET"), "GetMapping"),
								Case($("RequestMethod.POST"), "PostMapping"),
								Case($("RequestMethod.PUT"), "PutMapping"),
								Case($("RequestMethod.DELETE"), "DeleteMapping"),
								Case($("RequestMethod.PATCH"), "PatchMapping"))
							.filter(ignore -> repository
								.isAlreadyImported("org.springframework.web.bind.annotation.RequestMethod"))
							.orElse(() -> API.Match(identifier).option(
									Case($("GET"), Function1.identity()),
									Case($("POST"), Function1.identity()),
									Case($("PUT"), Function1.identity()),
									Case($("DELETE"), Function1.identity()),
									Case($("PATCH"), Function1.identity()))
								.filter(m -> repository
									.isAlreadyStaticImported(
											"org.springframework.web.bind.annotation.RequestMethod." + m)));
					}).map(annotationName -> {
						repository.add("org.springframework.web.bind.annotation." + annotationName);
						List<MemberValuePair> pairs = pairRepository.findExcludeMember("method");
						Annotation annotation = API.Match(pairs).of(
								Case(List::isEmpty, ast.newMarkerAnnotation()),
								Case(t -> t.size() == 1 && (pairs.get(0).getName().getIdentifier().equals("path")
										|| pairs.get(0).getName().getIdentifier().equals("value")),
										o -> {
											MemberValuePair pair = pairs.get(0);
											SingleMemberAnnotation memberAnnotation =
													ast.newSingleMemberAnnotation();
											StringLiteral literal = ast.newStringLiteral();
											literal.setEscapedValue(pair.getValue().toString());
											memberAnnotation.setValue(literal);
											return memberAnnotation;
										}),
								Case(API.$(), o -> {
									NormalAnnotation n = ast.newNormalAnnotation();
									pairs.stream()
										.map(pair -> ASTNode.copySubtree(ast.newMemberValuePair().getAST(), pair))
										.forEach(n.values()::add);
									return n;
								}));
						annotation.setTypeName(ast.newSimpleName(annotationName));
						return annotation;
					});
			}).peek(ex -> {
				node.modifiers().set(0, ex);
			});
		return false;
	}
	
}
