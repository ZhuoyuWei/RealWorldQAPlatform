package org.wzy.method.elImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wzy.meta.NELink;
import org.wzy.method.EntityLinkInter;

public class NgramEntityLinker implements EntityLinkInter{

	
	public Map<String,Integer> entity2id;
	public Map<String,Integer> relation2id;
	
	public int minlength=1;
	public int maxlength=3;
	
	
	@Override
	public List<NELink> LinkingString(String text) {
		// TODO Auto-generated method stub
		List<NELink> neList=new ArrayList<NELink>();

		String[] ss=text.split("[\\s]+");
		
		for(int i=minlength;i<=maxlength;i++)
		{
			for(int j=0;j<ss.length-i;j++)
			{
				StringBuilder sb=new StringBuilder();
				sb.append(ss[j]);
				for(int k=1;k<i;k++)
				{
					sb.append("_");
					sb.append(ss[j+k]);
				}
				String tmp=sb.toString().toLowerCase();
				//String mid=name2mid.get(tmp);
				Integer id=entity2id.get(tmp);
				if(id!=null)
				{
					NELink nelink=new NELink();
					nelink.kbId=id;
					nelink.source_str=tmp;
					nelink.surface_name=tmp;
					neList.add(nelink);
				}
			}
		}
		
		return neList;
	}
	
	@Override
	public void SetEntityAndRelationMap(Map<String, Integer> entity2id, Map<String, Integer> relation2id) {
		// TODO Auto-generated method stub
		this.entity2id=entity2id;
		this.relation2id=relation2id;
	}	

}
