package org.wzy.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.wzy.meta.Question;
import org.wzy.tool.IOTool;
import org.wzy.tool.ParserTool;

public class PrintAllDependencyTrees {

	
	public List<Question> questionList=new ArrayList<Question>();
	
	
	public void ReadAndParsing(String filename) throws IOException
	{
		//BufferedReader br=new BufferedReader(new FileReader(filename));
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf8"));
		String buffer=null;
		int line=0;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			if(line==0)
			{
				line++;
				continue;
			}
			
			Question q=new Question();
			q.AI2ParsingString(buffer);
			questionList.add(q);
			
		}
		br.close();
		
		System.out.println("Question number:\t"+questionList.size());
	}
	
	public void DeParsingAll()
	{
		for(int i=0;i<questionList.size();i++)
		{
			questionList.get(i).DependencyParsing();
		}
	}
	
	public void PrintDeQuestion(String filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintStream ps=new PrintStream(filename,"utf8");
		for(int i=0;i<questionList.size();i++)
		{
			IOTool.PrintOneDeParsedQuestion(questionList.get(i), ps, "\n");
		}
		ps.close();
		
	}
	
	public static void main(String[] args) throws IOException
	{
		PrintAllDependencyTrees pdt=new PrintAllDependencyTrees();
		pdt.ReadAndParsing("D:\\KBQA\\DataSet\\AI2-8thGr-NDMC-Feb2016\\8thGr-NDMC-Train.csv");
		ParserTool.CreateUniqueObject();
		long start=System.currentTimeMillis();
		pdt.DeParsingAll();
		long end=System.currentTimeMillis();
		System.out.println("Parsing all takes "+(end-start)+" ms");
		pdt.PrintDeQuestion("D:\\KBQA\\DataSet\\AI2-8thGr-NDMC-Feb2016\\8thGr-NDMC-Train.out");
		
	}
}
