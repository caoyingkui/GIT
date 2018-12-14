define(function(exports) {
    exports.select = function(code_editor, methods){
        /*
        * {
        *   [
        *       {
        *           'start':1,
        *           'length':2,
        *           'name':'method'
        *       }
        *   ]
        * }
        * */
        var cur_line = code_editor.getCursorPosition()['row'];

        for (var i = 0; i < len(methods); i ++) {
            var method = methods[i];
            var start = method['start'];
            var length = method['length'];
            var name = method['name'];
            if (start <= cur_line && end <= cur_line) {
                clean_marker(code_editor);
                add_marker(code_editor, start, length);
            }

        }

    }

    function clean_marker(code_editor) {
        var length = code_editor.getLength();
        for (var line = 0; line < length; line ++) {
            code_editor.removeMarker(line);
        }
    }

    function add_marker(code_editor, start, length) {
        require.config({paths: {'ace': '../lib/ace/ace.js'}});
        require(['ace/ace'], function (ace) {
            var Range = require('ace/range').Range;
            code_editor.addMarker(new Range(start, 0, start + length - 1, 1), "ace_active-line", "fullLine");
        });
    }

});
