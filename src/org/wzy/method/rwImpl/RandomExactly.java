package org.wzy.method.rwImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.wzy.meta.GroundPath;
import org.wzy.meta.KNode;
import org.wzy.method.RandomWalkInter;

public class RandomExactly implements RandomWalkInter{
	
	Map<Integer, KNode> Kgraph;
	Random rand=new Random();
	
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
			GroundPath path=OneRoundWalk(startnode,endnode,maxLength);
			if(path!=null)
				pathList.add(path);
		}
		return pathList;
	}



	public GroundPath OneRoundWalk(int startnode,int endnode,int maxLength)
	{
		GroundPath path=new GroundPath();
		path.entityList.add(startnode);
		DFS(path,endnode,maxLength);
		if(path.entityList.size()>0&&path.entityList.get(path.entityList.size()-1)==endnode)
			return path;
		else
			return null;
	}


	public void DFS(GroundPath path,int endnode,int maxLength)
	{
		int s=path.entityList.get(path.entityList.size()-1);
		KNode knode=Kgraph.get(s);
		if(knode==null)
		{
			path.entityList.clear();
			return;
		}
		List<Integer> relList=knode.neighbors.get(endnode);
		if(relList!=null)
		{
			path.entityList.add(endnode);
			if(relList.size()>1)
			{
				int index=rand.nextInt(relList.size());
				path.relationList.add(relList.get(index));
			}
			else if(relList.size()==1)
			{
				path.relationList.add(relList.get(0));
			}
			else
			{
				path.entityList.clear();
			}
			return;
		}
		
		if(path.relationList.size()<maxLength)
		{
			// random walk
			if(knode.edges.length<maxLength*2)
			{
				List<Integer> candidateList=new ArrayList<Integer>();
				for(int i=0;i<knode.edges.length;i++)
				{
					if(!path.entitySet.contains(knode.edges[i][1]))
					{
						candidateList.add(i);
					}
				}
				if(candidateList.size()>0)
				{
					int index=rand.nextInt(candidateList.size());
					path.entityList.add(knode.edges[index][1]);
					path.relationList.add(knode.edges[index][0]);
					DFS(path,endnode,maxLength);
				}
				else
				{
					path.entityList.clear();
				}
			}
			else
			{
				int index=rand.nextInt(knode.edges.length);
				while(true)
				{
					int[] rn=knode.edges[index];
					if(path.entitySet.contains(rn[1]))
					{
						index=rand.nextInt(knode.edges.length);
					}
					else
					{
						path.entityList.add(rn[1]);
						path.relationList.add(rn[0]);
						break;
					}
				}
				DFS(path,endnode,maxLength);
			}
		}
		
	}




}
