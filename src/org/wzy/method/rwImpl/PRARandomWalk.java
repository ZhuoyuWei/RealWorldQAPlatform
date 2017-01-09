package org.wzy.method.rwImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.wzy.meta.GroundPath;
import org.wzy.meta.KNode;
import org.wzy.method.RandomWalkInter;

public class PRARandomWalk implements RandomWalkInter
{

	Map<Integer, KNode> Kgraph;
	Random rand=new Random();
	
	
	
	@Override
	public void SetKGraph(Map<Integer, KNode> Kgraph) {
		// TODO Auto-generated method stub
		this.Kgraph=Kgraph;
		IntiPRA();
	}
	
	@Override
	public List<GroundPath> RandomWalk(int startnode, int endnode, int maxLength, int maxRound) {
		// TODO Auto-generated method stub
		
		List<GroundPath> pathList=new ArrayList<GroundPath>();
		for(int round=0;round<maxRound;round++)
		{
			GroundPath path=OneRoundWalk(startnode,endnode,maxLength);
			if(path!=null)
				pathList.add(path);
		}
		return pathList;
	}
	
	public void IntiPRA()
	{
		long start=System.currentTimeMillis();
		Iterator it=Kgraph.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			KNode knode=(KNode)entry.getValue();
			knode.BuildRelTypeIndex();
		}
		long end=System.currentTimeMillis();
		System.out.println("PRA init at "+(end-start)+" ms");
	}
	
	
	public GroundPath OneRoundWalk(int startnode,int endnode, int maxLength)
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
				return null;
			}
			List<Integer> relList=knode.neighbors.get(endnode);
			if(relList!=null&&relList.size()>0)
			{
				path.entityList.add(endnode);
				if(relList.size()>1)
				{
					int index=RandomChooseRel(knode,relList);
					if(index<0)
						return null;
					path.relationList.add(relList.get(index));
				}
				else if(relList.size()==1)
				{
					path.relationList.add(relList.get(0));
				}
				return path;
			}
			
			//random next
			int index=RandomChooseNextNode(knode,path);
			if(index<0)
			{
				return null;
			}
			else
			{
				path.relationList.add(knode.edges[index][0]);
				path.entityList.add(knode.edges[index][1]);
				path.entitySet.add(knode.edges[index][1]);
			}
		}
		
		if(path.entityList.get(path.entityList.size()-1).equals(endnode))
			return path;
		else
			return null;
	}
	
	public int RandomChooseRel(KNode node,List<Integer> relList)
	{
		double[] probabilities=new double[relList.size()];
		probabilities[0]=1./node.reltype2nextnum.get(relList.get(0));
		for(int i=1;i<relList.size();i++)
		{
			probabilities[i]=probabilities[i-1]+1./node.reltype2nextnum.get(relList.get(i));
		}
		double index=rand.nextDouble()*probabilities[relList.size()-1];
		for(int i=0;i<probabilities.length;i++)
		{
			if(index<probabilities[i])
				return i;
		}
		return -1;
	}
	
	public int RandomChooseNextNode(KNode node,GroundPath path)
	{
		if(node.edges.length<=1)
		{
			if(path.entitySet.contains(node.edges[0][1]))
				return -1;
			else
				return 0;
		}
		
		
		double[] probs=new double[node.edges.length];
		for(int i=0;i<node.edges.length;i++)
		{
			if(path.entitySet.contains(node.edges[i][1]))
				probs[i]=0.;
			else
				probs[i]=1./node.reltype2nextnum.get(node.edges[i][0]);
		}
		
		for(int i=1;i<node.edges.length;i++)
		{
			probs[i]+=probs[i-1];
		}
		
		double index=rand.nextDouble()*probs[probs.length-1];
		for(int i=0;i<probs.length;i++)
		{
			if(index<probs[i])
				return i;
		}
		return -1;
		
		
	}


}
