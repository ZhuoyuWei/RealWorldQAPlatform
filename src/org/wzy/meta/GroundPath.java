package org.wzy.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroundPath {
	
	public List<Integer> entityList=new ArrayList<Integer>();
	public List<Integer> relationList=new ArrayList<Integer>();	
	public Set<Integer> entitySet=new HashSet<Integer>();
	
	public ConceptPath GetConceptPath()
	{
		ConceptPath cpath=new ConceptPath();
		cpath.relationList=new ArrayList<Integer>(relationList);
		return cpath;
	}
	
	public GroundPath CopyOne()
	{
		GroundPath path=new GroundPath();
		path.entityList=new ArrayList<Integer>(entityList);
		path.relationList=new ArrayList<Integer>(relationList);
		path.entitySet=new HashSet<Integer>(entitySet);
		return path;
	}
}
