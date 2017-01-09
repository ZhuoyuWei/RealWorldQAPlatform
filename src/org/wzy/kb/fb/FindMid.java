package org.wzy.kb.fb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class FindMid {

	
	public Map<String,String> name2mid=new HashMap<String,String>();
	public Map<String,String> mid2name=new HashMap<String,String>();	
	
	public void ReadMap(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");
			if(ss.length!=2)
				continue;
			name2mid.put(ss[1].toLowerCase(), ss[0]);
			mid2name.put(ss[0], ss[1].toLowerCase());
		}
	}
	
	public void ReadAndPrintMid(String inputfile,String outputfile,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		String buffer=null;
		PrintWriter pw=new PrintWriter(outputfile,code);
		int count=0;
		while((buffer=br.readLine())!=null)
		{
			String mid=name2mid.get(buffer);
			if(mid==null)
			{
				count++;
			}
			else
			{
				pw.println(mid+"\t"+buffer);
			}
		}	
		pw.flush();
		pw.close();
		br.close();
		System.err.println("error entity "+count);
	}
	
	
	
	public static void main(String[] args) throws IOException
	{
		FindMid fm=new FindMid();
		fm.ReadMap(args[0], "utf8");
		fm.ReadAndPrintMid(args[1], args[2], "utf8");
	}
}
