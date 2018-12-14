var stomp_client;
var code_editor;
var histories;
var last_marker_area;
function on_load(){
    var socket = new SockJS('/gs-guide-websocket');
    stomp_client = Stomp.over(socket);
    stomp_client.connect({}, function(frame) {
        stomp_client.subscribe("/message/histories", function (message) {
            var body = JSON.parse(message.body);
            var code = body["code"];
            code_editor.setValue(code);
            histories = body["histories"];
        });
    });

}

function editor_initialize() {
    require(["js/ace1"], function (ace1) {
        code_editor = require("ace/ace").edit("editor");
        code_editor.setOptions({
            autoScrollEditorIntoView: true,
            maxLines: 50,
            minLines: 50
        });
        code_editor.renderer.setScrollMargin(10, 10, 10, 10);
    });
}

function editor_click() {
    if (histories) {
        require(["js/ace1"], function (ace1) {
            var Range = require("ace/range").Range;
            var size = code_editor.session.getLength();
            if (last_marker_area != null)
                code_editor.removeSelectionMarker(last_marker_area);

            var cur_line = code_editor.getCursorPosition()["row"];
            for (var i = 0; i < histories.length; i++) {
                var history = histories[i];
                var start = history["start"];
                var length = history["length"];
                var events = {"events": history["events"]};
                if (cur_line >= start && cur_line < start + length) {
                    last_marker_area = code_editor.addSelectionMarker(new Range(start, 0, start + length, 0));
                    scanner("90%", events);
                }
            }
        });
    }


}


async function search_history_click() {
    var text = await Swal({
        input: 'textarea',
        inputPlaceholder: '请输入文件名',
        showCancelButton: true
    })

    if (text) {
        var class_name = text['value'];
        if (class_name)
            stomp_client.send("/search/histories", {}, class_name);
    }
}


function scanner(width, events) {
    var html = generate_html2(events);
    Swal({
        html: html,
        heightAuto:true,
        width: width
    });

    var size = events["events"].length;
    for (var i = 0; i < size; i ++) {
        var diff_editor = new AceDiff({
            element: '.diff_' + i.toString(),
            left: {
                //由于产生的events下标越小，表示event越新
                //而按照diff的习惯老的应该在左边，即下标大的在前（左）
                content: events["events"][size - i - 1]["old_content"]
            },
            right: {
                //由于产生的events下标越小，表示event越新
                //而按照diff的习惯老的应该在左边，即下标大的在前（左）
                content: events["events"][size - i - 1]["new_content"]
            }
        });
    }

}

function generate_html(size) {
    var html = '<div class="slider">\n';

    for (var i = 0; i < size; i ++) {
        html += '<a href="#slide-' + i.toString() + '">' + i.toString() + '</a>\n'
    }

    html += '<div class = "slides">';
    for (var i = 0; i < size; i ++) {
        html += '<div id="slide-'+ i.toString() + '" class="slide-' + i.toString() + '">\n';
        html += '</div>\n';
    }
    html += '</div>';
    html += '</div>';

    return html;
}

function generate(){
    //require('../handlebar/node_modules/handlebars/lib/handlebars');
    var source = document.getElementById("entry-template").innerHTML;
    var template = Handlebars.compile(source);

    var context = {
        diffs:[
            {
                new_commit:'commit1',
                old_commit:'commit2',
                new_content:'content1',
                old_content:'content2',
                issue: {
                    id:'SOLR-2',
                    description: 'first commit'
                },
            },
            {
                new_commit:'commit3',
                old_commit:'commit4',
                new_content:'content3',
                old_content:'content4',
                issue: {
                    id:'SOLR-5',
                    description: 'second commit'
                },
            },
        ]
    }

    var s = template(context);
    console.log(s);
}

function generate_html2(context){
    Handlebars.registerHelper('each', function(content, option) {
       var html = '<div class="slider">';
       for(var i = 0; i < content.length; i ++){
           html += '<a href="#slide-' + i.toString() + '">' + i.toString() + '</a>';
       }

       html += '<div class="slides">';

       for(var i = 0; i < content.length; i ++) {
           html += '<div id ="slide-' + i.toString() + '" style="display:flex;flex-direction:column">';
           //由于产生的events下标越小，表示event越新
           //而按照diff的习惯老的应该在左边，即下标大的在前（左）
           html += option.fn(content[content.length - i - 1]);
           html += '<div id="diff_' + i.toString() + '" class="diff_' + i.toString() + '" style="height:70%;bottom:0px;width:100%;background:#000000;bottom:0px">'
           html += '</div>';
           html += '</div>';

       }
       html += '</div>';


       html += '</div>';
       return html;
       console.log(html)

    });
    /*
    var context = {
        diffs:[
            {
                new_commit:'commit1',
                old_commit:'commit2',
                new_content:'content1',
                old_content:'content2',
                issue_id:'SOLR-2',
                issue_description: 'first commit'
            },
            {
                new_commit:'commit3',
                old_commit:'commit4',
                new_content:'content3',
                old_content:'content4',
                issue_id:'SOLR-5',
                issue_description: 'second commit'
            }
        ]
    }
    */

    var source = document.getElementById("entry-template").innerHTML;
    var template = Handlebars.compile(source);
    var s = template(context);
    console.log(s);
    return s;
}
