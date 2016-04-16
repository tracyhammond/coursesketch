package parsers;

import com.mongodb.ServerAddress;
import coursesketch.database.RecognitionDatabaseClient;
import coursesketch.server.interfaces.ServerInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import protobuf.srl.sketch.Sketch;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by gigemjt on 4/16/16.
 */
public class XmlPDollarParserToDb {
    public static void main(String args[]) throws Exception {
        //Get the DOM Builder Factory
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        final List<ServerAddress> databaseUrl = new ArrayList<>();
        databaseUrl.add(new ServerAddress());

        RecognitionDatabaseClient client = new RecognitionDatabaseClient(new ServerInfo("localhost", 0, 0, false, true, "Recognition", databaseUrl));
        client.startDatabase();

        File f = new File("../xml_logs");
        navigateFiles(f, client);
        System.out.println(f.getAbsolutePath());

    }

    public static void navigateFiles(File folder, RecognitionDatabaseClient client) throws IOException, SAXException, ParserConfigurationException {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                navigateFiles(fileEntry, client);
            } else {
                System.out.println(fileEntry.getAbsolutePath());
                if (!fileEntry.getName().contains(".xml")) {
                    System.out.println(fileEntry.getName());
                    continue;
                }
                final InputStream inputStream = new FileInputStream(fileEntry);
                final Sketch.RecognitionTemplate recognitionTemplate = parseFile(inputStream);
                client.addTemplate(recognitionTemplate.getInterpretation(), recognitionTemplate.getStroke());
            }
        }
    }

    public static Sketch.RecognitionTemplate parseFile(InputStream stream) throws ParserConfigurationException, IOException, SAXException {
        Sketch.RecognitionTemplate.Builder template = Sketch.RecognitionTemplate.newBuilder();
        Sketch.SrlStroke.Builder stroke = Sketch.SrlStroke.newBuilder();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //Get the DOM Builder
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document =
                builder.parse(stream);
        NodeList nodeList = document.getDocumentElement().getChildNodes();
        final Element documentElement = document.getDocumentElement();
        System.out.println(documentElement);
        String gestureName = documentElement.getAttribute("Name");

        final NodeList gestureChildNodes = documentElement.getElementsByTagName("Point");
        for (int i = 1; i < gestureChildNodes.getLength(); i++) {
            final Node point = gestureChildNodes.item(i);
            final NamedNodeMap attributes = point.getAttributes();
            final Node nodeX = attributes.getNamedItem("X");
            final Node nodeY = attributes.getNamedItem("Y");
            final Node nodeTime = attributes.getNamedItem("T");
            long x = Long.parseLong(nodeX.getNodeValue());
            long y = Long.parseLong(nodeY.getNodeValue());
            long time = Long.parseLong(nodeTime.getNodeValue());

            Sketch.SrlPoint.Builder protoPoint = Sketch.SrlPoint.newBuilder();
            protoPoint.setId(UUID.randomUUID().toString());
            protoPoint.setX(x);
            protoPoint.setY(y);
            protoPoint.setTime(time);
            stroke.addPoints(protoPoint);
        }
        stroke.setId(UUID.randomUUID().toString());
        stroke.setTime(0);
        String realLabel = gestureName.substring(0, gestureName.length() - 2);
        System.out.println("REAL LABEL: " + realLabel);

        template.setStroke(stroke);
        template.setInterpretation(Sketch.SrlInterpretation.newBuilder().setConfidence(1).setLabel(realLabel));
        return template.build();
    }
}
