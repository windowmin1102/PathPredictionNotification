<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.json.simple.JSONObject"%>
<%@ page import="org.json.simple.JSONArray"%>
<%@page import="org.jdom2.*"%>
<%@page import="org.jdom2.output.*"%>

<%
String url = "jdbc:mysql://localhost/location_based_service?serverTimezone=UTC&useSSL=false";
String id="root";
String pw="941102";
Connection conn=null;
PreparedStatement pstmt=null;
ResultSet rs=null;

String sql="SELECT * FROM board";

try{
    Class.forName("com.mysql.jdbc.Driver");
    conn=DriverManager.getConnection(url,id, pw);
    
    pstmt=conn.prepareStatement(sql);
    rs=pstmt.executeQuery();	//db 실행
    
    JSONObject jsonMain=new JSONObject();
    JSONArray jArray=new JSONArray();
    int count=0;			//json 인덱스
 
    while(rs.next()){           
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("user_kind", rs.getString("kind"));
        jsonObject.put("user_location", rs.getString("location"));
        jsonObject.put("user_title", rs.getString("title"));
        jsonObject.put("user_times", rs.getString("times"));
        jArray.add(count, jsonObject);
        count++;
    }
     jsonMain.put("seandData", jArray);
    out.clear();
    out.println(jsonMain);
    out.flush();
    
}catch(Exception e){
    e.getStackTrace();
}finally{
    try{
        if(rs!=null)rs.close();
        if(pstmt!=null)pstmt.close();
        if(conn!=null)conn.close();
    }catch(Exception ex){
        ex.getStackTrace();
    }
}
%>