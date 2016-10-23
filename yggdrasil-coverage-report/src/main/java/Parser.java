/*
 * Copyright (c) 2016 Erik Håkansson, http://squark.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Erik Håkansson on 2016-06-16.
 * WirelessCar
 */
public class Parser {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: jacoco-parser [jacoco.xml]");
            System.exit(1);
        }
        new Parser().parse(args[0]);
    }

    public void parse(String pathToXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setValidating(false);
        factory.setNamespaceAware(true);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        //Get the DOM Builder
        DocumentBuilder builder = factory.newDocumentBuilder();

        //Load and Parse the XML document
        //document contains the complete XML as a Tree.
        Document document = builder.parse(new File(pathToXml));
        Element report = document.getDocumentElement();
        List<Counter> nodes = new ArrayList<Counter>() {
            @Override
            public String toString() {
                String string = "";
                String newLine = "";
                for (Counter counter : this) {
                    string += newLine;
                    string += counter.toString();
                    newLine = "\n";
                }
                return string;
            }
        };
        for (int i = 0; i < report.getChildNodes().getLength(); i++) {
            Node node = report.getChildNodes().item(i);
            if (Objects.equals(node.getNodeName(), "counter")) {
                nodes.add(new Counter(node.getAttributes().getNamedItem("type").getNodeValue(),
                        Integer.valueOf(node.getAttributes().getNamedItem("missed").getNodeValue()),
                        Integer.valueOf(node.getAttributes().getNamedItem("covered").getNodeValue())));

            }
        }
        System.out.println("--- COVERAGE ---");
        System.out.println(nodes);
        System.out.println("--- COVERAGE ---");
    }

    private class Counter {
        private String type;
        private int missed;
        private int covered;

        public Counter(String type, int missed, int covered) {
            this.type = type;
            this.missed = missed;
            this.covered = covered;
        }

        public String getType() {
            return type;
        }

        public int getMissed() {
            return missed;
        }

        public int getCovered() {
            return covered;
        }

        public String getCoverage() {
            double percent = ((double) covered / (double) (covered + missed)) * 100;
            return String.valueOf(new DecimalFormat("###.##").format(percent)) + "%";
        }

        @Override
        public String toString() {
            return type + ": [missed=" + missed + ", covered=" + covered + ", coverage=" + getCoverage() + "]";
        }
    }
}
