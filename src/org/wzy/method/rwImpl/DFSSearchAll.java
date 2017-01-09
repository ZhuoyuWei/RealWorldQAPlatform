package org.wzy.method.rwImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wzy.meta.GroundPath;
import org.wzy.meta.KNode;
import org.wzy.method.RandomWalkInter;

public class DFSSearchAll implements RandomWalkInter{

	Map<Integer, KNode> Kgraph;
	
	int MaxNeighbors=100;
	
	@Override
	public void SetKGraph(Map<Integer, KNode> Kgraph) {
		// TODO Auto-generated method stub
		this.Kgraph=Kgraph;
	}
	
	@Override
	public List<GroundPath> RandomWalk(int startnode, int endnode, int maxLength, int maxRound) {
		// TODO Auto-generated method stub
		
		List<GroundPath> pathList=new ArrayList<GroundPath>();
		
		GroundPath path=new GroundPath();
		path.entityList.add(startnode);
		
		DFS(path,endnode,maxLength,pathList);
		
		
		return pathList;
	}
	
	public void DFS(GroundPath path,int endnode,int maxLength,List<GroundPath> pathList)
	{
		int s=path.entityList.get(path.entityList.size()-1);
		KNode knode=Kgraph.get(s);
		if(knode==null)
		{
			return;
		}	
		int count=0;
		for(int i=0;i<knode.edges.length;i++)
		{
			if(knode.edges[i][1]==endnode)
			{
				GroundPath savepath=path.CopyOne();
				savepath.entityList.add(knode.edges[i][1]);
				savepath.relationList.add(knode.edges[i][0]);
				pathList.add(savepath);
			}
			else if(path.relationList.size()<maxLength)
			{
				
				if(path.entitySet.contains(knode.edges[i][1]))
					continue;
				
				path.entityList.add(knode.edges[i][1]);
				path.entitySet.add(knode.edges[i][1]);
				path.relationList.add(knode.edges[i][0]);
				
				DFS(path,endnode,maxLength,pathList);
				
				path.entityList.remove(path.entityList.size()-1);
				path.entitySet.remove(knode.edges[i][1]);
				path.relationList.remove(path.relationList.size()-1);
				
				count++;
				if(count>MaxNeighbors)
				{
					break;
				}
			}
		}
	}





}
