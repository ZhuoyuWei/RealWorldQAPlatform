package org.wzy.method.elImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wzy.meta.NELink;
import org.wzy.method.EntityLinkInter;

public class ChineseMaxMatchLinker implements EntityLinkInter{

	public Map<String, Integer> entity2id;
	public Map<String, Integer> relation2id;
	
	public int minlength=1;
	public int maxlength=8;

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
		
		for(int i=0;i<text.length();i++)
		{
			int j=i+maxlength-1;
			if(j>=text.length())
				j=text.length()-1;
			boolean flag=false;
			for(;j>=i;j--)
			{
				if(j-i+1<minlength)
					break;	
				String subtext=text.substring(i, j+1);
				Integer id=entity2id.get(subtext);
				if(id!=null)
				{
					NELink nelink=new NELink();
					nelink.kbId=id;
					nelink.source_str=subtext;
					nelink.surface_name=subtext;
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
