package org.wzy.method;

import java.util.Map;

import org.wzy.meta.NNParameter;

public interface TextRepresentInter {

	public double[] RepresentText(double[][] textEmbs,int dim);
	//public void SetParameters(NNParameter para);
	public void SetParameters(Map<String,String> paraMap);
	//public void SetWordEmbs(double[][] embs);
	public void InitGradients();
	
}
