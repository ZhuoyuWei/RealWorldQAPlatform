package org.wzy.kb.wiki;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CollectCateAndName {

	public Set<String> nameSet=new HashSet<String>();
	
	public void ReadAllCateFromWikiSub(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=2)
				continue;
			for(int i=0;i<2;i++)
			{
				String[] sss=ss[i].split(" ");
				StringBuilder sb=new StringBuilder(sss[0]);
				for(int j=1;j<sss.length;j++)
				{
					sb.append("_");
					sb.append(sss[j]);
				}
				String tmp=sb.toString();
				if(tmp.length()>1)
					nameSet.add(tmp);
			}
		}
		System.out.println("nameset size: "+nameSet.size());
	}
	
	public void PrintNameSet(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		Iterator it=nameSet.iterator();
		while(it.hasNext())
		{
			ps.println(it.next().toString());
		}
		ps.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		CollectCateAndName cc=new CollectCateAndName();
		cc.ReadAllCateFromWikiSub("D:\\KBQA\\DataDump\\hesz\\cate-subcate-en", "ascii");
		cc.PrintNameSet("D:\\KBQA\\DataDump\\hesz\\cate-set-en");
	}
}
