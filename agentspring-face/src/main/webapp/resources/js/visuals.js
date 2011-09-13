var fields = ['yaxis', 'type'];

var visconfig = {
    chart : {
        url : "visuals/save/chart",
        show : ['yaxis', 'type']
    },
    scatter : {
        url : "visuals/save/scatter",
        show : ['yaxis']
    }
};

var visuals = {
        // HACK: new visual is identified by empty string since ids can not be
        // empty
        '' : {
            type: 'line',
            sourcesIds: []
        }
};

function update_fields() {
    var config = visconfig[$("#clazz").val()];
    for (var i = 0; i < fields.length; i++) {
        if (config.show.indexOf(fields[i]) != -1) {
            $('.' + fields[i]).show();
        } else {
            $('.' + fields[i]).hide();
        }
    }
}

function update_visuals() {
    if ($('#id').val() == '') {
        $('#delete').hide();
        $('#show').hide();
    } else {
        $('#delete').show();
        $('#show').show();
    }
    ajax({
        url : root + "visuals/list",
        success : function(response) {
            $("#visuals").empty();
            var visz = response["visuals"].sort(title_sorter);
            for ( var i = 0; i < visz.length; i++) {
                var visual = visz[i];
                var link = $('<a/>', {
                    class: 'button1',
                    href: root + "visuals/edit/?id=" + visual.id,
                    text: visual.title
                });
                if ($('#id').val() === "") {
                    $(".button2").removeClass('button2').addClass('button2-sel');
                } else if (visual.id == $('#id').val()) {
                    $(".button2-sel").removeClass('button2-sel').addClass('button2');
                    $(link).removeClass('button1').addClass('button1-sel');
                }
                $("#visuals").append(link);
                visuals[visual.id] = visual;
            }
        },
        async: false
    });
}

function update_sources() {
    ajax({
        url : root + "sources/list",
        success : function(response) {
            var visual = visuals[visual_id];
            var sources = response.sources.sort(title_sorter);
            $("#sources").empty();
            $("#selected_sources").empty();
            if (visual.sourcesIds.length != 0) {
                $("#selected_sources").append($("<p/>", {
                    text: "Clink on data source to remove it from this visual:"
                }));
            }
            for ( var i = 0; i < sources.length; i++) {
                source = sources[i];
                var link = $('<div />', {
                    class: 'source',
                    id: source.id,
                    text: source.title
                });
                $(link).click(function() {
                    var index = visual.sourcesIds.indexOf(parseInt(this.id));
                    if (index == -1) {
                        visual.sourcesIds.push(parseInt(this.id));
                        update_sources();
                    } else {
                        visual.sourcesIds.splice(index, 1);
                        update_sources();
                    }
                });
                var edit = $('<img/>', {
                    src : root + "resources/img/edit.png",
                    class : 'source-edit',
                    alt : source.id,
                    title: "Edit '" + source.title + "' data source"
                });
                $(edit).click(function(event) {
                    event.stopPropagation();
                    window.location = root + "sources/edit?id=" + this.alt;
                });
                $(link).append(edit);
                if (visual.sourcesIds.indexOf(source.id) != -1) {
                    $("#selected_sources").append(link);
                    var link2 = $('<div />', {
                        class: 'source inactive',
                        text: source.title
                    });
                    $(link2).fadeTo(0, 0.5);
                    $("#sources").append(link2);
                } else {
                    $("#sources").append(link);
                }
            }
        },
    });
}

$(document).ready(function() {
    init_status();
    update_visuals();
    update_sources();
    $("#type").val(visuals[visual_id].type).attr('selected',true);
    $("#clazz").val(visuals[visual_id].clazz).attr('selected',true);
    update_fields();
    $('#clazz').change(function() {
        update_fields();
    });
    $('#save').click(function() {
        if (visuals[visual_id].sourcesIds.length == 0) {
            $('#error').text("Visual must have at least one data source");
            $('#error').show();
            return;
        }
        ajax({
            type : 'POST',
            url : root + visconfig[$("#clazz").val()].url,
            traditional: true,
            data : {
                id : $('#id').val(),
                title: $('#title').val(),
                sources: visuals[visual_id].sourcesIds,
                type: $('#type').val(),
                yaxis: $('#yaxis').val()
            },
            success : function(response) {
                if (response["success"]) {
                    $('#error').hide();
                    $('#id').val(response.id),
                    $('#success').text("Saved");
                    $('#success').show();
                    $('#success').fadeOut(1000);
                    update_visuals();
                } else {
                    $('#error').text(response["error"]);
                    $('#error').show();
                }
            },
        });
    });

    $('#delete').click(
            function() {
                var id = $('#id').val();
                check("Do you really want do delete '" + $('#title').val()
                        + "' visual?", function() {
                    window.location = root + "visuals/delete?id=" + id;
                });
            });

    $('#show').click(function() {
        window.location = root + "?visual=" + $('#id').val();
    });
});
