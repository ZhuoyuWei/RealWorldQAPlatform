package com.lipiji.mllib.rnn.lstm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;

import com.lipiji.mllib.layers.MatIniter;
import com.lipiji.mllib.layers.MatIniter.Type;
import com.lipiji.mllib.utils.Activer;

public class Cell implements Serializable {
    public static final long serialVersionUID = -7059290852389115565L;
    
    public static boolean update_wordembs=true;
    
    private int inSize;
    private int outSize;
    private int deSize;
    
    private DoubleMatrix Wxi;
    private DoubleMatrix Whi;
    private DoubleMatrix Wci;
    private DoubleMatrix bi;
    
    private DoubleMatrix Wxf;
    private DoubleMatrix Whf;
    private DoubleMatrix Wcf;
    private DoubleMatrix bf;
    
    private DoubleMatrix Wxc;
    private DoubleMatrix Whc;
    private DoubleMatrix bc;
    
    private DoubleMatrix Wxo;
    private DoubleMatrix Who;
    private DoubleMatrix Wco;
    private DoubleMatrix bo;
    
    private DoubleMatrix Why;
    private DoubleMatrix by;
    
    //gradients
    private DoubleMatrix gWxi;
    private DoubleMatrix gWhi;
    private DoubleMatrix gWci;
    private DoubleMatrix gbi;
    
    private DoubleMatrix gWxf;
    private DoubleMatrix gWhf;
    private DoubleMatrix gWcf;
    private DoubleMatrix gbf;
    
    private DoubleMatrix gWxc;
    private DoubleMatrix gWhc;
    private DoubleMatrix gbc;
    
    private DoubleMatrix gWxo;
    private DoubleMatrix gWho;
    private DoubleMatrix gWco;
    private DoubleMatrix gbo;
    
    private DoubleMatrix gWhy;
    private DoubleMatrix gby;
    
    public List<double[][]> gwords;
    public List<double[][]> swords;
    
    public Cell(int inSize, int outSize, MatIniter initer) {
        this.inSize = inSize;
        this.outSize = outSize;
        
        if (initer.getType() == Type.Uniform) {
            this.Wxi = initer.uniform(inSize, outSize);
            this.Whi = initer.uniform(outSize, outSize);
            this.Wci = initer.uniform(outSize, outSize);
            this.bi = new DoubleMatrix(1, outSize);
            
            this.Wxf = initer.uniform(inSize, outSize);
            this.Whf = initer.uniform(outSize, outSize);
            this.Wcf = initer.uniform(outSize, outSize);
            this.bf = new DoubleMatrix(1, outSize);
            
            this.Wxc = initer.uniform(inSize, outSize);
            this.Whc = initer.uniform(outSize, outSize);
            this.bc = new DoubleMatrix(1, outSize);
            
            this.Wxo = initer.uniform(inSize, outSize);
            this.Who = initer.uniform(outSize, outSize);
            this.Wco = initer.uniform(outSize, outSize);
            this.bo = new DoubleMatrix(1, outSize);
            
            this.Why = initer.uniform(outSize, inSize);
            this.by = new DoubleMatrix(1, inSize);
        } else if (initer.getType() == Type.Gaussian) {
            this.Wxi = initer.gaussian(inSize, outSize);
            this.Whi = initer.gaussian(outSize, outSize);
            this.Wci = initer.gaussian(outSize, outSize);
            this.bi = new DoubleMatrix(1, outSize);
            
            this.Wxf = initer.gaussian(inSize, outSize);
            this.Whf = initer.gaussian(outSize, outSize);
            this.Wcf = initer.gaussian(outSize, outSize);
            this.bf = new DoubleMatrix(1, outSize);
            
            this.Wxc = initer.gaussian(inSize, outSize);
            this.Whc = initer.gaussian(outSize, outSize);
            this.bc = new DoubleMatrix(1, outSize);
            
            this.Wxo = initer.gaussian(inSize, outSize);
            this.Who = initer.gaussian(outSize, outSize);
            this.Wco = initer.gaussian(outSize, outSize);
            this.bo = new DoubleMatrix(1, outSize);
            
            this.Why = initer.gaussian(outSize, inSize);
            this.by = new DoubleMatrix(1, inSize);
        }
    }
    
