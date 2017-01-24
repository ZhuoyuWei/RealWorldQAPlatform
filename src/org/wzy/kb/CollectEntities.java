package org.wzy.kb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectEntities {

	
	public Map<String,Integer> entity2id=new HashMap<String,Integer>();
	public List<String> id2entity=new ArrayList<String>();
	
	public Map<String,Integer> relation2id=new HashMap<String,Integer>();
	public List<String> id2relation=new ArrayList<String>();
	
	public void FilterOutNotTriplet(String inputfile,String outputfile,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		String buffer=null;
		PrintWriter pw=new PrintWriter(outputfile,code);
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length==3)
			{
				pw.println(buffer);
			}
		}
		pw.flush();
		pw.close();
		br.close();
	}
	
	public void ReadEntitiesFromFactFile(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			
			if(ss.length!=3)
			{
				System.out.println(buffer);
				continue;
			}
			
			Integer index=entity2id.get(ss[0]);
			if(index==null)
			{
				entity2id.put(ss[0], id2entity.size());
				id2entity.add(ss[0]);
			}
			
			
			index=relation2id.get(ss[1]);
			if(index==null)
			{
				relation2id.put(ss[1], id2relation.size());
				id2relation.add(ss[1]);
			}
			
			index=entity2id.get(ss[2]);
			if(index==null)
			{
				entity2id.put(ss[2], id2entity.size());
				id2entity.add(ss[2]);
			}
			
		}
		
		br.close();
		
	}
	
	public void PrintList(String filename, String code,List<String> strList) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,code);
		for(int i=0;i<strList.size();i++)
		{
			pw.println(strList.get(i)+"\t"+i);
		}
		pw.flush();
		pw.close();
	}
	
	public void ReIndexFacts(String inputfile,String outputfile,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		String buffer=null;
		PrintStream ps=new PrintStream(outputfile);
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=3)
				continue;
			Integer[] en=new Integer[3];
			en[0]=entity2id.get(ss[0]);
			if(en[0]==null)
				continue;
			en[1]=relation2id.get(ss[1]);
			if(en[1]==null)
				continue;
			en[2]=entity2id.get(ss[2]);
			if(en[2]==null)
				continue;
			ps.println(en[0]+"\t"+en[1]+"\t"+en[2]);
		}
		ps.close();
		br.close();
	}
	
	
	
	public static void main(String[] args) throws IOException
	{
		CollectEntities ce=new CollectEntities();
		//ce.FilterOutNotTriplet("D:\\KBQA\\DataDump\\chineseKb\\facts_chinese.conceptnet.gb2312.csv", "D:\\KBQA\\DataDump\\chineseKb\\facts_chinese.conceptnet.gb2312.filtered.csv", "gb2312");
		
		//ce.ReadEntitiesFromFactFile("D:\\KBQA\\DataDump\\chineseKb\\baidubaike.fact", "utf8");
		ce.ReadEntitiesFromFactFile("D:\\KBQA\\DataDump\\chineseKb\\facts_chinese.conceptnet.gb2312.filtered.csv", "gb2312");	
		
		ce.PrintList("D:\\KBQA\\DataDump\\chineseKb\\chinese_entity_conceptnet.db", "utf8", ce.id2entity);
		ce.PrintList("D:\\KBQA\\DataDump\\chineseKb\\chinese_relation_conceptnet.db", "utf8", ce.id2relation);		
		ce.ReIndexFacts("D:\\KBQA\\DataDump\\chineseKb\\facts_chinese.conceptnet.gb2312.filtered.csv", "D:\\KBQA\\DataDump\\chineseKb\\facts_chinese.conceptnet.gb2312.filtered.index", "gb2312");
	}
	
	
}
