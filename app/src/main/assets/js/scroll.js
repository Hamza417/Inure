    // We will find first visible element on the screen
    // by probing document with the document.elementFromPoint function;
    // we need to make sure that we dont just return
    // body element or any element that is very large;
    // best case scenario is if we get any element that
    // doesn't contain other elements, but any small element is good enough;
    var findSmallElementOnScreen = function() {
        var SIZE_LIMIT = 1024;
        var elem = undefined;
        var offsetY = 0;
        while (!elem) {
            var e = document.elementFromPoint(100, offsetY);
            if (e.getBoundingClientRect().height < SIZE_LIMIT) {
                elem = e;
            } else {
                offsetY += 50;
            }
        }
        return elem;
    };

    // Convert dom element to css selector for later use
    var getCssSelector = function(el) {
        if (!(el instanceof Element))
            return;
        var path = [];
        while (el.nodeType === Node.ELEMENT_NODE) {
            var selector = el.nodeName.toLowerCase();
            if (el.id) {
                selector += '#' + el.id;
                path.unshift(selector);
                break;
            } else {
                var sib = el, nth = 1;
                while (sib = sib.previousElementSibling) {
                    if (sib.nodeName.toLowerCase() == selector)
                       nth++;
                }
                if (nth != 1)
                    selector += ':nth-of-type('+nth+')';
            }
            path.unshift(selector);
            el = el.parentNode;
        }
        return path.join(' > ');
    };

    // Send topmost element and its top offset to java
    var reportScrollPosition = function() {
        var elem = findSmallElementOnScreen();
        if (elem) {
            var selector = getCssSelector(elem);
            var offset = elem.getBoundingClientRect().top;
            WebScrollListener.onScrollPositionChange(selector, offset);
        }
    }

    // We will report scroll position every time when scroll position changes,
    // but timer will ensure that this doesn't happen more often than needed
    // (scroll event fires way too rapidly)
    var previousTimeout = undefined;
    window.addEventListener('scroll', function() {
        clearTimeout(previousTimeout);
        previousTimeout = setTimeout(reportScrollPosition, 200);
    });
