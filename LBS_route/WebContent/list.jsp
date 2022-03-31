<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.json.simple.*"%>
<%@page import="org.jdom2.*"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head>
 <title>게시판</title>
 <meta http-equiv="content-type" content="text/html;charset=UTF-8">
 <meta name="viewport" content="user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, width=device-width">
 <link href="StyleSheet.css" rel="stylesheet" type="text/css">
 </head>
 <body>
 <%
 Class.forName("com.mysql.jdbc.Driver");
	String url = "jdbc:mysql://localhost/path_prediction_notification?serverTimezone=UTC&useSSL=false";
	String id = "root";
	String pass = "941102";
	
	try {
		Connection conn = DriverManager.getConnection(url,id,pass);
		Statement stmt = conn.createStatement();
		String sqlList = "SELECT * from notification_information order by num asc";
		ResultSet rs = stmt.executeQuery(sqlList);
		rs = stmt.executeQuery(sqlList);
%>      

        <header>
            <h1>알림 정보</h1>
        </header>

	<div class="wrap">
    	<ul>
 <%
 		int count=0;
		while(rs.next()) {
			int idx = rs.getInt(1);
			String title = rs.getString(4);
			String kind = rs.getString(3);
			String location = rs.getString(2);
			String time = rs.getString(5);
			
%>
     		<li>
				<div>
            		<span><strong><%=title %></strong></span>
    			</div>
        		<div>
            		<span><%=idx %>  |  <%=kind %>  |  <%=time %></span>
        		</div>
<% 
		}

	rs.close();
	stmt.close();
	conn.close();
} catch(SQLException e) {
	out.println( e.toString() );
}
%>

 			</li>
		</ul>
	</div>
        <footer>
        <input type=button value="글쓰기" OnClick="window.location='Write.jsp'">
        </footer>

</body> 
</html>



