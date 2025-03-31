package hu.qgears.parser.test.contentassist;

import org.junit.Test;

import hu.qgears.parser.contentassist.ProjectContentAssistProcessor;

/** Test for content assist infrastructure. */
public class TestContentAssist {
	@Test
	public void test1()
	{
		DummyProposalContext dpc=new DummyProposalContext();
		ProjectContentAssistProcessor cap=new ProjectContentAssistProcessor();
		cap.computeCompletionProposals(dpc, "2+2+", 4);
	}
}
