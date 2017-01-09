package org.wzy.method;

import java.util.List;
import java.util.Map;

import org.wzy.meta.GroundPath;
import org.wzy.meta.KNode;

public interface RandomWalkInter {
	
	public List<GroundPath>  RandomWalk(int startnode,int endnode,int maxLength,int maxRound);
	public void SetKGraph(Map<Integer,KNode> Kgraph);
}
