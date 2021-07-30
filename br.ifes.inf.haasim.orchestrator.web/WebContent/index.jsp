<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"
import="java.io.IOException,
		java.net.DatagramPacket,
		java.net.DatagramSocket,
		bridge.TelevotoBean,
		bridge.ServerSocket,
		java.util.*" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Televoto's Report</title>
		<script type="text/javascript" src="files/jquery.js"></script>
		<script type="text/javascript" src="files/functions.js"></script>
		<link rel="stylesheet" type="text/css" href="files/style.css">
	</head>
	
	<body>
		<div id="container">
			<div id="title">
				<H1>HDash Cloud HAASim Generator's Report</H1>
				<div id="loadData"> </div>
			</div>
			
			<br>
			<div id="container1">	
			
				<div id="col1">
		    		<p class="title">Generation Data</p>
		    		<table>
		    			<tr>
		    				<td>Agents: </td>
		    				<td> <span id="origAgents"></span> </td>
		    			</tr>
		    			<tr>
		    				<td>Redial: </td>
		    				<td> <span id="origRedial"></span> </td>
		    			</tr>
		    			<tr>
		    				<td>Frequency: </td>
		    				<td> <span id="origFrequency"></span> </td>
		    			</tr>
		    		</table>
				</div>
				
				<div id="col2">
		    		<p class="title">Real-time Agents</p>
		    		<table>
		    			<tr>
		    				<td>Total: </td>
		    				<td> <span id="agentTotal"></span> </td>
		    			</tr>
		    		</table>
				</div>
						
				<div id="col3">
					<div id="startAgents">
						<p class="title"> Start More Agents </p>
						<form method="post" action="StartAgentsServlet">
						  <input type="radio" name="qtt" value="25"> 25%
						  <input type="radio" name="qtt" value="50"> 50%
						  <input type="radio" name="qtt" value="100"> 100%
						  <p><input id="submit" type="submit" value="Start"/></p>
						</form>
					</div>
				</div>						
			</div>   <!--container1-->
			
		</div> <!-- container -->
	</body>
</html>

<%	
    Thread tSocket = new Thread( new ServerSocket() );
	tSocket.start();
%>