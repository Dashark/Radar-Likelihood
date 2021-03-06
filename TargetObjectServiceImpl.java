package com.powervotex.localserver.algorithm.service.impl;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.powervotex.localserver.algorithm.dto.CoObject;
import com.powervotex.localserver.algorithm.service.ITargetObjectService;

@Service
public class TargetObjectServiceImpl implements ITargetObjectService {

	// 算法服务
	private CorrelationProbabilityServiceImpl _cp_service;
	// 以对象关键字映射索引与对象本身
	private Map<String, Integer> _targets_index = Maps.newHashMap();
	private Map<String, CoObject> _target_objects = Maps.newHashMap();
	private Map<String, Integer> _radars_index = Maps.newHashMap();
	private Map<String, CoObject> _radar_objects = Maps.newHashMap();
	private double[] _probabilities;

	public TargetObjectServiceImpl() {

	}

	/**
	 * 依据索引，如果索引在前次表中则拷贝过来；如果索引为新的，则按照“规则”设置概率。
	 *  
	 * @param tidx 目标的索引
	 * @param oidx 观测的索引
	 * @return 索引对应的概率
	 */
	private double prevProbs(Integer tidx, Integer oidx) {
		if (tidx != null && oidx != null) {
			// 原有的检测对象，对应原有的目标，拷贝上次计算的概率
			return _probabilities[tidx * _radar_objects.size() + oidx];

		} else if (tidx != null && oidx == null) {
			// 新增的检测对象，对应原有的目标，概率设置很低
			return 0.01;

		} else if (tidx == null && oidx != null) {
			// 原有的检测对象，对应新增的目标，概率设置较低
			return 0.01;

		}
		// else if (tidx == null && oidx == null)
		// 新增的检测对象，对应新增的目标，概率设置较高
		return 0.5; // 50%
	}

	@Override
	public int updateObjects(Set<CoObject> targets, Set<CoObject> radars) {
		// targets的数量是一定的，radars的数量要与之一致
		// 如果radars数量多了，丢弃索引值大的（较新的观测）
		// 如果radars数量少了，补充临时的，ID值不可查询
		Set<CoObject> radars1 = radars.clone();
		int subsize = radars.size() - targets.size();
		if (subsize > 0) {
			radars1.clear();
			int i = 0;
			for (CoObject o : radars) {
				if (i < targets.size()) {
					radars1.add(o);
					i += 1;
				}
			}

		}
		else if (subsize < 0) {
			for (int i = subsize; i > 0; i++) {
				radars1.add(new CoObject(String.valueOf(i)));
			}
			
		}
		// 新的概率映射表
		double[] probs_new = new double[targets.size() * radars1.size()];
		Map<String, Integer> targets_index = Maps.newHashMap();
		Map<String, CoObject> target_objects = Maps.newHashMap();
		Map<String, Integer> radars_index = Maps.newHashMap();
		Map<String, CoObject> radar_objects = Maps.newHashMap();
		Integer tar_idx = 0; // 新表的目标索引（行）
		for (CoObject tar : targets) {
			Integer obj_idx = 0; // 新表的检测对象索引(列)
			Integer tidx = _targets_index.get(tar.getID()); // 原有目标集合的索引值
			// 原有的目标，不管原来的Idx
			CoObject origTar = _target_objects.get(tar.getID());
			tar.setEigen(origTar); // 刷新目标的坐标特征
			for (CoObject obj : radars1) {
				Integer oidx = _radars_index.get(obj.getID()); // 原有检测对象集合的索引值
				probs_new[tar_idx * radars1.size() + obj_idx] = prevProbs(tidx, oidx);
				radar_objects.put(obj.getID(), obj);
				radars_index.put(obj.getID(), obj_idx);
				obj_idx += 1; // 下一个检测对象
			}
			targets_index.put(tar.getID(), tar_idx);
			target_objects.put(tar.getID(), tar);
			tar_idx += 1; // 下一个目标
		}
		_probabilities = probs_new; // 替换概率表，此时为条件概率
		_target_objects = target_objects;
		_targets_index = targets_index;
		_radar_objects = radar_objects;
		_radars_index = radars_index;
		return targets.size() + radars1.size();
	}

