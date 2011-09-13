// all visuals data
var visuals = {};
// displayed charts containers
var charts = {};
// visuals in monitor
var monitor = [];
// currently displayed visuals
var displayed_visuals = {};
var page = "monitor";

function remove_visual() {
    var id = parseInt(this.alt);
    monitor.splice(monitor.indexOf(id), 1);
    delete displayed_visuals[id];
    $(charts[id].options.chart.renderTo).remove();
    delete charts[id];
    save_monitor();
    show_monitor();
}

function update_visual(visual_id) {
    var visual_data = visuals[visual_id];
    var visual = {};
    if (visual_data.clazz == "chart") {
        visual = new Timechart(visual_data);
    } else if (visual_data.clazz == "scatter") {
        visual = new Scatterchart(visual_data);
    }

    if (visual_id in displayed_visuals) {
        visual.update();
    } else {
        visual.create();
    }
}

function visual_drop(ev) {
    if (ev.stopPropagation) {
        ev.stopPropagation(); // Stops some browsers from redirecting.
    }
    var id = parseInt(ev.dataTransfer.getData('Text'));
    if (isNaN(id))
        return;
    if (monitor.length == 0) {
        $("#charts").empty();
    }
    if (monitor.indexOf(id) == -1) {
        monitor.push(id);
    }
    update_visual(id);
    save_monitor();
    return false;
}

function visual_dragover(ev) {
    if (ev.preventDefault) {
        ev.preventDefault();
    }
    return false;
}

function restart_hook() {
    if (page == "monitor") {
        show_monitor(true);
    } else {
        for(key in displayed_visuals) {
            show_chart(key);
        }
    }
    for (key in visuals) {
        visuals[key].last_tick = 0;
    }
}

function clear_visuals() {
    for (key in charts) {
        charts[key].destroy();
    }
    charts = {};
    $('#charts').empty();
    displayed_visuals = {};
}

function show_monitor(reset) {
    exceptions = {};
    if (page != "monitor" || reset == true) {
        clear_visuals();
    }
    page = "monitor";
    for ( var i = 0; i < monitor.length; i++) {
        update_visual(monitor[i]);
    }
    if (monitor.length == 0) {
        $('#charts').empty();
        $('<p/>', {
            text : "Drag and drop visuals here"
        }).appendTo("#charts");
    }
    $("#charts")[0].addEventListener("drop", visual_drop, false);
    $("#charts")[0].addEventListener("dragover", visual_dragover, false);
    $('.button2').removeClass('button2').addClass('button2-sel');
    $('.button1-sel').removeClass('button1-sel').addClass('button1');
}

function show_chart(chart) {
    exceptions = {};
    page = "chart";
    clear_visuals();
    $("#charts")[0].removeEventListener("drop", visual_drop, false);
    $("#charts")[0].removeEventListener("dragover", visual_dragover, false);
    $('.button2-sel').removeClass('button2-sel').addClass('button2');
    $('.button1-sel').removeClass('button1-sel').addClass('button1');
    $('#' + chart).removeClass('button1').addClass('button1-sel');
    update_visual(chart);
}

function update_visuals() {
    for ( var key in displayed_visuals) {
        update_visual(key);
    }
}

function update_log() {
    ajax({
        url : "engine/log",
        success : function(response) {
            var i = 0;
            var log = response['log'];
            $('#log').html('');
            for (i = 0; i < log.length; i++) {
                $('#log').append(log[i] + "<br />");
            }
        }
    });
}

function load_monitor() {
    ajax({
        url : root + "monitor/get",
        success : function(response) {
            monitor = response.visuals;
        },
        async : false
    });
}

function save_monitor() {
    ajax({
        url : root + "monitor/set",
        data : {
            visuals : monitor
        },
        traditional : true,
        type : 'POST'
    });
}

function load_visuals() {
    ajax({
        url : "visuals/list",
        success : function(response) {
            $("#visuals").empty();
            var visz = response["visuals"].sort(title_sorter);
            for ( var i = 0; i < visz.length; i++) {
                var visual = visz[i];
                var link = $("<a/>", {
                    href : '#',
                    id : visual.id,
                    class : 'button1',
                    draggable : 'true',
                    text : visual.title
                });
                link[0].addEventListener("dragstart", function(ev) {
                    ev.dataTransfer.effectAllowed = 'move';
                    ev.dataTransfer.setData('Text', ev.target
                            .getAttribute('id'));
                    return true;
                }, false);
                $(link).click(function() {
                    show_chart(this.id);
                });
                visuals[visual.id] = visual;
                $("#visuals").append(link);
            }
        },
        async : false
    });
}

$(document).ready(function() {
    load_visuals();
    load_monitor();
    var visual = get_url_parameter("visual");
    if (visual == "") {
        show_monitor();
    } else {
        show_chart(parseInt(visual));
    }
    init_status([ update_log, update_visuals ], restart_hook);
    $("#monitor").click(function() {
        show_monitor();
    });
});