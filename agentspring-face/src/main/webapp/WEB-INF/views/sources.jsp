<%@ include file="header.jsp"%>
<html>
<head>
<title>Data sources editor</title>
<%@ include file="head.jsp"%>
<script type="text/javascript" src="${root}/resources/jslib/ace.js"
    charset="utf-8"></script>
<script type="text/javascript"
    src="${root}/resources/jslib/mode-java.js" charset="utf-8"></script>
<script type="text/javascript"
    src="${root}/resources/jslib/jquery.dump.js" charset="utf-8"></script>
<script type="text/javascript" src="${root}/resources/js/sources.js"></script>
</head>
<body id="tab2" class="faux">
    <%@ include file="tabbar.jsp"%>
    <div class="left selection">
        <a class="button2" href="${root}sources/new">New data source</a>
        <div id="sources"></div>
    </div>
    <div class="content">
        <div id="editing">
            <label for="start_node">Start node: </label> <select
                id="start_node">
                <option value="">[No start node]</option>
                <c:forEach var="node" items="${start_nodes}">
                    <c:choose>
                        <c:when test="${node eq data_source.start}">
                            <option id="${node}" selected="selected">${node}</option>
                        </c:when>
                        <c:otherwise>
                            <option id="${node}">${node}</option>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </select><input id="id" value="${data_source.id}" type="hidden" /> <label
                for="title">Data source title: </label><input id="title"
                value="${data_source.title}" maxlength="40"/>
            <div id="editor">${data_source.script}</div>
            <br />
            <div>
                <div class="left">
                    <input type="button" value="Test query" id="test" />
                    <input type="button" value="Save query" id="save" />
                    <input type="button" value="Delete data source"
                        id="delete" />
                    <div id="success" class="invisible inline"></div>
                    <div id="error" class="invisible inline"></div>
                </div>
            </div>
            <br />
            <div>
                <pre id="output"></pre>
            </div>
        </div>
        <div id="editing_help">
            For example:
            <pre>
[v.label, v.in("REGION")
       .in("LOCATION")
       .filter{f.plantIsOperational(it, tick)}
       .out("TECHNOLOGY")
       .sum{it.capacity}]</pre>
            See: <a href="https://github.com/tinkerpop/gremlin/wiki">Gremlin Wiki</a>
        </div>
    </div>
    <%@ include file="footer.jsp"%>
</body>
</html>