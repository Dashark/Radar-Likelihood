package com.powervotex.localserver.algorithm.service;

/**
 * 目标（Target）与检测对象（Object）的关联概率服务
 * 计算 T - O 的关联概率
 */
public interface ICorrelationProbabilityService {

	/**
	 * 计算目标与检测对象的关联概率
	 * 
	 * @param cond_array 所有目标对一个检测对象的条件概率
	 * @param tarobj_array 所有目标对一个检测对象的特征向量
	 * @return
	 */
	public double[] calcuateProbability(double[] cond_array, double[] tarobj_array);
	
}
