package com.qdcz.spider.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.qdcz.spider.dao.JDBC;

public class SQLOperate {
	//数据库连接参数
	private String host;
	private String port;
	private String databaseName;
	private String charset;
	private String user;
	private String psword;
	
    // 数据库
	public JDBC datatool = null;
    
    /**
     * 构造方法
     * @param host
     * @param port
     * @param databaseName
     * @param charset
     * @param user
     * @param psword
     */
    public SQLOperate(String host, String port,String databaseName,String charset,String user, String psword) {
    	this.host = host;
    	this.port = port;
    	this.databaseName = databaseName;
    	this.charset = charset;
    	this.user = user;
    	this.psword = psword;
    	datatool = getJDBC();
    }

    private JDBC getJDBC() {
		JDBC jdbc = new JDBC(host, port,
				databaseName, charset,
				user, psword);
		jdbc.connect();
		return jdbc;
    }

    public void varifyDatatool() {
		try {
		    if (datatool == null) {
				datatool = getJDBC();
				return;
		    }
		    if (datatool.getConnection() == null) {
				datatool = getJDBC();
				return;
		    }
		    if (datatool.getConnection().isClosed()) {
				datatool = getJDBC();
				return;
		    }
		    if (datatool.getConnection().isValid(10000)) {
		    	datatool = getJDBC();
		    }
		} catch (SQLException e1) {
		    datatool = getJDBC();
		}
    }

    

    public static void main(String[] args) throws Exception {
    	// FetchController fs = new FetchController("h126", "tree_development");
    }



    public ResultSet executeQuery(String sql) {
    	varifyDatatool();
		ResultSet result = null;
		try {
		    result = datatool.executeQuery(sql);
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
		return result;
    }

    public int execute(String sql) {
    	varifyDatatool();
    	int num = 0;//数据库操作影响的行数
    	try {
    		num =  datatool.executeUpdate(sql);
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
    	return num;
    }

    public void close_db() {
    	datatool.close();
    }

}
