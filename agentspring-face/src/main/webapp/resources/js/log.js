var last = 0;

function update_log() {
    ajax({
        url: root + "engine/log",
        data : {
            from : last
        },
        success: function(response) {
            var log = response.log;
            last = response.last;
            for (var i = 0; i < log.length; i++) {
                $('#log').append(log[i] + "<br />");
            }
        },
        async : false
    });
}

function init_log() {
    ajax({
        url: root + "engine/log",
        data : {
            full : true
        },
        success: function(response) {
            $('#log').empty();
            log = response.log;
            last = response.last;
            for (i = 0; i < log.length; i++) {
                $('#log').append(log[i] + "<br />");
            }
        },
        async : false
    });
}

$(document).ready(function() {
    init_log();
    init_status([update_log]);
});