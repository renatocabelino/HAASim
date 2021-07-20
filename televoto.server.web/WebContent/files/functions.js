//Hour
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#loadData').load("pages/time.jsp");
	}, 1000); } );

//Total call
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#callTotal').load("pages/callTotal.jsp");
		}, 1000); } );

//Closed call history
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#callNotAnswered').load("pages/callNotAnswered.jsp");
	}, 1000); } );

//On negotiation call
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#callOnNegotiation').load("pages/callOnNegotiation.jsp");
	}, 1000); } );

//Established call
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#callEstablished').load("pages/callEstablished.jsp");
	}, 1000); } );

//Closing call
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#callInClosing').load("pages/callInClosing.jsp");
	}, 1000); } );
			//Total calls history
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#callTotalH').load("pages/callTotalHistory.jsp");
		}, 1000); } );
//Closed call history
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#callClosedH').load("pages/callClosedHistory.jsp");
	}, 1000); } );

//Lost call history
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#callLostH').load("pages/callLostHistory.jsp");
	}, 1000); } );

//Canceled call history
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#callCanceledH').load("pages/callCanceledHistory.jsp");
	}, 1000); } );

//Agent total
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#agentTotal').load("pages/agentTotal.jsp");
		}, 1000);
	}
);

//Agent idle
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#agentIdle').load("pages/agentIdle.jsp");
	}, 1000); } );

//Agent busy
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#agentBusy').load("pages/agentBusy.jsp");
	}, 1000); } );

//Waiting Time
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#WaitingTime').load("pages/waitingTime.jsp");
		}, 1000); } );

//Queue Time
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#QueueTime').load("pages/queueTime.jsp");
		}, 1000); } );

//Arrival Rate 
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#ArrivalRate').load("pages/arrivalRate.jsp");
		}, 1000); } );

//Average Waiting Time
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#averageWaitingTime').load("pages/averageWaitingTime.jsp");
		}, 1000); } );

//Average Queue Time
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#averageQueueTime').load("pages/averageQueueTime.jsp");
		}, 1000); } );

//Average Arrival Rate 
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#averageArrivalRate').load("pages/averageArrivalRate.jsp");
		}, 1000); } );