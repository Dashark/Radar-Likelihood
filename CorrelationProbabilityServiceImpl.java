package com.powervotex.localserver.radar.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import Jama.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.powervotex.localserver.business.common.utils.RedisUtil;

@Service
public class CorrlationProbabilityServiceImpl implements ICorrelationProbabilityService{

	private final int Dimension = 3; // 目前雷达数据的维度只有3
	// 雷达协方差矩阵，缺省为1.0，可能不对需要调整。
	private final Matrix _sigma_coefficient = new Matrix(Dimension, Dimension, 1.0);

	private final Set<Object> _targets, _objects; // 目标与检测对象的集合
	private final Set<Object> _update_targets, _update_objects; // 更新的目标与概率对象的集合
	// 辅助手段（String）确定的目标与检测对象之间的关联概率，int是Hash值，float是概率0~1
	private final Map<String, Map<Integer, Decimal>> _auxiliary_means = Maps.newHashMap();
	// 目标与检测对象之间的关联概率，int是Hash值，float是概率0~1
	private final Map<Integer, Decimal> _correlation_probabilities = Maps.newHashMap();

	private CorrlationProbabilityServiceImpl() {
		
	}
	public CorrlationProbabilityServiceImpl(Matrix targets, Matrix objects) {
		_targets = targets;
		_objects = objects;
	}

	@Override
	public int updateTargets(Set<Object> objs) {
		_update_targets = objs;
		return objs.size();
	}

	@Override
	public int updateObjects(List<Object> objs) {
		_update_objects = objs;
		return objs.size();
	}

	@Override
	public boolean matchAuxiliary(String method, Object target, Object object, float prob) {
		if (target.equals(object)) return false;

		Pair<Object, Object> pair = new Pair<>(target, object);
		Map<int, float> value = _auxiliary_means.get(method);//辅助手段，如人脸、RFID等，对应的关联数据
		if (value != null) {  // 某个辅助手段已经有集合了
			value.put(pair.hashCode(), prob);
		}
		else {  //新建映射并保存在辅助手段里
			Map<int, float> probs = Maps.newHashMap();
			probs.put(pair.hashCode(), prob);
			_auxiliary_means.put(method, probs);
		}
		return true;
	}

	private Map<Integer, Integer> mapTargetObject() {
		Set<Object> joinTargets = new HashSet<Object>();
		joinTargets.addAll(_targets);
		joinTargets.addAll(_update_targets);   // 并集，所有目标
		Set<Object> subTargets = new HashSet<Object>();
		subTargets.addAll(_targets);
		subTargets.removeAll(_update_targets);   //差集，消失的目标
		Set<Object> addTargets = new HashSet<Object>();
		addTargets.addAll(_update_targets);
		addTargets.removeAll(_targets);   //差集，新增的目标
		Set<Object> retainTargets = new HashSet<Object>();
		retainTargets.addAll(_update_targets);
		retainTargets.retainAll(_targets);   // 交集，更新的目标

		Set<Object> joinObjs = new HashSet<Object>();
		joinObjs.addAll(_objects);
		joinObjs.addAll(_update_objects);    // 并集，所有检测对象
		Set<Object> subObjs = new HashSet<Object>();
		subObjs.addAll(_objects);
		subObjs.removeAll(_update_objects);    // 差集，消失的检测对象
		Set<Object> addObjs = new HashSet<Object>();
		addObjs.addAll(_update_objects);
		addObjs.removeAll(_objects);    // 差集，新增的检测对象
		Set<Object> retainObjs = new HashSet<Object>();
		retainObjs.addAll(_objects);
		retainObjs.retainAll(_update_objects);    // 交集，更新的检测对象

		// 建立目标与检测对象之间的映射，并确认它们之间关系：1 新增，2 更新，3 消失
		Map<Integer, Integer> status_map = Maps.newHashMap();
		for (Object tar : joinTargets) {
			for (Object obj : joinObjs) {
				Pair<Object, Object> pair = new Pair<>(tar, obj);
				status_map.put(pair.hashCode(), 0);   // 初始化矩阵
			}
		}
		// 新增目标的映射
		for (Object tar : addTargets) {
			for (Object obj : joinObjs) {
				Pair<Object, Object> pair = new Pair<>(tar, obj);
				status_map.put(pair.hashCode(), 1);   // 初始化矩阵
			}
		}
		// 更新目标的映射
		for (Object tar : retainTargets) {
			for (Object obj : joinObjs) {
				Pair<Object, Object> pair = new Pair<>(tar, obj);
				status_map.put(pair.hashCode(), 2);   // 初始化矩阵
			}
		}
		// 消失目标的映射
		for (Object tar : subTargets) {
			for (Object obj : joinObjs) {
				Pair<Object, Object> pair = new Pair<>(tar, obj);
				status_map.put(pair.hashCode(), 3);   // 初始化矩阵
			}
		}
		// 消失的检测对象映射
		for (Object obj : subObjs) {
			for (Object tar : joinTargets) {
				Pair<Object, Object> pair = new Pair<>(tar, obj);
				status_map.put(pair.hashCode(), 3);   // 初始化矩阵
			}
		}
		// 更新的检测对象似乎不用处理
		// TODO 新增的检测对象需要处理吗？
		
		return status_map;
	}
	@Override
	public boolean calcuateProbability() {
		// 建立目标与检测对象之间的映射，并确认它们之间关系：1 新增，2 更新，3 消失
		Map<Integer, Integer> status_map = mapTargetObject();
		// 围绕着 _correlation_probabilities 更新关联概率
		Set<Integer> map1 = status_map.keySet();
		Set<Integer> map2 = _correlation_probabilities.keySet();
		return false;
	}

	@Override
	public float queryProbability(Object target, Object obj) {
		if (target.equals(obj)) return false;

		Pair<Object, Object> pair = new Pair<>(target, obj);
		// TODO 没有Key会返回0吗？
		float prob = _correlation_probabilities.get(pair.hashCode());
		return prob;
	}
}
