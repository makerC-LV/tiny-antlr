package shiva.cfg

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import java.util.regex.Matcher
import java.util.regex.Pattern

import static Rules.opt
import static Rules.ws

@CompileStatic
abstract public class Rule {
	
	public static boolean DEBUG
	
	
	private String name
	private boolean debug
	private String label

	protected boolean ignoreWhitespace
	
	private Map<Integer, ParseNode> cache = new HashMap<>()

	abstract protected ParseNode applyInternal(Document doc, int start)
	
	abstract protected Rule[] dependencies()
	
	abstract protected String descriptionFromDependencies()
	
	
	public Rule skipWs() {
		ignoreWhitespace = true;
		return this;
	}
	public String getName() {
		return name
	}
	
	public Rule setName(String s) {
		name = s;
		return this;
	}
	

	public ParseNode apply(Document doc, int start) {
		try {
			unguardedApply(start, doc);
		} catch (Exception e) {
			throw new RuntimeException("While applying ${getDescription()}", e)
		}
	}

	private ParseNode unguardedApply(int start, Document doc) {

		if (ignoreWhitespace) {
			start += skipWhitespace(doc, start)
		}
		if (debug || DEBUG) {
			println("Applying ${getDescription()} [start:$start] to: ..[${doc.getDiagnostic(start, 40)}]..")
		}
		ParseNode cached = cache.get(start);
		ParseNode res = cached == null ? applyInternal(doc, start) : cached;
		cache.put(start, res)

		if (debug || DEBUG) {
			println(res);
			if (res.matched) {
				int end = start + res.matchLength
				println "Matched $start-$end ..[" + doc.subseq(start, start + res.matchLength) + "].."
			}
		}
		if (res == null) {
			Thread.dumpStack()
		}

		return res
	}



	public String getDescription() {
		if (name != null) {
			return name
		}
		return label;
	}


	public Rule debug() {
		this.debug = true;
		return this;
	}

	
	protected ParseNode passNode(int start, int len, ParseNode... children) {
		ParseNode res = new ParseNode(this, true, start, len)
		children.each { r -> res.children.add(r) }
		return res
	}

	protected ParseNode failNode(int start, ParseNode...children) {
		ParseNode res = new ParseNode(this, false, start)
		children.each { r -> res.children.add(r) }
		return res
	}

	protected int skipWhitespace(Document d, int start) {
		int index = start
		while (index < d.length()) {
			if (!Character.isWhitespace(d.charAt(index))) {
				return index - start;
			}
			index++
		}
		return index - start
		
	}
	
	public void setDescriptions() {
		Set<Rule> visited = new HashSet<>()
		assignDescriptionDfs(visited);
	}
	
	protected void assignDescriptionDfs(Set<Rule> visited) {
		if (visited.contains(this)) {
			if (getDescription() == null) {
				label = getClass().getName().split("\\.").last()
				System.out.println("AssignDescriptionDfs: After assigning default name: $label")
			}
			return
		} else {
			visited.add(this)
			dependencies().each { r -> r.assignDescriptionDfs(visited) }
			if (getDescription() == null) {
				label = descriptionFromDependencies()
			}
		}
	}
	
	public void resetCache() {
		
		Set<Rule> visited = new HashSet<>()
		resetCacheDfs(visited);
	}

	protected void resetCacheDfs(HashSet<Rule> visited) {
		if (visited.contains(this)) {
			return
		}
		cache.clear()
		visited.add(this)
		dependencies().each { r -> resetCacheDfs(visited) }
	}
	
	public void skipWsRecursive() {
		Set<Rule> visited = new HashSet<>()
		skipWsDfs(visited);
	}

	protected void skipWsDfs(HashSet<Rule> visited) {
		if (visited.contains(this)) {
			return
		}
		skipWs()
		visited.add(this)
		dependencies().each { r -> r.skipWsDfs(visited) }
	}
}
