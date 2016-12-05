package org.wzy.kb.baike;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ProduceTriplets {

	public List<String[]> tripletList=new ArrayList<String[]>();
	
	public void ProcessOnePage(String text)
	{
		//System.out.println(text);
		String pattern="<item>(.*?)</item>";
		Pattern p=Pattern.compile(pattern);
		Matcher matcher=p.matcher(text);
		
		String sub=null;
		String pre=null;
		String obj=null;
		
		while(matcher.find())
		{
			String tripletstr=matcher.group(1);
			pattern="<subject>(.*?)</subject>";
			p=Pattern.compile(pattern);
			Matcher matcher2=p.matcher(tripletstr);
			if(matcher2.find())
			{
				sub=matcher2.group(1);
			}
			
			pattern="<predicate>(.*?)</predicate>";
			p=Pattern.compile(pattern);
			matcher2=p.matcher(tripletstr);
			if(matcher2.find())
			{
				pre=matcher2.group(1);
			}
			
			pattern="<object>(.*?)</object>";
			p=Pattern.compile(pattern);
			matcher2=p.matcher(tripletstr);
			if(matcher2.find())
			{
				obj=matcher2.group(1);
				String[] ss=obj.split("[，。、？！]+");
				for(int i=0;i<ss.length;i++)
				{
					ss[i]=ss[i].replaceAll("\\[.*?\\]", "");
					if(ss[i].length()>0&&ss[i].length()<10)
					{
						if(sub!=null&&pre!=null)
						{
							String[] tri=new String[3];
							tri[0]=sub;
							tri[1]=pre;
							tri[2]=ss[i];
							tripletList.add(tri);
						}
					}
				}
			}				
		}
		
		if(sub==null)
			return;
		pattern="<tag>(.*?)</tag>";
		p=Pattern.compile(pattern);
		matcher=p.matcher(text);
		while(matcher.find())
		{
			obj=matcher.group(1);
			String[] tri=new String[3];
			tri[0]=sub;
			tri[1]="tag";
			tri[2]=obj;
			tripletList.add(tri);
		}
		
	}
	
	public void ReadAndExtract(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		boolean inpageflag=false;
		StringBuilder sb=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.contains("<page>"))
			{
				inpageflag=true;
				sb=new StringBuilder();
			}
			else if(buffer.contains("</page>"))
			{
				inpageflag=false;
				ProcessOnePage(sb.toString());
			}
			else if(inpageflag)
			{
				sb.append(buffer.trim());
				//sb.append("\n");
			}
		}
		br.close();
	}
	
	public void PrintTriplets(String filename,String code) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,code);
		for(int i=0;i<tripletList.size();i++)
		{
			pw.println(tripletList.get(i)[0]+"\t"+tripletList.get(i)[1]+"\t"+tripletList.get(i)[2]);
		}
		pw.close();
	}
	
	
	
	public static void main(String[] args) throws IOException
	{
		ProduceTriplets pt=new ProduceTriplets();
		//pt.ReadAndExtract(args[0], "utf8");
		//pt.PrintTriplets(args[1], "utf8");
		pt.ReadAndExtract("D:\\KBQA\\DataDump\\baiduFinal0705a.txt", "utf8");
		pt.PrintTriplets("D:\\KBQA\\DataDump\\baidubaike.fact", "utf8");
	}
	
	
}
