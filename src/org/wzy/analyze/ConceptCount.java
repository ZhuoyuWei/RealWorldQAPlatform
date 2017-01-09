package org.wzy.analyze;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wzy.meta.*;
import org.wzy.method.KBModel;
import org.wzy.tool.IOTool;

class PathForSort implements Comparator
{
	ConceptPath path;
	int count;
	@Override
	public int compare(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		
		PathForSort p0=(PathForSort)arg0;
		PathForSort p1=(PathForSort)arg1;
			
		return p1.count-p0.count;
	}
}

public class ConceptCount {

	
	public Map<ConceptPath,Set<String>> path2question;
	public List<String> id2rel;
	
	public void BuildConceptPathMap(List<Question> qList)
	{
		path2question=new HashMap<ConceptPath,Set<String>>();
		for(int i=0;i<qList.size();i++)
		{
			for(int j=0;j<qList.get(i).ans_paths.length;j++)
			{
				for(int k=0;k<qList.get(i).ans_paths[j].length;k++)
				{
					ConceptPath path=qList.get(i).ans_paths[j][k];
					Set<String> qset=path2question.get(path);
					if(qset==null)
					{
						qset=new HashSet<String>();
						path2question.put(path,qset);
					}
					qset.add(qList.get(i).questionID);
				}
			}
		}
		
	}
	
	public void SortAndPrintPathAndCount(String filename,String code) throws FileNotFoundException, UnsupportedEncodingException
	{
		List<PathForSort> pathlist=new ArrayList<PathForSort>();
		Iterator it=path2question.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			PathForSort pfs=new PathForSort();
			pfs.path=(ConceptPath)entry.getKey();
			pfs.count=((Set<String>)entry.getValue()).size();
			pathlist.add(pfs);
		}
		Collections.sort(pathlist,new PathForSort());
		
		PrintWriter pw=new PrintWriter(filename,code);
		for(int i=0;i<pathlist.size();i++)
		{
			ConceptPath path=pathlist.get(i).path;
			for(int j=0;j<path.relationList.size();j++)
			{
				int relindex=path.relationList.get(j);
				if(relindex>=KBModel.reverseEdge)
				{
					pw.print("-"+id2rel.get(relindex-KBModel.reverseEdge)+"\t");
				}
				else
				{
					pw.print(id2rel.get(relindex)+"\t");
				}
				
			}
			pw.println(pathlist.get(i).count);
		}
		pw.close();
		
	}
	
	//public void Remove
	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{
		List<Question> trainList=IOTool.ReadSimpleQuestionsCVSWithConceptPaths(args[0], "utf8");
		List<Question> testList=IOTool.ReadSimpleQuestionsCVSWithConceptPaths(args[1], "utf8");
		
		List<Question> allList=new ArrayList<Question>();
		allList.addAll(trainList);
		allList.addAll(testList);
		
		ConceptCount cc=new ConceptCount();
		cc.BuildConceptPathMap(allList);
		cc.id2rel=IOTool.ReadLargeStringList(args[2], "utf8", 0, 10000);
		cc.SortAndPrintPathAndCount(args[3], "utf8");
		
		
	}
	
}
