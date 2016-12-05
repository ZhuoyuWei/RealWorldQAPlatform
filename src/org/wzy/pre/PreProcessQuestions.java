package org.wzy.pre;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.wzy.meta.Question;

public class PreProcessQuestions {

	public List<String[]> qList=new ArrayList<String[]>();
	public List<Question> questionList=new ArrayList<Question>();
	
	public void ReadQuestions(String filename) throws IOException
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
			String[] ss=buffer.split(",");
			qList.add(ss);
		}
		br.close();
		
		//debug
		for(int i=0;i<qList.get(0).length;i++)
		{
			System.out.println(qList.get(0)[i]);
		}
		
		
	}
	
	public List<Question> AI2ReadAndParsing(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		int line=0;
		List<Question> qList=new ArrayList<Question>();
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
			qList.add(q);
			
		}
		br.close();
		
		System.out.println("Question number:\t"+questionList.size());
		return qList;
	}
	
	
	
	public static void main(String[] args) throws IOException
	{
		//System.out.println("Console");
		PreProcessQuestions ppq=new PreProcessQuestions();
		//ppq.ReadQuestions("D:\\KBQA\\DataSet\\AI2-Elementary-NDMC-Feb2016\\Elementary-NDMC-Train.csv");
		ppq.AI2ReadAndParsing("D:\\KBQA\\DataSet\\AI2-Elementary-NDMC-Feb2016\\Elementary-NDMC-Train.csv");
	}
	
}
