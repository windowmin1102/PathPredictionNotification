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

	String sqlRoad = "SELECT * FROM road";

	try {
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection(url, id, pw);
		pstmt = conn.prepareStatement(sqlRoad);
		rs = pstmt.executeQuery(); //db 실행

		JSONObject jsonMain_road = new JSONObject();
		JSONArray jArray_road = new JSONArray();
		int count_road = 0;
		while (rs.next()) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", rs.getString("id"));
			jsonObject.put("pid_left", rs.getString("point_left"));
			jsonObject.put("pid_right", rs.getString("point_right"));
			jsonObject.put("include_sid", rs.getString("include_segment"));
			jArray_road.add(count_road, jsonObject);
			count_road++;
		}
		jsonMain_road.put("sendRoad", jArray_road);

		out.clear();
		out.println(jsonMain_road);
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