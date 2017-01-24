package org.wzy.analyze;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.wzy.meta.Question;
import org.wzy.tool.IOTool;

public class SplitTrainAndTest {

	
	
	public static List[] SimplestSplit(List<Question> qList)
	{
		List<Question> trainqList=new ArrayList<Question>();
		List<Question> testqList=new ArrayList<Question>();		
		List[] lists=new List[2];
		lists[0]=trainqList;
		lists[1]=testqList;
		
		for(int i=0;i<qList.size();i++)
		{
			if(i%2==0)
				lists[0].add(qList.get(i));
			else
				lists[1].add(qList.get(i));
		}
		
		return lists;
	}
	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{
		List<Question> qList=IOTool.ReadSimpleQuestionsCVS("D:\\KBQA\\DataSet\\ck12_6000\\processed_5subjects\\en5subjects\\physical-science_inf_all_1.14v.csv", "utf8");
		List[] lists=SimplestSplit(qList);
		IOTool.PrintSimpleQuestions(lists[0], "D:\\KBQA\\DataSet\\ck12_6000\\processed_5subjects\\en5subjects\\physical-science_inf_train_1.16v.csv", "utf8");
		IOTool.PrintSimpleQuestions(lists[1], "D:\\KBQA\\DataSet\\ck12_6000\\processed_5subjects\\en5subjects\\physical-science_inf_test_1.16v.csv", "utf8");		
	}
	
	
}
