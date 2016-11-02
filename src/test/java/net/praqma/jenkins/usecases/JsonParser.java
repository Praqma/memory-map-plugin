package net.praqma.jenkins.usecases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xmlpull.v1.XmlPullParserFactory;

public class JsonParser {

    public static final ObjectMapper jackson = new ObjectMapper();
    public static final Gson gson = new GsonBuilder().create();
    public static final GsonXml gsonXml = createGsonXml();

    public static String getRawXml(Node node) throws Exception {
        Document nodeDocument = node.getOwnerDocument();
        DOMImplementationLS domImplLS = (DOMImplementationLS) nodeDocument.getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        return serializer.writeToString(node);
    }

    public static Document parseXml(File xml) throws Exception {
        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(xml);
        return document;
    }

    private static GsonXml createGsonXml() {
        XmlParserCreator parserCreator = () -> {
            try {
                return XmlPullParserFactory.newInstance().newPullParser();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        return new GsonXmlBuilder().setXmlParserCreator(parserCreator).create();
    }
}
