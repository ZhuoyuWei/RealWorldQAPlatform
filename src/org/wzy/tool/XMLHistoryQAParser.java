package org.wzy.tool;

import java.util.ArrayList;
import java.util.List;

import org.wzy.meta.Question;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHistoryQAParser extends DefaultHandler{

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
