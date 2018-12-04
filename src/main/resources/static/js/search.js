/**
 * Created by oliver on 2018/11/6.
 */
var codes;
var comments;
var relation;
var code_editor;
var comment_editor;
var id_list = new Array();
var cur_comment_id = -1;

var search_finished = false;
var start_i = 0;
var id;

var id1,id2;

function Issue(index){
    comment_editor.setValue(comments[index]);
}

var stompClient = null;
function Submit(){
    require.config({paths: {"ace": "../lib/ace"}});
    // load ace and extensions
    require(["ace/ace"], function (ace) {
        var Range = require("ace/range").Range;
        if(id1 != null){
            code_editor.session.removeMarker(id1);
            code_editor.session.removeMarker(id2);
        }

        id1 = code_editor.session.addMarker(new Range(start_i, 0, start_i + 2, 0), "ace_active-line", "fullLine");
        id2 = code_editor.session.addMarker(new Range(start_i + 5 , 0, start_i + 7, 0), "ace_active-line", "fullLine");
        start_i ++;
    });

    comment_editor.setValue("");
    var code = code_editor.getValue();
    stompClient.send("/search/searching", {}, JSON.stringify({'code': code}));
}

function setCode(codes){
    code_editor.setReadOnly(true);
    var res="";
    for(var i=0;i<codes.length;++i){
        //res=res+"<span onmouseover=\"Issue("+i+")\">"+codes[i]+"</span>"+"<br/>";
        res += codes[i];
    }
    //document.getElementById("text-area").innerHTML=res;
    code_editor.setValue(res);
    search_finished = true;
}

function addMessage(msg){
    console.log("get message!");
    comment_editor.setValue( comment_editor.getValue() +  msg);
}

function connect(){
    var socket = new SockJS('/gs-guide-websocket')
    stompClient = Stomp.over(socket)
    stompClient.connect({}, function(frame){
        stompClient.subscribe("/message/feedback", function(feedbackMessage){
            addMessage(JSON.parse(feedbackMessage.body).content)
        });
        stompClient.subscribe("/message/result", function(searchMessage){
            var code = JSON.parse( JSON.parse(searchMessage.body).code);
            comments = JSON.parse( JSON.parse(searchMessage.body).comment);
            relation = JSON.parse( JSON.parse(searchMessage.body).relation);
            setCode(code)
        });
        stompClient.subscribe("/message/id", function(ID){
            id = ID;
            stompClient.subscribe("message/feedback/" + id, function(str){
                comment_editor.setValue(str);
            });
        });

    });
}


function onEditor(){
    if(search_finished) {
        var cur_line = code_editor.getCursorPosition()["row"];
        var next_comment_id = relation[cur_line];

        require.config({paths: {"ace": "../lib/ace"}});
        // load ace and extensions
        require(["ace/ace"], function (ace) {
            var Range = require("ace/range").Range;
            if(next_comment_id != cur_comment_id) {
                for (var i = 0; i < id_list.length; i++) {
                    code_editor.session.removeMarker(id_list[i]);
                }

                id_list = new Array();

                var count = 0
                for (var i = 0; i < relation.length; i++) {
                    if (relation[i] == next_comment_id) {
                        var id = code_editor.session.addMarker(new Range(i, 0, i, 1), "ace_active-line", "fullLine")
                        id_list.push(id);
                        count ++;
                    }
                }
                cur_comment_id = next_comment_id;
                comment_editor.setValue(count + " lines affected! \n" + comments[relation[cur_line]]);
            }
        });
    }
  //comment_editor.setValue(comments[curLine]);
}


function initialize() {
    require.config({paths: {"ace": "../lib/ace"}});
    // load ace and extensions
    require(["ace/ace"], function (ace) {
        code_editor = ace.edit("code-area");
        code_editor.setOptions({
          autoScrollEditorIntoView: true,
          maxLines: 50,
          minLines:1
        });
        code_editor.renderer.setScrollMargin(10, 10, 10, 10);
        code_editor.setHighlightActiveLine(false);
        code_editor.session.selection.on("changeCursor", onEditor);
        code_editor.setValue("code to search!");
        //code_editor.session.addMarker(new Range(1,0, 3, 2), "ace_active-line", "fullLine");


        comment_editor = ace.edit("message-area");
        comment_editor.setOptions({
            autoScrollEditorIntoView: true,
            maxLines: 50,
            minLines:20
        });
        comment_editor.setValue("");
        comment_editor.setFontSize(20);
    });
}

function reset(){
    code_editor.setValue("code to search!");
    code_editor.setReadOnly(false);
    comment_editor.setValue("");
}