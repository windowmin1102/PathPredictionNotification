<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>



<%@ page import="java.sql.*"%>
<%@ page import="java.util.Date"%>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.json.simple.JSONArray"%>
<%@page import="org.jdom2.*"%>
<%@page import="org.jdom2.output.*"%>

<%
request.setCharacterEncoding("UTF-8");
	

String sid = request.getParameter("sid");


		String url = "jdbc:mysql://localhost/path_prediction_notification?serverTimezone=UTC&useSSL=false";
		String id = "root";
		String pw = "941102";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "select * from notification_information where segment_id="+"'"+sid+"'";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, id, pw);
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery(); //db 실행
		
			JSONObject jsonMain = new JSONObject();
			JSONArray jArray = new JSONArray();
			int count = 0;
			while (rs.next()) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("num", rs.getInt("num"));
					jsonObject.put("segment_id", rs.getString("segment_id"));
					jsonObject.put("kind", rs.getString("kind"));
					jsonObject.put("content", rs.getString("notification_content"));
					jsonObject.put("time", rs.getString("date_time"));
					jArray.add(count, jsonObject);
					count++;
				
			}
			jsonMain.put("sendNotification", jArray);
			out.println(jsonMain);
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
