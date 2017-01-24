package org.wzy.kb.cn;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterOutChineseTriplet {

	public void ReadFile(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.contains("/c/zh/"))
			{
				
			}
		}
	}
	
    public static boolean isContainChinese(String str)
    {

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
	
	public static void DirectFilterFromTriplets(String inputfile,String outputfile,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		String buffer=null;
		PrintWriter pw=new PrintWriter(outputfile,code);
		while((buffer=br.readLine())!=null)
		{
			boolean flag=isContainChinese(buffer);
			if(flag)
			{
				pw.println(buffer);
			}
		}		
		pw.flush();
		pw.close();
		br.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		DirectFilterFromTriplets("D:\\KBQA\\DataDump\\conceptnet5_flat_csv_5.5\\data\\facts.conceptnet.csv"
				,"D:\\KBQA\\DataDump\\conceptnet5_flat_csv_5.5\\data\\facts_chinese.conceptnet.csv","utf8");
	}
}