    public Cell(int inSize, int outSize, MatIniter initer, int deSize) {
        this(inSize, outSize, initer);
        this.deSize = deSize;
        this.Why = new DoubleMatrix(outSize, deSize);
        this.by = new DoubleMatrix(1, deSize);
    }
    
    public int getInSize() {
        return inSize;
    }

    public int getOutSize() {
        return outSize;
    }
    
    public int getDeSize() {
        return deSize;
    }
    
    public void active(int t, Map<String, DoubleMatrix> acts) {
        DoubleMatrix x = acts.get("x" + t);
        DoubleMatrix preH = null, preC = null;
        if (t == 0) {
            preH = new DoubleMatrix(1, getOutSize());
            preC = preH.dup();
        } else {
            preH = acts.get("h" + (t - 1));
            preC = acts.get("c" + (t - 1));
        }
        
        DoubleMatrix i = Activer.logistic(x.mmul(Wxi).add(preH.mmul(Whi)).add(preC.mmul(Wci)).add(bi));
        DoubleMatrix f = Activer.logistic(x.mmul(Wxf).add(preH.mmul(Whf)).add(preC.mmul(Wcf)).add(bf));
        DoubleMatrix gc = Activer.tanh(x.mmul(Wxc).add(preH.mmul(Whc)).add(bc));
        DoubleMatrix c = f.mul(preC).add(i.mul(gc));
        DoubleMatrix o = Activer.logistic(x.mmul(Wxo).add(preH.mmul(Who)).add(c.mmul(Wco)).add(bo));
        DoubleMatrix gh = Activer.tanh(c);
        DoubleMatrix h = o.mul(gh);
        
        acts.put("i" + t, i);
        acts.put("f" + t, f);
        acts.put("gc" + t, gc);
        acts.put("c" + t, c);
        acts.put("o" + t, o);
        acts.put("gh" + t, gh);
        acts.put("h" + t, h);
    }
    
    
    public DoubleMatrix[] active(int t,DoubleMatrix x,DoubleMatrix preH,DoubleMatrix preC) {
        if (t == 0) {
            preH = new DoubleMatrix(1, getOutSize());
            preC = preH.dup();
        }
        
        //wzy debug
        //System.out.println("x\t"+x.rows+"\t"+x.columns);
        //System.out.println("Wxi\t"+Wxi.rows+"\t"+Wxi.columns);
        
        DoubleMatrix i = Activer.logistic(x.mmul(Wxi).add(preH.mmul(Whi)).add(preC.mmul(Wci)).add(bi));
        DoubleMatrix f = Activer.logistic(x.mmul(Wxf).add(preH.mmul(Whf)).add(preC.mmul(Wcf)).add(bf));
        DoubleMatrix gc = Activer.tanh(x.mmul(Wxc).add(preH.mmul(Whc)).add(bc));
        DoubleMatrix c = f.mul(preC).add(i.mul(gc));
        DoubleMatrix o = Activer.logistic(x.mmul(Wxo).add(preH.mmul(Who)).add(c.mmul(Wco)).add(bo));
        DoubleMatrix gh = Activer.tanh(c);
        DoubleMatrix h = o.mul(gh);
        
        /*acts.put("i" + t, i);
        acts.put("f" + t, f);
        acts.put("gc" + t, gc);
        acts.put("c" + t, c);
        acts.put("o" + t, o);
        acts.put("gh" + t, gh);
        acts.put("h" + t, h);*/
        
        DoubleMatrix[] res=new DoubleMatrix[7];
        res[0]=i;
        res[1]=f;
        res[2]=gc;
        res[3]=c;
        res[4]=o;
        res[5]=gh;
        res[6]=h;
        
        return res;
    }
    
