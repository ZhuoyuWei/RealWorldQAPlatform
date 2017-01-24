package org.wzy.analyze;

import java.io.File;
import java.util.List;

import org.wzy.meta.Question;
import org.wzy.tool.CoreNLPTool;
import org.wzy.tool.IOTool;

public class StemAllQ {

	public String Stem(String str)
	{
		StringBuilder sb=new StringBuilder();
		
		String[] ss=CoreNLPTool.UniqueObject.StemProcessing(str);
		
		for(int i=0;i<ss.length;i++)
		{
			sb.append(ss[i]);
			sb.append(" ");
		}
		
		return sb.toString().trim().toLowerCase();
	}
	
	
	public void StemAll(List<Question> qList)
	{
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			q.question_content=Stem(q.question_content);
			for(int j=0;j<q.answers.length;j++)
			{
				q.answers[j]=Stem(q.answers[j]);
			}
		}
	}
	
	public void StemOneFile(String inputfile,String outputfile,String code)
	{
		List<Question> qList=IOTool.ReadSimpleQuestionsCVS(inputfile, code);
		StemAll(qList);
		IOTool.PrintSimpleQuestions(qList, outputfile, code);
	}
	
	public void StemDir(String dirname)
	{
		File dir=new File(dirname);
		if(dir.isDirectory())
		{
			File[] files=dir.listFiles();
			for(int i=0;i<files.length;i++)
			{
				StemOneFile(files[i].getPath(),files[i].getPath()+".stem","utf8");
			}
		}
	}
	
	public static void main(String[] args)
	{
		CoreNLPTool.CreateUniqueObject();
		CoreNLPTool.UniqueObject.InitTool("tokenize, ssplit, pos, lemma");
		
		StemAllQ saq=new StemAllQ();
		saq.StemDir("D:\\KBQA\\DataSet\\ck12_6000\\processed_5subjects\\en5subjects");
	}
	
}
