package org.wzy.tool;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.trees.GrammaticalStructure;
import org.wzy.meta.Question;

public class IOTool {

	public static void PrintOneDeParsedQuestion(Question q,PrintStream ps,String symple)
	{
		ps.print(q.questionID+symple);
		ps.print(q.question_content+symple);
		PrintOneGraList(q.question_de_structure,ps,symple);
		ps.print(symple);
		for(int i=0;i<q.answer_de_structures.length;i++)
		{
			PrintOneGraList(q.answer_de_structures[i],ps,symple);
			ps.print(symple);
		}
		ps.println();
	}
	
	public static void PrintOneGraList(List<GrammaticalStructure> gList,PrintStream ps,String symple)
	{
		for(int i=0;i<gList.size();i++)
		{
			ps.print(gList.get(i).typedDependencies()+symple);
		}
	}
	
	public static void PrintSimpleQuestions(List<Question> qList,PrintStream ps)
	{
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			if(q.questionID==null||q.equals(""))
			{
				ps.print(q.originalQuestionID+",");
			}
			else
			{
				ps.print(q.questionID+",");
			}
			ps.print("\""+q.question_content+"\",");
			for(int j=0;j<q.answers.length;j++)
			{
				ps.print("\""+q.answers[j]+"\",");
			}
			ps.println(q.AnswerKey);
		}
	}
	
	public static void PrintSimpleQuestions(List<Question> qList,String filename,String code)
	{
		PrintWriter pw=null;
		try {
			pw = new PrintWriter(filename,code);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			if(q.questionID==null||q.equals(""))
			{
				pw.print(q.originalQuestionID+",");
			}
			else
			{
				pw.print(q.questionID+",");
			}
			pw.print("\""+q.question_content+"\",");
			for(int j=0;j<q.answers.length;j++)
			{
				pw.print("\""+q.answers[j]+"\",");
			}
			pw.println(q.AnswerKey);
		}
		pw.close();
	}	
	
	public static List<Question> ReadSimpleQuestionsCVS(String filename,String code)
	{
		List<Question> qList=new ArrayList<Question>();
		String buffer=null;
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
			
			while((buffer=br.readLine())!=null)
			{
				Question q=new Question();
				String[] ss=buffer.split(",");
				q.questionID=ss[0];
				q.AnswerKey=Integer.parseInt(ss[ss.length-1]);
				
				String pattern="\"(.*?)\"";
				Pattern p=Pattern.compile(pattern);
				Matcher matcher=p.matcher(buffer);
				List<String> tmpList=new ArrayList<String>();
				while(matcher.find())
				{
					String context=matcher.group(1);
					tmpList.add(context);
				}
				
				q.question_content=tmpList.get(0);
				q.answers=new String[tmpList.size()-1];
				for(int i=0;i<q.answers.length;i++)
				{
					q.answers[i]=tmpList.get(i+1);
				}
				
				qList.add(q);
				
			}
			br.close();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			System.out.println(buffer);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return qList;
	}
	
}
