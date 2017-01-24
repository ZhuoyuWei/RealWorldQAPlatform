package org.wzy.method;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.wzy.meta.ConceptPath;
import org.wzy.meta.GroundPath;
import org.wzy.meta.KNode;
import org.wzy.meta.NELink;
import org.wzy.meta.Question;

public class KBModel {
	
	public static final int reverseEdge=100000;
	public Map<String,Integer> entity2id;
	public Map<String,Integer> relation2id;
	public List<String> id2entity;
	public List<String> id2relation;
	
	public Map<Integer,List<int[]>> Kgraph;
	public Map<Integer,KNode> KnGraph;
	
	public EntityLinkInter entity_linker;
	public RandomWalkInter random_walker;
	public int MaxPathLength=3;
	public int MaxRound=100;
	

	
	//for debug, check what the path looks like
	public boolean ground_debug=true;
	public boolean concept_debug=false;
	public Map<String,String> mid2name;
	public PrintStream logps=null;
	public int pathcount=0;
	
	public int entitysize=0;
	public int relationsize=0;
	
	public void ReadEntityNameAndMid(String filename,String code) throws IOException
	{
		mid2name=new HashMap<String,String>();
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=2)
				continue;
			mid2name.put(ss[0], ss[1]);
		}
		br.close();
	}
	
