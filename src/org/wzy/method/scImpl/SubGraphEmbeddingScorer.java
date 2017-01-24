package org.wzy.method.scImpl;

import java.util.List;
import java.util.Map;

import org.wzy.meta.Question;
import org.wzy.method.ScoringInter;
import org.wzy.method.TrainInter;

public class SubGraphEmbeddingScorer implements ScoringInter,TrainInter{

	@Override
	public void InitScorer(Map<String, String> paraMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double ScoreQAPair(Question qus, int aindex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void PreProcessingQuestions(List<Question> qList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double CalLoss(Question q) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean Trainable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TrainInter GetTrainInter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void InitAllGradients() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void UpgradeGradients(double gamma) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void InitPathWeightRandomly(List<Question> questionList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void InitGradients() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void CalculateGradient(double[][] words_embs, double[] loss) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void CalculateGradient(String text, double[] loss) {
		// TODO Auto-generated method stub
		
	}

	
	
}
