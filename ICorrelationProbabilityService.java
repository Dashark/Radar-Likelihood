package com.powervotex.localserver.radar.service;

import java.util.Set;

/**
 * 目标（Target）与检测对象（Object）的关联概率服务
 * 计算 T - O 的关联概率
 */
public interface ICorrelationProbabilityService {

	/**
	 * 更新场景中存在的目标<br/>
	 * 
	 * @param objs 不重复的目标列表，应该保证目标是常量
	 * @return 加入的目标数量
	 */
	public int updateTargets(Set<Object> objs);

	/*
	 * 保存场景中存在的检测对象<br/>
	 * 记录存至redjs
	 * 
	 * @param objs 检测对象列表
	 * @return 加入的检测对象数量
	 */
	public int updateObjects(List<Object> objs);

	/**
	 * 通过辅助手段确认目标与检测对象的关联
	 * 
	 * @param method 辅助手段
	 * @param target 目标
	 * @param obj 检测对象
	 * @param float  关联概率，或置信度
	 * @return boolean 
	 */
	public boolean matchAuxiliary(String method, Object target, Object object, float prob);
	
	/**
	 * 计算目标与检测对象的关联概率
	 * 
	 * @return Boolean 
	 */
	public boolean calcuateProbability();
	
	/**
	 * 查询目标与检测对象的关联概率。
	 * 应该在每次计算之后查询，计算之间的概率不变，即增删目标或对象不会改变计算后的概率。
	 * 
	 * @param Object target 目标
	 * @param Object obj   检测对象
	 * @return float 概率
	 */
	public float queryProbability(Object target, Object obj);

}
