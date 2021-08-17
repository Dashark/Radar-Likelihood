package com.powervotex.localserver.algorithm.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class CoObject implements Serializable {

	private static final long serialVersionUID = -54421651138265510L;

	/**
	 * 对象的ID
	 */
	private final String id;
	
	/**
	 * 雷达检测对象的坐标（X，Y，Z）
	 */
	private float X, Y, Z;

	/**
	 * 特征值
	 */
	private double[] eigen;
	
	public CoObject(String id) {
		this.id = id;
		this.X = 0.0f;
		this.Y = 0.0f;
		this.Z = 0.0f;
	}
	public CoObject(String id, float X, float Y, float Z) {
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

	public String getID() {
		return id;
	}

	public double[] subEigen(CoObject other) {
		double[] results = new double[3];  //目前只有3个
		results[0] = X - other.X;
		results[1] = Y - other.Y;
		results[2] = Z - other.Z;
		return results;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CoObject) {
			CoObject coobj = (CoObject)obj;
			return id.equals(coobj.id);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
