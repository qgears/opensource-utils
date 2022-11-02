package hu.qgears.parser.math;

import hu.qgears.parser.IParser;
import hu.qgears.parser.ITreeElem;
import hu.qgears.parser.LanguageHelper;
import hu.qgears.parser.ParserLogger;
import hu.qgears.parser.impl.DefaultReceiver;

/**
 * API for evaluating mathematical expressions specified as a String.
 * @author agostoni
 *
 */
public class EvaluateMathExpression {

	protected IVariableResolver vars;
	
	@FunctionalInterface
	protected interface Operation {
		double op(double a, double b);
	}
	
	/**
	 * 
	 * @param vars the variable resolver. Might be <code>null</code> if the expressions do not contain variables.
	 */
	public EvaluateMathExpression(IVariableResolver vars) {
		super();
		this.vars = vars;
	}

	public static ITreeElem parse(String expression) throws Exception {
		IParser p = LanguageHelper.createParser(MathExpressionLanguage.getInstance().getLang(),expression,new ParserLogger());
		ITreeElem tree = p.parse(new DefaultReceiver());
		return tree;
	}
	
	/**
	 * Parses the given String as mathematical expressions, evaluates it, and returns the results as number. 
	 * 
	 * @param expression A single math expression to evaluate.
	 * @return
	 * @throws Exception
	 */
	public double evaluate(String expression) throws Exception {
		ITreeElem tree = parse(expression);
		if (tree.getSubs().size() == 1) {
			return evalTree(tree.getSubs().get(0));
		} else {
			throw new Exception("Exactly one expression is expected");
		}
	}
	
	protected double evalTree(ITreeElem tree) throws Exception {
	
		switch (tree.getTypeName()) {
		case "constInt":
			return Long.valueOf( tree.getString());
		case "constDoubleNumber":
			return Double.valueOf( tree.getString());
		case "expAdd":
			return handleOperation(tree, (a,b) -> a+b);
		case "expMul":
			return handleOperation(tree, (a,b) -> a*b);
		case "expDiv":
			return handleOperation(tree, (a,b) -> a/b);
		case "expSub":
			return handleOperation(tree, (a,b) -> a-b);
		case "bracketed":
			return handleBracket(tree);
		case "variable":
			return handleVariable(tree);
		default:
			throw new Exception("Unkown operand or term "+tree.getTypeName());
		}
	}


	protected double handleVariable(ITreeElem tree) throws Exception {
		if (vars == null) {
			throw new Exception("Variable resolver not specified, cannot resolve variable "+tree.getString() + " at "+tree);
		}
		Double ret = vars.resolveVar(tree.getString());
		if (ret == null) {
			throw new Exception("Undefined variable "+tree.getString() + " at "+tree);
		}
		return ret.doubleValue();
	}


	protected double handleBracket(ITreeElem tree) throws Exception {
		if (tree.getSubs().size() == 0) {
			throw new Exception("Empty bracket at "+tree.toString());
		} else if (tree.getSubs().size() == 1) {
			return evalTree(tree.getSubs().get(0));
		} else {
			throw new Exception("Single bracketed expression is expected at "+tree.toString());
		}
	}
	
	protected double handleOperation(ITreeElem tree,Operation o) throws Exception {
		if (tree.getSubs().size() == 2) {
			double a = evalTree(tree.getSubs().get(0));
			double b = evalTree(tree.getSubs().get(1));
			return o.op(a, b);
		} else {
			throw new Exception("Only two operand expected at "+tree);
		}
	}
	
}
