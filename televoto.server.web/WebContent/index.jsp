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
		<title>Televoto Server's Report</title>
		<script type="text/javascript" src="files/jquery.js"></script>
		<script type="text/javascript" src="files/functions.js"></script>
		<link rel="stylesheet" type="text/css" href="files/style.css">
	</head>
	
	<body>
		<div id="container">
			<div id="title">
				<H1>Televoto Server's Report</H1>
				<div id="loadData"> </div>
			</div>
			
			<br>
			<div id="container1">	
				
				<div id="col1">
		    		<p class="title">Real-time Agents</p>
		    		<table>
		    			<tr>
		    				<td>Total: </td>
		    				<td> <span id="agentTotal"></span> </td>
		    			</tr>
		    			<tr>
		    				<td> Idle:</td>
		    				<td> <span id="agentIdle"></span> </td>
		    			</tr>
		    			<tr>
		    				<td> Busy: </td>
		    				<td> <span id="agentBusy"></span> </td>
		    			</tr>
		    		</table>
				</div>
				
				<div id="col2">
					<p class="title">Real-time Calls</p>
					<table>
		    			<tr>
		    				<td>Total: </td>
		    				<td> <span id="callTotal"></span> </td>
		    			</tr>
		    			<tr>
		    				<td> Not answered:</td>
		    				<td> <span id="callNotAnswered"></span> </td>
		    			</tr>
		    			<tr>
		    				<td> On negotiation: </td>
		    				<td> <span id="callOnNegotiation"></span> </td>
		    			</tr>
		    			<tr>
		    				<td> Established: </td>
		    				<td> <span id="callEstablished"></span> </td>
		    			</tr>
		    			<tr>
		    				<td> In closing: </td>
		    				<td> <span id="callInClosing"></span> </td>
		    			</tr>
		    		</table>
				</div>
						
				<div id="col3">
					<p class="title">Call History</p>
					<table>
		    			<tr>
		    				<td>Total: </td>
		    				<td> <span id="callTotalH"></span> </td>
		    			</tr>
		    			<tr>
		    				<td> Closed:</td>
		    				<td> <span id="callClosedH"></span> </td>
		    			</tr>
		    			<tr>
		    				<td> Lost: </td>
		    				<td> <span id="callLostH"></span> </td>
		    			</tr>
		    			<tr>
		    				<td> Canceled: </td>
		    				<td> <span id="callCanceledH"></span> </td>
		    			</tr>
		    		</table>
				</div>						
			</div>   <!--container1-->
			
			<div id="container2">
				<div id="col4">
					<table>
						<tr>
							<td class="title">Waiting Time:</td>
							<td><span id="WaitingTime"></span> sec</td>
						</tr>
						<tr>
							<td class="title">Queue Time:</td>
							<td><span id="QueueTime"></span> sec</td>
						</tr>
						<tr>
							<td class="title">Arrival Rate:</td>
							<td><span id="ArrivalRate"></span> ag/sec</td>
						</tr>
						<tr><td><br></td></tr>
						<tr>
							<td class="title">Average Waiting Time:</td>
							<td><span id="averageWaitingTime"></span> sec</td>
						</tr>
						<tr>
							<td class="title">Average Queue Time:</td>
							<td><span id="averageQueueTime"></span> sec</td>
						</tr>
						<tr>
							<td class="title">Average Arrival Rate:</td>
							<td><span id="averageArrivalRate"></span> ag/sec</td>
						</tr>
					</table>
				</div>
				<div id="col5">
					<div id="startAgents">
						<p class="title"> Start More Agents </p>
						<form method="post" action="StartStopAgentsServlet">
						  <input type="radio" name="qtt" value="25"> 25%
						  <input type="radio" name="qtt" value="50"> 50%
						  <input type="radio" name="qtt" value="100"> 100%
						  <p><input id="submit" type="submit" value="Start"/></p>
						</form>
					</div>
				</div>
				<div id="col6">
					<div id="stopAgents">
						<p class="title"> Stop Agents </p>
						<form method="post" action="StartStopAgentsServlet">
						  <input type="radio" name="qtt" value="-25"> 25%
						  <input type="radio" name="qtt" value="-50"> 50%
						  <input type="radio" name="qtt" value="-100"> 100%
						  <p><input id="submit" type="submit" value="Stop"/></p>
						</form>
					</div>
				</div>
			</div> <!--container2-->
			
		</div> <!-- container -->
	</body>
</html>

<%	
    Thread tSocket = new Thread( new ServerSocket() );
	tSocket.start();
%>