package com.powervotex.localserver.algorithm.service.impl;

import org.springframework.stereotype.Service;

import com.powervotex.localserver.algorithm.service.ICorrelationProbabilityService;

import Jama.Matrix;

// TODO 需要单元测试，避免以后修改算法再重新验算
@Service
public class CorrelationProbabilityServiceImpl implements ICorrelationProbabilityService {

	private final int Dimension = 3; // 目前雷达数据的维度只有3(X,Y,Z)，更多特征就是更多维度。
	// 雷达协方差矩阵，缺省为0.01，表示1毫米。
	// 真实矩阵要雷达连续检测的点云坐标来专门计算。
	private Matrix _sigma_coefficient = Matrix.identity(Dimension, Dimension); //new Matrix(Dimension, Dimension, 0.01);
	private Matrix _sigma_inv;
	private double _sigma_det, _sigma_prev;

	public CorrelationProbabilityServiceImpl() {
			// 计算协方差矩阵的行列式根
		_sigma_det = Math.sqrt(_sigma_coefficient.det());
		// 协方差的逆矩阵
		_sigma_inv = _sigma_coefficient.inverse();
		_sigma_prev = Math.pow(2 * Math.PI, Dimension / 2.0);

	}
	public void init() {
		// 手工调整对角的方差
		/*
		_sigma_coefficient.set(0, 0, 0.005);
		_sigma_coefficient.set(1, 1, 0.005);
		_sigma_coefficient.set(2, 2, 0.005);
		*/

		// 计算协方差矩阵的行列式根
		_sigma_det = Math.sqrt(_sigma_coefficient.det());
		// 协方差的逆矩阵
		_sigma_inv = _sigma_coefficient.inverse();
		_sigma_prev = Math.pow(2 * Math.PI, Dimension / 2.0);
	}

	@Override
	public double[] calculateProbability(double[] cond_array, double[] tarobj_array) {
		Matrix cond = new Matrix(cond_array, 1); // 已知的条件概率
		Matrix target_obj = new Matrix(tarobj_array, 3);
		assert (target_obj.getRowDimension() == 3); // 3 x n 的矩阵
		// a set of target to only one object
		Matrix results = new Matrix(1, target_obj.getColumnDimension());

		int cols = target_obj.getColumnDimension();
		for (int i = 0; i < cols; ++i) {
			Matrix temp = target_obj.getMatrix(0, Dimension - 1, i, i); // 取得第 i 列
			Matrix temp_trans = temp.transpose(); // 转置
			Matrix temp1 = temp_trans.times(_sigma_inv); // 转置 乘以 逆
			Matrix temp2 = temp1.times(temp);
			double lamda = Math.exp(-temp2.get(0, 0) / 2.0);
			results.set(0, i, lamda / _sigma_prev / _sigma_det); // prob density of all
															// targets to one
															// object
		}
		results.arrayTimesEquals(cond); // 条件概率 逐个乘以 先验概率
		cols = results.getColumnDimension();
		double[][] results_array = results.getArrayCopy();
		double result_sum = 0.0;
		for (int i = 0; i < cols; ++i) {
			result_sum += results_array[0][i];
		}
		for (int i = 0; i < cols; ++i) {
			results_array[0][i] = results_array[0][i] / result_sum;
		}
		return results_array[0]; // 第一行数据
	}
}
