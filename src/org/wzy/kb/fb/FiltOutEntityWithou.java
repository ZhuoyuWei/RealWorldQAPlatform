package org.wzy.kb.fb;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

import org.wzy.tool.IOTool;

public class FiltOutEntityWithou {

	public Map<String,String> name2Mid1;
	public Map<String,String> name2Mid2;	
	
	public void FiltAndPrint(String filename1,String filename2) throws FileNotFoundException
	{
		System.out.println(name2Mid1.size()+"\t"+name2Mid2.size());
		Iterator it=name2Mid1.entrySet().iterator();
		PrintStream ps1=new PrintStream(filename1);
		PrintStream ps2=new PrintStream(filename2);		
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			String name=(String)entry.getKey();
			
			if(!name2Mid2.containsKey(name))
			{
				ps1.println(name+"\t"+name2Mid1.get(name));
			}
			else
			{
				ps2.println(name+"\t"+name2Mid1.get(name));
			}
		}
		ps1.close();
		ps2.close();
	}
	
	
	public static void main(String[] args) throws FileNotFoundException
	{
		FiltOutEntityWithou fo=new FiltOutEntityWithou();
		fo.name2Mid1=IOTool.ReadEntityNametoMid("D:\\KBQA\\DataDump\\freebase\\FBnames_inquestions_1-4_stem.mid", "utf8");
		fo.name2Mid2=IOTool.ReadEntityMidtoName("D:\\KBQA\\DataDump\\freebase\\entity.fb4", "utf8");		
		
		fo.FiltAndPrint("D:\\KBQA\\DataDump\\freebase\\missingtriplets.entity_missing","D:\\KBQA\\DataDump\\freebase\\missingtriplets.entity_has");
		
	}
	
}
