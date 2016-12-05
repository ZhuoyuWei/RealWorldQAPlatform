package org.wzy.kb.fb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wzy.meta.*;
import org.wzy.tool.IOTool;

public class QALinkToFB {
	
	
	public int link_question_count=0;
	public int link_answer_count=0;
	public int link_qa_count=0;
	public int link_question_count_1=0;
	public int link_answer_count_1=0;
	public int link_qa_count_1=0;	
	
	public boolean filter=true;
	
	
	public Map<String,String> name2mid=new HashMap<String,String>();
	//public Set<String> nameset=new HashSet<String>();
	public Map<String,String> nameMap=new HashMap<String,String>();
	
	public void ReadFBNamesFromFile(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("[\\s]+");
			if(ss.length==2)
			{
				name2mid.put(ss[1].toLowerCase(), ss[0]);
			}
		}
		br.close();
	}
	
	public void ReadWikiTitleFromFile(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			name2mid.put(buffer, "WikiTitle");
		}
		br.close();
	}
	public void ReadWikiCateFromFile(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			name2mid.put(buffer, "WikiTitle");
		}
		br.close();
	}	
	
	public List<String> SimpleLinkForString(String str,int minlength,int maxlength)
	{
		List<String> list=new ArrayList<String>();
		String[] ss=str.split("[\\s]+");
		
		for(int i=minlength;i<=maxlength;i++)
		{
			for(int j=0;j<ss.length-i;j++)
			{
				StringBuilder sb=new StringBuilder();
				sb.append(ss[j]);
				for(int k=1;k<i;k++)
				{
					sb.append("_");
					sb.append(ss[j+k]);
				}
				String tmp=sb.toString().toLowerCase();
				String mid=name2mid.get(tmp);
				if(mid!=null)
				{
					list.add(tmp);
					//nameset.add(tmp);
					if(nameMap.get(tmp)==null)
						nameMap.put(tmp,mid);
				}
			}
		}
		
		return list;
	}
	
	public int SimpleLinkingOneQuestion(Question q,int minlength,int maxlength)
	{
		boolean flag=false;
		
		//divide questions into for groups
		//0 means q and a has no entity in KB.
		//1 means q has entities but a has not.
		//2 means a has entities but q has not.
		//3 means q and a have entities in KB.
		int state=0;
		
		//for question content
		List<String> question_linking_list=SimpleLinkForString(q.question_content,minlength,maxlength);
		if(question_linking_list.size()>0)
		{
			flag=true;
			link_question_count+=question_linking_list.size();
			link_question_count_1++;
			state=1;
			
		}
		
		//for answers
		List<String> answer_linking_list=new ArrayList<String>();
		for(int i=0;i<q.answers.length;i++)
		{
			answer_linking_list.addAll(SimpleLinkForString(q.answers[i],minlength,maxlength));
		}
		if(answer_linking_list.size()>0)
		{
			//flag=true;
			link_answer_count+=answer_linking_list.size();
			link_answer_count_1++;
			if(flag)
			{
				link_qa_count+=question_linking_list.size()+answer_linking_list.size();
				link_qa_count_1++;
				state=3;
			}
			else
			{
				state=2;
			}
		}
		
		return state;

	}
	
	public void SimpleLinking(List<Question> qList,int minlength,int maxlength)
	{
		for(int i=0;i<qList.size();i++)
		{
			SimpleLinkingOneQuestion(qList.get(i),minlength,maxlength);
		}
	}
	
	public List<Question>[] SimpleLinkingAndSave(List<Question> qList,int minlength,int maxlength)
	{
		List<Question>[] dividedQuestions=new List[4];
		for(int i=0;i<4;i++)
			dividedQuestions[i]=new ArrayList<Question>();
		for(int i=0;i<qList.size();i++)
		{
			int state=SimpleLinkingOneQuestion(qList.get(i),minlength,maxlength);
			dividedQuestions[state].add(qList.get(i));
		}		
		return dividedQuestions;
	}
	
	public void PrintNameMap(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		Iterator it=nameMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			ps.println(entry.getKey()+"\t"+entry.getValue());
		}
		ps.close();
	}
	
	
	
	//public void SplitDataSet()
	
	public void FilterEntityMap()
	{
		Map<String,String> tmpMap=nameMap;
		nameMap=new HashMap<String,String>();
		Iterator it=tmpMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			String key=(String)entry.getKey();
			if(key.length()<2)
				continue;
			nameMap.put(key, (String)entry.getValue());
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		QALinkToFB qafb=new QALinkToFB();
		long start=System.currentTimeMillis();
		qafb.ReadFBNamesFromFile(args[0], args[1]);
		long end=System.currentTimeMillis();
		System.out.println("map size: "+qafb.name2mid.size()+" in "+(end-start)+" ms");
		List<Question> questionList=IOTool.ReadSimpleQuestionsCVS(args[2], args[1]);
		List<Question>[] divqLists=qafb.SimpleLinkingAndSave(questionList, Integer.parseInt(args[3]), Integer.parseInt(args[4]));
		/*String divdir=args[5];
		for(int i=0;i<4;i++)
		{
			IOTool.PrintSimpleQuestions(divqLists[i], divdir+i, args[1]);
		}*/
		
		System.out.println("results: ");
		System.out.println("q num: "+questionList.size());
		System.out.println(qafb.link_question_count_1);
		System.out.println(qafb.link_answer_count_1);
		System.out.println(qafb.link_qa_count_1);
		System.out.println("rate: ");
		System.out.println((double)qafb.link_question_count/(double)questionList.size());
		System.out.println((double)qafb.link_answer_count/(double)questionList.size());		
		System.out.println((double)qafb.link_qa_count/(double)questionList.size());		
		
		//qafb.PrintNameSet(args[5]);
		if(qafb.filter)
		{
			qafb.FilterEntityMap();
		}
		qafb.PrintNameMap(args[5]);
	}
}
