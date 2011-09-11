<%@ include file="header.jsp"%>
<html>
<head>
<title>AgentSpring dashboard</title>
<%@ include file="head.jsp"%>
<script type="text/javascript"
    src="${root}resources/jslib/highcharts.js"></script>
<script type="text/javascript"
    src="${root}resources/jslib/exporting.js"></script>
<script type="text/javascript" src="${root}resources/js/dashboard.js"></script>
<script type="text/javascript" src="${root}resources/js/visualization.js"></script>
</head>
<body id="tab1" class="faux">
    <%@ include file="tabbar.jsp"%>
    <div class="selection left">
        <a class="button2" id="monitor">Monitor</a>
        <div id="visuals"></div>
        <div id="residual"></div>
    </div>
    <div class="content">
        <div id="charts"></div>
        <div id="log"></div>
    </div>
    <%@ include file="footer.jsp"%>
</body>
</html>
