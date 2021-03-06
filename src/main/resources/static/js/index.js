

var log_editor;
var diff_editor;
var stompClient;
var file_list;

function initailaize(){
    log_editor_initialize();
    diff_editor_initialize();
    connect();
}

function connect(){
    var socket = new SockJS('/gs-guide-websocket')
    stompClient = Stomp.over(socket)
    stompClient.connect({}, function(frame){
        stompClient.subscribe("/message/commit", update_commit);
        stompClient.subscribe("/message/files", update_files);
        stompClient.subscribe("/message/diff", update_diff);
    });
}

function get_log(){
    stompClient.send("/search/log", {}, {});
}

function get_log_modify_specific_file(){
    stompClient.send("/search/specific_file_commit", {}, $("#input_file").val());
}

function get_log_related_to_specific_issue(){
    stompClient.send("/search/specific_issue_commit", {}, $("#input_issue").val());
}



function get_target_commit(){
    var target_commit = $("#target_commit").val();
    stompClient.send("/search/commit", {}, target_commit);
}

function get_target_file(){
    var target_commit = $("#target_commit").val();
    var target_file = $("#target_file").val();

    var re = {
        "target_commit": target_commit,
        "target_file": target_file
    };
    stompClient.send("/search/file", {}, JSON.stringify(re));
}

function file_filter() {
    var file = $("#file_filter").val().toLowerCase();
    if (file.length == 0) {
        $("#file_list").empty();
        for (var i = 0; i < file_list.length; i ++) {
            $("#file_list").append("<option>" + file_list[i] + "</option>");
        }
    } else {
        $("#file_list").empty();
        var target = "";
        for (var i = 0; i < file_list.length; i ++) {
            if (file_list[i].toLowerCase().indexOf(file) >= 0) {
                $("#file_list").append("<option>" + file_list[i] + "</option>");
                if (target.length == 0)
                    target = file_list[i];
            }
        }
        $("#file_list").val(target);
        $("#target_file").val(target);
    }
}

function commit_change(){
    var target_commit = $("#commit_list").find("option:selected").text();
    $("#target_commit").val(target_commit);
    $("#target_file").val("");
    get_target_commit();
}

function file_change(){
    var target_file = $("#file_list").find("option:selected").text();
    $("#target_file").val(target_file);
}


function diff_editor_initialize(former, latter){
    diff_editor = new AceDiff({
        element: '.acediff',
        left: {
            content: former,
        },
        right: {
            content: latter,
        },
    });
}

function update_commit(commits){
    var com = JSON.parse(commits.body);

    $("#commit_list").empty();
    for(var i = 0; i < com.length && i < 1000; i ++){
        $("#commit_list").append("<option>" + com[i] +"</option>");
    }
    $("#commit_list").val(com[0]);

    $("#target_commit").val(com[0]);

    /*
    $.each(com, function(index, commit){
        $("#commit").append("<option>" + commit +"</option>");
    });
    */
}

function update_files(message){
    var body = JSON.parse(message.body);
    var commit_message = body["message"];
    var files = body["files"];

    $("#commit_message").val(commit_message);

    file_list = new Array();
    var count = 0;
    $.each(files, function(index, file){
        file_list[count++] = file;
    });
    file_filter();

    // $("#file_list").empty();
    // var count = 0;
    // file_list = new Array();
    // $.each(files, function(index, file){
    //     $("#file_list").append("<option>" + file + "</option>");
    //     file_list[count++] = file;
    // });
    // $("#file_list").val(files[0]);
    // $("#target_file").val(files[0]);
}


function update_diff(message){
    var diff = JSON.parse(message.body);
    var former = diff["formerFile"];
    var latter = diff["curFile"];

    $("#acediff").empty();
    diff_editor_initialize(former, latter);
}

