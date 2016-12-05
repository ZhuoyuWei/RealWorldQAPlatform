package org.wzy.kb.fb;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class MidAndCount implements Comparator
{
	public String mid;
	public int count=0;
	@Override
	public int compare(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		
		MidAndCount m0=(MidAndCount)arg0;
		MidAndCount m1=(MidAndCount)arg1;
		
		return m1.count-m0.count;
	}
}

public class KBCollectFacts {

	
	
	public Set<String> entitySet=new HashSet<String>();
	public Map<String,String> mid2name=new HashMap<String,String>();
	
	public Map<String,List<String>> mid2factList=new HashMap<String,List<String>>();
	
	public void ReadEntities(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length==2)
			{
				entitySet.add(ss[1]);
			}
		}
		br.close();
	}
	
	public void ReadFactsAndPrint(String inputfile,String outputfile,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		String buffer=null;
		//PrintStream ps=new PrintStream(outputfile);
		PrintWriter pw=new PrintWriter(outputfile,code);
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length==3)
			{
				//entitySet.add(ss[1]);
				if(entitySet.contains(ss[0])||entitySet.contains(ss[2]))
				{
					pw.println(buffer);
				}
			}
		}
		pw.close();
		br.close();		
	}
	
	public void ReadFactsAndSaveAsList(String inputfile,String outputfile,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		String buffer=null;
		//PrintStream ps=new PrintStream(outputfile);
		//PrintWriter pw=new PrintWriter(outputfile,code);
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length==3)
			{
				//entitySet.add(ss[1]);
				if(entitySet.contains(ss[0]))
				{
					List<String> list=mid2factList.get(ss[0]);
					if(list==null)
					{
						list=new ArrayList<String>();
						mid2factList.put(ss[0], list);
					}
					list.add(buffer);
				}
				if(entitySet.contains(ss[2]))
				{
					List<String> list=mid2factList.get(ss[2]);
					if(list==null)
					{
						list=new ArrayList<String>();
						mid2factList.put(ss[2], list);
					}
					list.add(buffer);
				}				
			}
		}
		br.close();	
		
		Iterator it=mid2factList.entrySet().iterator();
		List<MidAndCount> mcList=new ArrayList<MidAndCount>(mid2factList.size());
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			String mid=(String)entry.getKey();
			MidAndCount mc=new MidAndCount();
			mc.mid=mid;
			mc.count=((List<String>)entry.getValue()).size();
			mcList.add(mc);
		}
		Collections.sort(mcList,new MidAndCount());
		
		
		PrintStream ps=new PrintStream(outputfile);
		for(int i=0;i<mcList.size();i++)
		{
			ps.println(mcList.get(i).mid+"\t"+mcList.get(i).count);
		}
		
		ps.close();
	}	

	public Set<String> ReadFactsCollectEntities(String filename) throws IOException
	{
		Set<String> entitySet=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			entitySet.add(ss[0]);
			entitySet.add(ss[2]);
		}
		System.out.println("entity size: "+entitySet.size());
		return entitySet;
	}
	public void FilterFactsInSet(String factfile,String outfactfile) throws IOException
	{
		//Set<String> entitySet=new HashSet<String>();
		BufferedReader br=new BufferedReader(new FileReader(factfile));
		PrintStream ps=new PrintStream(outfactfile);
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			//entitySet.add(ss[0]);
			//entitySet.add(ss[2]);
			
			if(mid2name.get(ss[0])!=null&&mid2name.get(ss[2])!=null)
			{
				ps.println(buffer);
			}
		}
		
		ps.close();
		br.close();
		
	}	
	
	
	public void ReadFBNamesFromFile(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("[\\s]+");
			if(ss.length==2)
			{
				//name2mid.put(ss[1].toLowerCase(), ss[0]);
				mid2name.put(ss[0], ss[1].toLowerCase());
			}
		}
		br.close();
	}
	
	
	public void CollectEntityNames(String filename) throws FileNotFoundException
	{
		Iterator it=entitySet.iterator();
		int count=0;
		PrintStream ps=new PrintStream(filename);
		while(it.hasNext())
		{
			String mid=(String)it.next();
			String name=mid2name.get(mid);
			if(name!=null)
			{
				count++;
				ps.println(name+"\t"+mid);
			}
		}
		System.out.println(count+" entities have name and the sum is "+entitySet.size());
		ps.close();
	}
	
	
	
	public static void GetFactsForEntities(String[] args) throws IOException
	{
		KBCollectFacts kbc=new KBCollectFacts();
		kbc.ReadEntities(args[0], "ascii");
		kbc.ReadFactsAndPrint(args[1], args[2], "ascii");		
	}
	
	public static void StatisticFactDistribution(String[] args) throws IOException
	{
		KBCollectFacts kbc=new KBCollectFacts();
		kbc.ReadEntities(args[0], "ascii");
		kbc.ReadFactsAndSaveAsList(args[1], args[2], "ascii");
	}
	
	public static void MapMid2Names(String[] args) throws IOException
	{
		KBCollectFacts kbc=new KBCollectFacts();	
		kbc.entitySet=kbc.ReadFactsCollectEntities(args[0]);
		kbc.ReadFBNamesFromFile(args[1], "ascii");
		kbc.CollectEntityNames(args[2]);		
	}
	
	public static void FilterAllInEntitySets(String[] args) throws IOException
	{
		KBCollectFacts kbc=new KBCollectFacts();	
		kbc.ReadFBNamesFromFile(args[0], "ascii");		
		kbc.FilterFactsInSet(args[1], args[2]);
	}
	
	public static void main(String[] args) throws IOException
	{
		//StatisticFactDistribution(args);
		//MapMid2Names(args);
		FilterAllInEntitySets(args);
		//GetFactsForEntities(args);
	}
	
}
