package com.github.wreulicke.ast.spoon;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

public class Processor extends AbstractProcessor<CtMethod<?>> {
	
	@Override
	public void process(CtMethod<?> element) {
		Optional<CtAnnotation<? extends Annotation>> annotation = element.getAnnotations()
			.stream()
			.filter(a -> a.getAnnotationType().getQualifiedName()
				.equals("org.springframework.web.bind.annotation.RequestMapping"))
			.findFirst();
		annotation.ifPresent(a -> {
			Map<String, CtExpression> values = a.getValues();
			CtExpression<?> e = values.get("method");
			if (e instanceof CtFieldRead<?>
					|| (e instanceof CtNewArray<?> && ((CtNewArray<?>) e).getElements().size() == 1)) {
				CtFieldRead<?> method = (e instanceof CtNewArray<?>)
						? (CtFieldRead) ((CtNewArray<?>) e).getElements().get(0) : (CtFieldRead) e;
				String string = method.toString();
				CtTypeReference<Annotation> r = element.getFactory().createTypeReference();
				
				if (string.equals("RequestMethod.GET")) {
					r.setSimpleName("GetMapping");
				} else if (string.equals("RequestMethod.POST")) {
					r.setSimpleName("PostMapping");
				} else if (string.equals("RequestMethod.DELETE")) {
					r.setSimpleName("DeleteMapping");
				} else if (string.equals("RequestMethod.PUT")) {
					r.setSimpleName("PutMapping");
				} else if (string.equals("RequestMethod.PATCH")) {
					r.setSimpleName("PatchMapping");
				} else
					return;
				r.setPackage(
						element.getFactory().createPackageReference()
							.setSimpleName("org.springframework.web.bind.annotation"));
				Map<String, CtExpression> newValues =
						values.entrySet().stream().filter(entry -> !entry.getKey().equals("method"))
							.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
				if (newValues.size() == 1 && newValues.containsKey("path")) {
					CtExpression ex = newValues.get("path");
					newValues.remove("path");
					newValues.put("value", ex);
				}
				a.setValues(newValues);
				a.setAnnotationType(r);
			}
		});
	}
	
}
