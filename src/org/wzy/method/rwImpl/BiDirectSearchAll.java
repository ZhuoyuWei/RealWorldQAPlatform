package org.wzy.method.rwImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.wzy.meta.GroundPath;
import org.wzy.meta.KNode;
import org.wzy.method.RandomWalkInter;

class BFSNode
{
	public int nodeid;
	public int parent;
	public int color;
	public int length;
}

public class BiDirectSearchAll implements RandomWalkInter{

	Map<Integer, KNode> Kgraph;
	
	@Override
	public void SetKGraph(Map<Integer, KNode> Kgraph) {
		// TODO Auto-generated method stub
		this.Kgraph=Kgraph;
	}	
	
	@Override
	public List<GroundPath> RandomWalk(int startnode, int endnode, int maxLength, int maxRound) {
		// TODO Auto-generated method stub
		
		List<GroundPath> pathList=new ArrayList<GroundPath>();
		
		int startLength=maxLength/2;
		int endLength=maxLength-startLength;
		
		Map<Integer,BFSNode> startTree=BFS(startnode,startLength);
		Map<Integer,BFSNode> endTree=BFS(endnode,endLength);
		
		if(startTree.size()>endTree.size())
		{
			Object t=startTree;
			startTree=endTree;
			endTree=(Map<Integer,BFSNode>)t;
		}
		
		Iterator it=startTree.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			Integer startindex=(Integer)entry.getKey();
			BFSNode endNode=endTree.get(startindex);
			if(endNode!=null)
			{
				BFSNode startNode=(BFSNode)entry.getValue();
				
				List<Integer> startList=GetEntityPathFromBFSNode(startTree,startNode,true);
				List<Integer> endList=GetEntityPathFromBFSNode(endTree,endNode,false);
				for(int i=1;i<endList.size();i++)
				{
					startList.add(endList.get(i));
				}
				
				Set<Integer> nodeSet=new HashSet<Integer>();
				nodeSet.addAll(startList);
				if(nodeSet.size()<startList.size())
					continue;
				
				List<GroundPath> tmpList=this.GeneGroundPathsFromEntityPath(startList);
				pathList.addAll(tmpList);
				
			}
		}
		
		
		return pathList;
	}
	
	public List<Integer> GetEntityPathFromBFSNode(Map<Integer,BFSNode> tree,BFSNode node,boolean reverse)
	{	
		List<Integer> indexList=new ArrayList<Integer>();
		BFSNode tmpNode=node;
		while(true)
		{
			if(tmpNode==null)
			{
				break;
			}
			indexList.add(tmpNode.nodeid);
			
			if(tmpNode.parent<0)
				break;
			tmpNode=tree.get(tmpNode.parent);
		}	
		
		if(reverse)
		{
			List<Integer> tmpList=indexList;
			indexList=new ArrayList<Integer>();
			for(int i=tmpList.size()-1;i>=0;i--)
			{
				indexList.add(tmpList.get(i));
			}
		}
		
		return indexList;
	}
	
	public List<GroundPath> GeneGroundPathsFromEntityPath(List<Integer> entityList)
	{
		List<GroundPath> pathList=new ArrayList<GroundPath>();
		GroundPath gpath=new GroundPath();
		gpath.entityList.add(entityList.get(0));
		pathList.add(gpath);
		for(int i=1;i<entityList.size();i++)
		{
			KNode prenode=Kgraph.get(entityList.get(i-1));
			List<Integer> relList=prenode.neighbors.get(entityList.get(i));
			if(relList==null||relList.size()<=0)
			{
				pathList.clear();
				return pathList;
			}
			else if(relList.size()==1)
			{
				for(int j=0;j<pathList.size();j++)
				{
					pathList.get(j).entityList.add(entityList.get(i));
					pathList.get(j).relationList.add(relList.get(0));
				}
			}
			else
			{
				List<GroundPath> tmpList=pathList;
				pathList=new ArrayList<GroundPath>();
				for(int j=0;j<tmpList.size();j++)
				{
					for(int k=0;k<relList.size();k++)
					{
						GroundPath path=tmpList.get(j).CopyOne();
						path.entityList.add(entityList.get(i));
						path.relationList.add(relList.get(k));
						pathList.add(path);
					}
				}				
			}
		}
		return pathList;
	}
	
	//public void ProducePathDFS()
	
	public Map<Integer,BFSNode> BFS(int startNode,int maxLength)
	{
		Map<Integer,BFSNode> visitedNodes=new HashMap<Integer,BFSNode>();
		
		BFSNode node=new BFSNode();
		node.nodeid=startNode;
		node.parent=-1;
		node.length=0;
		node.color=0;
		visitedNodes.put(startNode, node);
		
		Queue<Integer> queue=new LinkedList<Integer>();
		queue.add(startNode);
		
		while(!queue.isEmpty())
		{
			int nodeindex=queue.poll();
			node=visitedNodes.get(nodeindex);
			node.color=1;
			
			KNode knode=Kgraph.get(node.nodeid);
			if(knode!=null&&node.length<maxLength)
			{
				for(int i=0;i<knode.edges.length;i++)
				{
					BFSNode childnode=visitedNodes.get(knode.edges[i][1]);
					if(childnode==null)
					{
						childnode=new BFSNode();
						childnode.nodeid=knode.edges[i][1];
						childnode.parent=node.nodeid;
						childnode.length=node.length+1;
						childnode.color=0;
						visitedNodes.put(childnode.nodeid, childnode);
						queue.add(childnode.nodeid);
					}
				}
			}
			node.color=2;		
		}
		
		return visitedNodes;
	}
	
	


}
