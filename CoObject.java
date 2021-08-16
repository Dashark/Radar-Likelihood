package com.powervotex.localserver.algorithm.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class CoObject implements Serializable {

	private static final long serialVersionUID = -54421651138265510L;

	/**
	 * 雷达检测对象的ID
	 */
	private final String id;
	
	/**
	 * 雷达检测对象的坐标（X，Y，Z）
	 */
	private Integer X, Y, Z;

	/**
	 * 特征值
	 */
	private double[] eigen;
	
	public CoObject(String id) {
		this.id = id;
		this.X = 0;
		this.Y = 0;
		this.Z = 0;
	}
	public CoObject(String id, Integer X, Integer Y, Integer Z) {
		this.id = id;
		this.X = X;
		this.Y = Y;
		this.Z = Z;
	}

	public void setEigen(CoObject other) {
		X = other.X;
		Y = other.Y;
		Z = other.Z;
	}
	
	public double[] subEigen(CoObject other) {
		double[] results = new double[3];  //目前只有3个
		results[0] = X - other.X;
		results[1] = Y - other.Y;
		results[2] = Z - other.Z;
		return results;
	}
}
