package com.github.wreulicke.ast.jdt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.MemberValuePair;

import javaslang.control.Option;

public class MemberValuePairRepository {
	private Map<String, MemberValuePair> map;
	
	
	public MemberValuePairRepository(List<MemberValuePair> pairs) {
		map = pairs.stream().reduce(new HashMap<String, MemberValuePair>(), (r, p) -> {
			r.put(p.getName().getIdentifier(), p);
			return r;
		}, (r1, r2) -> {
			r1.putAll(r2);
			return r1;
		});
	}
	
	public Option<MemberValuePair> findMember(String identifier) {
		return Option.of(map.get(identifier));
	}
	
	public List<MemberValuePair> findExcludeMember(String identifier) {
		return map.entrySet().stream().filter(e -> !e.getKey().equals(identifier)).map(Entry::getValue)
			.collect(Collectors.toList());
	}
}
