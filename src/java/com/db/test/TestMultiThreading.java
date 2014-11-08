package com.db.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestMultiThreading {

	private final int COUNT = 1000000;
	private volatile List<String> strNumbersMultPI = new ArrayList<String>();
	private volatile List<String> strNumbers = new ArrayList<String>(COUNT);

	public static void main(String[] args) {
		new TestMultiThreading();
	}

	public TestMultiThreading() {
		long t1 = System.currentTimeMillis();
		generateData();
		processData(0, COUNT);
		System.out.println("Dauer: " + (System.currentTimeMillis() - t1));
		for (int i = 0; i < 10; i++) {
			System.out.println(strNumbers.get(i) + "->" + strNumbersMultPI.get(i));
		}
		
//		generateData();
		processDataMultiThreaded();
		for (int i = 0; i < 10; i++) {
			System.out.println(strNumbers.get(i) + "->" + strNumbersMultPI.get(i));
		}
	}

	private void processData(int from, int to) {
		for (int i = from; i < to; i++) {
			double d = Double.valueOf(strNumbers.get(i)) * Math.PI;
			strNumbersMultPI.add(String.valueOf(d));
		}
	}
	
	private void processDataMultiThreaded() {
		int countProcessors = Runtime.getRuntime().availableProcessors();
		
		List<Thread> thrs = new ArrayList<Thread>(countProcessors);
		final int countPerProc = COUNT / countProcessors;
		for (int i = 0; i < countProcessors; i++) {
			final int from = (i * countPerProc);
			final int to = from + countPerProc;
			Thread thr = new Thread() {
				@Override
				public void run() {
					String threadName = Thread.currentThread().getName();
					System.out.println(threadName + " started. Processing " + from + "-" + to);
					processData(from, to);
					System.out.println(threadName + " finished");
				}
			};
			thrs.add(thr);
		}
		long t1 = System.currentTimeMillis();
		for (int i = 0; i < countProcessors; i++) {
			thrs.get(i).start();
		}
		for (int i = 0; i < countProcessors; i++) {
			try {
				thrs.get(i).join();
			} catch (InterruptedException ex) {
				Logger.getLogger(TestMultiThreading.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println("Dauer: " + (System.currentTimeMillis() - t1));
	}

	private List<String> generateData() {
		Random rnd = new Random();
		for (int i = 0; i < COUNT; i++) {
			double x = rnd.nextDouble() * COUNT;
			strNumbers.add(String.valueOf(x));
		}
		return strNumbers;
	}
}
