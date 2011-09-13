var editor;

function update_sources() {
    $("#using").empty();
    $("#using").append("Visuals using this data source: ");
    if ($('#id').val() == '') {
        $('#delete').hide();
        $('#using').hide();
    } else {
        $('#using').show();
        ajax({
            url : root + "sources/visuals?id=" + $("#id").val(),
            success : function(response) {
                visuals = response.visuals.sort(title_sorter);
                if (visuals.length == 0) {
                    $("#using").append("none");
                } else {
                    for (var i = 0; i < visuals.length; i++) {
                        $("#using").append($("<a/>", {
                            text: visuals[i].title,
                            href: root + "visuals/edit?id=" + visuals[i].id
                        }));
                        if (i < visuals.length - 1) {
                            $("#using").append(", ");
                        }
                    }
                }
            }
        });
    }
    ajax({
        url : root + "sources/list",
        success : function(response) {
            sources = response.sources.sort(title_sorter);
            $("#sources").empty();
            for ( var i = 0; i < sources.length; i++) {
                source = sources[i];
                var link = $('<a/>', {
                    class: 'button1',
                    href: root + "sources/edit/?id=" + source.id,
                    text: source.title
                });
                if ($('#id').val() === "") {
                    $(".button2").removeClass('button2').addClass('button2-sel');
                } else if (source.id == $('#id').val()) {
                    $(".button2-sel").removeClass('button2-sel').addClass('button2');
                    $(link).removeClass('button1').addClass('button1-sel');
                }
                $("#sources").append(link);
            }
        },
    });
}

$(document).ready(
        function() {
            init_status();
            update_sources();
            $('#test').click(function() {
                ajax({
                    url : root + "db/query",
                    data : {
                        start : $('#start_node').val(),
                        query : editor.getSession().getValue()
                    },
                    success : function(response) {
                        if (response["success"]) {
                            $('#output').text($.dump(response["result"]));
                        } else {
                            $('#output').text(response["exception"]);
                        }
                    },
                });
            });

            $('#save').click(function() {
                ajax({
                    type : 'POST',
                    url : root + "sources/save",
                    data : {
                        id : $('#id').val(),
                        query : editor.getSession().getValue(),
                        start_node : $('#start_node').val(),
                        title: $('#title').val()
                    },
                    success : function(response) {
                        if (response.success) {
                            $('#id').val(response.id)
                            $('#error').hide();
                            $('#success').text("Saved");
                            $('#success').show();
                            $('#success').fadeOut(1000);
                            update_sources();
                        } else {
                            $('#error').text(response["error"]);
                            $('#error').show();
                        }
                    },
                });
            });

            $('#delete').click(function() {
                    check("Do you really want do delete '" + $('#title').val()
                            + "' data source?", 
                    function() {
                        ajax({
                            type : 'POST',
                            url : root + "sources/delete",
                            data : {
                                id : $('#id').val(),
                            },
                            success : function(response) {
                                if (response["success"]) {
                                    window.location = root + "sources/new";
                                } else {
                                    $('#error').text(response["error"]);
                                    $('#error').show();
                                }
                            },
                    })}
                )});
        });

window.onload = function() {
    editor = ace.edit("editor");
    editor.setTheme("ace/theme/eclipse");
    editor.setShowPrintMargin(true);
    var GroovyMode = require("ace/mode/groovy").Mode;
    editor.getSession().setMode(new GroovyMode());
};