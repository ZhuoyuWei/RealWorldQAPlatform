package org.wzy.main;

import org.wzy.fun.TrainModel;
import org.wzy.meta.Question;
import org.wzy.tool.IOTool;

public class TrainTrainableModel {

	
	public Question[] questions;
	
	
	public static void main(String[] args)
	{
		TrainTrainableModel ttm=new TrainTrainableModel();
		ttm.questions=IOTool.ReadSimpleQuestionsCVS(args[0], "utf8").toArray(new Question[0]);
		
		//CreatAnd
		
		TrainModel trainModel=new TrainModel();
		trainModel.Training(ttm.questions);
	}
}
