$(function() {

    var rend = new marked.Renderer();

    marked.setOptions({
        langPrefix: '',
        highlight: function(code) {
            return hljs.highlightAuto(code).value;
        }
    });

    rend.code = function(code, lang, escaped) {
        var lineArray = code.split(/\r\n|\r|\n/);
        var len = 0;
        if (lineArray == null) {
            len = code.length;

        } else {
            $.each(lineArray, function(index, val) {
                if (len < val.length) {
                    len = val.length;
                }
            });
        }

        var code = code.replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');

        if (!lang) {
            return '<pre><code style="' +
                ' display: block; word-wrap: normal; overflow-x: scroll;' +
                ' width: ' + len + 'rem; ' +
                ' -webkit-text-size-adjust: none;' +
                '">' +
                code +
                '\n</code></pre>';
        }

        return '<pre><code class="' +
            lang +
            '" style="' +
            ' display: block; word-wrap: normal; overflow-x: scroll;' +
            ' width: ' + len + 'rem; ' +
            ' -webkit-text-size-adjust: none;' +
            '">' +
            code +
            '\n</code></pre>';
    };

    function escSub(text) {
        var result = text.match(/~+.*?~+/g);
        if (result == null) {
            return text;
        }

        $.each(result, function(index, val) {
            if (val.lastIndexOf('~~', 0) === 0) {
                return true;
            }
            var escapedText = val.replace(/~/, '<sub>');
            escapedText = escapedText.replace(/~/, '</sub>');
            var reg = new RegExp(val, 'g');
            text = text.replace(reg, escapedText);
        });

        return text;
    }

    function escSup(text) {
        var result = text.match(/\^.*?\^/g);
        if (result == null) {
            return text;
        }

        $.each(result, function(index, val) {
            var escapedText = val.replace(/\^/, '<sup>');
            escapedText = escapedText.replace(/\^/, '</sup>');
            val = val.replace(/\^/g, '\\^');
            var reg = new RegExp(val, 'g');
            text = text.replace(reg, escapedText);
        });

        return text;
    }

    preview = function setMarkdown(md_text, codeScrollDisable) {
        if (md_text == "") {
            return false;
        }

        md_text = md_text.replace(/\\n/g, "\n");
        md_text = escSub(md_text);
        md_text = escSup(md_text);

        // markdown html
        var md_html;
        if (codeScrollDisable) {
            md_html = marked(md_text);
        } else {
            md_html = marked(md_text, {
                renderer: rend
            });
        }

        $('#preview').html(md_html);

        $('pre code').each(function(i, block) {
            hljs.highlightBlock(block);
        });
    };
});