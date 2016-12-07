package org.wzy.pre;

import java.util.ArrayList;
import java.util.List;

import org.wzy.meta.*;
import org.wzy.tool.IOTool;


public class FilterOutMoreThan4Candidate {

	
	public List<Question> goodList=new ArrayList<Question>();
	public List<Question> badList=new ArrayList<Question>();
	
	
	public void FilterQuestion(List<Question> ori_qList)
	{
		List<Question> res_qList=new ArrayList<Question>();
		
		for(int i=0;i<ori_qList.size();i++)
		{
			Question q=ori_qList.get(i);
			if(q.answers.length==4)
			{
				goodList.add(q);
			}
			else
			{
				badList.add(q);
			}
		}

	}
	
	
	public static void main(String[] args)
	{
		List<Question> ori_qList=IOTool.ReadSimpleQuestionsCVS("D:\\KBQA\\DataSet\\ck12_6000\\processed_5subjects\\physical-science\\physical-science_qa_11.30v.txt", "utf8");
		FilterOutMoreThan4Candidate fo=new FilterOutMoreThan4Candidate();
		
		fo.FilterQuestion(ori_qList);
		
		IOTool.PrintSimpleQuestions(fo.goodList,"D:\\KBQA\\DataSet\\ck12_6000\\processed_5subjects\\physical-science\\physical-science_qa_11.30v.4can", "utf8");
		IOTool.PrintSimpleQuestions(fo.badList,"D:\\KBQA\\DataSet\\ck12_6000\\processed_5subjects\\physical-science\\physical-science_qa_11.30v.no4can", "utf8");		
		
	}
}
