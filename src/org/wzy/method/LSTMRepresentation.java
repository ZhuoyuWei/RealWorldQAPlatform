package org.wzy.method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jblas.DoubleMatrix;
import org.wzy.meta.NNParameter;
import org.wzy.meta.Question;

import com.lipiji.mllib.layers.MatIniter;
import com.lipiji.mllib.layers.MatIniter.Type;
import com.lipiji.mllib.rnn.lstm.Cell;

public class LSTMRepresentation implements TextRepresentInter, TrainInter{
	
	
	public Cell lstm;
	
	public DoubleMatrix RepresentText(DoubleMatrix[] textEmbs)
	{
		DoubleMatrix[] states=null;
		for(int i=0;i<textEmbs.length;i++)
		{
		    /**res[0]=i;
		    res[1]=f;
		    res[2]=gc;
		    res[3]=c;
		    res[4]=o;
		    res[5]=gh;
		    res[6]=h;
		    */
			DoubleMatrix preH=states!=null?states[6]:null;
			DoubleMatrix preC=states!=null?states[3]:null;
			states=lstm.active(i, textEmbs[i],preH,preC);
		}
		return lstm.LastY(states[6]);
	}
	
	public DoubleMatrix[] doubles2DMs(double[][] textEmbs)
	{
		DoubleMatrix[] textDms=new DoubleMatrix[textEmbs.length];
		for(int i=0;i<textEmbs.length;i++)
		{
			textDms[i]=new DoubleMatrix(textEmbs[i]);
		}
		return textDms;
	}

	@Override
	public double[] RepresentText(double[][] textEmbs, int dim) {
		// TODO Auto-generated method stub

		DoubleMatrix[] textDms=doubles2DMs(textEmbs);
		DoubleMatrix dm_res=RepresentText(textDms);
		
		return dm_res.toArray();
	}

	@Override
	public void SetParameters(Map<String, String> paraMap) {
		// TODO Auto-generated method stub
		
		int inSize=Integer.parseInt(paraMap.get("inSize"));
		int outSize=Integer.parseInt(paraMap.get("outSize"));
		
		int typeLabel=Integer.parseInt(paraMap.get("typeLabel"));
		MatIniter.Type type=typeLabel==0?MatIniter.Type.Uniform:MatIniter.Type.Gaussian;
		double scale=Double.parseDouble(paraMap.get("scale"));
		double miu=Double.parseDouble(paraMap.get("miu"));
		double sigma=Double.parseDouble(paraMap.get("sigma"));
		
		lstm=new Cell(inSize,outSize,new MatIniter(type,scale,miu,sigma));
		
		String paraFile=paraMap.get("paraFile");
		if(paraFile!=null)
		{
			//read learned parameters from file
		}
		
	}

	@Override
	public void InitGradients() {
		// TODO Auto-generated method stub
		lstm.IntiGradients();
	}
	

	@Override
	public void UpgradeGradients(double gamma) {
		// TODO Auto-generated method stub
		lstm.UpdateParameters(gamma);
	}

	@Override
	public void CalculateGradient(double[][] words_embs, double[] loss) {
		// TODO Auto-generated method stub
		
		DoubleMatrix[] words_dms=doubles2DMs(words_embs);
		DoubleMatrix loss_dms=new DoubleMatrix(loss);
		
		//forward
		DoubleMatrix[][] states=new DoubleMatrix[words_embs.length][];
		for(int i=0;i<words_dms.length;i++)
		{
		    /**res[0]=i;
		    res[1]=f;
		    res[2]=gc;
		    res[3]=c;
		    res[4]=o;
		    res[5]=gh;
		    res[6]=h;
		    */
			
			DoubleMatrix preH=null;
			DoubleMatrix preC=null;
			if(i>0)
			{
				preH=states[i-1][6];
				preC=states[i-1][3];
			}
			states[i]=lstm.active(i, words_dms[i],preH,preC);
		}
		
		
		//backward
		if(lstm.update_wordembs)
		{
			lstm.swords.add(words_embs);
		}
		lstm.bptt(states, words_dms, words_dms.length-1, loss_dms);
	}




	
}