    public void bptt(Map<String, DoubleMatrix> acts, int lastT, double lr) {
        for (int t = lastT; t > -1; t--) {
            DoubleMatrix py = acts.get("py" + t);
            DoubleMatrix y = acts.get("y" + t);
            DoubleMatrix deltaY = py.sub(y);
            acts.put("dy" + t, deltaY);
            
            // cell output errors
            DoubleMatrix h = acts.get("h" + t);
            DoubleMatrix deltaH = null;
            if (t == lastT) {
                deltaH = Why.mmul(deltaY.transpose()).transpose();
            } else {
                DoubleMatrix lateDgc = acts.get("dgc" + (t + 1));
                DoubleMatrix lateDf = acts.get("df" + (t + 1));
                DoubleMatrix lateDo = acts.get("do" + (t + 1));
                DoubleMatrix lateDi = acts.get("di" + (t + 1));
                deltaH = Why.mmul(deltaY.transpose()).transpose()
                        .add(Whc.mmul(lateDgc.transpose()).transpose())
                        .add(Whi.mmul(lateDi.transpose()).transpose())
                        .add(Who.mmul(lateDo.transpose()).transpose())
                        .add(Whf.mmul(lateDf.transpose()).transpose());
            }
            acts.put("dh" + t, deltaH);
            
            
            // output gates
            DoubleMatrix gh = acts.get("gh" + t);
            DoubleMatrix o = acts.get("o" + t);
            DoubleMatrix deltaO = deltaH.mul(gh).mul(deriveExp(o));
            acts.put("do" + t, deltaO);
            
            // status
            DoubleMatrix deltaC = null;
            if (t == lastT) {
                deltaC = deltaH.mul(o).mul(deriveTanh(gh))
                        .add(Wco.mmul(deltaO.transpose()).transpose());
            } else {
                DoubleMatrix lateDc = acts.get("dc" + (t + 1));
                DoubleMatrix lateDf = acts.get("df" + (t + 1));
                DoubleMatrix lateF = acts.get("f" + (t + 1));
                DoubleMatrix lateDi = acts.get("di" + (t + 1));
                deltaC = deltaH.mul(o).mul(deriveTanh(gh))
                        .add(Wco.mmul(deltaO.transpose()).transpose())
                        .add(lateF.mul(lateDc))
                        .add(Wcf.mmul(lateDf.transpose()).transpose())
                        .add(Wci.mmul(lateDi.transpose()).transpose());
            }
            acts.put("dc" + t, deltaC);
            
            // cells
            DoubleMatrix gc = acts.get("gc" + t);
            DoubleMatrix i = acts.get("i" + t);
            DoubleMatrix deltaGc = deltaC.mul(i).mul(deriveTanh(gc));
            acts.put("dgc" + t, deltaGc);
        
            DoubleMatrix preC = null;
            if (t > 0) {
                preC = acts.get("c" + (t - 1));
            } else {
                preC = DoubleMatrix.zeros(1, h.length);
            }
            // forget gates
            DoubleMatrix f = acts.get("f" + t);
            DoubleMatrix deltaF = deltaC.mul(preC).mul(deriveExp(f));
            acts.put("df" + t, deltaF);
        
            // input gates
            DoubleMatrix deltaI = deltaC.mul(gc).mul(deriveExp(i));
            acts.put("di" + t, deltaI);
        }
        updateParameters(acts, lastT, lr);
    }
    
    
    /**res[0]=i;
    res[1]=f;
    res[2]=gc;
    res[3]=c;
    res[4]=o;
    res[5]=gh;
    res[6]=h;
    */
    public void bptt(DoubleMatrix[][] states, DoubleMatrix[] words, int lastT,DoubleMatrix lost) {
    	
    	/*DoubleMatrix lateDgc = null;
        DoubleMatrix lateDf = null;
        DoubleMatrix lateDo = null;
        DoubleMatrix lateDi = null;
        DoubleMatrix lateDc = null;*/     
        
        
        DoubleMatrix[] deltaHs=new DoubleMatrix[lastT+1];
    	DoubleMatrix[] deltaGcs = new DoubleMatrix[lastT+1];
        DoubleMatrix[] deltaFs = new DoubleMatrix[lastT+1];
        DoubleMatrix[] deltaOs = new DoubleMatrix[lastT+1];
        DoubleMatrix[] deltaIs = new DoubleMatrix[lastT+1];      
        DoubleMatrix[] deltaCs=new DoubleMatrix[lastT+1];    	
        
        DoubleMatrix[][] delta_res=new DoubleMatrix[7][];
        delta_res[0]=deltaIs;
        delta_res[1]=deltaFs;
        delta_res[2]=deltaGcs;
        delta_res[3]=deltaCs;
        delta_res[4]=deltaOs;
        delta_res[6]=deltaHs;
        
        
        for (int t = lastT; t > -1; t--) {
        	
        	//by wzy, It is the error loss, @L/@LSTM, and then @L/@Theta=(@L/@LSTM)*(@LSTM/@Theta), the latter one @LSTM/@Theta is computed in following.
            /*DoubleMatrix py = acts.get("py" + t);
            DoubleMatrix y = acts.get("y" + t);
            DoubleMatrix deltaY = py.sub(y);
            acts.put("dy" + t, deltaY);*/
        	//Give the lost as deltaY directly;
        	DoubleMatrix deltaY=lost;
        	if(t<lastT)
        	{
        		//re-calculate deltaY
        		
        	}
            
            // cell output errors
            DoubleMatrix h = states[t][6];
            DoubleMatrix deltaH = null;
            if (t == lastT) {
            	//System.out.println("wzy debug "+Why.rows+" "+Why.columns+"\t"+deltaY.rows+" "+deltaY.columns);
                deltaH = Why.mmul(deltaY.transpose()).transpose();
            } else {

                deltaH = Why.mmul(deltaY.transpose()).transpose()
                        .add(Whc.mmul(deltaGcs[t+1].transpose()).transpose())
                        .add(Whi.mmul(deltaIs[t+1].transpose()).transpose())
                        .add(Who.mmul(deltaOs[t+1].transpose()).transpose())
                        .add(Whf.mmul(deltaFs[t+1].transpose()).transpose());
            }
            //acts.put("dh" + t, deltaH);
            deltaHs[t]=deltaH;
            
            // output gates
            DoubleMatrix gh = states[t][5];
            DoubleMatrix o = states[t][4];
            DoubleMatrix deltaO = deltaH.mul(gh).mul(deriveExp(o));
            //acts.put("do" + t, deltaO);
            deltaOs[t]=deltaO;
            // status
            DoubleMatrix deltaC = null;
            if (t == lastT) {
                deltaC = deltaH.mul(o).mul(deriveTanh(gh))
                        .add(Wco.mmul(deltaO.transpose()).transpose());
            } else {
                DoubleMatrix lateF = states[t+1][1];

                deltaC = deltaH.mul(o).mul(deriveTanh(gh))
                        .add(Wco.mmul(deltaO.transpose()).transpose())
                        .add(lateF.mul(deltaCs[t+1]))
                        .add(Wcf.mmul(deltaFs[t+1].transpose()).transpose())
                        .add(Wci.mmul(deltaIs[t+1].transpose()).transpose());
            }
            //acts.put("dc" + t, deltaC);
            deltaCs[t]=deltaC;

            
            // cells
            DoubleMatrix gc = states[t][2];
            DoubleMatrix i = states[t][0];
            DoubleMatrix deltaGc = deltaC.mul(i).mul(deriveTanh(gc));
            //acts.put("dgc" + t, deltaGc);
            deltaGcs[t]=deltaGc;
        
            DoubleMatrix preC = null;
            if (t > 0) {
                //preC = acts.get("c" + (t - 1));
                preC=states[t-1][3];
            } else {
                preC = DoubleMatrix.zeros(1, h.length);
            }
            // forget gates
            DoubleMatrix f = states[t][1];
            DoubleMatrix deltaF = deltaC.mul(preC).mul(deriveExp(f));
            //acts.put("df" + t, deltaF);
            //lateDf=deltaF;
            deltaFs[t]=deltaF;
        
            // input gates
            DoubleMatrix deltaI = deltaC.mul(gc).mul(deriveExp(i));
            //acts.put("di" + t, deltaI);
            //lateDi=deltaI;
            deltaIs[t]=deltaI;
        }
        //updateParameters(acts, lastT, lr);
        CalParametersGradients(states, delta_res, words, lost, lastT);
    }
    
