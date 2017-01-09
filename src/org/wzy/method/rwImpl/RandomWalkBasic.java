package org.wzy.method.rwImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.wzy.meta.GroundPath;
import org.wzy.meta.KNode;
import org.wzy.method.RandomWalkInter;

public class RandomWalkBasic implements RandomWalkInter
{

	public Map<Integer, KNode> Kgraph;
	public Random rand=new Random();
	public int MaxTimes=10;

	
	
	@Override
	public void SetKGraph(Map<Integer, KNode> Kgraph) {
		// TODO Auto-generated method stub
		this.Kgraph=Kgraph;
		
	}
	
	@Override
	public List<GroundPath> RandomWalk(int startnode, int endnode, int maxLength, int maxRound) {
		// TODO Auto-generated method stub
		
		List<GroundPath> pathList=new ArrayList<GroundPath>();
		for(int round=0;round<maxRound;round++)
		{
			OneRoundWalk(startnode,endnode,maxLength,pathList);
		}
		return pathList;
	}
	
	
	
	
	public void OneRoundWalk(int startnode,int endnode, int maxLength,List<GroundPath> pathList)
	{
		GroundPath path=new GroundPath();
		path.entityList.add(startnode);
		path.entitySet.add(startnode);
		
		for(int i=0;i<maxLength;i++)
		{
			int s=path.entityList.get(i);
			KNode knode=Kgraph.get(s);
			if(knode==null||knode.edges.length<=0)
			{
				path.entityList.clear();
				return;
			}
			List<Integer> relList=knode.neighbors.get(endnode);
			if(relList!=null&&relList.size()>0)
			{
				path.entityList.add(endnode);
				if(relList.size()>1)
				{
					int index=RandomChooseRel(knode,relList);
					if(index<0)
						return;
					path.relationList.add(relList.get(index));
				}
				else if(relList.size()==1)
				{
					path.relationList.add(relList.get(0));
				}
				//return path;
				pathList.add(path);
				path.entityList.remove(path.entityList.size()-1);
				path.relationList.remove(path.relationList.size()-1);
			}
			
			//random next
			int index=RandomChooseNextNode(knode,path,endnode);
			if(index<0)
			{
				return;
			}
			else
			{
				path.relationList.add(knode.edges[index][0]);
				path.entityList.add(knode.edges[index][1]);
				path.entitySet.add(knode.edges[index][1]);
			}
		}
		
	}
	
	public int RandomChooseRel(KNode node,List<Integer> relList)
	{
		if(relList.size()>1)
			return rand.nextInt(relList.size());
		else if(relList.size()==1)
			return 0;
		else
			return -1;
	}
	
	public int RandomChooseNextNode(KNode node,GroundPath path,int endnode)
	{
		if(node.edges.length<=1)
		{
			if(path.entitySet.contains(node.edges[0][1]))
				return -1;
			else
				return 0;
		}
		
		for(int i=0;i<MaxTimes;i++)
		{
			int randindex=rand.nextInt(node.edges.length);
			if(!path.entitySet.contains(node.edges[randindex][1])&&node.edges[randindex][1]!=endnode)
				return randindex;
		}
		return -1;
		
	}


}

