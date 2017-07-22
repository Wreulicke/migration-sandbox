package com.github.wreulicke.ast.spoon;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.compiler.Environment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.reflect.visitor.PrettyPrinter;
import spoon.reflect.visitor.printer.ElementPrinterHelper;
import spoon.reflect.visitor.printer.ListPrinter;
import spoon.reflect.visitor.printer.PrinterHelper;

public class ProcessorTest {
	@Test
	public void test() {
		SpoonAPI api = new Launcher() {
			@Override
			public PrettyPrinter createPrettyPrinter() {
				return new SimpleMemberPrettyPrinter(getEnvironment());
			}
		};
		api.getEnvironment().setAutoImports(true);
		api.getEnvironment().setNoClasspath(true);
		api.getEnvironment().setComplianceLevel(8);
		api.addInputResource("src/test/resources/OldController.java");
		api.addProcessor(new Processor());
		api.prettyprint();
		api.setSourceOutputDirectory("target/spooned");
		api.run();
	}
	
	
	/**
	 * TODO fix Dirty Hacking
	 * 
	 * @author wreulicke
	 *
	 */
	private class SimpleMemberPrettyPrinter extends DefaultJavaPrettyPrinter {
		
		private PrinterHelper printer;
		
		private ElementPrinterHelper elementPrinterHelper;
		
		
		public SimpleMemberPrettyPrinter(Environment env) {
			super(env);
			try {
				Field field = DefaultJavaPrettyPrinter.class.getDeclaredField("printer");
				field.setAccessible(true);
				Object printer = field.get(this);
				this.printer = (PrinterHelper) printer;
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
			elementPrinterHelper = getElementPrinterHelper();
		}
		
		@Override
		public <A extends Annotation> void visitCtAnnotation(CtAnnotation<A> annotation) {
			elementPrinterHelper.writeAnnotations(annotation);
			printer.write("@");
			scan(annotation.getAnnotationType());
			Map<String, CtExpression> values = annotation.getValues();
			int size = values.size();
			if (size == 1 && values.containsKey("value")) {
				printer.write('(');
				elementPrinterHelper.writeAnnotationElement(annotation.getFactory(),
						annotation.getValues().get("value"));
				printer.write(')');
			} else if (size > 0) {
				try (ListPrinter lp = printer.createListPrinter("(", ", ", ")")) {
					for (Entry<String, CtExpression> e : values.entrySet()) {
						lp.printSeparatorIfAppropriate();
						printer.write(e.getKey() + " = ");
						elementPrinterHelper.writeAnnotationElement(annotation.getFactory(), e.getValue());
					}
				}
			}
		}
	}
}
