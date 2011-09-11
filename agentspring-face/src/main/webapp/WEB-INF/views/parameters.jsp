<%@ include file="header.jsp"%>
<html>
<head>
<title>Scenario parameters editor</title>
<%@ include file="head.jsp"%>
<link href="${root}resources/css/jquery-ui-1.8.16.custom.css"
    rel="stylesheet" type="text/css" />
<script type="text/javascript"
    src="${root}resources/jslib/jquery-ui-1.8.16.custom.min.js"></script>
<script type="text/javascript" src="${root}resources/js/parameters.js"></script>
</head>
<body id="tab4">
    <%@ include file="tabbar.jsp"%>
    <%@ include file="footer.jsp"%>
    <div class="content2">
        <div id="params"></div>
        <div>
            <input type="button" value="Save parameters" id="save" />
            <div id="success" class="invisible inline"></div>
            <div id="error" class="invisible inline"></div>
        </div>
    </div>
</body>
</html>