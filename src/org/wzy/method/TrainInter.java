package org.wzy.method;

import java.util.ArrayList;
import java.util.List;

import org.wzy.meta.Question;

public interface TrainInter {

	public void InitGradients();
	public void CalculateGradient(double[][] words_embs,double[] loss);
	public void CalculateGradient(String text,double[] loss);	
	public void UpgradeGradients(double gamma);
}
