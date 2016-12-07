package org.wzy.tool;

public class StringTool {

	public static String JoinStrings(String[] strs,String join)
	{
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<strs.length;i++)
		{
			if(i==strs.length-1)
			{
				sb.append(strs[i]);
			}
			else
			{
				sb.append(strs[i]).append(join);
			}
		}
		return sb.toString();
	}
	public static String JoinStrings(String[] strs,int startindex,String join)
	{
		StringBuilder sb=new StringBuilder();
		for(int i=startindex;i<strs.length;i++)
		{
			if(i==strs.length-1)
			{
				sb.append(strs[i]);
			}
			else
			{
				sb.append(strs[i]).append(join);
			}
		}
		return sb.toString();
	}	
}
