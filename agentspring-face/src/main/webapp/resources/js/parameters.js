function objSorter(a, b) {
    if (a.clazz.toLowerCase() < b.clazz.toLowerCase()) {
        return -1
    } else if (a.clazz.toLowerCase() > b.clazz.toLowerCase()) {
        return 1
    } else {
        if (a.id.toLowerCase() < b.id.toLowerCase()) {
            return -1
        } else if (a.id.toLowerCase() > b.id.toLowerCase()) {
            return 1;
        } else {
            return 0;
        }
    }
}

function update_fields() {
    ajax({
        url : root + "parameters/list",
        success : function(response) {
            var div = $('#params');
            div.empty();
            var objs = response.parameters.sort(objSorter);
            for (var i = 0; i < objs.length; i++) {
                var obj = objs[i];
                var objdiv = $("<div/>", {
                    class: 'obj'
                });
                var titlediv = $("<div/>", {
                    class: 'objtitlediv'
                });
                titlediv.append($('<b />', {
                    class: 'objtitle',
                    text: obj.id
                }));
                //titlediv.append(' (');
                titlediv.append($('<i />', {
                    text: obj.clazz
                }));
                //titlediv.append(')');
                objdiv.append(titlediv);
                div.append(objdiv);
                for (var j = 0; j < obj.parameters.length; j++) {
                	var paramdiv = $("<div/>", {
                        class: 'param'
                    });
                	objdiv.append(paramdiv);
                    var field = obj.parameters[j];
                    var value = field.value;
                    var label = field.label;
                    var from = field.from;
                    var to = field.to;
                    var step = field.step;
                    
                    if (step == null) {
                    	step = (Math.abs(to - from)) / 1000;
                    }
                    paramdiv.append($("<div/>", {
                        class: 'label',
                        text: label
                    }));
                    //paramdiv.append(": ");
                    var valuediv = $("<div/>", {
                        class: 'paramvalue'
                    });
                    paramdiv.append(valuediv);
                    if (field.from != null && field.to != null) {
                        var slider = $('<div />', {
                            class: "slider"
                        });
                        slider.slider({
                            value: value,
                            min: from,
                            max: to,
                            step: step,
                            slide: function(event, ui) {
                                $(this).siblings('.value').val(ui.value);
                            }
                        });
                        valuediv.append(slider);
                    }
                    if (value === true || value === false) {
                    	var select = $("<select />");
                    	if (value) {
                    		select.append("<option value=\"true\" selected>True</option>");
                    		select.append("<option value=\"false\">False</option>");
                    	} else {
                    		select.append("<option value=\"true\">True</option>");
                    		select.append("<option value=\"false\" selected>False</option>");
                    	}
                    	valuediv.append(select);
                    } else {
	                    valuediv.append($("<input />", {
	                        class: 'value',
	                        value: value,
	                        change: function () {
	                            $($(this).siblings('.slider')).slider("value", this.value);
	                        }
	                    }));
                    }
                    valuediv.append($("<input />", {
                        type: 'hidden',
                        class: 'field',
                        value: field.field
                    }));
                }
            }
        },
    });
}

$(document).ready(function() {
    init_status();
    update_fields();
    $('#save').click(function() {
        var ids = [];
        var fields = [];
        var counts = [];
        var values = [];
        var objs = $('.obj');
        for (var i = 0; i < objs.length; i++) {
            var obj = objs[i];
            var id = $(obj).find('.objtitle').text();
            ids.push(id);
            var objfields = $(obj).find('.field');
            var objvalues = $(obj).find('.value');
            counts.push(objfields.length);
            for (var j = 0; j < objfields.length; j++) {
                fields.push($(objfields[j]).val());
                values.push($(objvalues[j]).val());
            }
        }
        ajax({
            url: root + "parameters/save",
            type : 'POST',
            traditional: true,
            data : {
                ids: ids,
                fields: fields,
                counts: counts,
                values: values
            },
            success: function(response) {
                if (response.success) {
                    $('#error').hide();
                    $('#success').text("Saved");
                    $('#success').show();
                    $('#success').fadeOut(1000);
                } else {
                    $('#error').text(response["error"]);
                    $('#error').show();
                }
            },
        });
    });
});