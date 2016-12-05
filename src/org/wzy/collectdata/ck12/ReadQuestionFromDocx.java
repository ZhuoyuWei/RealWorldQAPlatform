package org.wzy.collectdata.ck12;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PAPX;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.wzy.meta.Question;
import org.wzy.tool.IOTool;

public class ReadQuestionFromDocx {

	public List<Question> questionList=new ArrayList<Question>();
	public List<String> filenameList=new ArrayList<String>();
	public List<Integer> qnumList=new ArrayList<Integer>();
	
	public String ReadOneDocx(String filename) throws IOException
	{
		InputStream is = new FileInputStream(filename);  
	    XWPFDocument doc = new XWPFDocument(is);  
	    XWPFWordExtractor extractor = new XWPFWordExtractor(doc);  
	    extractor.setFetchHyperlinks(false);
	    String text = extractor.getText(); 
		
		/*InputStream is = new FileInputStream(filename);  
	    XWPFDocument doc = new XWPFDocument(is);  
	    StringBuilder sb=new StringBuilder();
	    
	    List<XWPFParagraph> paras = doc.getParagraphs();  
	    for (XWPFParagraph para : paras)
	    {    
	         sb.append(para.getText());  
	         sb.append("\n");
	    } */ 
		
	    is.close();
	    return text;
	}
	
	
	
	public List<Question> ParsingOneDocx(String filename,String text)
	{
		List<Question> qList=new ArrayList<Question>();
		String[] ss=text.split("\n");
		Question q=new Question();
		List<String> ansList=new ArrayList<String>();
		boolean flag=false;
		int count=0;
		for(int i=1;i<ss.length;i++)
		{
			if(ss[i].length()<=0)
			{
				if(ansList.size()>2)
				{
					q.answers=ansList.toArray(new String[0]);
					q.originalQuestionID=count+"";
					count++;
					qList.add(q);
				}
				flag=false;
				q=new Question();
				ansList=new ArrayList<String>();
			}
			else if(!flag)
			{
				q.question_content=ss[i];
				flag=true;
			}
			else
			{
				ansList.add(ss[i]);
			}
		}
		
		filenameList.add(filename);
		qnumList.add(count);
		
		return qList;
	}
	
