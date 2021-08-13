package com.powervotex.localserver.radar.service;

import java.util.Set;

/**
 * 目标（Target）与检测对象（Object）的关联概率服务
 * 计算 T - O 的关联概率
 */
public interface ITargetObjectService {

	/*
	 * 保存场景中存在的检测对象<br/>
	 * 
	 * @param objs 检测对象列表
	 * @return 加入的检测对象数量
	 */
	public int updateRadarObjects(Set<RadarObject> objs);

	/**
	 * RFID 手段确认目标与检测对象之间的关联。
	 * 同时业务上新增一个目标，目前没有其它接口可以新增目标
	 */
	public boolean matchByRFID(BusinessTarget btar, RadarObject rtar);
	
	/**
	 * 业务上怎么减少一个目标？ 这儿不清楚
	 */
	public int removeTargets(Set<BusinessTarget> objs);
	
	/**
	 * 查询目标的关联概率。
	 * 
	 * @param Object target 目标
	 * @return float 概率
	 */
	public Map<RadarObject, Decimal> queryProbability(BusinessTarget btar);

}