/*	public Map<String,Integer> ReadName2IdMap(String filename,String code) throws IOException
	{
		Map<String,Integer> map=new HashMap<String,Integer>();
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=2)
				continue;
			Integer index=map.get(ss[0]);
			if(index==null)
			{
				map.put(ss[0], Integer.parseInt(ss[1]));
			}
		}
		br.close();
		return map;
	}
	
	public List<String> ReadId2MidMap(String filename,String code) throws IOException
	{
		List<String> list=new ArrayList<String>();
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=2)
				continue;
			list.add(ss[0]);
		}
		br.close();
		return list;
	}*/
	
	// remain some bugs, commented at 1.5 by wzy. id2entity only has 4000 entities
	public void ReadEntityList(String filename,String code) throws IOException
	{
		entity2id=new HashMap<String,Integer>();
		id2entity=new ArrayList<String>();

		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=2)
				continue;
			entity2id.put(ss[0], Integer.parseInt(ss[1]));
			id2entity.add(ss[0]);
			
		}
		
		br.close();
	}	
	
	public void ReadEntityListOneCol(String filename,String code) throws IOException
	{
		entity2id=new HashMap<String,Integer>();
		id2entity=new ArrayList<String>();

		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");
			entity2id.put(ss[0], id2entity.size());
			id2entity.add(ss[0]);
		}
		br.close();
	}	
	public void ReadEntityListSecondCol(String filename,String code,int colindex) throws IOException
	{
		entity2id=new HashMap<String,Integer>();
		id2entity=new ArrayList<String>();

		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("[\\s]+");
			if(ss.length==2)
			{
				entity2id.put(ss[colindex].toLowerCase(), id2entity.size());
				id2entity.add(ss[colindex].toLowerCase());
			}
		}
		br.close();
	}		
	
	
	public void ReadRelationList(String filename,String code) throws IOException
	{
		relation2id=new HashMap<String,Integer>();
		id2relation=new ArrayList<String>();

		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		int tmprelationsize=0;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=2)
				continue;
			int relindex=Integer.parseInt(ss[1]);
			relation2id.put(ss[0], relindex);
			id2relation.add(ss[0]);
			
			if(tmprelationsize<relindex)
				tmprelationsize=relindex;
		}
		tmprelationsize+=1;
		if(relationsize<tmprelationsize)
		{
			relationsize=tmprelationsize;
		}
		br.close();
	}		

	
	public Map<Integer,List<int[]>> ReadTripeltGraph(String filename,String code) throws IOException
	{
		Map<Integer,List<int[]>> graph=new HashMap<Integer,List<int[]>>();
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		int edgecount=0;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=3)
				continue;
			int[] triplet=new int[3];
			for(int i=0;i<3;i++)
			{
				triplet[i]=Integer.parseInt(ss[i]);
			}
			
			for(int i=0;i<3;i+=2)
			{
				//only direct
				if(i>0)
					break;
				List<int[]> neighborList=graph.get(triplet[i]);
				if(neighborList==null)
				{
					neighborList=new ArrayList<int[]>();
					graph.put(triplet[i], neighborList);
				}
				int[] edge=new int[2];
				if(i==0)
				{
					edge[0]=triplet[1];
					edge[1]=triplet[2];
				}
				else
				{
					edge[0]=triplet[1]+reverseEdge;
					edge[1]=triplet[0];
				}
				neighborList.add(edge);
				edgecount++;
				
			}
			
			//max entity index and max relation index
			if(triplet[0]>entitysize)
				entitysize=triplet[0];
			if(triplet[2]>entitysize)
				entitysize=triplet[2];
			if(triplet[1]>relationsize)
				relationsize=triplet[1];
		}
		entitysize+=1;
		relationsize+=1;
		br.close();
		
		System.out.println("edges in knowledge graph is "+edgecount);
		
		return graph;
	}
	
	public void ChangeKGraph2KnGraph(boolean deleteKgraph)
	{
		KnGraph=new HashMap<Integer,KNode>();
		Iterator it=Kgraph.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			KNode knode=new KNode();
			knode.entity=(Integer)entry.getKey();
			knode.edges=((List<int[]>)entry.getValue()).toArray(new int[0][]);
			for(int i=0;i<knode.edges.length;i++)
			{
				//knode.neighbors.add(knode.edges[i][1]);
				//knode.neighbors.put(knode.edges[i][1], knode.edges[i][0]);
				List<Integer> list=knode.neighbors.get(knode.edges[i][1]);
				if(list==null)
				{
					list=new ArrayList<Integer>();
					knode.neighbors.put(knode.edges[i][1], list);
				}
				list.add(knode.edges[i][0]);
			}
			KnGraph.put(knode.entity,knode);
		}
		if(deleteKgraph)
		{
			Kgraph=null;
		}
	}
	
	
	////////////////////////////Entity Linking///////////////////////////////
	
	
	////////////////////////////Random Walk/////////////////////////////////

	public static int has_entity_count=0;
	////////////////////////Process a Question and an Answer///////////////////////
	public ConceptPath[] MiningPaths(Question question,int answerIndex)
	{

		
		
		List<NELink> qllist=entity_linker.LinkingString(question.question_content);
		List<NELink> allist=entity_linker.LinkingString(question.answers[answerIndex]);
		
		if(qllist.size()>0&&allist.size()>0)
		{
			has_entity_count++;
		}
		
		List<GroundPath> pathList=new ArrayList<GroundPath>();
		
		//policy 1, when question and answer both have NELink
		if(!qllist.isEmpty()&&!allist.isEmpty())
		{
			for(int i=0;i<qllist.size();i++)
			{
				for(int j=0;j<allist.size();j++)
				{
					if(qllist.get(i).kbId==allist.get(j).kbId)
						continue;
					List<GroundPath> tmp_pathList=random_walker.RandomWalk(qllist.get(i).kbId,allist.get(j).kbId,MaxPathLength,MaxRound);
					pathList.addAll(tmp_pathList);
				}
			}
		}
		//policy 2, when at least one of question and answer has no NELink
		/*else
		{
			qllist.addAll(allist);
			for(int i=0;i<qllist.size();i++)
			{
				for(int j=i+1;j<qllist.size();j++)
				{
					if(qllist.get(i).kbId==qllist.get(j).kbId)
						continue;
					List<GroundPath> tmp_pathList=random_walker.RandomWalk(qllist.get(i).kbId,qllist.get(j).kbId,MaxPathLength,MaxRound);
					pathList.addAll(tmp_pathList);
				}
			}
		}*/
		
		//debug by wzy at 12.15, check ground path
		if(ground_debug)
		{
			/*logps.println("*********"+question.questionID+"**************** "+answerIndex+" ***************"+pathList.size());
			for(int i=0;i<pathList.size();i++)
			{
				GroundPath path=pathList.get(i);
				int j=0;
				for(;j<path.relationList.size();j++)
				{
					String entityName=id2entity.get(path.entityList.get(j));
					String relationName=null;
					if(path.relationList.get(j)>=reverseEdge)
						relationName="-"+id2relation.get(path.relationList.get(j)-reverseEdge);
					else
						relationName=id2relation.get(path.relationList.get(j));
					logps.print(entityName+"\t"+relationName+"\t");
				}
				String entityName=id2entity.get(path.entityList.get(j));
				logps.println(entityName);
			}*/
			
			int maxcount=pathList.size()<5?pathList.size():5;
			for(int i=0;i<maxcount;i++)
			{
				GroundPath path=pathList.get(i);
				int j=0;
				for(;j<path.relationList.size();j++)
				{
					logps.print(path.entityList.get(j)+"\t"+path.relationList.get(j)+"\t");
				}
				logps.println(path.entityList.get(j));
			}
			
		}
		
		//concepting paths
		Map<ConceptPath,ConceptPath> pathMap=new HashMap<ConceptPath,ConceptPath>();
		for(int i=0;i<pathList.size();i++)
		{
			ConceptPath cpath=pathList.get(i).GetConceptPath();
			ConceptPath path_inmap=pathMap.get(cpath);
			if(path_inmap==null)
			{
				path_inmap=cpath;
				pathMap.put(cpath, path_inmap);
			}
			path_inmap.count++;
		}
		
		ConceptPath[] paths=new ConceptPath[pathMap.size()];
		Iterator it=pathMap.entrySet().iterator();
		int count=0;
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			paths[count++]=(ConceptPath)entry.getKey();
		}
		
		
		if(concept_debug)
		{
			logps.println("*********"+question.questionID+"**************** "+answerIndex+" ***************"+pathList.size());
			for(int i=0;i<paths.length;i++)
			{
				ConceptPath path=paths[i];
				int j=0;
				for(;j<path.relationList.size();j++)
				{
					String relationName=null;
					/*if(path.relationList.get(j)>=reverseEdge)
						relationName="-"+id2relation.get(path.relationList.get(j)-reverseEdge);
					else
						relationName=id2relation.get(path.relationList.get(j));
					logps.print(relationName+"\t");*/
					logps.print(path.relationList.get(j));
					
				}
				logps.println();
			}
		}
		
		return paths;
	}
	

	
	public static void main(String[] args) throws IOException
	{
		PreProcessMid2Id(args);
		/*long start=System.currentTimeMillis();
		KBModel kbm=new KBModel();
		kbm.Kgraph=kbm.ReadTripeltGraph(args[0], "utf8");
		long end=System.currentTimeMillis();
		
		Runtime runtime=Runtime.getRuntime();
		System.out.println("处理器的数目"+runtime.availableProcessors());
		System.out.println("空闲内存量："+runtime.freeMemory()/ 1024L/1024L + "M av");
		System.out.println("使用的最大内存量："+runtime.maxMemory()/ 1024L/1024L + "M av");
		System.out.println("内存总量："+runtime.totalMemory()/ 1024L/1024L + "M av");
		
		System.out.println("Time: "+(end-start)+"ms");*/
	}
	
	public void BuildLinkBetweenNameAndId(String inputfile,String outputfile)
	{
		
	}
	
	public static void PreProcessMid2Id(String[] args) throws IOException
	{
		KBModel kbm=new KBModel();
		
		kbm.ReadAndIndexEntityAndRelation(args[0], args[1]);
		kbm.ReadEntityNameAndMid(args[2], args[1]);
		kbm.PrintString2IdListForEntity(kbm.entityList, args[3], args[1]);
		kbm.PrintString2IdList(kbm.entityList, args[4], args[1]);
		kbm.PrintString2IdList(kbm.relationList, args[5], args[1]);
		kbm.ReWriterTriplets(args[0], args[6], args[1]);
		//kbm.ReWriterTriplets(args[0]+".filted", args[5]+".filted", args[1]);
		
		
		
	}
	
	Map<String,Integer> entityMap=new HashMap<String,Integer>();
	Map<String,Integer> relationMap=new HashMap<String,Integer>();
	List<String> entityList=new ArrayList<String>();
	List<String> relationList=new ArrayList<String>();
	
	public void ReadAndIndexEntityAndRelation(String filename,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filename),code));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=3)
				continue;
			for(int i=0;i<3;i+=2)
			{
				Integer index=entityMap.get(ss[i]);
				if(index==null)
				{
					entityMap.put(ss[i],entityList.size());
					entityList.add(ss[i]);
				}
			}
			Integer index=relationMap.get(ss[1]);
			if(index==null)
			{
				relationMap.put(ss[1], relationList.size());
				relationList.add(ss[1]);
			}
		}
	}
	
	public void PrintString2IdListForEntity(List<String> list,String filename,String code) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,code);
		for(int i=0;i<list.size();i++)
		{
			String name=mid2name.get(list.get(i));
			if(name==null)
			{
				//name=list.get(i);
				continue;
			}
			pw.println(name+"\t"+i);
		}
		pw.close();
	}	
	public void PrintString2IdList(List<String> list,String filename,String code) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter pw=new PrintWriter(filename,code);
		for(int i=0;i<list.size();i++)
		{
			pw.println(list.get(i)+"\t"+i);
		}
		pw.close();
	}
	
	public void ReWriterTriplets(String inputfile,String outputfile,String code) throws IOException
	{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(inputfile),code));
		String buffer=null;
		PrintWriter pw=new PrintWriter(outputfile,code);
		int readcount=0;
		int writecount=0;
		long start=System.currentTimeMillis();
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			if(ss.length!=3)
				continue;
			readcount++;
			Integer entity1=entityMap.get(ss[0]);
			if(entity1==null)
				continue;
			Integer relation=relationMap.get(ss[1]);
			if(relation==null)
				continue;
			Integer entity2=entityMap.get(ss[2]);
			if(entity2==null)
				continue;
			pw.println(entity1+"\t"+relation+"\t"+entity2);
			writecount++;
			
		}
		
		pw.close();
		br.close();
		long end=System.currentTimeMillis();
		
		System.out.println("Change triplets "+readcount+"\t"+writecount+"\t"+(end-start)+"ms");
		
	}
	
	
}


