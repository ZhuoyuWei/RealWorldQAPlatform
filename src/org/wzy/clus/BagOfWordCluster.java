package org.wzy.clus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wzy.meta.Question;
import org.wzy.tool.CoreNLPTool;
import org.wzy.tool.IOTool;
import org.wzy.tool.StringTool;

import edu.stanford.nlp.util.CoreMap;

public class BagOfWordCluster implements QAClusterInter{

	
	
	public Map<String,Integer> word2index;
	public List<String> index2word;
	
	
	public void CollectWordTable(List<Question> questionList)
	{
		word2index=new HashMap<String,Integer>();
		index2word=new ArrayList<String>();
		for(int i=0;i<questionList.size();i++)
		{
			Question q=questionList.get(i);
			if(q.question_stan_sentences==null)
			{
				q.CoreNLPProcessing();
			}
			//for question's content
			List<String> lemmaList=CoreNLPTool.UniqueObject.GetAllLemmasFromCoreMapList(q.question_stan_sentences);
			//for answers' contents
			for(int j=0;j<q.answers.length;j++)
			{
				List<String> tmpList=CoreNLPTool.UniqueObject.GetAllLemmasFromCoreMapList(q.answer_stan_sentences[j]);
				lemmaList.addAll(tmpList);
			}
			
			for(int j=0;j<lemmaList.size();j++)
			{
				String tmp=lemmaList.get(j).toLowerCase().trim();
				if(tmp.length()<2)
					continue;
				Integer index=word2index.get(lemmaList.get(j));
				if(index==null)
				{
					word2index.put(lemmaList.get(j),word2index.size());
					index2word.add(lemmaList.get(j));
				}
			}
		}
	}
	
	public void PrintWordListToFile(String filename,String code) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,code);
		for(int i=0;i<index2word.size();i++)
		{
			pw.println(i+"\t"+index2word.get(i));
		}
		pw.flush();
		pw.close();
	}
	
	public void ReadWordListFromFile(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		word2index=new HashMap<String,Integer>();
		index2word=new ArrayList<String>();
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			word2index.put(ss[1], Integer.parseInt(ss[0]));
			index2word.add(ss[1]);
		}
		br.close();
	}
	
	@Override
	public void Clustering(List<Question> questionList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ProduceClusterFeatures(List<Question> questionList) {
		// TODO Auto-generated method stub
		
		for(int i=0;i<questionList.size();i++)
		{
			Question q=questionList.get(i);
			if(q.question_stan_sentences==null)
			{
				q.CoreNLPProcessing();
			}
			//for question's content
			List<String> lemmaList=CoreNLPTool.UniqueObject.GetAllLemmasFromCoreMapList(q.question_stan_sentences);
			//for answers' contents
			for(int j=0;j<q.answers.length;j++)
			{
				List<String> tmpList=CoreNLPTool.UniqueObject.GetAllLemmasFromCoreMapList(q.answer_stan_sentences[j]);
				lemmaList.addAll(tmpList);
			}
			
			List<Integer> tokenindexList=new ArrayList<Integer>();
			for(int j=0;j<lemmaList.size();j++)
			{
				String tmp=lemmaList.get(j).toLowerCase().trim();
				if(tmp.length()<2)
					continue;
				Integer index=word2index.get(lemmaList.get(j));
				if(index!=null)
				{
					tokenindexList.add(index);
				}
			}
			Collections.sort(tokenindexList);
			

			StringBuilder sb=new StringBuilder();
			sb.append("{");			
			for(int j=0;j<tokenindexList.size();)
			{
				int count=1;
				int index=tokenindexList.get(j);
				int k=j+1;
				for(;k<tokenindexList.size();k++)
				{
					if(index==tokenindexList.get(k))
					{
						count++;
					}
					else
					{
						break;
					}
				}
				j=k;
				
				if(j>=tokenindexList.size())
				{
					sb.append(index+" "+count);
				}
				else
				{
					sb.append(index+" "+count).append(",");
				}
				
			}
			sb.append("}");
			q.bagofword_feature_string=sb.toString();
			
		}
		
	}



	@Override
	public void PreProcessingQuestions(List<Question> qList) {
		// TODO Auto-generated method stub
		for(int i=0;i<qList.size();i++)
		{
			Question q=qList.get(i);
			
			//for question's content
			String question_content=q.question_content;
			//replace question tab at the beginning: 1 .
			String[] ss=question_content.split("[\\s]+");
			if(ss.length>2&&ss[0].matches("[0-9]+")&&(ss[1].equals(".")||ss[1].equals("-rrb-")))
			{
				question_content=StringTool.JoinStrings(ss, 2, " ");
			}
			//replace space line
			question_content=question_content.replaceAll("[_]+", "what");
			//replace number
			question_content=question_content.replaceAll("-?[0-9]+[\\.,]?+[0-9]*", "value");
			//replace / to divide
			question_content=question_content.replaceAll("/", " divide ");
			q.question_content=question_content;
			
			//for answers' contents
			
			for(int j=0;j<q.answers.length;j++)
			{
				String answer=q.answers[j];
				
				//remove label
				
				answer=answer.replaceAll("-[rltb]{3}-[\\s]+", "");
				
				//replace number
				answer=answer.replaceAll("-?[0-9]+[\\.,]?+[0-9]*", "value");
				
				//replace / to divide
				answer=answer.replaceAll("/", " divide ");
				
				q.answers[j]=answer;
			}
		}
	}
	
	public void PrintFeatureString(List<Question> qList,String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		for(int i=0;i<qList.size();i++)
		{
			ps.println(qList.get(i).bagofword_feature_string);
		}
		ps.close();
	}

	
	public void PrintFileHead(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		ps.println("@RELATION qa");
		ps.println();
		for(int i=0;i<index2word.size();i++)
		{
			ps.println("@ATTRIBUTE "+index2word.get(i)+"  NUMERIC");
		}
		ps.println();
		ps.println("@DATA");
		ps.println();
		ps.close();
	}
	
	public static void AppendArffHead(String[] args) throws IOException
	{
		BagOfWordCluster bwc=new BagOfWordCluster();
		bwc.ReadWordListFromFile("D:\\KBQA\\DataSet\\ck12_6000\\lemmaList_12.06v", "utf8");
		bwc.PrintFileHead("D:\\KBQA\\DataSet\\ck12_6000\\arff_head_12.06v");
	}
	
	public static void main(String[] args) throws IOException
	{
		/*CoreNLPTool.CreateUniqueObject();
		CoreNLPTool.UniqueObject.InitTool("tokenize, ssplit, pos, lemma");
		BagOfWordCluster bwc=new BagOfWordCluster();
		List<Question> qList=IOTool.ReadSimpleQuestionsCVS("D:\\KBQA\\DataSet\\ck12_6000\\mcqa_12.06v.4can", "utf8");
		bwc.PreProcessingQuestions(qList);
		bwc.CollectWordTable(qList);
		bwc.PrintWordListToFile("D:\\KBQA\\DataSet\\ck12_6000\\lemmaList_12.06v", "utf8");
		bwc.ProduceClusterFeatures(qList);
		bwc.PrintFeatureString(qList, "D:\\KBQA\\DataSet\\ck12_6000\\bagofword_feature_12.06v.arff");*/
		AppendArffHead(args);
	}
}
