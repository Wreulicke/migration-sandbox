package com.github.wreulicke.ast.sandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javaslang.Function1;
import javaslang.control.Option;

public class Match<T> {
	T target;
	
	
	public Match(T o) {
		target = o;
	}
	
	public static <T> Match<T> of(T o) {
		return new Match<T>(o);
	}
	
	public <R> ParameterizedMetcher<T, R> Case(Predicate<T> predicate, R retVal) {
		return new ParameterizedMetcher<T, R>(this.target, Arrays.asList(new Case<T, R>(predicate, retVal)));
	}
	
	public <R> ParameterizedMetcher<T, R> Case(Predicate<T> predicate, Function1<T, R> retVal) {
		return new ParameterizedMetcher<T, R>(this.target, Arrays.asList(new Case<T, R>(predicate, retVal)));
	}
	
	public <R> ParameterizedMetcher<T, R> Case(Predicate<T> predicate, Supplier<R> retVal) {
		return new ParameterizedMetcher<T, R>(this.target, Arrays.asList(new Case<T, R>(predicate, retVal)));
	}
	
	
	public static class ParameterizedMetcher<T, R> {
		T target;
		
		List<Case<T, R>> cases;
		
		
		public ParameterizedMetcher(T target, List<Match.Case<T, R>> cases) {
			this.target = target;
			this.cases = cases;
		}
		
		public ParameterizedMetcher<T, R> Case(Predicate<T> predicate, R retVal) {
			ArrayList<Case<T, R>> cases = new ArrayList<>(this.cases);
			cases.add(new Case<T, R>(predicate, retVal));
			return new ParameterizedMetcher<T, R>(this.target, cases);
		}
		
		public ParameterizedMetcher<T, R> Case(Predicate<T> predicate, Function1<T, R> retVal) {
			ArrayList<Case<T, R>> cases = new ArrayList<>(this.cases);
			cases.add(new Case<T, R>(predicate, retVal));
			return new ParameterizedMetcher<T, R>(this.target, cases);
		}
		
		public ParameterizedMetcher<T, R> Case(Predicate<T> predicate, Supplier<R> retVal) {
			ArrayList<Case<T, R>> cases = new ArrayList<>(this.cases);
			cases.add(new Case<T, R>(predicate, retVal));
			return new ParameterizedMetcher<T, R>(this.target, cases);
		}
		
		public Option<R> option() {
			for (Case<T, R> caze : cases) {
				if (caze.matches(target)) {
					return Option.some(caze.getValue(target));
				}
			}
			return Option.none();
		}
		
		public <X> Option<X> map(Function1<R, X> mapper) {
			return option().map(mapper);
		}
		
	}
	
	public static class Case<T, R> {
		Predicate<T> predicate;
		
		Function1<T, R> ret;
		
		
		public Case(Predicate<T> predicate, Function1<T, R> retVal) {
			this.predicate = predicate;
			this.ret = retVal;
		}
		
		public Case(Predicate<T> predicate, Supplier<R> retVal) {
			this.predicate = predicate;
			this.ret = ignore -> retVal.get();
		}
		
		public Case(Predicate<T> predicate, R retVal) {
			this.predicate = predicate;
			this.ret = ignore -> retVal;
		}
		
		public boolean matches(T o) {
			return predicate.test(o);
		}
		
		public R getValue(T o) {
			return ret.apply(o);
		}
		
	}
}
