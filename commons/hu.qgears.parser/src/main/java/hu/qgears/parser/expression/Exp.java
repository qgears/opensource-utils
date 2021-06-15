package hu.qgears.parser.expression;

public class Exp {
	String rule = "(lower+plus)+lower";
	String id;

	public Exp(String rule, String id) {
		super();
		this.rule = rule;
		this.id = id;
	}
}
