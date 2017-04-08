package com.qdcz.spider.utils;

import java.io.IOException;
import java.net.URI;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class HDFSOperations {
	
	public static void main(String args[]){
		System.out.println("file length "+getFileLength(args[0]));
		System.out.println("file number "+getFileNum(args[0]));
		
	}
	
	public static void get_sub_catalog(String uri,Vector<Path> catalogs) throws IOException{
		
		Configuration conf = new Configuration();
		FileSystem.get(conf);
	    FileSystem fs = FileSystem.get(URI.create(uri), conf);
	    	
		Path path = new Path(uri);
		FileStatus status = fs.getFileStatus(path);
	
		if(!status.isDir())
		   return;
		
		FileStatus[] sub_status = fs.listStatus(path);
		Path[] sub_paths = FileUtil.stat2Paths(sub_status);
		
		for(Path one : sub_paths){
			catalogs.add(one);
		}
	}

	public static boolean create_catalog(String uri) {

		boolean isSuccess = false;
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(uri), conf);
			Path path = new Path(uri);
			isSuccess = fs.mkdirs(path);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return isSuccess;
	}
	
	public static boolean delete_catalog(String uri){
		
		boolean isSuccess = false;

		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(uri), conf);
			Path path = new Path(uri);
			isSuccess = fs.delete(path, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return isSuccess;
	}
	
	/**
	 * 删除文件
	 * 
	 * @param uri
	 * @return
	 */
	public static void deleteFile(String uri) {
		delete_catalog(uri);
	}
	
	//lzy 2015-12-26 判断hdfs下目录是否存在
	public static boolean ifExist(String uri){
		boolean ifExist = false;
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(uri), conf);
			Path path = new Path(uri);
			ifExist =fs.exists(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ifExist;
	}
	
	//lzy 2015-12-26 获得文件夹下的文件个数
	public static int getFileNum(String uri){
		int filenum = 0;
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(uri), conf);
			Path path = new Path(uri);
			FileStatus[] filelist = fs.listStatus(path);
			filenum = filelist.length;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filenum;
	}

	//lzy 2015-12-27 获得文件大小
	public static long getFileLength(String uri){
		long filelen = 0;
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(uri), conf);
			Path path = new Path(uri);
			FileStatus[] filelist = fs.listStatus(path);
			for(FileStatus file : filelist)
				filelen = file.getLen()+filelen;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filelen;
	}
	
	public static void copy_to_local(String src,String des){
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get( conf);
			Path srcPath = new Path(src);
			Path desPath = new Path(des);
			fs.copyToLocalFile(srcPath, desPath);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static boolean rename_catalog(String source,String target){
		boolean isSuccess = false;
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(conf);
			Path sourcePath = new Path(source);
			Path targetPath = new Path(target);
			isSuccess = fs.rename(sourcePath, targetPath);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return isSuccess;
	}
	
	public static boolean copy_catalog(String source,String target) throws IOException, InterruptedException, ClassNotFoundException{
		boolean isSuccess = false;
		
		Configuration conf = new Configuration();
		Job job = new Job(conf,"Copy One Catalog to Another");
		job.setJarByClass(HDFSOperations.class);
		job.setMapperClass(Copy_One_Catalog_To_Another_Map.class);
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(Text.class);       
	    job.setOutputValueClass(NullWritable.class);
	    FileInputFormat.addInputPath(job, new Path(source));
	    FileOutputFormat.setOutputPath(job, new Path(target));
	    
	    job.waitForCompletion(true);
	
		return isSuccess;
	}
	
	public static void merge_catalogs(String one,String two,String out) throws IOException, InterruptedException, ClassNotFoundException{
		
		Configuration conf = new Configuration();
		Job job = new Job(conf,"Merge Catalogs");
		job.setJarByClass(HDFSOperations.class);
		job.setMapperClass(Merge_Catalogs_Map.class);
		job.setReducerClass(Merge_Catalogs_Reduce.class);
		job.setNumReduceTasks(30);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(NullWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);
		FileInputFormat.addInputPath(job, new Path(one));
		FileInputFormat.addInputPath(job, new Path(two));
		FileOutputFormat.setOutputPath(job, new Path(out));
		
		job.waitForCompletion(true);
	}
	
	public static class Merge_Catalogs_Map extends Mapper<Object,Text,Text,NullWritable>{
		public void map(Object key,Text value,Context context){
			try {
				context.write(value, NullWritable.get());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static class Merge_Catalogs_Reduce extends
	Reducer<Text, NullWritable, Text, NullWritable>{
		
		public void reduce(Text key, Iterable<NullWritable> values,
				Context context){
			try {
				context.write(key, NullWritable.get());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static class Copy_One_Catalog_To_Another_Map extends
	Mapper<Object, Text, Text, NullWritable>{
		public void map(Object key, Text value, Context context) {
			try {
				context.write(value, NullWritable.get());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//read file from hdfs
	public static String readConfigFile(String configPath) {
		String uri = configPath;
		if(uri.equals("")){
			System.out.println("file path error");
			return null;
		}
		byte[] buffer = null;
		try {
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(URI.create(uri), conf);
			Path path = new Path(uri);

			if (fs.exists(path)) {
				FileStatus stat = fs.getFileStatus(path);
				FSDataInputStream in = null;
				int buffer_length = Integer.parseInt(String.valueOf(stat.getLen()));
				buffer = new byte[buffer_length];

				in = fs.open(path);
				IOUtils.readFully(in, buffer, 0, buffer_length);
				in.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new String(buffer);
	}
	
	

}
