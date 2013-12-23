package com.mopub.mobileads.util.vast;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class VastXmlManager {
    private static final String ROOT_TAG = "MPMoVideoXMLDocRoot";
    private static final String ROOT_TAG_OPEN = "<" + ROOT_TAG + ">";
    private static final String ROOT_TAG_CLOSE = "</" + ROOT_TAG + ">";

    enum VastElementName {
        IMPRESSION_TRACKER ("Impression"),
        VIDEO_TRACKER ("Tracking"),
        CLICK_THROUGH ("ClickThrough"),
        CLICK_TRACKER ("ClickTracking"),
        MEDIA_FILE ("MediaFile"),
        VAST_AD_TAG ("VASTAdTagURI"),
        MP_IMPRESSION_TRACKER ("MP_TRACKING_URL");

        private final String name;

        private VastElementName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    };

    enum VastElementAttributeName {
        EVENT ("event");

        private final String name;

        private VastElementAttributeName(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    };

    enum VastElementAttributeValue {
        START ("start"),
        FIRST_QUARTILE ("firstQuartile"),
        MIDPOINT ("midpoint"),
        THIRD_QUARTILE ("thirdQuartile"),
        COMPLETE ("complete");

        private final String value;

        private VastElementAttributeValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    };

    private Document mVastDoc;

    void parseVastXml(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        // if the xml string starts with <?xml?>, this tag can break parsing if it isn't formatted exactly right
        // or if it's not the first line of the document...we're just going to strip it
        xmlString = xmlString.replaceFirst("<\\?.*\\?>", "");

        // adserver may embed additional impression trackers as a sibling node of <VAST>
        // wrap entire document in root node for this case.
        String documentString = ROOT_TAG_OPEN + xmlString + ROOT_TAG_CLOSE;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setCoalescing(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        mVastDoc = documentBuilder.parse(new InputSource(new StringReader(documentString)));
    }

    String getVastAdTagURI() {
        List<String> uriWrapper = getStringDataAsList(VastElementName.VAST_AD_TAG);
        return (uriWrapper.size() > 0) ? uriWrapper.get(0) : null;
    }

    List<String> getImpressionTrackers() {
        List<String> impressionTrackers = getStringDataAsList(VastElementName.IMPRESSION_TRACKER);
        impressionTrackers.addAll(getStringDataAsList(VastElementName.MP_IMPRESSION_TRACKER));

        return impressionTrackers;
    }

    List<String> getVideoStartTrackers() {
        return getVideoTrackerByAttribute(VastElementAttributeValue.START);
    }

    List<String> getVideoFirstQuartileTrackers() {
        return getVideoTrackerByAttribute(VastElementAttributeValue.FIRST_QUARTILE);
    }

    List<String> getVideoMidpointTrackers() {
        return getVideoTrackerByAttribute(VastElementAttributeValue.MIDPOINT);
    }

    List<String> getVideoThirdQuartileTrackers() {
        return getVideoTrackerByAttribute(VastElementAttributeValue.THIRD_QUARTILE);
    }

    List<String> getVideoCompleteTrackers() {
        return getVideoTrackerByAttribute(VastElementAttributeValue.COMPLETE);
    }

    String getClickThroughUrl() {
        List<String> clickUrlWrapper = getStringDataAsList(VastElementName.CLICK_THROUGH);
        return (clickUrlWrapper.size() > 0) ? clickUrlWrapper.get(0) : null;
    }

    List<String> getClickTrackers() {
        return getStringDataAsList(VastElementName.CLICK_TRACKER);
    }

    String getMediaFileUrl() {
        List<String> urlWrapper = getStringDataAsList(VastElementName.MEDIA_FILE);
        return (urlWrapper.size() > 0) ? urlWrapper.get(0) : null;
    }

    private List<String> getVideoTrackerByAttribute(VastElementAttributeValue attributeValue) {
        return getStringDataAsList(VastElementName.VIDEO_TRACKER, VastElementAttributeName.EVENT, attributeValue);
    }

    private List<String> getStringDataAsList(VastElementName elementName) {
        return getStringDataAsList(elementName, null, null);
    }

    private List<String> getStringDataAsList(VastElementName elementName, VastElementAttributeName attributeName, VastElementAttributeValue attributeValue) {
        ArrayList<String> results = new ArrayList<String>();

        if (mVastDoc == null) {
            return results;
        }

        NodeList nodes = mVastDoc.getElementsByTagName(elementName.getName());

        if (nodes == null) {
            return results;
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node != null && nodeMatchesFilter(node, attributeName, attributeValue)) {
                // since we parsed with coalescing set to true, CDATA is added as the child of the element
                Node textChild = node.getFirstChild();
                if (textChild != null) {
                    String textValue = textChild.getNodeValue();
                    if (textValue != null) {
                        results.add(textValue.trim());
                    }
                }
            }
        }

        return results;
    }

    private boolean nodeMatchesFilter(Node node, VastElementAttributeName attributeName, VastElementAttributeValue attributeValue) {
        if (attributeName == null || attributeValue == null) {
            return true;
        }

        NamedNodeMap attrMap = node.getAttributes();
        if (attrMap != null) {
            Node attrNode = attrMap.getNamedItem(attributeName.getName());
            if (attrNode != null && attributeValue.getValue().equals(attrNode.getNodeValue())) {
                return true;
            }
        }

        return false;
    }
}
