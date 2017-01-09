package org.wzy.kb.wiki;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectPageTitle {

	public List<String> titleList=new ArrayList<String>();
	public Set<String> titleSet=new HashSet<String>();
	
	public void ReadTitles(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf8"));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String pattern="<title>(.*?)</title>";
			Pattern p=Pattern.compile(pattern);
			Matcher matcher=p.matcher(buffer);
			if(matcher.find())
			{
				String title=matcher.group(1);
				title=title.toLowerCase().replaceAll("[\\s]+", "_");
				titleList.add(title);
			}
		}
		br.close();
	}
	
	public void PrintAllList(String filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,"utf8");
		for(int i=0;i<titleList.size();i++)
		{
			pw.println(titleList.get(i));
		}
		pw.close();
	}
	
	public void ReadTitlesAndPrint(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),"utf8"));
		PrintWriter pw=new PrintWriter(outputfile,"utf8");
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String pattern="<title>(.*?)</title>";
			Pattern p=Pattern.compile(pattern);
			Matcher matcher=p.matcher(buffer);
			if(matcher.find())
			{
				String title=matcher.group(1);
				title=title.toLowerCase().replaceAll("[\\s]+", "_");
				//titleList.add(title);
				pw.println(title);
			}
		}
		br.close();
	}	
	
	public void ReadWikiAndPrintPages(String inputfile,String outputdir,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		//PrintWriter pw=new PrintWriter(outputfile,"utf8");
		String buffer=null;
		StringBuilder sb=new StringBuilder();
		String lasttitle=null;
		while((buffer=br.readLine())!=null)
		{
			String pattern="<title>(.*?)</title>";
			Pattern p=Pattern.compile(pattern);
			Matcher matcher=p.matcher(buffer);
			if(matcher.find())
			{
				String title=matcher.group(1);
				title=title.toLowerCase().replaceAll("[\\s]+", "_");
				//titleList.add(title);
				//pw.println(title);
				
				if(lasttitle!=null&&titleSet.contains(lasttitle)&&!lasttitle.contains("/"))
				{
					
					PrintWriter pw=new PrintWriter(outputdir+lasttitle,code);
					pw.print(sb.toString());
					pw.flush();
					pw.close();
				}
				sb=new StringBuilder();
				lasttitle=title;
				sb.append(buffer);
			}
			else
			{
				sb.append(buffer);
				sb.append("\n");
			}
		}
		br.close();
		
	}		
	
	public void ReadNameSet(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf8"));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<1)
				continue;
			titleSet.add(buffer);
		}
		br.close();
	}
	
	
	public static void main(String[] args) throws IOException
	{
		CollectPageTitle cpt=new CollectPageTitle();
		//cpt.ReadTitles(args[0]);
		//cpt.PrintAllList(args[1]);
		//cpt.ReadTitlesAndPrint(args[0], args[1]);
		cpt.ReadNameSet(args[0], "utf8");
		cpt.ReadWikiAndPrintPages(args[1], args[2], "utf8");
	}
}
