package org.wzy.pre;

import java.util.List;

import org.wzy.meta.Question;
import org.wzy.tool.IOTool;

public class IndexQA {

	
	public static void main(String[] args)
	{
		List<Question> qList=IOTool.ReadSimpleQuestionsCVS(args[0], "ascii");
		String label=args[1];
		for(int i=0;i<qList.size();i++)
		{
			qList.get(i).originalQuestionID=label+i;
			qList.get(i).questionID=label+i;
		}
		IOTool.PrintSimpleQuestions(qList, args[2], "ascii");
	}
}
