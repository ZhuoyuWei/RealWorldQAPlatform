package org.wzy.kb.cn;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractFactsFromSource {

	
	public void ReadAndPrintFacts(String inputfile,String outputfile,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		String buffer=null;
		PrintWriter pw=new PrintWriter(outputfile,code);
		String pattern="/a/\\[(.*?)\\]";
		Pattern p=Pattern.compile(pattern);
		while((buffer=br.readLine())!=null)
		{
			Matcher matcher=p.matcher(buffer);
			while(matcher.find())
			{
				String factstr=matcher.group(1);
				String[] ss=factstr.split(",");
				if(ss.length!=3)
					continue;
				for(int i=0;i<3;i++)
				{
					ss[i]=ss[i].trim();
					String[] sss=ss[i].split("/");
					ss[i]=sss[sss.length-1];
					
					
					
				}
				pw.println(ss[1]+"\t"+ss[0]+"\t"+ss[2]);
			}
		}
		pw.close();
		br.close();
	}
	
	public void FileterBySentence(String inputfile,String outputfile,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		String buffer=null;
		PrintWriter pw=new PrintWriter(outputfile,code);
		String entityPattern="\\[\\[(.*?)\\]\\]";
		Pattern p=Pattern.compile(entityPattern);
		String relPattern="\\]\\](.*?)\\[\\[";
		Pattern relp=Pattern.compile(relPattern);
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			String sen=ss[ss.length-1];
			Matcher matcher=p.matcher(sen);
			String[] entities=new String[2];
			int count=0;
			while(matcher.find())
			{
				entities[count++]=matcher.group(1);
			}
			if(count!=2)
				continue;
			Matcher relMatcher=relp.matcher(sen);
			String rel=null;
			if(relMatcher.find())
			{
				rel=relMatcher.group(1).trim();
				//String[] tmpss=rel.split(" ");
			}
			else
			{
				continue;
			}
			//String rel=sen.replaceAll(entityPattern, "").trim();
			
			//change
			String[] printss=new String[3];
			printss[0]=entities[0];
			printss[1]=rel;
			printss[2]=entities[1];
			for(int i=0;i<3;i++)
			{
				printss[i]=printss[i].toLowerCase().trim().replaceAll("[\\s]+", "_");
			}
			
			pw.println(printss[0]+"\t"+printss[1]+"\t"+printss[2]);
		}
		pw.close();
		br.close();
	}
	
	public void CollectAllNamesAndIndex(String filename,String code,String entityfile,String relfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		
		Map<String,Integer> name2id=new HashMap<String,Integer>();
		List<String> id2name=new ArrayList<String>();
		
		Map<String,Integer> rel2id=new HashMap<String,Integer>();
		List<String> id2rel=new ArrayList<String>();	
		
		Map<String,Integer>[] maps=new Map[3];
		List<String>[] lists=new List[3];
		
		maps[0]=name2id;
		maps[1]=rel2id;
		maps[2]=name2id;
		
		lists[0]=id2name;
		lists[1]=id2rel;
		lists[2]=id2name;
		
		
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=3)
				continue;
			for(int i=0;i<3;i++)
			{
				Integer index=maps[i].get(ss[i]);
				if(index==null)
				{
					maps[i].put(ss[i], lists[i].size());
					lists[i].add(ss[i]);
				}
			}
		}
		
		br.close();
		
		
		
		String[] outputfiles=new String[2];
		outputfiles[0]=entityfile;
		outputfiles[1]=relfile;
		for(int i=0;i<2;i++)
		{
			PrintWriter pw=new PrintWriter(outputfiles[i],code);

			for(int j=0;j<lists[i].size();j++)
			{
				String token=lists[i].get(j).toLowerCase().trim();
				token=token.replaceAll("[\\s]+", "_");
				
				pw.println(token+"\t"+j);
			}
			pw.close();
		}
		
	}
	
	public void CollectAllNamesAndIndexPrintFacts(String filename,String code,String entityfile,String relfile,String factoutputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		
		Map<String,Integer> name2id=new HashMap<String,Integer>();
		List<String> id2name=new ArrayList<String>();
		
		Map<String,Integer> rel2id=new HashMap<String,Integer>();
		List<String> id2rel=new ArrayList<String>();	
		
		Map<String,Integer>[] maps=new Map[3];
		List<String>[] lists=new List[3];
		
		maps[0]=name2id;
		maps[1]=rel2id;
		maps[2]=name2id;
		
		lists[0]=id2name;
		lists[1]=id2rel;
		lists[2]=id2name;
		
		PrintWriter pwfact=new PrintWriter(factoutputfile,code);
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=3)
				continue;
			for(int i=0;i<3;i++)
			{
				Integer index=maps[i].get(ss[i]);
				if(index==null)
				{
					index=lists[i].size();
					maps[i].put(ss[i], index);
					lists[i].add(ss[i]);
				}
				if(i==0)
				{
					pwfact.print(index);
				}
				else
				{
					pwfact.print("\t"+index);
				}
			}
			pwfact.println();
		}
		
		br.close();
		pwfact.close();
		
		
		
		String[] outputfiles=new String[2];
		outputfiles[0]=entityfile;
		outputfiles[1]=relfile;
		for(int i=0;i<2;i++)
		{
			PrintWriter pw=new PrintWriter(outputfiles[i],code);

			for(int j=0;j<lists[i].size();j++)
			{
				String token=lists[i].get(j).toLowerCase().trim();
				token=token.replaceAll("[\\s]+", "_");
				
				pw.println(token+"\t"+j);
			}
			pw.close();
		}
		
	}
	
	public void ReadMapAndList(String filename,String code,Map<String,Integer> map,List<String> list) throws NumberFormatException, IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			map.put(ss[0], Integer.parseInt(ss[1]));
			list.add(ss[0]);
		}
		br.close();
	}
	
	
	public void IndexFacts(String entityFile,String relFile,String factfile,String outputfile,String code) throws NumberFormatException, IOException
	{
		Map<String,Integer> name2id=new HashMap<String,Integer>();
		List<String> id2name=new ArrayList<String>();
		
		Map<String,Integer> rel2id=new HashMap<String,Integer>();
		List<String> id2rel=new ArrayList<String>();	
		
		ReadMapAndList(entityFile,code,name2id,id2name);
		ReadMapAndList(relFile,code,rel2id,id2rel);	
		
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(factfile),code));
		String buffer=null;
		PrintWriter pw=new PrintWriter(outputfile,code);
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=3)
				continue;
			Integer[] triplets=new Integer[3];
			triplets[0]=name2id.get(ss[0]);
			triplets[1]=rel2id.get(ss[1]);
			triplets[2]=name2id.get(ss[2]);
			
			for(int i=0;i<3;i++)
			{
				if(triplets[i]==null)
				{
					System.err.println("error fact index");
					System.exit(-1);
				}
			}
			
			pw.println(triplets[0]+"\t"+triplets[1]+"\t"+triplets[2]);
			
			
		}
		br.close();
		pw.close();
		
		
	}
	
	public void PrintEntityIndex(String entityfile,String inputfile,String outputfile) throws NumberFormatException, IOException
	{
		Map<String,Integer> name2id=new HashMap<String,Integer>();
		List<String> id2name=new ArrayList<String>();
		ReadMapAndList(entityfile,"utf8",name2id,id2name);
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),"utf8"));
		String buffer=null;
		PrintWriter pw=new PrintWriter(outputfile,"utf8");
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<1)
				continue;
			Integer index=name2id.get(buffer);
			if(index==null)
			{
				System.err.println("entity index error");
				System.exit(-1);
			}
			pw.println(buffer+"\t"+index);
		}
		pw.close();
		br.close();
		
	}

	
	public static void main(String[] args) throws IOException
	{
		ExtractFactsFromSource effs=new ExtractFactsFromSource();
		//effs.ReadAndPrintFacts(args[0], args[1], "utf8");
		//effs.FileterBySentence(args[0], args[1], "utf8");		
		//effs.CollectAllNamesAndIndex(args[0], "utf8", args[1], args[2]);
		//effs.CollectAllNamesAndIndexPrintFacts(args[0], "utf8", args[1], args[2],args[3]);
		effs.IndexFacts(args[0], args[1], args[2], args[3], "utf8");
		//effs.PrintEntityIndex(args[0], args[1], args[2]);
	}
	
}
