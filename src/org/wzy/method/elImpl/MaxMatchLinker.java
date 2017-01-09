package org.wzy.method.elImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wzy.meta.NELink;
import org.wzy.method.EntityLinkInter;

public class MaxMatchLinker implements EntityLinkInter{

	Map<String, Integer> entity2id;
	Map<String, Integer> relation2id;
	
	public int minlength=1;
	public int maxlength=4;

	@Override
	public void SetEntityAndRelationMap(Map<String, Integer> entity2id, Map<String, Integer> relation2id) {
		// TODO Auto-generated method stub
		
		this.entity2id=entity2id;
		this.relation2id=relation2id;
		
	}	
	
	@Override
	public List<NELink> LinkingString(String text) {
		// TODO Auto-generated method stub
		
		List<NELink> neList=new ArrayList<NELink>();
		String[] ss=text.split("[\\s]+");
		
		for(int i=0;i<ss.length;i++)
		{
			int j=i+maxlength-1;
			if(j>=ss.length)
				j=ss.length-1;
			boolean flag=false;
			for(;j>=i;j--)
			{
				if(j-i+1<minlength)
					break;
				StringBuilder sb=new StringBuilder();
				sb.append(ss[i]);
				for(int k=i+1;k<=j;k++)
				{
					sb.append("_");
					sb.append(ss[k]);
				}
				String tmp=sb.toString().toLowerCase();
				
				Integer id=entity2id.get(tmp);
				if(id!=null)
				{
					NELink nelink=new NELink();
					nelink.kbId=id;
					nelink.source_str=tmp;
					nelink.surface_name=tmp;
					neList.add(nelink);
					flag=true;
					break;
				}
			}
			
			if(flag)
			{
				i=j;
			}
		}
		
		return neList;
	}


}
