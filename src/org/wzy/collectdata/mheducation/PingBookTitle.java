package org.wzy.collectdata.mheducation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingBookTitle {

	public Set<Long> robotSiteSet=new HashSet<Long>();
	
	public List<String> urlList=new ArrayList<String>();
	
	
	
	 private String getContent(URL url) {
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
	                return null;
	            }
	 
	            if (responseCode >= 400) {
	                //viewArea.setText("请求失败，错误码:" + responseCode);
	                return null;
	            }
	            
	            if(responseCode==301)
	            {
	            	String jump=con.getHeaderField("Location");
	            	System.out.println(jump);
	            	if(jump.endsWith("404.html"))
	            	{
	            		return null;
	            	}
	            	else
	            	{
	            		return getContent(new URL(jump)); 
	            	}
	            }
	 
	            InputStream is = con.getInputStream();
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	 
	            String str = null;
	            while ((str = br.readLine()) != null)
	                builder.append(str);
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            con.disconnect();
	        }
	        return builder.toString();
	    }
	

	
	public void ReadRobotText(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String pattern="([0-9x]{10})";
			Pattern p=Pattern.compile(pattern);
			Matcher matcher=p.matcher(buffer);
			if(matcher.find())
			{
				String token=matcher.group(1);
				if(token.indexOf('x')>=0)
				{
					/*int mid=token.indexOf('x');
					String pre="";
					String sub="";
					if(mid==0)
					{
						sub=token.substring(1, token.length());
					}
					else if(mid==token.length()-1)
					{
						pre=token.substring(0, token.length()-1);
					}
					else
					{
						pre=token.substring(0,mid);
						sub=token.substring(mid,token.length());
					}

					for(int i=0;i<10;i++)
					{
						robotSiteSet.add(Long.parseLong(pre+i+sub));
					}*/
					continue;
				}
				else
				{
					robotSiteSet.add(Long.parseLong(token));
				}
				
			}
			
		}
		
		System.out.println("Total robot num: "+robotSiteSet.size());
		
	}
	
	public String IntegerTo10String(Long num)
	{
		byte[] sequence=new byte[10];
		for(int i=0;i<10;i++)
		{
			if(num==0)
				sequence[9-i]='0';
			else
			{
				byte tmp='0';
				sequence[9-i]=(byte)(tmp+num%10);
			}
			num/=10;
		}
		return new String(sequence);
	}
	
	public void ProcessAllPages(Long startPage,Long endPage,String logfile) throws MalformedURLException, FileNotFoundException
	{
		PrintStream logps=new PrintStream(logfile);
		System.out.println(startPage+" "+endPage);
		for(Long page=startPage;page<=endPage;page++)
		{
			//if(robotSiteSet.contains(page))
				//continue;
			String str_page=IntegerTo10String(page);
			System.out.println(str_page);
			String urlstring="http://highered.mheducation.com/sites/"+str_page+"/student_view0/index.html";
			URL url=new URL(urlstring);
			String content=getContent(url);
			if(content!=null)
			{
				urlList.add(urlstring);
				logps.println(urlstring);
				logps.flush();
			}
			//TestOnePage(url);
		}
		logps.close();
			
	}
	
	
	
	public static void main(String[] args) throws IOException
	{
		PingBookTitle pbt=new PingBookTitle();
		pbt.ReadRobotText("D:\\KBQA\\SpiderData\\Material_Area\\httphighered.mheducation.com\\robots.txt");
		pbt.ProcessAllPages(110l,1000000000l,"D:\\KBQA\\SpiderData\\Material_Area\\httphighered.mheducation.com\\index110_to1000000000.log");
		
		//pbt
	}
	
	
}
