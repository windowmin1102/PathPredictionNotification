<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.json.simple.*"%>
<%@page import="org.jdom2.*"%>
        <%
request.setCharacterEncoding("UTF-8"); //받아오는 값들을 한글로 인코딩

String kind = request.getParameter("kind"); 
String location = request.getParameter("location");
String title = request.getParameter("title");  

java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
String today = formatter.format(new java.util.Date());
int maxNum = 0;
 Class.forName("com.mysql.jdbc.Driver");
	String url = "jdbc:mysql://localhost/path_prediction_notification?serverTimezone=UTC&useSSL=false";
	String id = "root";
	String pass = "941102";
	
	try {
		Connection conn = DriverManager.getConnection(url,id,pass);
		Statement stmt = conn.createStatement();
		String sqlMax_num = "select MAX(num) AS alias FROM notification_information";
		ResultSet rs = stmt.executeQuery(sqlMax_num);
		rs = stmt.executeQuery(sqlMax_num);
		
		if(rs.next()) {
			maxNum = rs.getInt(1) + 1;
		}else {
			maxNum = 1;
		}
		rs.close();
		String sql = "INSERT INTO notification_information VALUES(?,?,?,?,?)"; 
		PreparedStatement pstmt = conn.prepareStatement(sql);
		
		pstmt.setInt(1, maxNum);
		pstmt.setString(2, location);
		pstmt.setString(3, kind);
		pstmt.setString(4, title);
		pstmt.setString(5, today);
		
		pstmt.execute();
		pstmt.close();
		conn.close(); 
	}catch(SQLException e) { 
		out.println( e.toString() );
		} 

%>
  <script language=javascript>
   self.window.alert("입력한 글을 저장하였습니다.");
   location.href="list.jsp"; 
</script>

