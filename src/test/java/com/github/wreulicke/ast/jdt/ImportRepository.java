package com.github.wreulicke.ast.jdt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import com.google.common.collect.Sets;

public class ImportRepository {
	
	private AST ast;
	
	private List<ImportDeclaration> imports;
	
	private Set<ImportDeclaration> importSet;
	
	private Set<ImportDeclaration> staticImportSet;
	
	
	public ImportRepository(List<ImportDeclaration> imports, AST ast) {
		this.imports = imports;
		Map<Boolean, Set<ImportDeclaration>> map = imports.stream()
			.collect(Collectors.groupingBy(ImportDeclaration::isStatic, Collectors.toSet()));
		importSet = map.get(false);
		staticImportSet = map.get(false);
		this.ast = ast;
	}
	
	public void add(String qualifiedName) {
		if (isAlreadyImported(qualifiedName)) {
			return;
		}
		ImportDeclaration declaration = ast.newImportDeclaration();
		declaration.setName(ast.newName(qualifiedName));
		imports.add(declaration);
		importSet = Sets.union(Sets.newHashSet(declaration), importSet);
	}
	
	public Optional<ImportDeclaration> findIdentifier(String identifier) {
		return importSet.stream()
			.filter(isImportedQualifier(getPackageName(identifier), identifier))
			.findFirst();
	}
	
	public Optional<ImportDeclaration> findStaticIdentifier(String identifier) {
		return staticImportSet.stream()
			.filter(isImportedQualifier(getPackageName(identifier).flatMap(this::getPackageName), identifier))
			.findFirst();
	}
	
	public boolean isAlreadyImported(String identifier) {
		return findIdentifier(identifier).isPresent();
	}
	
	public boolean isAlreadyStaticImported(String identifier) {
		return findStaticIdentifier(identifier).isPresent();
	}
	
	private Predicate<ImportDeclaration> isImportedQualifier(Optional<String> packageName, String identifier) {
		return name -> {
			if (name.isOnDemand()) {
				return packageName.isPresent()
						&& packageName.equals(getPackageName(name.getName().getFullyQualifiedName()));
			} else {
				return identifier.equals(name.getName().getFullyQualifiedName());
			}
		};
	}
	
	private Optional<String> getPackageName(String fqcn) {
		int index = fqcn.lastIndexOf('.');
		if (index > 0) {
			return Optional.of(fqcn.substring(0, index));
		} else
			return Optional.empty();
	}
}