    public void updateParameters(Map<String, DoubleMatrix> acts, int lastT, double lr) {
        /*DoubleMatrix gWxi = new DoubleMatrix(Wxi.rows, Wxi.columns);
        DoubleMatrix gWhi = new DoubleMatrix(Whi.rows, Whi.columns);
        DoubleMatrix gWci = new DoubleMatrix(Wci.rows, Wci.columns);
        DoubleMatrix gbi = new DoubleMatrix(bi.rows, bi.columns);
        
        DoubleMatrix gWxf = new DoubleMatrix(Wxf.rows, Wxf.columns);
        DoubleMatrix gWhf = new DoubleMatrix(Whf.rows, Whf.columns);
        DoubleMatrix gWcf = new DoubleMatrix(Wcf.rows, Wcf.columns);
        DoubleMatrix gbf = new DoubleMatrix(bf.rows, bf.columns);
        
        DoubleMatrix gWxc = new DoubleMatrix(Wxc.rows, Wxc.columns);
        DoubleMatrix gWhc = new DoubleMatrix(Whc.rows, Whc.columns);
        DoubleMatrix gbc = new DoubleMatrix(bc.rows, bc.columns);
        
        DoubleMatrix gWxo = new DoubleMatrix(Wxo.rows, Wxo.columns);
        DoubleMatrix gWho = new DoubleMatrix(Who.rows, Who.columns);
        DoubleMatrix gWco = new DoubleMatrix(Wco.rows, Wco.columns);
        DoubleMatrix gbo = new DoubleMatrix(bo.rows, bo.columns);
        
        DoubleMatrix gWhy = new DoubleMatrix(Why.rows, Why.columns);
        DoubleMatrix gby = new DoubleMatrix(by.rows, by.columns);
        */
        
        
        for (int t = 0; t < lastT + 1; t++) {
            DoubleMatrix x = acts.get("x" + t).transpose();
            gWxi = gWxi.add(x.mmul(acts.get("di" + t)));
            gWxf = gWxf.add(x.mmul(acts.get("df" + t)));
            gWxc = gWxc.add(x.mmul(acts.get("dgc" + t)));
            gWxo = gWxo.add(x.mmul(acts.get("do" + t)));
            
            if (t > 0) {
                DoubleMatrix preH = acts.get("h" + (t - 1)).transpose();
                DoubleMatrix preC = acts.get("c" + (t - 1)).transpose();
                gWhi = gWhi.add(preH.mmul(acts.get("di" + t)));
                gWhf = gWhf.add(preH.mmul(acts.get("df" + t)));
                gWhc = gWhc.add(preH.mmul(acts.get("dgc" + t)));
                gWho = gWho.add(preH.mmul(acts.get("do" + t)));
                gWci = gWci.add(preC.mmul(acts.get("di" + t)));
                gWcf = gWcf.add(preC.mmul(acts.get("df" + t)));
            }
            gWco = gWco.add(acts.get("c" + t).transpose().mmul(acts.get("do" + t)));
            gWhy = gWhy.add(acts.get("h" + t).transpose().mmul(acts.get("dy" + t)));
            
            gbi = gbi.add(acts.get("di" + t));
            gbf = gbf.add(acts.get("df" + t));
            gbc = gbc.add(acts.get("dgc" + t));
            gbo = gbo.add(acts.get("do" + t));
            gby = gby.add(acts.get("dy" + t));
        }
        
        Wxi = Wxi.sub(clip(gWxi.div(lastT)).mul(lr));
        Whi = Whi.sub(clip(gWhi.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
        Wci = Wci.sub(clip(gWci.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
        bi = bi.sub(clip(gbi.div(lastT)).mul(lr));
        
        Wxf = Wxf.sub(clip(gWxf.div(lastT)).mul(lr));
        Whf = Whf.sub(clip(gWhf.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
        Wcf = Wcf.sub(clip(gWcf.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
        bf = bf.sub(clip(gbf.div(lastT)).mul(lr));
        
        Wxc = Wxc.sub(clip(gWxc.div(lastT)).mul(lr));
        Whc = Whc.sub(clip(gWhc.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
        bc = bc.sub(clip(gbc.div(lastT)).mul(lr));

        Wxo = Wxo.sub(clip(gWxo.div(lastT)).mul(lr));
        Who = Who.sub(clip(gWho.div(lastT < 2 ? 1 : (lastT - 1))).mul(lr));
        Wco = Wco.sub(clip(gWco.div(lastT)).mul(lr));
        bo = bo.sub(clip(gbo.div(lastT)).mul(lr));
        
        Why = Why.sub(clip(gWhy.div(lastT)).mul(lr));
        by = by.sub(clip(gby.div(lastT)).mul(lr));
    }
    
    
    public void IntiGradients()
    {
        gWxi = new DoubleMatrix(Wxi.rows, Wxi.columns);
        gWhi = new DoubleMatrix(Whi.rows, Whi.columns);
        gWci = new DoubleMatrix(Wci.rows, Wci.columns);
        gbi = new DoubleMatrix(bi.rows, bi.columns);
        
        gWxf = new DoubleMatrix(Wxf.rows, Wxf.columns);
        gWhf = new DoubleMatrix(Whf.rows, Whf.columns);
        gWcf = new DoubleMatrix(Wcf.rows, Wcf.columns);
        gbf = new DoubleMatrix(bf.rows, bf.columns);
        
        gWxc = new DoubleMatrix(Wxc.rows, Wxc.columns);
        gWhc = new DoubleMatrix(Whc.rows, Whc.columns);
        gbc = new DoubleMatrix(bc.rows, bc.columns);
        
        gWxo = new DoubleMatrix(Wxo.rows, Wxo.columns);
        gWho = new DoubleMatrix(Who.rows, Who.columns);
        gWco = new DoubleMatrix(Wco.rows, Wco.columns);
        gbo = new DoubleMatrix(bo.rows, bo.columns);
        
        gWhy = new DoubleMatrix(Why.rows, Why.columns);
        gby = new DoubleMatrix(by.rows, by.columns);
        
        gwords=new ArrayList<double[][]>();
        swords=new ArrayList<double[][]>();
    }
    
    public void CalParametersGradients(DoubleMatrix[][] states,DoubleMatrix[][] deltas,DoubleMatrix[] words,DoubleMatrix lost,int lastT) {
    	
        DoubleMatrix[] deltaIs=deltas[0];
        DoubleMatrix[] deltaFs=deltas[1];
        DoubleMatrix[] deltaGcs=deltas[2];
        DoubleMatrix[] deltaCs=deltas[3];
        DoubleMatrix[] deltaOs=deltas[4];
        DoubleMatrix[] deltaHs=deltas[6];
        
        DoubleMatrix[] is=deltas[0];
        DoubleMatrix[] fs=deltas[1];
        DoubleMatrix[] gcs=deltas[2];
        DoubleMatrix[] cs=deltas[3];
        DoubleMatrix[] os=deltas[4];
        DoubleMatrix[] hs=deltas[6];        

        DoubleMatrix[] gx=null;
        
        if(update_wordembs)
        {
        	gx=new DoubleMatrix[lastT+1];
        	for(int i=0;i<=lastT;i++)
        	{
        		gx[i]=new DoubleMatrix(words[i].rows,words[i].columns);
        	}
        }
        
        //Why and by are only in the last T
        gWhy = gWhy.add(hs[lastT].transpose().mmul(lost));
        gby = gby.add(lost);
        
        for (int t = 0; t < lastT + 1; t++) {
            DoubleMatrix x = words[t].transpose();
            gWxi = gWxi.add(x.mmul(deltaIs[t]));
            gWxf = gWxf.add(x.mmul(deltaFs[t]));
            gWxc = gWxc.add(x.mmul(deltaGcs[t]));
            gWxo = gWxo.add(x.mmul(deltaOs[t]));
            
            if(update_wordembs)
            {
            	gx[t]=gx[t].add(deltaIs[t].mmul(Wxi));
            	gx[t]=gx[t].add(deltaFs[t].mmul(Wxf));
            	gx[t]=gx[t].add(deltaGcs[t].mmul(Wxc));
            	gx[t]=gx[t].add(deltaOs[t].mmul(Wxo));   
            }
            
            if (t > 0) {
                DoubleMatrix preH = hs[t-1].transpose();
                DoubleMatrix preC = cs[t-1].transpose();
                gWhi = gWhi.add(preH.mmul(deltaIs[t]));
                gWhf = gWhf.add(preH.mmul(deltaFs[t]));
                gWhc = gWhc.add(preH.mmul(deltaGcs[t]));
                gWho = gWho.add(preH.mmul(deltaOs[t]));
                gWci = gWci.add(preC.mmul(deltaIs[t]));
                gWcf = gWcf.add(preC.mmul(deltaFs[t]));
                
            }
            gWco = gWco.add(cs[t].transpose().mmul(deltaOs[t]));
            //gWhy = gWhy.add(acts.get("h" + t).transpose().mmul(acts.get("dy" + t)));
            
            gbi = gbi.add(deltaIs[t]);
            gbf = gbf.add(deltaFs[t]);
            gbc = gbc.add(deltaGcs[t]);
            gbo = gbo.add(deltaOs[t]);
            //gby = gby.add(acts.get("dy" + t));
        }
        
        
        if(update_wordembs)
        {
        	
        	double[][] gx_double=new double[gx.length][];
        	for(int i=0;i<gx.length;i++)
        	{
        		gx_double[i]=gx[i].toArray();
        	}
        	gwords.add(gx_double);
        	//swords.add(words);
        }
        
        
        
        //need divide lastT?
    }
    
    
    public void UpdateParameters(double lr)
    {
        Wxi = Wxi.sub(clip(gWxi).mul(lr));
        Whi = Whi.sub(clip(gWhi).mul(lr));
        Wci = Wci.sub(clip(gWci).mul(lr));
        bi = bi.sub(clip(gbi).mul(lr));
        
        Wxf = Wxf.sub(clip(gWxf).mul(lr));
        Whf = Whf.sub(clip(gWhf).mul(lr));
        Wcf = Wcf.sub(clip(gWcf).mul(lr));
        bf = bf.sub(clip(gbf).mul(lr));
        
        Wxc = Wxc.sub(clip(gWxc).mul(lr));
        Whc = Whc.sub(clip(gWhc).mul(lr));
        bc = bc.sub(clip(gbc).mul(lr));

        Wxo = Wxo.sub(clip(gWxo).mul(lr));
        Who = Who.sub(clip(gWho).mul(lr));
        Wco = Wco.sub(clip(gWco).mul(lr));
        bo = bo.sub(clip(gbo).mul(lr));
        
        Why = Why.sub(clip(gWhy).mul(lr));
        by = by.sub(clip(gby).mul(lr));
        
        //update word embeddings
        if(update_wordembs)
        {
        	if(swords!=null&&gwords!=null&&swords.size()==gwords.size())
        	{
        		for(int i=0;i<swords.size();i++)
        		{
        			double[][] xs=swords.get(i);
        			double[][] gxs=gwords.get(i);
        			
        			if(xs.length==gxs.length)
        			{
        				for(int j=0;j<xs.length;j++)
        				{
        					//xs[j]=xs[j].sub(clip(gxs[j]).mul(lr));
        					for(int k=0;k<xs[j].length;k++)
        					{
        						//clip?
        						
        						xs[j][k]-=gxs[j][k]*lr;
        					}
        				}
        			}
        		}
        	}
        }
    }
    
    public DoubleMatrix deriveExp(DoubleMatrix f) {
        return f.mul(DoubleMatrix.ones(1, f.length).sub(f));
    }
    
    public DoubleMatrix deriveTanh(DoubleMatrix f) {
        return DoubleMatrix.ones(1, f.length).sub(MatrixFunctions.pow(f, 2));
    }
    
    public DoubleMatrix clip(DoubleMatrix x) {
        //double v = 10;
        //return x.mul(x.ge(-v).mul(x.le(v)));
        return x;
    }
    
    public DoubleMatrix decode (DoubleMatrix ht) {
        return Activer.softmax(ht.mmul(Why).add(by));
    }
    
    public DoubleMatrix LastY(DoubleMatrix ht)
    {
    	return ht.mmul(Why).add(by);
    }
}