package shiva.cfg


import static shiva.cfg.Rules.*

import java.lang.reflect.Field
import java.lang.reflect.Type

import groovy.transform.CompileStatic
import shiva.cfg.ParseNode.ErrorInfo

@CompileStatic
class Grammar {
	
	public Grammar() {
		
	}
	
	// Set the names of the instance rules
	protected assignNamesToRules() {
		println this.class
		for (Field f in this.class.declaredFields) {
			try {
				Type t = f.type
				println("Field $f.name class ${t.name}")
				if (Rule.class.isAssignableFrom((Class) t)) {
					Rule r = f.get(this) as Rule
					println("Setting $f.name")
					r.setName(f.name)
				} else {
					println "Not assignable"
				}
			} catch (IllegalAccessException) {
				println "Exc for $f.name"
			}
		}
	}

	public ParseNode parse(Rule app, Document d) {
		app.setDescriptions()
		app.resetCache()
		ParseNode r = app.apply(d, 0);
		if (!r.matched) {
			ErrorInfo ei = r.errorInfo(d);
			
			System.err.println("Line: $ei.line Col: $ei.lineOffset Expected: ${ei.expect}  \
Found: ${d.getDiagnostic(ei.docOffset, 40)} ");
		} else {
			println(r)
		}
		return r;
	}
	
	static void main(String[] args) {
		Grammar g = new Grammar()
		Document doc = new StringDocument("a b  a b B y e")
//		Document doc = new StringDocument("")
		Rule a = lit("a").setName("A");
		Rule b = lit("b").setName("B")
		Rule ws = ws().setName("WS")
		Rule terminal = alt(a, b, ws).setName("Letter")
		Rule s = seq(plus(terminal), eof()).setName("START");
		g.parse(s, doc)
	}
	
}
