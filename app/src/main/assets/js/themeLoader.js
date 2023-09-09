function loadCss(cssFile) {
            var link = document.createElement("link");
            link.rel = "stylesheet";
            link.type = "text/css";
            link.href = cssFile;
            document.head.appendChild(link);
        }

        function loadDarkCss() {
            CssLoader.loadDarkCss();
            console.log("dark css loaded");
        }

        function loadLightCss() {
            CssLoader.loadLightCss();
            console.log("light css loaded");
        }