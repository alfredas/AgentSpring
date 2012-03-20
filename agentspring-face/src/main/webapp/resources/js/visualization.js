function BasicChartOptions() {
    this.chart = {
        defaultSeriesType : 'line',
        width : 500,
        animation : false,
        borderColor : '#ECF1EF',
        borderWidth : 1,
        zoomType: 'xy'
    };
    this.plotOptions = {
        series : {
            animation : false
        },
        area : {
            marker : {
                enabled : false
            }
        },
        line : {
            marker : {
                enabled : false
            }
        }
    };
    this.title = {};
    this.xAxis = {
        title : {
            text : 'Time'
        }
    };
    this.yAxis = {
        title : {
            text : 'Euro'
        },
        min : null
    };
    this.exporting = {
        enabled : true
    };
    this.credits = {
        enabled : false
    };
    this.series = [];
};

function Highchart(chart_data) {
    this.chart_data = chart_data;
}

Highchart.prototype.init = function(series) {
    var chart;
    var chart_options = new BasicChartOptions();

    var container = $("<div />", {
        class : 'vis-container'
    })[0];
    var buttons = $("<div />", {
        class : 'vis-buttons'
    })
    var edit_button = $('<img/>', {
        src : root + "resources/img/edit.png",
        class : 'vis-button',
        alt : this.chart_data.id
    });
    $(edit_button).click(function() {
        window.location = root + "visuals/edit?id=" + this.alt;
    });

    if (this.chart_data.clazz == "scatter") {
        chart_options.chart.defaultSeriesType = "scatter";
    } else {
        if (this.chart_data.type == "stacked_area") {
            chart_options.plotOptions.area.stacking = "normal";
            chart_options.chart.defaultSeriesType = "area";
        } else {
            chart_options.chart.defaultSeriesType = this.chart_data.type;
        }
    }
    if (page == "monitor") {
        var button = $('<img/>', {
            src : root + "resources/img/remove2.png",
            class : 'vis-button',
            alt : this.chart_data.id
        });
        $(buttons).append(button);
        $(buttons).append(edit_button);
        $(button).click(remove_visual);
    } else {
        $(buttons).append(edit_button);
        chart_options.chart.width = 1000;
        chart_options.chart.height = 500;
    }

    chart_options.yAxis.title.text = this.chart_data.yaxis;
    chart_options.title.text = this.chart_data.title;
    chart_options.chart.renderTo = container;
    chart_options.series = series;

    chart = new Highcharts.Chart(chart_options);
    $('#charts').append(container);
    charts[this.chart_data.id] = chart;
    displayed_visuals[this.chart_data.id] = true;
    $(container).append(buttons);

    return chart;
}

Highchart.prototype.check_exception = function(result, url) {
    if (result.exception != undefined) {
        exceptions[url] = result.exception;
        return true;
    } else {
        if (url in exceptions) {
            delete exceptions[url];
        }
        return false;
    }
}

function Scatterchart(chart_data) {
    Highchart.call(this, chart_data);
};

Scatterchart.prototype = new Highchart();

Scatterchart.prototype.load = function(url, callback) {
    var series = {};
    var parent = this;
    return ajax({
        url : url,
        success : function(response) {
            if (parent.check_exception(response, url) || response.length == 0)
                return;
            var nodes = response[0].result;
            for ( var j = 0; j < nodes.length; j++) {
                var label = nodes[j][0];
                if (series[label] == undefined) {
                    series[label] = [];
                }
                var xs = nodes[j][1];
                var ys = nodes[j][2];
                for ( var i = 0; i < xs.length; i++) {
                    series[label].push({
                        y : parseFloat(ys[i]),
                        x : parseFloat(xs[i])
                    });
                }
            }
            callback(series);
        }
    });
}

Scatterchart.prototype.create = function() {
    this.init();
    this.update();
}

Scatterchart.prototype.update = function() {
    var chart = charts[this.chart_data.id];
    for ( var i = 0; i < chart.series.length; i++) {
        chart.series[i].remove(false);
    }
    for ( var d = 0; d < this.chart_data.sources.length; d++) {
        var url = root + "db/history?data=" + this.chart_data.sources[d].id
                + "&last=true";
        this.load(url, function(series) {
            for ( var key in series) {
                var label = key + "";
                var serie = chart.get(label);
                if (serie == null) {
                    chart.addSeries({
                        name : label,
                        id : label
                    });
                    serie = chart.get(label);
                }
                for ( var i = 0; i < series[key].length; i++) {
                    var point = series[key][i];
                    serie.addPoint([ point.x, point.y ], false);
                }
            }
            chart.redraw();
        });
    }
}

function Timechart(chart_data) {
    Highchart.call(this, chart_data);
};

Timechart.prototype = new Highchart();

Timechart.prototype.load = function(url, callback) {
    var series = {};
    var parent = this;
    return ajax({
        url : url,
        success : function(response) {
            for ( var i = 0; i < response.length; i++) {
                if (parent.check_exception(response[i], url))
                    continue;
                var nodes = response[i].result;
                for ( var j = nodes.length - 1; j >= 0; j--) {
                    if (nodes[j] == null)
                        continue;
                    var label = nodes[j][0];
                    if (series[label] == undefined) {
                        series[label] = [];
                    }
                    var value = nodes[j][1];
                    if (value != null) {
                        value = parseFloat(value);
                    }
                    series[label].push({
                        y : value,
                        x : response[i].tick
                    });
                }
                parent.chart_data.last_tick = response[i].tick;
            }
            callback(series);
        }
    });
}

Timechart.prototype.create = function() {
    var calls = [];
    var callcount = 0;
    var s = [];
    var parent = this;
    var callback = function(series) {
        callcount++;
        for ( var key in series) {
            s.push({
                name : "" + key,
                data : series[key],
                id : "" + key
            });
        }
        if (callcount == parent.chart_data.sources.length) {
            parent.init(s);
        }
    }
    for ( var d = 0; d < this.chart_data.sources.length; d++) {
        var url = root + "db/history?data=" + this.chart_data.sources[d].id;
        this.load(url, callback);
    }
}

Timechart.prototype.update = function() {
    var chart = charts[this.chart_data.id];
    for ( var d = 0; d < this.chart_data.sources.length; d++) {
        var url = root + "db/history?data=" + this.chart_data.sources[d].id;
        if (this.chart_data.last_tick != undefined) {
            url += "&from=" + this.chart_data.last_tick;
        }
        this.load(url, function(series) {
            for ( var key in series) {
                var label = key + "";
                var serie = chart.get(label);
                if (serie == null) {
                    chart.addSeries({
                        name : label,
                        id : label
                    });
                    serie = chart.get(label);
                }
                for ( var i = 0; i < series[key].length; i++) {
                    var point = series[key][i];
                    serie.addPoint([ point.x, point.y ], false);
                }
            }
            chart.redraw();
        });
    }
}
