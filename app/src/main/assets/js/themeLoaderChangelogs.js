// Define a JavaScript interface using the window object
window.CssLoader = {
    loadLightCss: function() {
        // JavaScript code to load the light CSS
        loadCss('file:///android_asset/css/html_decoration_light.css');
    },
    loadDarkCss: function() {
        // JavaScript code to load the dark CSS
        loadCss('file:///android_asset/css/html_decoration_dark.css');
    }
};

function loadCss(cssFile) {
    var link = document.createElement("link");
    link.rel = "stylesheet";
    link.type = "text/css";
    link.href = cssFile;
    document.head.appendChild(link);
}