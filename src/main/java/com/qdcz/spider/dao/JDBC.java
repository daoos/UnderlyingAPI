package com.qdcz.spider.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * JDBC数据访问工具类， 在不使用连接池时可使用该类访问数据库
 * 
 * @author jd
 *
 */
public class JDBC {
	String		driver	= "com.mysql.jdbc.Driver";
	String		url		= "jdbc:mysql://172.16.40.18:3306/forum_message?useUnicode=true&characterEncoding=utf8&rewriteBatchedStatements=true";
	String		user	= "root";
	String		pwd		= "yqmysql";
	String      name    = "allforum";
	int			count;
	Connection	conn;
	Statement	state;
	ResultSet	rs;

	public JDBC() {
		try {
			Class.forName(driver);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}
	
	public JDBC(String host,String port,String databaseName,String charset,String user,String password) {
		this.url = "jdbc:mysql://"+host+":"+port+"/"+databaseName+"?useUnicode=true&characterEncoding="+charset;
		this.user = user;
		this.pwd = password;
		this.name = databaseName;
		try {
			Class.forName(driver);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}
	}

	public void connect2() {
		try {
			conn = DriverManager.getConnection(url, user, pwd);
			state = conn.createStatement();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void connect() {
		try {
			conn = DriverManager.getConnection(url, user, pwd);
			conn.setAutoCommit(true);
			state = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return this.conn;
	}
	
	public Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(url, user, pwd);
	}
	
	public void close() {
		try {
			if (state != null) {
				state.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public ResultSet executeQuery(String sql) {
		try {
			if (state == null) {
				connect();
			}
			rs = state.executeQuery(sql);
		} catch (Exception e) {
			System.out.println(e);
		}
		return rs;
	}

	public int getCounts(String sql) {
		try {
			if (state == null) {
				connect();
			}
			rs = state.executeQuery(sql);
			rs.last();
			count = rs.getRow();
		} catch (Exception e) {
			System.out.println(e);
		}
		return count;
	}

	public int executeUpdate(String sql) {
//		System.out.println(sql);
//		MyLog.debug(this.getClass(),sql);
		int num = 0;
		try {
			if (state == null) {
				connect();
			}
			num = state.executeUpdate(sql);
		} catch (Exception e) {
			System.out.println(e);
		}
		return num;
	}

	public void rollback() throws SQLException {
		conn.rollback();
	}

	public void commit() throws SQLException {
		conn.commit();
	}

	public int[] executeBatch(String[] sql) {
		int[] result = null;
		try {
			Statement sm = conn.createStatement();
			for (int i = 0; i < sql.length; i++) {
				sm.addBatch(sql[i]);
			}
			result = sm.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean ifTableExit(String tablename) throws SQLException{
		DatabaseMetaData dbmd = conn.getMetaData();
		rs = dbmd.getTables(null, null, tablename, null);
		return rs.next();
	}
	
	
	//判断forum_info表中是否有某条记录存在
	public boolean ifRecordExit(String forumname) throws SQLException{
		String sql = "select * from `forum_info` where domain = \""+forumname+"\"";
//		System.out.println(sql);
		rs = executeQuery(sql);
		return rs.next();	
	}


	public static void main(String args[]) throws SQLException {
		JDBC d = new JDBC();
	}
}