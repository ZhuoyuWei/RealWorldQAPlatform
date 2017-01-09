package org.wzy.meta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;




public class KNode {
	public int entity;
	public int[][] edges;
	public Map<Integer,List<Integer>> neighbors=new HashMap<Integer,List<Integer>>();
	//public Map<Integer,Double> reltype2prob;
	public Map<Integer,Integer> reltype2nextnum;
	

	
	public void BuildRelTypeIndex()
	{
		reltype2nextnum=new HashMap<Integer,Integer>();
		for(int i=0;i<edges.length;i++)
		{
			Integer count=reltype2nextnum.get(edges[i][0]);
			if(count==null)
			{
				reltype2nextnum.put(edges[i][0], 1);
			}
			else
			{
				count++;
				reltype2nextnum.remove(edges[i][0]);
				reltype2nextnum.put(edges[i][0], count);
			}
		}
		/*reltype2prob=new HashMap<Integer,Double>();
		Iterator it=reltype2nextnum.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			Integer rel=(Integer)entry.getKey();
			Integer count=Integer
		}*/
	}
}
