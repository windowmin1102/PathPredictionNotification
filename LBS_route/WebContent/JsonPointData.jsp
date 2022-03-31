<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.Date"%>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.json.simple.JSONArray"%>
<%@page import="org.jdom2.*"%>
<%@page import="org.jdom2.output.*"%>

<%
	String url = "jdbc:mysql://localhost/path_prediction_notification?serverTimezone=UTC&useSSL=false";
	String id = "root";
	String pw = "941102";
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;

	String sqlPoint = "SELECT * FROM point";

	try {
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(url, id, pw);

		pstmt = conn.prepareStatement(sqlPoint);
		rs = pstmt.executeQuery(); //db 실행

		JSONObject jsonMain_point = new JSONObject();
		JSONArray jArray_point = new JSONArray();
		int count_point = 0; //json 인덱스

		while (rs.next()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", rs.getString("id"));
			jsonObject.put("latitude", rs.getDouble("latitude"));
			jsonObject.put("longitude", rs.getDouble("longitude"));
			jsonObject.put("cross_road", rs.getBoolean("cross_road"));
			jArray_point.add(count_point, jsonObject);
			count_point++;
		}
		jsonMain_point.put("sendPoint", jArray_point);
		out.clear();
		out.println(jsonMain_point);
		out.flush();
	} catch (Exception e) {
		e.getStackTrace();
	} finally {
		try {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null)
				conn.close();
		} catch (Exception ex) {
			ex.getStackTrace();
		}
	}
%>