	@Override
	public boolean matchByFace(CoObject target, CoObject radar, float prob) {
		Integer tidx = _targets_index.get(target.getID());
		Integer oidx = _radars_index.get(radar.getID());
		if (tidx != null && oidx != null) {
			int size = _radar_objects.size();
			float subprob = (1.0 - prob) / (size - 1);
			for (int i = 0; i < _radar_objects.size(); ++i) {
				// 原有概率全部(行)
				_probabilities[tidx * _radar_objects.size() + i] = subprob;
			}
			for (int i = 0; i < _target_objects.size(); ++i) {
				// 原有概率全部(列)
				_probabilities[i * _radar_objects.size() + oidx] = subprob;
			}
			_probabilities[tidx * _radar_objects.size() + oidx] = prob;
			target.setEigen(radar);
			return true;
		}
		return false;
	}

	@Override
	public boolean matchByRFID(CoObject target, CoObject radar) {
		Integer tidx = _targets_index.get(target.getID());
		Integer oidx = _radars_index.get(radar.getID());
		if (tidx != null && oidx != null) {
			for (int i = 0; i < _radar_objects.size(); ++i) {
				// 原有概率全部清零(行)
				_probabilities[tidx * _radar_objects.size() + i] = 0.0;
			}
			for (int i = 0; i < _target_objects.size(); ++i) {
				// 原有概率全部清零(列)
				_probabilities[i * _radar_objects.size() + oidx] = 0.0;
			}
			_probabilities[tidx * _radar_objects.size() + oidx] = 1.0; // 确定是匹配的
			target.setEigen(radar);
			return true;
		}
		return false;
	}

	@Override
	public double[] calculateProbability() {
		// 特征向量 3 x n
		double[] tarobjs = new double[3 * _radar_objects.size()];
		// 条件概率 1 x n
		double[] cond = new double[_radar_objects.size()];
		//for (CoObject obj : _radar_objects.values()) {
		for (CoObject tar : _target_objects.values()) {
			// 一个检测对象
			//Integer obj_idx = _radars_index.get(obj.getID());
			Integer tar_idx = _targets_index.get(tar.getID());
			// 映射所有的目标
			//for (CoObject tar : _target_objects.values()) {
		  for (CoObject obj : _radar_objects.values()) {
				//Integer tar_idx = _targets_index.get(tar.getID());
			  Integer obj_idx = _radars_index.get(obj.getID());
				// 上一轮概率作为条件概率
				cond[obj_idx] = _probabilities[tar_idx * _radar_objects.size() + obj_idx];
				double[] temps = tar.subEigen(obj);
				// 行转换列
				tarobjs[obj_idx] = temps[0];
				tarobjs[obj_idx + _radar_objects.size()] = temps[1];
				tarobjs[obj_idx + _radar_objects.size() * 2] = temps[2];
			}
			// 计算新的概率（一个检测对象与所有目标的）
			_cp_service = new CorrelationProbabilityServiceImpl();
			_cp_service.init();
			double[] prob_obj = _cp_service.calculateProbability(cond, tarobjs);
			// 更新概率表, 按照检测对象更新所有目标
			for (CoObject obj : _radar_objects.values()) {
				Integer obj_idx = _radars_index.get(obj.getID());
				_probabilities[tar_idx * _radar_objects.size() + obj_idx] = prob_obj[obj_idx];
			}

		}
		return _probabilities;
	}

	@Override
	public Map<String, Map<String, Double>> queryProbability() {
		Map<String, Map<String, Double>> results = Maps.newHashMap();
		for (CoObject tar : _target_objects.values()) {
			// 一个目标
			Integer tar_idx = _targets_index.get(tar.getID());
			Map<String, Double> radar_result = Maps.newHashMap();
			for (CoObject obj : _radar_objects.values()) {
				// 一个检测对象
				Integer obj_idx = _radars_index.get(obj.getID());
				radar_result.put(obj.getID(), _probabilities[tar_idx * _radar_objects.size() + obj_idx]);
			}
			results.put(tar.getID(), radar_result);
		}
		return results;
	}
}
