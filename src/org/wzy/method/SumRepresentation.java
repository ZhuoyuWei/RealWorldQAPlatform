package org.wzy.method;

import java.util.List;
import java.util.Map;

import org.wzy.meta.NNParameter;

public class SumRepresentation implements TextRepresentInter,TrainInter{


	@Override
	public double[] RepresentText(double[][] textEmbs,int dim) {
		// TODO Auto-generated method stub
		double[] res=new double[dim];
		for(int i=0;i<textEmbs.length;i++)
		{
			for(int j=0;j<dim;j++)
			{
				res[j]+=textEmbs[i][j];
			}
		}
		
		if(textEmbs.length>1)
		{
			for(int i=0;i<dim;i++)
			{
				res[i]/=textEmbs.length;
			}
		}
		
		return res;
	}

	@Override
	public void SetParameters(Map<String, String> paraMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void CalculateGradient(double[][] words_embs, double[] loss) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void UpgradeGradients(double gamma) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void InitGradients() {
		// TODO Auto-generated method stub
		
	}




}
