package org.wzy.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import edu.stanford.nlp.trees.GrammaticalStructure;

import org.wzy.meta.ConceptPath;
import org.wzy.meta.Question;
import org.wzy.tool.XMLHistoryQAParser;
import org.xml.sax.SAXException;

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
			PrintOneSimpleQuestion(qList.get(i),pw);
		}
		pw.close();
	}	
	

	
	public static void PrintOneSimpleQuestion(Question q,PrintWriter pw)
	{
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
	
	public static Map<String,String> ReadEntityMidtoName(String filename,String code)
	{
		Map<String,String> map=new HashMap<String,String>();
		String buffer=null;
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
			
			while((buffer=br.readLine())!=null)
			{
				String[] ss=buffer.split("\t");
				if(ss.length!=2)
					continue;
				map.put(ss[0], ss[1].toLowerCase());
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
		
		return map;
	}
	
	public static Map<String,String> ReadEntityNametoMid(String filename,String code)
	{
		Map<String,String> map=new HashMap<String,String>();
		String buffer=null;
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
			
			while((buffer=br.readLine())!=null)
			{
				String[] ss=buffer.split("\t");
				if(ss.length!=2)
					continue;
				map.put(ss[1].toLowerCase(),ss[0]);
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
		
		return map;
	}	
	
	
	public static List<String> ReadLargeStringList(String filename,String code,int index,int defaultsize)
	{
		List<String> strList=new ArrayList<String>(defaultsize);
		
		try {
			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				String[] ss=buffer.split("\t");
				if(ss.length!=2)
					continue;
				strList.add(ss[index]);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.err.println("total string size is "+strList.size());
		
		return strList;
	}
	
	public static void PrintDoubleMatrix(double[][] matrix,String filename,int precision)
	{
		String format="%."+precision+"f";
		try {
			PrintStream ps=new PrintStream(filename);
			for(int i=0;i<matrix.length;i++)
			{
				for(int j=0;j<matrix[i].length;j++)
				{
					if(j==matrix[i].length-1)
					{
						ps.println(String.format(format, matrix[i][j]));
					}
					else
					{
						ps.print(String.format(format, matrix[i][j])+"\t");
					}
				}
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static double[][] ReadDoubleMatrix(String filename,int defaultsize,int dim)
	{
		List<double[]> matrix=new ArrayList<double[]>(defaultsize);
		int errcount=0;
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				String[] ss=buffer.split("\t");
				if(ss.length!=dim)
				{
					errcount++;
				}
				else
				{
					double[] row=new double[dim];
					for(int i=0;i<ss.length;i++)
					{
						row[i]=Double.parseDouble(ss[i]);
					}
					matrix.add(row);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return matrix.toArray(new double[0][]);
	}
	
	public static void PrintSimpleQuestionsWithConceptPaths(List<Question> qList,String filename,String code)
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
			pw.print(q.AnswerKey);
			
			if(q.ans_paths!=null)
			{
				pw.print(",");
				for(int j=0;j<q.ans_paths.length;j++)
				{
					pw.print(q.ans_paths[j].length);
					for(int k=0;k<q.ans_paths[j].length;k++)
					{
						pw.print(" ");
						ConceptPath cpath=q.ans_paths[j][k];
						
						for(int h=0;h<cpath.relationList.size();h++)
						{
							if(h==0)
								pw.print(cpath.relationList.get(h));
							else
								pw.print("_"+cpath.relationList.get(h));
						}
					}
					if(j!=q.ans_paths.length-1)
						pw.print("\t");
				}
			}
			pw.println();
			
			
		}
		pw.close();
	}	
	
	public static void PrintSimpleQuestionsWithConceptPathsWithEntities(List<Question> qList,List<String> relList,String filename,String code)
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
			pw.print(q.AnswerKey);
			
			if(q.ans_paths!=null)
			{
				pw.print(",");
				for(int j=0;j<q.ans_paths.length;j++)
				{
					pw.print(q.ans_paths[j].length);
					for(int k=0;k<q.ans_paths[j].length;k++)
					{
						pw.print(" ");
						ConceptPath cpath=q.ans_paths[j][k];
						
						for(int h=0;h<cpath.relationList.size();h++)
						{
							if(h==0)
								pw.print(relList.get(cpath.relationList.get(h)));
							else
								pw.print("|"+relList.get(cpath.relationList.get(h)));
						}
					}
					if(j!=q.ans_paths.length-1)
						pw.print("\t");
				}
			}
			
			if(q.qlinkList!=null)
			{
				pw.print(","+q.qlinkList.size()+"\t");
				for(int j=0;j<q.qlinkList.size();j++)
				{
					if(j==q.qlinkList.size()-1)
					{
						pw.print(q.qlinkList.get(j).surface_name.replaceAll("[\\s]+", "_"));
					}
					else
					{
						pw.print(q.qlinkList.get(j).surface_name.replaceAll("[\\s]+", "_")+" ");						
					}
				}
			}
			if(q.alinkLists!=null)
			{
				pw.print(","+q.alinkLists.length);
				for(int j=0;j<q.alinkLists.length;j++)
				{
					pw.print("\t"+q.alinkLists[j].size());
					for(int k=0;k<q.alinkLists[j].size();k++)
					{
						pw.print(" "+q.alinkLists[j].get(k).surface_name.replaceAll("[\\s]+", "_"));
					}
				}
			}
			
			pw.println();
			
			
		}
		pw.close();
	}	

	
	public static List<Question> ReadSimpleQuestionsCVSWithConceptPaths(String filename,String code)
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
				q.AnswerKey=Integer.parseInt(ss[ss.length-2]);
				
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
				
				String pathStr=ss[ss.length-1];
				String[] anspaths_str=pathStr.split("\t");
				if(anspaths_str.length!=q.answers.length)
				{
					System.err.println("error occurs when read qa concept paths");
					System.exit(-1);
				}
				q.ans_paths=new ConceptPath[anspaths_str.length][];
				for(int i=0;i<q.ans_paths.length;i++)
				{
					String[] path_strs=anspaths_str[i].split(" ");
					int pathnum=Integer.parseInt(path_strs[0]);
					if(pathnum!=path_strs.length-1)
					{
						System.err.println("error occurs when read concept paths for one question answer pair");
						System.exit(-1);
					}
					q.ans_paths[i]=new ConceptPath[pathnum];
					for(int j=0;j<pathnum;j++)
					{
						q.ans_paths[i][j]=new ConceptPath();
						String[] rels=path_strs[j+1].split("_");
						for(int k=0;k<rels.length;k++)
						{
							q.ans_paths[i][j].relationList.add(Integer.parseInt(rels[k]));
						}
					}
				}
				
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
	
	public static void PrintWrongQuestionIdWithPredict(List<Question> qList,String filename,String code) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,code);
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			if(q.AnswerKey==q.predict_answer)
				continue;
			pw.println(q.questionID+"\t"+q.AnswerKey+"\t"+q.predict_answer);
		}
		pw.close();
	}
	
	public static void PrintQuestionSnippetFromLucene(List<Question> qList,String filename,String code) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,code);
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			pw.println(q.questionID);
			for(int j=0;j<q.paragraphs.length;j++)
			{
				pw.println(q.paragraphs[j]);
			}
		}
		pw.close();
	}	
	
	
	public static List<Question> ReadXMLQuestion(String filename)
	{
		 SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		 XMLHistoryQAParser xmlParser=new XMLHistoryQAParser(); 
         try {
			SAXParser saxParser = saxParserFactory.newSAXParser();
			InputStream inputStream = new FileInputStream(new File(filename)); 
			
			saxParser.parse(inputStream, xmlParser);
			
         } catch (ParserConfigurationException | SAXException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
         } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
         }  
         return xmlParser.qList;
	}
	
	public static void PrintQuestionsPredictScores(List<Question> qList,String filename,String code) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,code);
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			pw.print(q.questionID);
			if(q.scores!=null)
				for(int j=0;j<q.scores.length;j++)
				{
					pw.print("\t"+q.scores[j]);
				}
			else
				for(int j=0;j<4;j++)
				{
					pw.print("\t"+0);					
				}
			pw.println();
		}
		pw.close();
	}
	
	public static void Norming(double[] scores)
	{
		double max=0.;
		double min=Double.MAX_VALUE;
		for(int i=0;i<scores.length;i++)
		{
			if(max<scores[i])
			{
				max=scores[i];
			}
			if(min>scores[i])
			{
				min=scores[i];
			}
		}
		
		double tmp=max-min;
		
		
		
		for(int i=0;i<scores.length;i++)
		{
			scores[i]-=min;
			if(tmp>1e-2)
				scores[i]/=tmp;
		}
		
		
	}
	
	public static void PrintQuestionsPredictScoresNormed(List<Question> qList,String filename,String code) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,code);
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			pw.print(q.questionID);
			if(q.scores!=null)
			{
				Norming(q.scores);
				for(int j=0;j<q.scores.length;j++)
				{
					pw.print("\t"+q.scores[j]);
				}
			}
			else
			{
				for(int j=0;j<4;j++)
				{
					pw.print("\t"+0);					
				}
			}
			pw.println();
		}
		pw.close();
	}	
	
	
	public static void RandomPickWrongQuestionsAndPrint(List<Question> testList,List<Question> trainList, double rate,String trainfile,String testfile,String code,boolean conceptPath)
	{
		int num=(int)(testList.size()*rate);
		RandomPickWrongQuestionsAndPrint(testList,trainList,num,trainfile,testfile,code,conceptPath);
	}
	
	public static void RandomPickWrongQuestionsAndPrint(List<Question> testList,List<Question> trainList, int num,String trainfile,String testfile,String code,boolean conceptPath)
	{

		List<Integer> wrongList=new ArrayList<Integer>();
		Random rand=new Random();
		for(int i=0;i<testList.size();i++)
		{
			int maxindex=0;
			Question q=testList.get(i);
			for(int j=1;j<q.answers.length;j++)
			{
				if(q.scores[maxindex]<q.scores[j])
				{
					maxindex=j;
				}
			}
			if(maxindex!=q.AnswerKey)
			{
				wrongList.add(i);
			}
		}
		
		Set<Integer> randSet=new HashSet<Integer>();
		
		if(num>wrongList.size()/3)
		{
			System.err.println("wrong questions are too many");
		}
		while(randSet.size()<num)
		{
			int randindex=rand.nextInt(wrongList.size());
			randSet.add(wrongList.get(randindex));
		}
		
		List<Question> leftTest=new ArrayList<Question>();
		List<Question> bechangedTest=new ArrayList<Question>();
		for(int i=0;i<testList.size();i++)
		{
			if(randSet.contains(i))
			{
				bechangedTest.add(testList.get(i));
			}
			else
			{
				leftTest.add(testList.get(i));
			}
		}
		
		testList=leftTest;
		trainList.addAll(bechangedTest);
		
		if(conceptPath)
		{
			IOTool.PrintSimpleQuestionsWithConceptPaths(trainList, trainfile, code);
			IOTool.PrintSimpleQuestionsWithConceptPaths(testList, testfile, code);
		}
		else
		{
			IOTool.PrintSimpleQuestions(trainList, trainfile, code);
			IOTool.PrintSimpleQuestions(testList, testfile, code);			
		}
	}
	
	
}
