package test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wzy.tool.IOTool;

public class DebugFacts {

	public Map<String,Integer> rel2count=new HashMap<String,Integer>();
	
	public List<String> id2mid;
	public Map<String,String> mid2name;
	
	public List<String> id2rel;
	
	public void PrintGroundNamesMain(String mid_id_file,String mid_name_file,String relfile,String inputfile,String outputfile) throws IOException
	{
		id2mid=IOTool.ReadLargeStringList(mid_id_file, "utf8", 0, 1900000);
		mid2name=IOTool.ReadEntityMidtoName(mid_name_file, "utf8");
		id2rel=IOTool.ReadLargeStringList(relfile, "utf8", 0, 5000);
		ReadAndPrintGroundPaths(inputfile,outputfile,"utf8");
	}
	
	public void ReadAndPrintGroundPaths(String inputfile,String outputfile,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		String buffer=null;
		PrintWriter pw=new PrintWriter(outputfile,code);
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length<=1)
				continue;
			for(int i=0;i<ss.length;i++)
			{
				int index=Integer.parseInt(ss[i]);
				if(i%2==0)
				{
					String mid=id2mid.get(index);
					String name=mid2name.get(mid);
					if(name==null)
						name=mid;
					pw.print(name+"\t");
				}
				else
				{
					String rel=id2rel.get(index);
					pw.print(rel+"\t");
				}
			}
			pw.println();
		}
		br.close();
		pw.close();
	}
	
	public void ReadAndCountRelations(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf8"));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			Integer count=rel2count.get(ss[1]);
			if(count==null)
			{
				rel2count.put(ss[1], 1);
			}
			else
			{
				count++;
				rel2count.remove(ss[1]);
				rel2count.put(ss[1], count);
			}
		}
		br.close();
		
		class ValueAndIndex implements Comparator
		{
			public int value=0;
			public String rel;
			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				
				ValueAndIndex vi1=(ValueAndIndex)o1;
				ValueAndIndex vi2=(ValueAndIndex)o2;
				
				if(Math.abs(vi1.value-vi2.value)<1e-10)
					return 0;
				else if(vi1.value>vi2.value)
					return -1;
				else
					return 1;
			}
		}
		
		
		List<ValueAndIndex> viList=new ArrayList<ValueAndIndex>();
		Iterator it=rel2count.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			String rel=(String)entry.getKey();
			Integer count=(Integer)entry.getValue();
			
			ValueAndIndex vi=new ValueAndIndex();
			vi.rel=rel;
			vi.value=count;
			viList.add(vi);
		}
		
		Collections.sort(viList,new ValueAndIndex());
		
		for(int i=0;i<viList.size();i++)
		{
			System.out.println(viList.get(i).value+"\t"+viList.get(i).rel);
		}
	}
	
	public void CollectFirstEntitySet(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf8"));
		String buffer=null;
		Set<String> entitySet=new HashSet<String>();
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			entitySet.add(ss[0]);
		}
		br.close();
		System.out.println("entity size "+entitySet.size());
	}
	
	public static void main(String[] args) throws IOException
	{
		DebugFacts df=new DebugFacts();
		//df.ReadAndCountRelations(args[0]);
		//df.CollectFirstEntitySet(args[0]);
		df.PrintGroundNamesMain(args[0], args[1], args[2], args[3], args[4]);
	}
	
	
}
