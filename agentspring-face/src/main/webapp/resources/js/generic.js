var ajax_errors = {};
var exceptions = {};
var resume_animation = undefined;

function check(message, action) {
    if (confirm(message)) {
        action();
    }
}

function enable_resume(control) {
    $(control).show();
    $(control).fadeTo(0, 1);
    $(control).css('cursor', 'pointer');
    if (resume_animation == undefined) {
        animate = function() {
            $(control).fadeTo(500, 0.5);
            $(control).fadeTo(500, 1);
        }
        animate();
        resume_animation = setInterval(animate, 1000);
    }
}

function hide_resume(control) {
    if (resume_animation != undefined) {
        clearInterval(resume_animation);
        $(control).hide();
        resume_animation = undefined;
    }
}

function enable_control(control) {
    $(control).fadeTo(0, 1);
    $(control).show();
    $(control).css('cursor', 'pointer');
}

function disable_control(control) {
    $(control).fadeTo(0, 0.2);
    $(control).show();
    $(control).css('cursor', 'auto');
}

function update_status() {
    ajax({
        url: root + "engine/status",
        success: function(response) {
            var state = response['state'];
            if (state != "STOPPED") {
                $('#status').html($('<div/>', {
                        class: 'status-running',
                        text: "Engine is running, current tick = "
                            + response['tick'] + " (" + state + ")"
                }));
            } else {
                $('#status').html($('<div />', {
                        class: 'status-stopped',
                        text: "Engine is ready"
                }));
            }

            if (state == "STOPPED" || state == "CRASHED") {
                enable_control($("#start"));
                disable_control($('#pause'));
                hide_resume($('#resume'));
                $("#stop").hide();
            } else if (state == "STOPPING" || state == "PAUSING") {
                disable_control($("#stop"));
                disable_control($('#pause'));
                $("#start").hide();
                hide_resume($('#resume'));
            } else if (state == "RUNNING") {
                enable_control($("#stop"));
                enable_control($('#pause'));
                $("#start").hide();
                hide_resume($('#resume'));
            } else if (state == "PAUSED") {
                disable_control($("#stop"));
                enable_resume($('#resume'));
                $('#pause').hide();
                $("#start").hide();
            }
        },
        error: function(error) {
            $('#status').html($('<div />', {
                    class : 'status-offline',
                    text: 'Engine is offline'
            }));
            $('#pause').hide();
            $("#start").hide();
            $('#stop').hide();
            $("#resume").hide();
        }
    });
}

function update_error_bar() {
    var bar = $("#error-bar");
    bar.empty();
    var error = false;
    for ( var i in exceptions) {
        bar.append(i + ": Exception: " + exceptions[i] + "<br />");
        error = true;
    }
    for ( var i in ajax_errors) {
        var error = ajax_errors[i];
        bar.append(i + ": Error " + error.status + "<br />");
        error = true;
    }
    if (!error) {
        bar.hide();
    } else {
        bar.show();
    }
}

function ajax(data) {
    data.dataType = "json";
    if (data.error == undefined) {
        data.error = function(error) {
            ajax_errors[data.url] = error;
            update_error_bar();
        }
    }
    var success;
    if (data.success != undefined) {
        success = data.success;
    } else {
        success = function(response) {};
    }
    data.success = function(response) {
        success(response);
        delete ajax_errors[data.url];
        update_error_bar();
    }
    return $.ajax(data);
}

function setup_scenario() {
    ajax({
        url: root + "engine/scenarios",
        success: function(response) {
            $('#scenario').empty();
            var scenarios = response.scenarios;
            for (var i = 0; i < scenarios.length; i++) {
                $('#scenario').append($('<option />', {
                        value: scenarios[i],
                        text: scenarios[i]
                }));
            }
            ajax({
               url: root + "engine/scenario",
               success: function(response) {
                   $('#scenario').val(response.scenario);
                   $('#scenario').change(function() {
                       ajax({
                           url: root + "engine/load",
                           type: 'POST',
                           data: {
                               scenario: $('#scenario').val()
                           },
                           success: function(response) {
                               if (typeof(update_fields) != 'undefined') {
                                   update_fields();
                               }
                           }
                       });
                   });
               }
            });
        },
    });
}

function init_status(functions) {
    $('#loadingDiv').hide().ajaxStart(function() {
// $('body').css('cursor', 'wait');
            $(this).show();
    }).ajaxStop(function() {
            $(this).hide();
// $('body').css('cursor', 'auto');
    });
    update = function() {
        ajax({
            url: root + "engine/listen",
        });
        update_status();
        update_error_bar();
        if (functions != undefined) {
            for ( var i = 0; i < functions.length; i++) {
                functions[i]();
            };
        }
    }
    update();
    setInterval(update, 5000);
    $('#start').click(function() {
        ajax({
            url: root + "engine/start",
            success: function(response) {
                if (response['success']) {
                    update_status();
                }
            },
        });
    });
    $('#stop').click(function() {
        ajax({
            url: root + "engine/stop",
            success: function(response) {
                update_status();
            },
        });
    });
    $('#pause').click(function() {
        ajax({
            url: root + "engine/pause",
            success: function(response) {
                update_status();
            },
        });
    });
    $('#resume').click(function() {
        ajax({
            url: root + "engine/resume",
            success: function(response) {
                update_status();
            },
        });
    });
    setup_scenario();
}
