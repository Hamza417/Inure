package app.simple.inure.apk.xml;

import app.simple.inure.apk.structure.xml.XmlCData;
import app.simple.inure.apk.structure.xml.XmlNamespaceEndTag;
import app.simple.inure.apk.structure.xml.XmlNamespaceStartTag;
import app.simple.inure.apk.structure.xml.XmlNodeEndTag;
import app.simple.inure.apk.structure.xml.XmlNodeStartTag;

/**
 * callback interface for parse binary xml file.
 */
public interface XmlStreamer {
    
    void onStartTag(XmlNodeStartTag xmlNodeStartTag);
    
    void onEndTag(XmlNodeEndTag xmlNodeEndTag);
    
    void onCData(XmlCData xmlCData);
    
    void onNamespaceStart(XmlNamespaceStartTag tag);
    
    void onNamespaceEnd(XmlNamespaceEndTag tag);
}