package org.wzy.pre;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.wzy.meta.Question;
import org.wzy.tool.IOTool;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class XMLHistoryQAParser extends DefaultHandler
{
	
	public List<Question> qList=new ArrayList<Question>();
	public String strbuffer="";
	public List<String> ansList;
	public int totalQuestionCount=0;
	
	@Override  
    public void startDocument() throws SAXException {  
        System.out.println("---->startDocument() is invoked...");  
    }  
      
    @Override  
    public void endDocument() throws SAXException {  
        System.out.println("---->endDocument() is invoked...");  
    }  
      
    @Override  
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {  
    	
    	if(qName.equals("question"))
    	{
    		Question qnow=new Question();
    		qList.add(qnow);
    		qnow.originalQuestionID=attributes.getValue("id");
    		qnow.questionID=totalQuestionCount+"";
    		totalQuestionCount++;
    		ansList=new ArrayList<String>();
    	}
    	else if(qName.equals("candidate"))
    	{
    		Integer label=Integer.parseInt(attributes.getValue("value"));
    		if(label==1)
    		{
    			qList.get(qList.size()-1).AnswerKey=ansList.size();
    		}
    	}
       
    }  
  
    @Override  
    public void endElement(String uri, String localName, String qName) throws SAXException {  
    	
    	if(qName.equals("type"))
    	{
    		qList.get(qList.size()-1).examName=strbuffer;
    	}
    	else if(qName.equals("description"))
    	{
    		qList.get(qList.size()-1).question_content=strbuffer;
    	}
    	else if(qName.equals("candidate"))
    	{
    		ansList.add(strbuffer);
    	}
    	else if(qName.equals("question"))
    	{
    		qList.get(qList.size()-1).answers=ansList.toArray(new String[0]);
    	}    	
    }  
      
    @Override  
    public void characters(char[] ch, int start, int length) throws SAXException {  
    	strbuffer=new String(ch, start, length); 
    	
    }  
}
    


public class ReadXMLQuestion {

	
	public List<Question> ReadXMLQuestion(String filename)
	{
		 SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		 XMLHistoryQAParser xmlParser=new XMLHistoryQAParser(); 
         try {
			SAXParser saxParser = saxParserFactory.newSAXParser();
			InputStream inputStream = new FileInputStream(new File(filename)); 
			
			saxParser.parse(inputStream, xmlParser);
			
         } catch (ParserConfigurationException | SAXException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
         } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
         }  
         return xmlParser.qList;
	}
	
	
	public static void JustTest() throws FileNotFoundException
	{
		ReadXMLQuestion rxq=new ReadXMLQuestion();
		long start=System.currentTimeMillis();
		List<Question> questionList=rxq.ReadXMLQuestion("D:\\KBQA\\DataSet\\history for senior high school entrance examination\\history_2014_utf-8.xml");
		long end=System.currentTimeMillis();
		System.out.println("size: "+questionList.size()+" at "+(end-start)+" ms");
		PrintStream ps=new PrintStream("D:\\KBQA\\DataSet\\history for senior high school entrance examination\\printTest");
		IOTool.PrintSimpleQuestions(questionList, ps);
	}
	
	
	public static void CollectAllHistoryQuestion(String inputfile,String outputfile) throws FileNotFoundException
	{
		ReadXMLQuestion rxq=new ReadXMLQuestion();
		long start=System.currentTimeMillis();
		List<Question> questionList=rxq.ReadXMLQuestion(inputfile);
		long end=System.currentTimeMillis();
		System.out.println("size: "+questionList.size()+" at "+(end-start)+" ms");
		//PrintStream ps=new PrintStream(outputfile);
		IOTool.PrintSimpleQuestions(questionList,outputfile,"utf8");
		
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		CollectAllHistoryQuestion("D:\\KBQA\\DataSet\\MiddleSchoolHistory\\middle_school_history.xml"
				,"D:\\KBQA\\DataSet\\MiddleSchoolHistory\\middle_school_history.cvs");
		
	}
	
}


   
