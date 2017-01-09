package org.wzy.meta;

import java.util.ArrayList;
import java.util.List;

public class ConceptPath {
	
	public List<Integer> relationList=new ArrayList<Integer>();	
	
	public int count=0;
	
	@Override
	public int hashCode()
	{
		int sum=0;
		if(relationList!=null)
		{
			for(int i=0;i<relationList.size();i++)
			{
				sum*=31;
				sum+=relationList.get(i);
			}
		}
		return sum;
	}
	
	public boolean equals(Object o)
	{
		ConceptPath cp=(ConceptPath)o;
		if(cp==null)
			return false;
		if(cp.relationList==null)
		{
			return relationList==null;
		}
		if(relationList.size()!=cp.relationList.size())
			return false;
		for(int i=0;i<relationList.size();i++)
		{
			if(!relationList.get(i).equals(cp.relationList.get(i)))
				return false;
		}
		return true;
	}
}
