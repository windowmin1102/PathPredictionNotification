<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.json.simple.*"%>
<%@page import="org.jdom2.*"%>

<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta http-equiv="content-type" content="text/html;charset=UTF-8">
        <meta name="viewport" content="user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, width=device-width">
        <title>알림등록</title>
		<link href="StyleSheet.css" rel="stylesheet" type="text/css">
    </head>
    <body>
        <header>
            <h1>알림등록</h1>
        </header>
		<script language = "javascript"> // 자바 스크립트 시작
        function writeCheck() { 
        	var form = document.writeform; 
        	if( !form.kind.value ){ 
        		alert( "종류를 입력해 적어주세요" );
        		form.kind.focus();
        		return; 
        	} if( !form.location.value ) {
        		alert( "위치를 입력해 적어주세요" );
        		form.location.focus(); 
        		return; 
        	} if( !form.title.value ) {
        		alert( "내용을 입력해 주세요" );
        		form.title.focus(); 
        		return; 
        	} 
        		form.submit(); 
        	} 
		</script>

      
        <div class="wrap">
        <form name=writeform method=post action="write_ok.jsp">

            <div class="content_line">
                <span class="header">종류</span>
                <span><input name="kind" id="kind" type="text" class="input"></span>
            </div>
            <div class="content_line">
                <span class="header">도로아이디</span>
                <span><input name="location" id="location" type="text" class="input"></span>
            </div>
            <div class="content_line">
                <span class="header">글내용</span>
                <span><textarea name="title" id="title" class="text"></textarea></span>
            </div>
            </form>
        </div>
        <footer>
        <input type=button value="등록" OnClick="javascript:writeCheck();">
        </footer>
    </body>
</html>

