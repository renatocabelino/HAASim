//Hour
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#loadData').load("pages/time.jsp");
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

//Agent total
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#agentTotal').load("pages/agentTotal.jsp");
		}, 1000);
	}
);

//Original data generation - Agents
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#origAgents').load("pages/originalAgents.jsp");
		}, 1000);
	}
);

//Original data generation - Redial
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#origRedial').load("pages/originalRedial.jsp");
		}, 1000);
	}
);

//Original data generation - Frequency
jQuery(document).ready(function() {
	setInterval(function() {
		jQuery('#origFrequency').load("pages/originalFrequency.jsp");
		}, 1000);
	}
);