package test;

import org.wzy.tool.CoreNLPTool;

public class TestPostag {

	
	public static void main(String[] args)
	{
		if(CoreNLPTool.UniqueObject==null)
		{
			CoreNLPTool.CreateUniqueObject();
			CoreNLPTool.UniqueObject.InitTool("tokenize, ssplit, pos");
		}
		String text="earth orbit the sun once a year . about how many time do the moon orbit earth in a year ?";
		String res=CoreNLPTool.UniqueObject.RemoveNouns(text);
		System.out.println(res);
	}
	
}
