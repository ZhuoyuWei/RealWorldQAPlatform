package org.wzy.pre;

import java.util.ArrayList;
import java.util.List;

import org.wzy.meta.Question;
import org.wzy.tool.CoreNLPTool;
import org.wzy.tool.IOTool;

public class PreProcessCK12QA {

	//int count=0;
	
	public String MoveLabel(String text)
	{
		//count++;
		String[] ss=text.split("[\\s]+");
		if(ss.length>0)
		{
			/*if(count>30000)
			{
				System.out.println("");
			}*/
			ss[0]=ss[0].replaceAll("[0-9a-zA-Z]+[\\)\\.]+", "");
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<ss.length;i++)
			{
				sb.append(ss[i]+"\t");
			}
			return sb.toString().trim().toLowerCase();
		}
		else
		{
			return text;
		}
	}
	
	public String SimpleChangeString(String s)
	{	
		s=MoveLabel(s);
		String[] ss=CoreNLPTool.UniqueObject.StemProcessing(s);
		StringBuilder sb=new StringBuilder();
		if(ss.length<=0)
		{
			//System.out.println(s);
			return "";
		}
		sb.append(ss[0]);
		for(int i=1;i<ss.length;i++)
		{
			sb.append(" ");
			sb.append(ss[i]);
		}
		return sb.toString();
	}
	
	public boolean PreQAPipeline(Question q)
	{
		boolean flag=false;
		q.question_content=q.question_content.replaceAll("[0-10]+\\)", "").trim();
		q.question_content=SimpleChangeString(q.question_content);
		flag|=q.question_content.equals("");
		
		for(int i=0;i<q.answers.length;i++)
		{
			q.answers[i]=q.answers[i].replaceAll("[a-g]+\\)", "").trim();
			q.answers[i]=SimpleChangeString(q.answers[i]);
			flag|=q.answers[i].equals("");
		}
		return flag;
	}
	
	public void StemQuestions(String inputfile,String outputfile)
	{
		List<Question> questionList=IOTool.ReadSimpleQuestionsCVS(inputfile, "utf8");
		CoreNLPTool.CreateUniqueObject();
		CoreNLPTool.UniqueObject.InitTool("tokenize, ssplit, pos, lemma");
		
		for(int i=0;i<questionList.size();i++)
		{
			Question question=questionList.get(i);
			question.question_content=SimpleChangeString(question.question_content);
			for(int j=0;j<question.answers.length;j++)
			{
				question.answers[j]=SimpleChangeString(question.answers[j]);
			}
		}
		
		IOTool.PrintSimpleQuestions(questionList, outputfile, "utf8");
	}
	
	public static void main(String[] args)
	{
		PreProcessCK12QA pp=new PreProcessCK12QA();
		pp.StemQuestions("D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\Exam01-MiddleSchool-NDMC-Dev.csv.simple"
				, "D:\\KBQA\\DataSet\\AI2_Large\\AI2LicensedScienceQuestions_NoDiagrams_All\\AI2LicensedScienceQuestions_NoDiagrams_All\\Exam01-MiddleSchool-NDMC-Dev.csv.simple.stem");
		
		/*List<Question> questionList=IOTool.ReadSimpleQuestionsCVS(args[0], "ascii");
		CoreNLPTool.CreateUniqueObject();
		PreProcessCK12QA pp=new PreProcessCK12QA();
		
		List<Question> yesList=new ArrayList<Question>();
		List<Question> noList=new ArrayList<Question>();
		
		for(int i=0;i<questionList.size();i++)
		{
			if(pp.PreQAPipeline(questionList.get(i)))
			{
				yesList.add(questionList.get(i));
			}
			else
			{
				noList.add(questionList.get(i));
			}
			
		}
		
		IOTool.PrintSimpleQuestions(yesList, args[1], "ascii");
		IOTool.PrintSimpleQuestions(noList, args[2], "ascii");
		IOTool.PrintSimpleQuestions(questionList, args[3], "ascii");*/
	}
	
}
