package org.wzy.collectdata.ck12;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DownloadQuiz {

	public List<String> quizNameList=new ArrayList<String>();

	private boolean getContent(URL url,String filename) {
		
		System.out.println(url.toString());
		
        StringBuffer builder = new StringBuffer();
 
        int responseCode = -1;
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");// IE代理进行下载
            con.setConnectTimeout(60000);
            con.setReadTimeout(60000);
            con.setInstanceFollowRedirects(false); 
 
            // 获得网页返回信息码
            responseCode = con.getResponseCode();
            
            System.out.println(responseCode);
            
 
            if (responseCode == -1) {
                //viewArea.setText("连接失败:" + url.toString());
                return false;
            }
 
            if (responseCode >= 400) {
                //viewArea.setText("请求失败，错误码:" + responseCode);
                return false;
            }
            
            if(responseCode==301)
            {
            	String jump=con.getHeaderField("Location");
            	return getContent(new URL(jump),filename); 
            }
 
            InputStream is = con.getInputStream();
            BufferedInputStream bis=new BufferedInputStream(is);
            BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(filename));
            byte[] buffer=new byte[4096];
            int len=-1;
            while((len=bis.read(buffer))!=-1)
            {
            	bos.write(buffer,0,len);
            }
            bos.flush();
 
            bis.close();
            bos.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            con.disconnect();
        }
        return true;
    }
	
	public void ReadList(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			quizNameList.add(buffer.trim());
			
		}
		br.close();
	}
	
	public void DownloadAllInList(String urlbase,String outputdir,String label) throws MalformedURLException
	{
		int count=0;
		for(int i=0;i<quizNameList.size();i++)
		{
			String[] ss=quizNameList.get(i).split("[\\s]+");
			StringBuilder sb=new StringBuilder();
			for(int j=0;j<ss.length;j++)
			{
				sb.append(ss[j]);
				sb.append("-");
			}
			sb.append(label);
			URL url=new URL(urlbase+sb.toString());
			String filename=outputdir+sb.toString()+".docx";
			
			if(getContent(url,filename))
			{
				count++;
			}
			
		}
		System.out.println("Total url: "+quizNameList.size());
		System.out.println("GetDoc: "+count);
	}
	
	
	public void FilterNoDocs(String inputdir,String outputfile) throws FileNotFoundException
	{
		Set<String> set=new HashSet<String>();
		//set.addAll(quizNameList);
		List<String> wronglist=new ArrayList<String>();
		File dir=new File(inputdir);
		if(dir.isDirectory())
		{
			File[] files=dir.listFiles();
			for(int i=0;i<files.length;i++)
			{
				String name=files[i].getName();
				String[] ss=name.split("-");
				StringBuilder sb=new StringBuilder();
				sb.append(ss[0]);
				for(int j=1;j<ss.length-1;j++)
				{
					sb.append(" ");
					sb.append(ss[j]);
				}
				String tmpstr=sb.toString();
				/*if(!set.contains(tmpstr))
				{
					wronglist.add(name);
				}*/
				set.add(tmpstr);
			}
		}
		
		
		for(int i=0;i<quizNameList.size();i++)
		{
			if(!set.contains(quizNameList.get(i)))
			{
				wronglist.add(quizNameList.get(i));
			}
		}
		System.out.println(wronglist.size());
		
		PrintStream ps=new PrintStream(outputfile);
		for(int i=0;i<wronglist.size();i++)
		{
			ps.println(wronglist.get(i));
		}
		
		ps.close();
		
		
	}
	
	public static void main(String[] args) throws IOException
	{
		DownloadQuiz dq=new DownloadQuiz();
		dq.ReadList("D:\\KBQA\\SpiderData\\Material_Area\\ck12_downloadable_quizs\\physical\\title_list");
		//dq.DownloadAllInList("http://www.ck12.org/flx/show/quiz/", "D:\\KBQA\\SpiderData\\Material_Area\\ck12_downloadable_quizs\\physical\\downloads\\","Quiz");		
		dq.DownloadAllInList("http://www.ck12.org/flx/show/answer%20key/", "D:\\KBQA\\SpiderData\\Material_Area\\ck12_downloadable_quizs\\physical\\downloads2\\","Quiz-PPB-Answer-Key");
		//dq.FilterNoDocs("D:\\KBQA\\SpiderData\\Material_Area\\ck12_downloadable_quizs\\physical\\downloads\\", "D:\\KBQA\\SpiderData\\Material_Area\\ck12_downloadable_quizs\\physical\\wrong_list2");
	}
}
