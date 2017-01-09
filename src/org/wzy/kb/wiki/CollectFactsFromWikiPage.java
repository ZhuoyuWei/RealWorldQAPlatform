package org.wzy.kb.wiki;

import java.io.BufferedReader;
import java.io.File;
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

import org.wzy.tool.CoreNLPTool;



public class CollectFactsFromWikiPage {

	public List<String> titleList;
	public List<String> pageList;
	
	public String[] pronouns={"it","they","them","he","she","him","her"};
	boolean stem=false;
	
	public String ReadOnePage(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		StringBuilder sb=new StringBuilder();
		while((buffer=br.readLine())!=null)
		{
			sb.append(buffer);
			sb.append("\n");
		}
		br.close();
		return sb.toString();
	}
	
	public void ReadPagesByTitle(String inputdir,String code,String outputfile) throws FileNotFoundException, UnsupportedEncodingException
	{
		titleList=new ArrayList<String>();
		pageList=new ArrayList<String>();
		File dir=new File(inputdir);
		if(stem)
		{
			CoreNLPTool.CreateUniqueObject();
			CoreNLPTool.UniqueObject.InitTool("tokenize, ssplit, pos, lemma");
		}
		PrintWriter pw=new PrintWriter(outputfile,code);
		if(dir.isDirectory())
		{
			File[] files=dir.listFiles();
			for(int i=0;i<files.length;i++)
			{
				String filename=files[i].getPath();
				String title=files[i].getName();
				
				//String page=null;
				try {
					//page=ReadOnePage(filename,code);
					List<String[]> tripletList=this.CollectFactsForOnePage(filename, code, title);
					for(int j=0;j<tripletList.size();j++)
					{
						String[] ss=tripletList.get(j);
						pw.println(ss[0]+"\t"+ss[1]+"\t"+ss[2]);
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*if(page!=null)
				{
					titleList.add(title);
					pageList.add(page);
				}*/
			}
		}
		pw.close();
		System.out.println("page: "+titleList.size()+" "+pageList.size());
		
	}
	
	public List<String[]> CollectFactsForOnePage(String filename,String code,String title) throws IOException
	{
		List<String[]> tripletList=new ArrayList<String[]>();
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		String entitypattern="\\[\\[(.*?)\\]\\]";
		Pattern p=Pattern.compile(entitypattern);
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<5)
				continue;
			String[] sentences=buffer.split("[,.?!;]+");
			for(int i=0;i<sentences.length;i++)
			{
				if(sentences[i].length()<5)
					continue;
				
				sentences[i]=sentences[i].trim().toLowerCase();
				
				
				Matcher matcher=p.matcher(sentences[i]);
				String obj_entity=null;
				if(matcher.find())
				{
					obj_entity=matcher.group(1);
					if(obj_entity.length()<1)
						continue;
				}
				else
				{
					continue;
				}
				
				if(matcher.find())  //only one entity in a sentence
				{
					continue;
				}
				
				
				//System.out.println(sentences[i]);
				//sentences[i]=sentences[i].replaceAll("[\\s]+(.*?)"+entitypattern+"(.*?)[\\s]+", " &obj& ");
				//System.out.println(sentences[i]);
				sentences[i]=sentences[i].replaceAll(entitypattern, " &obj& ");
			
				
				String[] sen_ss=null;
				if(stem)
				{
					sen_ss=CoreNLPTool.UniqueObject.StemProcessing(sentences[i]);
				}
				else
				{
					sen_ss=sentences[i].split("[\\s]+");
				}
				
				String[] sub_ss=title.split("_");
				int sub_in_sen_index=-1;
				int sub_l=-1;
				for(int j=0;j<sen_ss.length-sub_ss.length+1;j++)
				{
					boolean flag=true;
					for(int k=0;k<sub_ss.length;k++)
					{
						if(!sen_ss[j+k].equals(sub_ss[k]))
						{
							flag=false;
							break;
						}
					}
					if(flag)
					{
						sub_in_sen_index=j;
						sub_l=sub_ss.length;
						break;
					}
				}
				
				if(sub_in_sen_index<0)
				{
					for(int j=0;j<sen_ss.length;j++)
					{
						boolean flag=false;
						for(int k=0;k<pronouns.length;k++)
						{
							if(sen_ss[j].equals(pronouns[k]))
							{
								flag=true;
								break;
							}
						}
						if(flag)
						{
							sub_in_sen_index=j;
							sub_l=1;
							break;
						}
					}
				}
				
				if(sub_in_sen_index<0)
					continue;
				
				
				
				int obj_in_sen_index=-1;
				for(int j=0;j<sen_ss.length;j++)
				{
					if(sen_ss[j].equals("&obj&"))
					{
						obj_in_sen_index=j;
						break;
					}
				}
				
				
				
				
				String sub_str=title;
				String obj_str=null;
				if(stem)
				{
					String[] tmpss=CoreNLPTool.UniqueObject.StemProcessing(obj_entity);
					obj_str=tmpss[0];
					for(int j=1;j<tmpss.length;j++)
					{
						obj_str+="_"+tmpss[j];
					}
				}
				else
				{
					obj_str=obj_entity.replaceAll(" ", "_");
				}
				
				String rel_str=null;
				if(sub_in_sen_index<obj_in_sen_index&&sub_in_sen_index+sub_l<obj_in_sen_index)
				{
					rel_str=sen_ss[sub_in_sen_index+sub_l];
					for(int j=sub_in_sen_index+sub_l+1;j<obj_in_sen_index;j++)
					{
						rel_str+="_"+sen_ss[j];
					}
				}
				else if(sub_in_sen_index>obj_in_sen_index&&obj_in_sen_index+1<sub_in_sen_index)
				{
					rel_str=sen_ss[obj_in_sen_index+1];
					for(int j=obj_in_sen_index+2;j<sub_in_sen_index;j++)
					{
						rel_str+="_"+sen_ss[j];
					}	
					
					String tmp=sub_str;
					sub_str=obj_str;
					obj_str=tmp;
					
				}
				
				if(rel_str!=null)
				{
					String[] triplet=new String[3];
					triplet[0]=sub_str;
					triplet[1]=rel_str;
					triplet[2]=obj_str;
					tripletList.add(triplet);
				}
				
			}
		}
		br.close();
		
		return tripletList;
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{
		CollectFactsFromWikiPage cf=new CollectFactsFromWikiPage();
		cf.ReadPagesByTitle(args[0], "utf8",args[1]);
		//cf.ReadPagesByTitle("D:\\KBQA\\DataDump\\wikipedia\\debug_get_facts_from_pages", "utf8","D:\\KBQA\\DataDump\\wikipedia\\fact.log");
	}
	
}
