package org.wzy.pre.chinese;

import java.util.List;

import org.wzy.meta.*;
import org.wzy.tool.IOTool;

public class Change2CSV {

	public static void RemoveQuotation(List<Question> qList)
	{
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			q.question_content=q.question_content.replace("\"", "");//.replace("“", "").replace("”", "");
			for(int j=0;j<q.answers.length;j++)
			{
				q.answers[j]=q.answers[j].replace("\"", "");
			}
			q.questionID="wcb"+q.questionID;
		}
	}
	
	public static void main(String[] args)
	{
		List<Question> qList=IOTool.ReadXMLQuestion("D:\\KBQA\\DataSet\\MiddleSchoolBiology\\biology_2013_2016_withoutABCD.xml");
		RemoveQuotation(qList);
		IOTool.PrintSimpleQuestions(qList, "D:\\KBQA\\DataSet\\MiddleSchoolBiology\\biology_2013_2016_withoutABCD.csv", "utf8");
	}
	
}
