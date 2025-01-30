package hu.qgears.parser.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import hu.qgears.parser.test.contentassist.TestContentAssist;
import hu.qgears.parser.test.expression.TestExpression;
import hu.qgears.parser.test.expression.TestExpression2;
import hu.qgears.parser.test.math.TestEvaluateMathExpression;

@RunWith(Suite.class)
@SuiteClasses(value = {
		TestTokenizer.class, TestLanguageParser.class, TestBuildLanguage.class,
		TestContentAssist.class, TestExpression.class, TestExpression2.class,
		TestEvaluateMathExpression.class
})
public class AllTest {

}