	public List<Question> ReadQuestionDirectly(String filename,String recordname) throws IOException
	{
		List<Question> qList=new ArrayList<Question>();
		
		/*InputStream is = new FileInputStream(filename);  
	    XWPFDocument doc = new XWPFDocument(is);  
	    XWPFWordExtractor extractor = new XWPFWordExtractor(doc);  
	    extractor.setFetchHyperlinks(false);
	    String text = extractor.getText(); 
	    is.close();*/
	    
	    InputStream is = new FileInputStream(filename);  
	    XWPFDocument doc = null;
	    try
	    {
	    	doc=new XWPFDocument(is);  
	    }catch(Exception e)
	    {
	    	/*HWPFDocument tmp_doc=new HWPFDocument(is);
	    	Range range=tmp_doc.getRange();
	    	int paranum=range.numParagraphs();*/
	    	System.out.println(recordname);
	    	//System.exit(-1);
			filenameList.add(recordname);
			qnumList.add(0);
			return qList;
	    	
	    }
	    List<XWPFParagraph> paraList=doc.getParagraphs();
	    int paraindex=0;
	    
		
	    
	    Question q=new Question();
		List<String> ansList=new ArrayList<String>();
		boolean flag=false;
		int count=0;
		for(int i=1;i<paraList.size();i++)
		{
			XWPFParagraph para=paraList.get(i);
			String paratext=para.getText().replaceAll("\n", "");
			if(paratext.length()<=0)
			{
				if(ansList.size()>=4)
				{
					//check empty questions
					StringBuilder sb=new StringBuilder();
					sb.append(q.question_content);
					for(int j=0;j<ansList.size();j++)
					{
						sb.append(ansList.get(j));
					}
					if(sb.toString().length()>20)
					{
						q.answers=ansList.toArray(new String[0]);
						q.originalQuestionID=count+"";
						count++;
						qList.add(q);
					}
				}
				flag=false;
				q=new Question();
				ansList=new ArrayList<String>();
			}
			else if(!flag)
			{
				q.question_content=paratext;
				flag=true;
			}
			else
			{
				String anstext=paratext;
				if(para.getRuns().get(0).getColor()!=null)
				{
					if(anstext.length()<3)
					{
						String tmplowcase=anstext.toLowerCase();
						if(tmplowcase.charAt(0)=='a')
						{
							q.AnswerKey=0;
						}
						else if(tmplowcase.charAt(0)=='b')
						{
							q.AnswerKey=1;
						}
						else if(tmplowcase.charAt(0)=='c')
						{
							q.AnswerKey=2;
						}				
						else if(tmplowcase.charAt(0)=='d')
						{
							q.AnswerKey=3;
						}
						else if(tmplowcase.charAt(0)=='e')
						{
							q.AnswerKey=4;
						}
						else if(tmplowcase.charAt(0)=='f')
						{
							q.AnswerKey=5;
						}
						else
						{
							q.AnswerKey=ansList.size();
							ansList.add(anstext);
						}
					}
					else
					{
						q.AnswerKey=ansList.size();
						ansList.add(anstext);
					}
				}
				else
				{
					ansList.add(anstext);
				}
				
			}			
		}
		
		filenameList.add(recordname);
		qnumList.add(count);		
		
		//System.out.println(count+"\t"+qList.size());
		//IOTool.PrintSimpleQuestions(qList, "D:\\KBQA\\SpiderData\\Material_Area\\ck12_downloadable_quizs\\physical-science\\downloads2\\multi-choice.q", "ascii");
		
		return qList;
	}
	
	
	public void ReadAllFile(String dirname) throws IOException
	{
		File dir=new File(dirname);
		if(dir.isDirectory())
		{
			File[] files=dir.listFiles();
			for(int i=0;i<files.length;i++)
			{
				//String text=ReadOneDocx(files[i].getPath());
				//List<Question> tmp_qList=ParsingOneDocx(files[i].getName(),text);
				List<Question> tmp_qList=ReadQuestionDirectly(files[i].getPath(),files[i].getName());
				questionList.addAll(tmp_qList);
			}
		}
		System.out.println("total questions: "+questionList.size());
	}
	
	public void PrintDebugLog(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		for(int i=0;i<filenameList.size();i++)
		{
			ps.println(filenameList.get(i)+"\t"+qnumList.get(i));
		}
		ps.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		ReadQuestionFromDocx rq=new ReadQuestionFromDocx();
		/*String buffer=rq.ReadOneDocx("D:\\KBQA\\SpiderData\\Material_Area\\ck12�ֶ�\\Conservation-of-Momentum-in-One-Dimension-Quiz.docx");
		PrintStream ps=new PrintStream("D:\\KBQA\\SpiderData\\Material_Area\\ck12�ֶ�\\atomstomolecules_quiz.txt");
		ps.println(buffer);
		ps.close();*/
		String dir="D:\\KBQA\\SpiderData\\Material_Area\\ck12_downloadable_quizs\\physical-science\\";
		rq.ReadAllFile(dir+"downloads2");
		IOTool.PrintSimpleQuestions(rq.questionList, dir+"multi-choice.q", "ascii");
		rq.PrintDebugLog(dir+"mc.log");
		//rq.ReadQuestionDirectly("D:\\KBQA\\SpiderData\\Material_Area\\ck12�ֶ�\\atomstomolecules_quiz.docx");
		//rq.ReadQuestionDirectly("D:\\KBQA\\SpiderData\\Material_Area\\ck12_downloadable_quizs\\physical-science\\downloads2\\Bond-Polarity-Quiz-MS-PS-Answer-Key.docx");
		
	
	}
}
