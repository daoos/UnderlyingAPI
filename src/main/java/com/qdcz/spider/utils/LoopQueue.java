package com.qdcz.spider.utils;

import java.util.LinkedList;
import java.util.List;

public class LoopQueue<T> {
	private List<T> list;
	
	public LoopQueue(){
		this.list = new LinkedList<T>();
	}

	public void add(T element){
		this.list.add(element);
	}
	public synchronized T get(){
		T element = list.get(0);
		list.remove(0);
		list.add(element);
		return element;
	}
	public static void main(String[] args){
		LoopQueue<String> loopqueue = new LoopQueue<>();
		loopqueue.add("111");
		loopqueue.add("222");
		loopqueue.add("333");
		System.out.println("一开始：  "+loopqueue.list);
		for(int i=0;i<100;i++){
			String S = loopqueue.get();
			System.out.println("取出一个元素： "+S+"  之后list的情况为：  "+loopqueue.list);
		}
	}

}
