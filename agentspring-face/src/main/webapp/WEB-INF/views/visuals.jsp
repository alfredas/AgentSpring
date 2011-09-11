<%@ include file="header.jsp"%>
<html>
<head>
<title>Visuals editor</title>
<%@ include file="head.jsp"%>
<script type="text/javascript" src="${root}/resources/js/visuals.js"></script>
</head>
<body id="tab3" class="faux">
    <%@ include file="tabbar.jsp"%>
    <script type="text/javascript">
                    var visual_id = "${visual.id}";
                </script>
    <div class="selection left">
        <a class="button2" href="${root}visuals/new">New visual</a>
        <div id="visuals"></div>
    </div>
    <div class="content">
        <div id="editing">
            <span class="labeljust"><label for="clazz">Visual class: </label></span>
            <select id="clazz">
                <option value="chart" selected="selected">Time
                    chart</option>
                <option value="scatter">Scatterchart</option>
            </select> <br /> <input id="id" value="${visual.id}" type="hidden" />
            <span class="labeljust"><label for="title">Visual title: </label></span>
            <input id="title"
                value="${visual.title}" maxlength="40"/> <br />
            <div class="yaxis">
                <span class="labeljust"><label for="yaxis">Yaxis label: </label></span><input
                    id="yaxis" maxlength="40" value="${visual.yaxis}" />
            </div>
            <div class="type">
                <span class="labeljust"><label for="type">Chart type: </label></span><select id="type">
                    <option value="line">Line</option>
                    <option value="area">Area</option>
                    <option value="stacked_area">Stacked area</option>
                </select>
            </div>
            <div id="selected_sources">
                <!-- Selected sources for current visual -->
            </div>
            <div>
                <input type="button" value="Save visual" id="save" /> <input
                    type="button" value="Delete visual" id="delete" />
                <div id="success" class="invisible inline"></div>
                <div id="error" class="invisible inline"></div>
            </div>
            <p>Click on data source to add it to visual:</p>
            <div id="sources">
                <!-- Available unselected sources -->
            </div>
            
        </div>
    </div>
    <%@ include file="footer.jsp"%>
</body>
</html>