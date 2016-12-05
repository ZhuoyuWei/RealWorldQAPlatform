package org.wzy.ir;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class SplitLargeFile {
	
	
	public void SimpleReadAndPrint(String inputfile,String outputdir,int filelines) throws FileNotFoundException, UnsupportedEncodingException, IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),"utf8"));
		String buffer=null;
		int linecount=0;
		int filecount=0;
		PrintWriter pw=new PrintWriter(outputdir+filecount,"utf8");
		
		while((buffer=br.readLine())!=null)
		{
			pw.println(buffer);
			linecount++;
			if(linecount>=filelines)
			{
				linecount=0;
				pw.close();
				filecount++;
				pw=new PrintWriter(outputdir+filecount,"utf8");
			}
		}
		
		pw.close();
		br.close();
	}
	
	public static void main(String[] args) throws NumberFormatException, FileNotFoundException, UnsupportedEncodingException, IOException
	{
		SplitLargeFile slf=new SplitLargeFile();
		//slf.SimpleReadAndPrint(args[0], args[1], Integer.parseInt(args[2]));
		slf.SimpleReadAndPrint(args[0], args[1], Integer.parseInt(args[2]));
	}

}
